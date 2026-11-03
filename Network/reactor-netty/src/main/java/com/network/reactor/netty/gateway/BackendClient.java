package com.network.reactor.netty.gateway;

import com.network.reactor.netty.gateway.route.RouteDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.time.Duration;
import java.util.Set;

public class BackendClient {

    private final HttpClient httpClient;


    //不允许直接转发的逐跳 Header
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-connection",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length",
            ""
    );


    public BackendClient() {
        this.httpClient = HttpClient.create()
                .protocol(HttpProtocol.H2C)
                .responseTimeout(Duration.ofSeconds(30));
    }


    public Mono<Void> forward(HttpServerRequest request, HttpServerResponse response, RouteDefinition route){

        String uri = request.uri();


        return this.httpClient
                .host(route.getIp())
                .port(route.getPort())
                // 转发请求的 Header
                .headers(headers -> copyHeaders(request.requestHeaders(),headers))
                //保留原始的 Http method
                .request(request.method())
                //保留 gRpc方法路径
                .uri(uri)
                .send(request.receive().retain())
                
                .responseConnection((backendResponse, connection) -> {
                    response.status(backendResponse.status().code());

                    // 复制普通响应 Header
                    backendResponse.responseHeaders().forEach(entry -> {
                        String name = entry.getKey();

                        // 不转发 HTTP/2 内部信息及传输编码信息
                        if (!name.equalsIgnoreCase("transfer-encoding")
                                && !name.equalsIgnoreCase("connection")
                                && !name.equalsIgnoreCase("x-http2-stream-id")) {

                            response.header(name, entry.getValue());
                        }
                    });

                    // 先完整读取 Backend 响应 Body
                    return connection.inbound()
                            .receive()
                            .aggregate()
                            .asByteArray()

                            // Body 读取完成后，再获取 Backend Trailer
                            .defaultIfEmpty(new byte[0])
                            .flatMap(body ->
                                    backendResponse.trailerHeaders()
                                            .defaultIfEmpty(new DefaultHttpHeaders())
                                            .flatMap(trailers -> {

                                                System.out.println("准备转发 Backend Trailer: " + trailers);

                                                response.status(backendResponse.status().code());

                                                response.header("Content-Type", "application/grpc");

                                                // 声明 Trailer
                                                response.header("Trailer", "grpc-status, grpc-message");

                                                // 关键：启用 chunked 传输模式
                                                response.chunkedTransfer(true);

                                                // 设置真正的 Trailer
                                                response.trailerHeaders(out -> {
                                                    String grpcStatus = trailers.get("grpc-status");
                                                    String grpcMessage = trailers.get("grpc-message");

                                                    if (grpcStatus != null) {
                                                        out.set("grpc-status", grpcStatus);
                                                    }

                                                    if (grpcMessage != null) {
                                                        out.set("grpc-message", grpcMessage);
                                                    }
                                                });


                                                return  response.sendByteArray(Mono.just(body)).then();

                                            })

                            );
                })
                // // 错误处理
                .onErrorResume(error -> {
                    error.printStackTrace();
                    return response.status(502).sendString(Mono.just("Gateway backend error"));
                }).then();

    }


    /**
     * 复制 Header
     */
    private void copyHeaders(HttpHeaders source, HttpHeaders target) {

        source.forEach(entry -> {

            String name = entry.getKey();

            if (!HOP_BY_HOP_HEADERS.contains(
                    name.toLowerCase()
            )) {
                target.add(name, entry.getValue());
            }
        });
    }

}
