package com.network.reactor.netty.spring.auto;

import com.network.reactor.netty.gateway.GrpcGateway;
import com.network.reactor.netty.gateway.forward.GrpcForwardClient;
import com.network.reactor.netty.gateway.route.RouteDefinition;
import com.network.reactor.netty.gateway.route.RouteManager;
import com.network.reactor.netty.spring.ReactorGatewayProxy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoConfiguration // Spring Boot 3.x 推荐，比 @Configuration 更轻量
@EnableConfigurationProperties(ProxyConfigProperties.class) // 启用配置属性绑定
@ConditionalOnClass(ReactorGatewayProxy.class) // 仅当类路径下存在 GreetingService 时才生效
public class GatewayAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean // 条件2：容器中还没有该Bean时（允许用户自定义覆盖）
    @ConditionalOnProperty(prefix = "gateway", name = "enabled", havingValue = "true", matchIfMissing = true) // 条件3：配置开关
    public RouteManager myService(ProxyConfigProperties properties) {
        RouteManager routeManager = new RouteManager();
        List<ProxyConfigProperties.ProxyConfig> gRpc = properties.gRpc();
        List<ProxyConfigProperties.ProxyConfig> http = properties.http();
        Optional.ofNullable(gRpc).orElse(Collections.emptyList()).forEach(rpc ->
                routeManager.register(new RouteDefinition(rpc.id(), rpc.serviceName(), rpc.ip(), rpc.port()))
        );
        Optional.ofNullable(http).orElse(Collections.emptyList()).forEach(rpc ->
                routeManager.register(new RouteDefinition(rpc.id(), rpc.serviceName(), rpc.ip(), rpc.port()))
        );

        return routeManager;
    }


    @Bean
    @ConditionalOnBean(value = RouteManager.class)
    public GrpcGateway grpcGateway(RouteManager routeManager, ProxyConfigProperties properties){
        return GrpcGateway.builder()
                .routeManager(routeManager)
                .host(properties.host())
                .port(properties.port())
                .grpcForwardClient(new GrpcForwardClient())
                .build();
    }

    @Bean
    @ConditionalOnBean(value = GrpcGateway.class)
    public ReactorGatewayProxy grpcProxy(GrpcGateway gateway){
        ReactorGatewayProxy gatewayProxy = new ReactorGatewayProxy(gateway);
        gatewayProxy.run();

        return gatewayProxy;
    }



}
