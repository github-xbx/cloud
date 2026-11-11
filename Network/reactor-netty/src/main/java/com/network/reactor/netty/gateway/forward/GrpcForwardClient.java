package com.network.reactor.netty.gateway.forward;

import com.network.reactor.netty.gateway.route.RouteDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrpcForwardClient {
    private static final Logger logger = LogManager.getLogger(GrpcForwardClient.class);

    private final HttpClient httpClient;
    private final ConnectionProvider provider;

    private static final String TRAILER_NAMES="grpc-status, grpc-message, grpc-status-details-bin";

    //不允许直接转发的逐跳 Header
    private static final Set<String> GRPC_TRAILER_NAMES = Set.of("grpc-status", "grpc-message", "grpc-status-details-bin");


    public GrpcForwardClient() {
        this.provider = ConnectionProvider.builder("grpc-gateway-backend")
                .maxConnections(100)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();

        this.httpClient = HttpClient.create(provider)
                .protocol(HttpProtocol.H2C)
                .responseTimeout(Duration.ofSeconds(30));
    }


    public Mono<Void> forward(HttpServerRequest request, HttpServerResponse response, RouteDefinition route){


        AtomicBoolean responseStarted = new AtomicBoolean(false);

        return this.httpClient
                .host(route.getIp())
                .port(route.getPort())
                // 转发请求的 Header
                .headers(headers -> copyRequestHeaders(request.requestHeaders(),headers))

                .request(request.method())//保留原始的 Http method

                .uri( request.uri() )//保留 gRpc方法路径
                .send(request.receive().retain()) // 请求body 直接流式转发
                .responseConnection((backendResponse, connection) -> {
                    response.status(backendResponse.status().code());

                    // 复制普通响应 Header
                    copyResponseHeaders(backendResponse.responseHeaders(), response);

                    /*
                     * 预先声明需要转发的 Trailer。
                     *
                     * 注意：这里先按标准 gRPC Trailer 处理。
                     */
                    response.header("Trailer", TRAILER_NAMES);
                    response.chunkedTransfer(true);

                    /*
                     * 4. 暂存 Backend Trailer
                     */
                    HttpHeaders trailers = new DefaultHttpHeaders();
                    // 注册下游 trailer 回调
                    // Backend Body 完成并读取 Trailer 后，才会完成下游发送流。
                    response.trailerHeaders(out ->{
                        copyGrpcTrailers(trailers, out);
                    });

                    //核心 流式读取 Backend Body
                    Flux<ByteBuf> responseBody = connection.inbound().receive().retain();

                    //Body 读取完成之后 读取Backend Trailer concatWith 保证Trailer读取完成之后，下游响应流才完成。
                    Flux<ByteBuf> responseWithTrailers = responseBody.concatWith(
                            backendResponse.trailerHeaders()
                                    .defaultIfEmpty(new DefaultHttpHeaders())
                                    .doOnNext(t -> {
                                        copyGrpcTrailers(t, trailers);
                                    }).thenMany(Flux.empty())
                    );

                    responseStarted.set(true);
                    logger.info("开始流式转发 Backend 响应");
                    return response.send(responseWithTrailers).then()
                             // 客户端取消时,取消对应 Backend 请求 不要在征尘完成时主动关闭连接，正常完成链接可以由连接池复用
                            .doFinally(signal -> {
                                if (signal == SignalType.CANCEL){
                                    connection.dispose();
                                }
                            });


                })
                // // 错误处理
                .onErrorResume(error -> {
                    //error.printStackTrace();
                    logger.error(error);
                    if (responseStarted.get()){
                        // 响应已经开始，不能再修改 HTTP 状态码。 让当前 HTTP/2 Stream 以错误结束。
                        return Mono.error(error);
                    }
                    return response.status(502).sendString(Mono.just("Gateway backend error")).then();
                }).then();

    }


    /*
     * 请求 Headers 转发
     */
    private void copyRequestHeaders(HttpHeaders source, HttpHeaders target) {

        for (Map.Entry<String, String> entry : source) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (isHopByHopHeader(name)) {
                continue;
            }

            /*
             * HTTP/2 只允许 TE: trailers
             */
            if (name.equalsIgnoreCase("te")) {
                if ("trailers".equalsIgnoreCase(value.trim())) {
                    target.add("te", "trailers");
                }
                continue;
            }
            /*
             * Host、Content-Length 等由 Gateway 重新处理。
             */
            if (name.equalsIgnoreCase("host") || name.equalsIgnoreCase("content-length")
                    || name.equalsIgnoreCase("trailer")) {
                continue;
            }

            /*
             * 保留 gRPC Metadata：
             *
             * grpc-timeout
             * grpc-encoding
             * grpc-accept-encoding
             * authorization
             * user-agent
             * 自定义 Metadata
             */
            target.add(name, value);
        }
    }



    /**
     * 复制 Header
     */
    private void copyResponseHeaders(HttpHeaders source, HttpServerResponse target) {

        for (Map.Entry<String, String> entry : source) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (isHopByHopHeader(name)) {
                continue;
            }
            if (name.equalsIgnoreCase("content-length")
                    || name.equalsIgnoreCase("transfer-encoding")
                    || name.equalsIgnoreCase("trailer")
                    || name.equalsIgnoreCase("connection")) {
                continue;
            }

            /*
             * addHeader 保留重复 Header。
             */
            target.addHeader(name, value);
        }
    }

    /*
     * Hop-by-hop Headers
     */
    private boolean isHopByHopHeader(String name) {
        return name.equalsIgnoreCase("connection")
                || name.equalsIgnoreCase("keep-alive")
                || name.equalsIgnoreCase("proxy-connection")
                || name.equalsIgnoreCase("transfer-encoding")
                || name.equalsIgnoreCase("upgrade");
    }

    /*
     * 转发 gRPC Trailer
     */
    private void copyGrpcTrailers(HttpHeaders source, HttpHeaders target) {

        for (Map.Entry<String, String> entry : source) {
            String name = entry.getKey();
            if (GRPC_TRAILER_NAMES.contains(name.toLowerCase())) {
                target.add(name, entry.getValue());
            }
        }
    }
}
