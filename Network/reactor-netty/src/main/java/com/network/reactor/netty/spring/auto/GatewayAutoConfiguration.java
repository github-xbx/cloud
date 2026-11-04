package com.network.reactor.netty.spring.auto;

import com.network.reactor.netty.gateway.route.RouteDefinition;
import com.network.reactor.netty.gateway.route.RouteManager;
import com.network.reactor.netty.spring.ReactorGatewayConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration // Spring Boot 3.x 推荐，比 @Configuration 更轻量
@EnableConfigurationProperties(ProxyConfigProperties.class) // 启用配置属性绑定
@ConditionalOnClass(ReactorGatewayConfig.class) // 仅当类路径下存在 GreetingService 时才生效
public class GatewayAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean // 条件2：容器中还没有该Bean时（允许用户自定义覆盖）
    @ConditionalOnProperty(prefix = "my.service", name = "enabled", havingValue = "true", matchIfMissing = true) // 条件3：配置开关
    public RouteManager myService(ProxyConfigProperties properties) {
        RouteManager routeManager = new RouteManager();
        List<ProxyConfigProperties.ProxyConfig> gRpc = properties.gRpc();
        List<ProxyConfigProperties.ProxyConfig> http = properties.http();
        gRpc.forEach(rpc ->
                routeManager.register(new RouteDefinition(rpc.id(), rpc.serviceName(), rpc.ip(), rpc.port()))
        );
        http.forEach(rpc ->
                routeManager.register(new RouteDefinition(rpc.id(), rpc.serviceName(), rpc.ip(), rpc.port()))
        );

        return routeManager;
    }





}
