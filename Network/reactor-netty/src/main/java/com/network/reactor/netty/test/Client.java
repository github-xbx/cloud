package com.network.reactor.netty.test;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Client {

    public static void main(String[] args) {

        TcpClient client = Client.client();


        Connection connection = client.connectNow(Duration.ofSeconds(2)); //阻塞，只再启动期间

        connection.outbound()
                .sendString(Mono.just("123"), StandardCharsets.UTF_8)
                .then()               // 必须then 否则不会flush
                .subscribe();

        connection.inbound()
                .receive()      //Flux<ByteBuf>
                .asString(StandardCharsets.UTF_8)
                .subscribe(System.out::println, err -> System.out.println(err));


    }




    public static TcpClient client(){
        return TcpClient.create()
                .host("127.0.0.1")
                .port(8080)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .doOnChannelInit((observer, channel, remateAddress) -> {
                    channel.pipeline()
                            .addLast("frameDecoder", new LineBasedFrameDecoder(8192))
                            .addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                })
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10)))
                .wiretap("tcp", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);


    }
}
