package com.network.reactor.netty.spring.auto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * spring boot 配置
 * @param gRpc
 * @param http
 */
@ConfigurationProperties(prefix = "gateway")
public record ProxyConfigProperties(
        Boolean enabled,
        @DefaultValue("0.0.0.0") String host,
        @DefaultValue("8080") int port,
        @NestedConfigurationProperty List<ProxyConfig> gRpc,
        @NestedConfigurationProperty List<ProxyConfig> http
) {
    public record ProxyConfig(
            String id,
            //gRPC 服务的全名
            String serviceName,
            //后端ip
            String ip,
            //后端端口
            int port,
            @DefaultValue("1") int weight
    ){}
}



