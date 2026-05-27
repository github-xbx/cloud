package com.xbx.study.web.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;


@Configuration
public class BeanConfiguration {


    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "application.enable", name = "redis", havingValue = "true")
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(
                new ClassPathResource("redisson-cluster.yml").getInputStream()
        );
        return Redisson.create(config);
    }

}
