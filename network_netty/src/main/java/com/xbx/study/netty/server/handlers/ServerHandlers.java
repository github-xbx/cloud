package com.xbx.study.netty.server.handlers;

import com.xbx.study.common.constants.NettySceneEnum;
import com.xbx.study.netty.protobuf.UserProtobuf;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Handler加载器
 * 负责根据配置动态创建Handler实例
 */
public class ServerHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandlers.class);


    /**
     * Handler实例缓存
     */
    private List<ChannelHandler> handlerList = new ArrayList<>();


    public ServerHandlers() {
    }

    public ServerHandlers(List<ChannelHandler> list) {
        this.handlerList = list;
    }

    public List<ChannelHandler> getHandlerList() {
        return handlerList;
    }

    public void setHandlerList(List<ChannelHandler> handlerList) {
        this.handlerList = handlerList;
    }

    public static ServerHandlers.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private NettySceneEnum scene;


        public Builder scene(NettySceneEnum scene) {
            this.scene = scene;
            return this;
        }

        public ServerHandlers build() {

            return switch (scene) {
                case BASIC -> {
                    List<ChannelHandler> list = List.of(
                            new ServerChannelEventHandler(),
                            new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                    yield new ServerHandlers(list);
                }
                case HTTP -> {
                    List<ChannelHandler> httpList = List.of(new ServerChannelEventHandler());
                    yield new ServerHandlers(httpList);
                }
                case PROTOBUF -> {
                    List<ChannelHandler> protobufList = List.of(
                            new ServerChannelEventHandler(),
                            new ProtobufVarint32FrameDecoder(),
                            new ProtobufDecoder(UserProtobuf.User.getDefaultInstance()),
                            new ServerProtobufHandler());
                    yield new ServerHandlers(protobufList);
                }
                default -> new ServerHandlers();
            };
        }


    }



    /**
     * 清空缓存
     */
    public void clearCache() {
        handlerList.clear();
    }


}