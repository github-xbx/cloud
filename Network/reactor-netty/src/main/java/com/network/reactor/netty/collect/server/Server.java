package com.network.reactor.netty.collect.server;


import com.network.reactor.netty.collect.server.handler.DeviceInfoRespHandler;
import com.network.reactor.netty.collect.server.handler.ReadRecordRespHandler;
import com.network.reactor.netty.collect.server.handler.SetWindSpeedUnitRespHandler;
import com.network.reactor.netty.collect.server.handler.WriteConfigDataRespHandler;
import com.network.reactor.netty.collect.server.handler.reactive.handler.DeviceInfoHandler;
import com.network.reactor.netty.collect.domain.MessageHandler;
import com.network.reactor.netty.collect.server.handler.reactive.handler.ReadRecordHandler;
import com.network.reactor.netty.collect.server.message.*;
import com.network.reactor.netty.collect.server.protocol.ProtocolFrameDecoder;
import com.network.reactor.netty.collect.server.protocol.MessageCodecSharable;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.NettyPipeline;
import reactor.netty.tcp.TcpServer;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Logger logger = LogManager.getLogger(Server.class);

    private static Map<Class<?>, MessageHandler<?>> map = new HashMap<>();


    private static final Boolean isCollect = true;

    static {
        map.put(DeviceInfoRespMsg.class, new DeviceInfoHandler());
        map.put(ReadRecordRespMsg.class, new ReadRecordHandler());
    }


    public static void main(String[] args) {
        //new Server().start();

        new Server().reactiveStart();
    }


    public void start(){

        TcpServer.create()
                .port(8080)
                //reactor.right.reactiveBridge 是 reactor-netty 自己的 ChannelOperationsHandler，
                // 它先拿到入站 ByteBuf，塞进 inbound.receive() 就不再往后传了。
                //索引要用 addBefore 放到ReactiveBridge前面
                //addBefore 按调用顺序依次插到它前面，所以顺序不会乱。出站方向不变
                .doOnChannelInit((observer, channel, remateAddress) -> {
                    channel.pipeline()
                            .addBefore(NettyPipeline.ReactiveBridge,"idle",new IdleStateHandler(0,30,0))
                            .addBefore(NettyPipeline.ReactiveBridge,"protocol",new ProtocolFrameDecoder())
                            .addBefore(NettyPipeline.ReactiveBridge,"codeSharable",new MessageCodecSharable())
                            .addBefore(NettyPipeline.ReactiveBridge,"deviceInfo",new DeviceInfoRespHandler())
                            .addBefore(NettyPipeline.ReactiveBridge,"readRecord",new ReadRecordRespHandler())
                            .addBefore(NettyPipeline.ReactiveBridge,"setWindSpeed",new SetWindSpeedUnitRespHandler())
                            .addBefore(NettyPipeline.ReactiveBridge,"writeConfig",new WriteConfigDataRespHandler());
                })
                .doOnConnection(conn -> {
                    logger.info("[Server] 新连接 => {}",conn.channel().remoteAddress());
                    conn.channel().config().setAutoRead(true);  // 开启读（autoRead 只管读，不影响写）
                    conn.onDispose(() -> {
                        logger.info("[Server] 连接关闭 => {}",conn.channel().remoteAddress());
                    });
                })

                .handle((inbound, outbound) ->
                        outbound.sendObject(Mono.just(new DeviceInfoReqMsg()))
                                .then()
                                .doOnSuccess(ignored -> logger.info("[Server] 已发送设备信息请求"))
                                .doOnError(error -> logger.error("[Server] 发送设备信息请求失败", error))
                                // 关键：handle 返回的 Publisher 一完成，reactor-netty 就会关闭这条连接。
                                // 必须 and 上 inbound，让连接一直挂着，否则"发完就断"，客户端报连接被中止。
                                .and(inbound.receiveObject().then())
                )

                .wiretap("tcp", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .bindNow()
                .onDispose()
                .block();


    }



    public void reactiveStart(){

        TcpServer.create()
                .port(8080)
                .doOnChannelInit((co, channel, socketAddress) -> {
                    channel.pipeline()
                            .addBefore(NettyPipeline.ReactiveBridge,"protocol", new ProtocolFrameDecoder())
                            .addBefore(NettyPipeline.ReactiveBridge,"sharable", new MessageCodecSharable());
                })
                .doOnConnection(conn -> {
                    logger.info("[Server] 新连接 => {}",conn.channel().remoteAddress());
                   // conn.channel().config().setAutoRead(true);  // 开启读（autoRead 只管读，不影响写）
                    conn.onDispose(() -> {
                        logger.info("[Server] 连接关闭 => {}",conn.channel().remoteAddress());
                    });
                })
                .handle(this::serverHandler)
                .wiretap("tcp",LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .bindNow()
                .onDispose()
                .block();

    }



    private Mono<Void> serverHandler(NettyInbound in, NettyOutbound out){

        // Mono 表示 0个 或 1个 收条信息 ，建立连接之后发送的一条信息
        Mono<Object> device = isCollect ? Mono.just(new DeviceInfoReqMsg()) : Mono.empty();
        // Flux 表示 1个 或 n个  server 和 client 之间 的交互流
        Flux<Object> flux = in.receiveObject().concatMap(this::onMessage);

        // 将两个流合并成一个 并写入
        // 注： 合流方式有两种 Flux.concat() 和 FLux.marge()
        // Flux.concat() 是串行方式 先把首帧发完，再订阅入站流
        // Flux.marge() 是并行方式 两条流同时推进。心跳那种定时推送要用它
        return out.sendObject(Flux.concat(device,flux)).then();
    }


    @SuppressWarnings("unchecked")
    private Mono<Object> onMessage(Object message){
        MessageHandler<Object> handler = (MessageHandler<Object>) map.get(message.getClass());
        return handler == null ? Mono.empty() : handler.handle(message);
    }




}
