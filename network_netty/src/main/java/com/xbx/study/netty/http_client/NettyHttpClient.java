package com.xbx.study.netty.http_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class NettyHttpClient {


    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClient.class);

    private final InetSocketAddress address;

    public NettyHttpClient(InetSocketAddress address) {
        this.address = address;
    }


    public void sendPost(String uri, String body) {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {


            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            //http 编解码器
                            pipeline.addLast(new HttpClientCodec());
                            //聚合 http 消息为完整的 FullHttpResponse
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            //业务处理
                            pipeline.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                                    logger.info("状态码: {}", msg.status());
                                    logger.info("响应头: {}", msg.headers());
                                    logger.info("响应体: {}", msg.content().toString(CharsetUtil.UTF_8));
                                    ctx.close();
                                }
                            });

                        }
                    });
            ChannelFuture future = bootstrap.connect(address).sync();

            // 构造 JSON 请求体
            ByteBuf content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);

            // 构造 http post 请求
            DefaultHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri, content);
            request.headers().set(HttpHeaderNames.HOST, address.getHostString());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            request.headers().set("X-API-KEY", "sk_pave_7x9a2b4c8d3e5f1k_1730284567");

            future.channel().writeAndFlush(request);
            logger.info("请求发送成功.....");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

}
