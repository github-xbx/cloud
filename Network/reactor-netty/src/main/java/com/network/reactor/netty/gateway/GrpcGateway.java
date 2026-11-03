package com.network.reactor.netty.gateway;


import com.network.reactor.netty.gateway.config.GatewayConfig;
import com.network.reactor.netty.gateway.route.RouteDefinition;
import com.network.reactor.netty.gateway.route.RouteManager;
import com.network.reactor.netty.gateway.route.RouteMatcher;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;

public class GrpcGateway {

    // 上游 gRPC 服务地址（此处用 h2c 明文演示）
    private static final String UPSTREAM_HOST = "localhost";
    private static final int UPSTREAM_PORT = 6565;


    private DisposableServer disposableServer;

    private final RouteManager routeManager = GatewayConfig.createRouteManager();
    private final BackendClient backendClient = new BackendClient();



    //复用 HttpClient ，是生产环境需要 配置连接池
    public static final HttpClient httpClient = HttpClient.create()
            .protocol(HttpProtocol.H2C)  //明文 Http/2;TLS
            .host(UPSTREAM_HOST)
            .port(UPSTREAM_PORT);


    public static void main(String[] args) {
        GrpcGateway server = new GrpcGateway();
        server.start();
    }


    private static Flux<Void> handleProxy(HttpServerRequest inbound, HttpServerResponse outbound){

        //1.构造对上游的请求：方法、URI、handlers 原样透传
        //  入站 body 以 FLux<ByteBuf> 形式直接作为出战 body 发送 天然支持流式与背压
        return httpClient.
                request(inbound.method())
                .uri(inbound.uri())
                .send(inbound.receive().retain())  //关键：retain 防止ByteBuf 被过早的释放
                .response((upstreamRes, body) -> {
                    //2. 先写回上游的初始响应头和状态码
                    outbound.status(upstreamRes.status());
                    upstreamRes.responseHeaders().forEach(entry -> {
                        outbound.header(entry.getKey(), entry.getValue());
                    });

                    //3. gRPC 的最终状态在 trailers 中，必须强制 chunked 才能发出 trailers
                    outbound.chunkedTransfer(true);

                    //4. 在响应体完成时，将上游的 trailers 原样写入下游 trailers
                    outbound.trailerHeaders(trailer -> {
                        //在写入trailer 的时刻，上游的 trailerheader Mono 应当已经完成
                        upstreamRes.trailerHeaders().subscribe(upstreamTrailers -> {
                            upstreamTrailers.forEach(entry -> {
                                trailer.add(entry.getKey(), entry.getValue());
                            });
                        });
                    });
                    // 3. 桥接响应体：直接使用 body (ByteBufFlux)
                    return outbound.send(body.retain()).then();
                });

    }



    public void start(){
        disposableServer = HttpServer.create()
                .host("0.0.0.0")
                .port(8080)
                .protocol(HttpProtocol.H2C)
                .doOnConnection(connection -> {

                    System.out.println(
                            "[Gateway-Inbound] 新连接: "
                                    + connection.channel().remoteAddress()
                    );

                    connection.onDispose(() -> {
                        System.out.println(
                                "[Gateway-Inbound] 连接关闭: "
                                        + connection.channel().remoteAddress()
                        );
                    });

                })
                .handle((request, response)->{

                    String path = request.uri();
                    String method = request.method().name();

                    String serviceName = RouteMatcher.getServiceName(path);
                    if (serviceName == null || method == null) {
                        return request.receive().then(
                                response.status(400)
                                        .sendString(Mono.just("Invalid gRPC path")).then()
                        );
                    }
                    RouteDefinition route = routeManager.getRoute(serviceName);

                    if (route == null) {
                        return request.receive().then(
                                response.status(404)
                                        .sendString(Mono.just("Route not found")).then()
                        );
                    }

                    System.out.println("收到 HTTP 请求");
                    System.out.println("method: " + method);
                    System.out.println("path: " + path);
                    System.out.println("content-type: "
                            + request.requestHeaders().get("content-type"));

                    // 执行后端转发
                    return backendClient.forward(request, response, route);

                }).bindNow();

        System.out.println(
                "Gateway started: " + disposableServer.port()
        );

        disposableServer.onDispose().block();
    }


}
