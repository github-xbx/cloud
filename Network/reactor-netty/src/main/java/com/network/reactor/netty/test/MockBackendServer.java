package com.network.reactor.netty.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class MockBackendServer {


    public static void main(String[] args) {
        DisposableServer server = HttpServer.create()
                .host("127.0.0.1")
                .port(9000)
                .protocol(HttpProtocol.H2C)
                .handle((request, response) -> {

                    System.out.println("Backend 收到请求: " + request.uri());

                    response.status(200);
                    response.header("content-type", "application/grpc");

                    // 每隔 1 秒发送一条 gRPC Message
                    Flux<ByteBuf> messageFlux = Flux
                            .interval(Duration.ofSeconds(1))
                            .take(10)
                            .map(index -> {
                                String message = "Server Message " + index;
                                System.out.println("发送消息: " + message);
                                return encodeGrpcMessage(message);
                            });

                    response.status(200);
                    response.header("content-type", "application/grpc");
                    response.trailerHeaders(h -> h.set("grpc-status", "0"));
                    return response.send(messageFlux).then();

                })
                .bindNow();

        System.out.println(
                "Mock Backend started: " + server.port()
        );

        server.onDispose().block();
    }

    /**
     * 编码 gRPC Message Frame
     *
     * 1 byte: 压缩标识，0 表示未压缩
     * 4 bytes: 消息长度，大端序
     * N bytes: Protobuf Message
     */
    private static ByteBuf encodeGrpcMessage(String message) {

        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        ByteBuf buffer = Unpooled.buffer(5 + payload.length);

        buffer.writeByte(0);
        buffer.writeInt(payload.length);
        buffer.writeBytes(payload);

        return buffer;
    }

}
