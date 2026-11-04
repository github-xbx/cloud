package com.network.reactor.netty.spring.auto;

import com.network.reactor.netty.spring.ReactorGatewayConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration // Spring Boot 3.x 推荐，比 @Configuration 更轻量
@EnableConfigurationProperties(ProxyConfigProperties.class) // 启用配置属性绑定
@ConditionalOnClass(ReactorGatewayConfig.class) // 仅当类路径下存在 GreetingService 时才生效
public class GatewayAutoConfiguration {




}
