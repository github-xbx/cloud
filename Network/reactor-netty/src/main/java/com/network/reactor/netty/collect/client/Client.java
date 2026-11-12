package com.network.reactor.netty.collect.client;

import com.network.reactor.netty.collect.client.handler.DeviceInfoResponseHandler;
import com.network.reactor.netty.collect.client.handler.HeartBeatResponseHandler;
import com.network.reactor.netty.collect.client.handler.ReadRecordResponseHandler;
import com.network.reactor.netty.collect.client.handler.SetWindSpeedUnitHandler;
import com.network.reactor.netty.collect.client.handler.WriteConfigDataHandler;
import com.network.reactor.netty.collect.client.handler.reactive.handler.DeviceInfoHandler;
import com.network.reactor.netty.collect.client.handler.reactive.handler.ReadRecordHandler;
import com.network.reactor.netty.collect.client.message.impl.DeviceInfoRequest;
import com.network.reactor.netty.collect.client.message.impl.DeviceInfoResponse;
import com.network.reactor.netty.collect.client.message.impl.ReadRecordResponse;
import com.network.reactor.netty.collect.client.protocol.MessageCodecSharable;
import com.network.reactor.netty.collect.client.protocol.ProtocolFrameDecoder;
import com.network.reactor.netty.collect.domain.MessageHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.NettyPipeline;
import reactor.netty.tcp.TcpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final int PORT = 8080;

    private static Map<Class<?>, MessageHandler<?>> handlerMap = new HashMap<>();
    static {
        handlerMap.put(DeviceInfoResponse.class, new DeviceInfoHandler());
        handlerMap.put(ReadRecordResponse.class, new ReadRecordHandler());
    }



    public static void main(String[] args) throws InterruptedException {
        //new Client().conn();
        new Client().reactorConn();
    }



    public void conn() throws InterruptedException {

        DeviceInfoRequest deviceInfoRequest = new DeviceInfoRequest();
        deviceInfoRequest.setDeviceId(100);
        deviceInfoRequest.setDeviceType(1);
        deviceInfoRequest.setRecodeType(1);
        deviceInfoRequest.setVersion(1);
        deviceInfoRequest.setFrame(2);

        Connection connection = TcpClient.create()
                .host("127.0.0.1")
                .port(PORT)
                .doOnChannelInit((observer, channel, remateAddress) -> {
                    channel.pipeline()
                            .addBefore(NettyPipeline.ReactiveBridge,"idle",new IdleStateHandler(0, 30, 0))
                            .addBefore(NettyPipeline.ReactiveBridge,"protocol",new ProtocolFrameDecoder())
                            .addBefore(NettyPipeline.ReactiveBridge,"codeSharable",new MessageCodecSharable())
                            .addBefore(NettyPipeline.ReactiveBridge,"deviceInfo",new DeviceInfoResponseHandler(deviceInfoRequest))
                            .addBefore(NettyPipeline.ReactiveBridge,"readRecord",new ReadRecordResponseHandler())
                            .addBefore(NettyPipeline.ReactiveBridge,"setWindSpeed",new SetWindSpeedUnitHandler())
                            .addBefore(NettyPipeline.ReactiveBridge,"writeConfig",new WriteConfigDataHandler())
                            .addBefore(NettyPipeline.ReactiveBridge,"heart",new HeartBeatResponseHandler());
                })
                .wiretap("tcp", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                // reactor-netty 是"按需读取"：没人订阅入站，channel 的 autoRead 就是 false，一条数据都读不到。
                // 这里订阅入站，让 reactor 持续读取；消息本身由 pipeline 里的 handler 处理，
                // 落到这里的都是没人认领的消息，打出来方便排查。
                .handle((in, out) -> in.receiveObject()
                        .doOnNext(msg -> logger.info("[Client] 未被业务 handler 处理的消息 => {}", msg))
                        .then()
                )
                .connectNow(Duration.ofSeconds(3));


        System.out.println("TCP 已连接，channel=" + connection.channel());

        // 保持主线程运行；收到 Ctrl+C 时进程退出，连接随之关闭
        new CountDownLatch(1).await();
    }




    public void reactorConn() throws InterruptedException {
        TcpClient.create()
                .host("127.0.0.1")
                .port(8080)
                .doOnChannelInit((co, channel, address) -> {
                    channel.pipeline()
                            .addBefore(NettyPipeline.ReactiveBridge,"protocol", new ProtocolFrameDecoder())
                            .addBefore(NettyPipeline.ReactiveBridge,"sharable", new MessageCodecSharable());
                })
                .handle(this::clientHandler)
                .wiretap("tcp",LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .connectNow(Duration.ofSeconds(3));

        // 保持主线程运行；收到 Ctrl+C 时进程退出，连接随之关闭
        new CountDownLatch(1).await();
    }

    @SuppressWarnings("unchecked")
    public Mono<Void> clientHandler(NettyInbound in, NettyOutbound out){

        Flux<Object> flux = in.receiveObject().concatMap((a) -> {
            MessageHandler<Object> messageHandler = (MessageHandler<Object>)handlerMap.get(a.getClass());
            return messageHandler == null ? Mono.empty() : messageHandler.handle(a);
        });
        return out.sendObject(flux).then();
    }

}
