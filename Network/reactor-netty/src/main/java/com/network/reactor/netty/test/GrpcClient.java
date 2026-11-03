package com.network.reactor.netty.test;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GrpcClient {

    public static void main(String[] args) throws InterruptedException {

        // 1. 创建 HTTP/2 客户端
        HttpClient client = HttpClient.create()
                .protocol(HttpProtocol.H2C)
                .host("127.0.0.1")
                .port(8080)
                .doOnConnected(connection -> {

                    System.out.println(
                            "[Gateway-Outbound] 连接建立: "
                                    + connection.channel().remoteAddress()
                    );

                })

                .doOnDisconnected(connection -> {

                    System.out.println(
                            "[Gateway-Outbound] 连接断开: "
                                    + connection.channel().remoteAddress()
                    );

                })

                .doOnRequest((request, connection) -> {

                    System.out.println(
                            "[Gateway-Outbound] 请求发送: "
                                    + request.resourceUrl()
                    );

                })

                .doOnResponse((response, connection) -> {

                    System.out.println(
                            "[Gateway-Outbound] 后端响应: "
                                    + response.status()
                    );

                });

        // 2. 构造 Protobuf 数据
        // 注意：实际项目中应该使用生成的 Protobuf Request 对象
        byte[] protobufBytes = "hello gRPC".getBytes();

        // 3. 编码 gRPC Frame
        byte[] requestFrame = encodeGrpcFrame(protobufBytes);

        // 4. 发起 gRPC 请求
        byte[] response = client.post()
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
                                .flatMap(body -> Mono.error(
                                        new RuntimeException(
                                                "HTTP错误: " +
                                                        res.status().code() +
                                                        ", body: " + body
                                        )
                                ));
                    }

                    // 6. 接收完整响应体
                    return conn.inbound()
                            .receive()
                            .aggregate()
                            .asByteArray()
                            .defaultIfEmpty(new byte[0])

                            // 7. HTTP/2 数据读取完成后，再读取 Trailers
                            .flatMap(responseBytes ->
                                    res.trailerHeaders()
                                            .defaultIfEmpty(new DefaultHttpHeaders())
                                            .map(trailers -> {

                                                // 8. 获取 gRPC 状态
                                                String grpcStatus =
                                                        trailers.get("grpc-status");

                                                String grpcMessage =
                                                        trailers.get("grpc-message");

                                                // 某些 trailers-only 响应会将状态放在响应头中
                                                if (grpcStatus == null) {
                                                    grpcStatus = res.responseHeaders()
                                                            .get("grpc-status");
                                                }

                                                if (!"0".equals(grpcStatus)) {
                                                    throw new RuntimeException(
                                                            "gRPC调用失败: status=" +
                                                                    grpcStatus +
                                                                    ", message=" +
                                                                    grpcMessage
                                                    );
                                                }

                                                // 9. 解码 gRPC Frame
                                                return decodeGrpcFrame(responseBytes);
                                            })
                            );

                })

                .blockLast();

        // 10. 获取 Protobuf 响应数据
        if (response != null) {
            System.out.println("响应数据长度：" + response.length);

            System.out.println(
                    "响应数据：" +
                            java.util.Arrays.toString(response)
            );
        }


        Thread.sleep(30000);

        System.out.println("客户端准备退出");
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