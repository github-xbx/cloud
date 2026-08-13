package com.xbx.study.web.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
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


    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource){

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setConfigLocation(new ClassPathResource("quartz.properties"));
        factory.setDataSource(dataSource);
        return factory;

    }


    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory connectionFactory){

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //使用Jackson2JsonRedisSerializer 来序列化和反序列化redis的key
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);  //开启类型信息支持
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,Object.class);


        template.setKeySerializer(new StringRedisSerializer());  //key 使用 String 序列化方式
        template.setValueSerializer(serializer); //value序列化方式使用jackson

        //设置 hash key 和 value 序列化模式为 jackson
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

}
