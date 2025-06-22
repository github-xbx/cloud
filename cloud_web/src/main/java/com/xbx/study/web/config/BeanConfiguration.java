package com.xbx.study.web.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class BeanConfiguration {

    /**
     * 需要配置bean 否则会找不到数据库配置信息
     * @return com.alibaba.druid.pool.DruidDataSource
     */
    //@ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource druid() {
        return new DruidDataSource();
    }
}
