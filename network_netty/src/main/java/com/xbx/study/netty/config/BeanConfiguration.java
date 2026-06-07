package com.xbx.study.netty.config;

import com.xbx.study.common.constants.NettySceneEnum;
import com.xbx.study.netty.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class BeanConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BeanConfiguration.class);

    private final PropertiesConfig properties;
    @Autowired
    public BeanConfiguration(PropertiesConfig properties) {
        this.properties = properties;
    }

    /**
     * netty server bean
     */
    @Bean(initMethod = "server", destroyMethod = "destroy")
    public NettyServer protobufNettyServer() {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", properties.getServerPort());
        logger.debug("加载服务器，ip=>{}, port=>{}",address.getHostName(),address.getPort());
        return new NettyServer(address, NettySceneEnum.PROTOBUF);
    }
}