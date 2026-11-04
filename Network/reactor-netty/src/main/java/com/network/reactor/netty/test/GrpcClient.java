package com.network.reactor.netty.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GrpcClient {

    public static void main(String[] args) throws InterruptedException {


        ConnectionProvider provider = ConnectionProvider
                .builder("grpc-client")
                .maxConnections(1)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();



        try {


            // 1. 创建 HTTP/2 客户端
            HttpClient client = HttpClient.create(provider)
                    .protocol(HttpProtocol.H2C)
                    .host("127.0.0.1")
                    .port(8080)
                    .doOnConnected(connection -> {
                        System.out.println("[Gateway-Outbound] 连接建立: " + connection.channel().remoteAddress());
                    })
                    .doOnDisconnected(connection -> {
                        System.out.println("[Gateway-Outbound] 连接断开: " + connection.channel().remoteAddress());
                    })
                    .doOnRequest((request, connection) -> {
                        System.out.println("[Gateway-Outbound] 请求发送: " + request.resourceUrl());
                    })
                    .doOnResponse((response, connection) -> {
                        System.out.println("[Gateway-Outbound] 后端响应: " + response.status());
                    });

            // 2. 构造 Protobuf 数据
            // 注意：实际项目中应该使用生成的 Protobuf Request 对象
            byte[] protobufBytes = "hello gRPC".getBytes();

            // 3. 编码 gRPC Frame
            byte[] requestFrame = encodeGrpcFrame(protobufBytes);

            // 4. 发起 gRPC 请求
            client.post()
                    .uri("/user.UserService/GetUser")

                    .send((request, outbound) -> {

                        request.header("Content-Type", "application/grpc");
                        request.header("te", "trailers");

                        // 发送 gRPC 二进制数据
                        return outbound.sendByteArray(Mono.just(requestFrame));
                    })

                    .responseConnection((res, conn) -> {

                        // 5. 检查 HTTP 状态
                        if (res.status().code() != 200) {

                            return conn.inbound()
                                    .receive()
                                    .aggregate()
                                    .asString()
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.<Void>error(
                                            new RuntimeException("HTTP错误: " + res.status().code() + ", body: " + body)
                                    ));
                        }

                        // 6. 边收边解帧：不缓冲整条流，收够一条完整 gRPC Message 就打印一条
                        ByteBuf cumulation = Unpooled.buffer();

                        return conn.inbound()
                                .receive()
                                .doOnNext(chunk -> {
                                    // receive() 发出的 ByteBuf 在 onNext 返回后会被释放，必须先拷贝
                                    cumulation.writeBytes(chunk);
                                    drainGrpcFrames(cumulation);
                                })
                                .doFinally(signal -> cumulation.release())

                                // 7. Body 流结束后再读取 Trailers
                                .then(res.trailerHeaders()
                                        .defaultIfEmpty(new DefaultHttpHeaders())
                                        .doOnNext(trailers -> checkGrpcStatus(res, trailers))
                                        .then());
                    })

                    .blockLast();

        }finally {
            // 所有请求结束后，再释放客户端连接池
            provider.disposeLater().block(Duration.ofSeconds(10));

            System.out.println("客户端连接池已释放");
        }

        System.out.println("客户端准备退出");
    }

    /**
     * 从累积缓冲区中解出所有已完整的 gRPC Message 并打印。
     * 一帧 = 1 byte 压缩标志 + 4 bytes 长度 + N bytes payload
     */
    private static void drainGrpcFrames(ByteBuf cumulation) {

        while (cumulation.readableBytes() >= 5) {

            int index = cumulation.readerIndex();

            byte compressed = cumulation.getByte(index);
            int length = cumulation.getInt(index + 1);

            if (length < 0) {
                throw new IllegalArgumentException("非法 gRPC Frame 长度: " + length);
            }

            // 半帧：等下一块数据
            if (cumulation.readableBytes() < 5 + length) {
                break;
            }

            cumulation.skipBytes(5);

            byte[] payload = new byte[length];
            cumulation.readBytes(payload);

            if (compressed != 0) {
                throw new UnsupportedOperationException("暂不支持压缩的 gRPC 消息");
            }

            System.out.println("收到服务端消息: " + new String(payload, StandardCharsets.UTF_8));
        }
    }

    /**
     * 校验 gRPC 状态。
     * MockBackendServer 目前不发 trailer，这里只告警，不丢弃已经收到的消息。
     */
    private static void checkGrpcStatus(HttpClientResponse res, HttpHeaders trailers) {

        String grpcStatus = trailers.get("grpc-status");
        String grpcMessage = trailers.get("grpc-message");

        // 某些 trailers-only 响应会将状态放在响应头中
        if (grpcStatus == null) {
            grpcStatus = res.responseHeaders().get("grpc-status");
        }

        if (grpcStatus == null) {
            System.out.println("警告: 未收到 grpc-status trailer（Mock 后端没有发送 trailer），响应体已完整接收");
            return;
        }

        if (!"0".equals(grpcStatus)) {
            throw new RuntimeException("gRPC调用失败: status=" + grpcStatus + ", message=" + grpcMessage);
        }

        System.out.println("gRPC 状态正常: grpc-status=0");
    }

    /**
     * 编码 gRPC Frame
     */
    public static byte[] encodeGrpcFrame(byte[] protobufBytes) {

        ByteBuffer buffer = ByteBuffer.allocate(5 + protobufBytes.length);

        buffer.order(ByteOrder.BIG_ENDIAN);

        // 压缩标志：0 = 不压缩
        buffer.put((byte) 0);

        // Protobuf 消息长度
        buffer.putInt(protobufBytes.length);

        // Protobuf 数据
        buffer.put(protobufBytes);

        return buffer.array();
    }

    /**
     * 解码单条、未压缩的 gRPC Frame
     */
    public static byte[] decodeGrpcFrame(byte[] frame) {

        if (frame == null || frame.length < 5) {
            throw new IllegalArgumentException("非法 gRPC Frame");
        }

        ByteBuffer buffer = ByteBuffer.wrap(frame)
                .order(ByteOrder.BIG_ENDIAN);

        // 压缩标志
        byte compressed = buffer.get();

        if (compressed != 0) {
            throw new UnsupportedOperationException(
                    "暂不支持压缩的 gRPC 消息"
            );
        }

        // 消息长度
        int length = buffer.getInt();

        if (length < 0 || length != buffer.remaining()) {
            throw new IllegalArgumentException(
                    "gRPC Frame 长度不匹配，length=" + length +
                            ", remaining=" + buffer.remaining()
            );
        }

        byte[] protobufBytes = new byte[length];

        buffer.get(protobufBytes);

        return protobufBytes;
    }
}