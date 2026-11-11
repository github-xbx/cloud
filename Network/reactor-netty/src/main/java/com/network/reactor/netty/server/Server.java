package com.network.reactor.netty.server;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpServer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class Server {


    public static void main(String[] args) {

            TcpServer tcpServer = new Server().tcpServer(new DeviceMessageHandler());
        tcpServer.bindUntilJavaShutdown(
                Duration.ofSeconds(30),
                server -> System.out.println(
                        "TCP Server started: " + server.port()
                )
        );

    }



    public TcpServer tcpServer(DeviceMessageHandler handler){
        TcpServer server = TcpServer.create()
                .host("127.0.0.1")
                .port(8080)
                //设置tcp pipeline 解码器
                .doOnChannelInit((observer, channel, remateAddress) -> {
                    channel.pipeline()
                            .addLast("frameDecoder", new LineBasedFrameDecoder(8192))
                            .addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                })
                // 每个设备连接的业务处理
                .handle((inbound, outbound) -> {

                    AtomicReference<Connection> ref = new AtomicReference<>();
                    inbound.withConnection(ref::set);

                    Connection connection = ref.get();

                    //连接关闭时候的处理操作
                    connection.onDispose()
                            .doFinally((signal) -> {
                                //
                                System.out.println("connection dispose "+signal);
                            })
                            .subscribe();


                    return inbound.receive().asString(StandardCharsets.UTF_8)

                            .concatMap(message ->
                                    handler.handler(message, connection).map(response -> response + "\n")
                            )
                            .as(outbound::sendString);

                });

        return server;

    }



}
