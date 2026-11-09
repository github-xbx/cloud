package com.network.reactor.netty.collect.client;

import com.network.reactor.netty.collect.client.handler.DeviceInfoResponseHandler;
import com.network.reactor.netty.collect.client.handler.ReadRecordResponseHandler;
import com.network.reactor.netty.collect.client.handler.SetWindSpeedUnitHandler;
import com.network.reactor.netty.collect.client.handler.WriteConfigDataHandler;
import com.network.reactor.netty.collect.client.protocol.MessageCodecSharable;
import com.network.reactor.netty.collect.client.protocol.ProtocolFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.tcp.TcpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;

public class Client {

    private static final int PORT = 8080;




    public void conn(){

        TcpClient.create()
                .host("127.0.0.1")
                .port(PORT)
                .doOnChannelInit((observer, channel, remateAddress) -> {
                    channel.pipeline()
                            .addLast(new IdleStateHandler(0, 30, 0))
                            .addLast(new ProtocolFrameDecoder())
                            .addLast(new MessageCodecSharable())
                            .addLast(new DeviceInfoResponseHandler())
                            .addLast(new ReadRecordResponseHandler())
                            .addLast(new SetWindSpeedUnitHandler())
                            .addLast(new WriteConfigDataHandler());
                })
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10)))
                .wiretap("tcp", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .connectNow(Duration.ofSeconds(3));


    }



}
