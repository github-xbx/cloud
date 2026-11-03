package com.network.reactor.netty.test;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;

public class MockBackendServer {


    public static void main(String[] args) {
        DisposableServer server = HttpServer.create()
                .host("127.0.0.1")
                .port(9000)
                .protocol(HttpProtocol.H2C)
                .handle((request, response) -> {

                    System.out.println(
                            "Backend 收到请求: " + request.uri()
                    );

                    return response
                            .status(200)
                            .header(HttpHeaderNames.CONTENT_TYPE, "application/grpc")
                            .header(HttpHeaderNames.TRAILER, "grpc-status")
                            .trailerHeaders(headers -> {
                                headers.set("grpc-status", "0");
                            })
                            .send(request.receive().retain());
                })
                .bindNow();

        System.out.println(
                "Mock Backend started: " + server.port()
        );

        server.onDispose().block();
    }
}
