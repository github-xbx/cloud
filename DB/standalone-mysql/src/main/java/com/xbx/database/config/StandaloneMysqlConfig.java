package com.xbx.database.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/**
 * 默认数据源配置。
 *
 * <p>默认值来自 {@code standalone-mysql-defaults.yml}。消费者未配置 {@code spring.datasource.url}
 * 时使用模块内置默认值创建数据源；一旦消费者配置了标准 {@code spring.datasource.*}，
 * 本默认 bean 退避，改由 Spring Boot {@code DataSourceAutoConfiguration} 使用消费者配置创建。
 */
@Configuration
@EnableConfigurationProperties(StandaloneMysqlProperties.class)
@PropertySource(value = "classpath:standalone-mysql-defaults.yml", factory = YamlPropertySourceFactory.class)
public class StandaloneMysqlConfig {

    @Bean
    @Conditional(OnMissingDataSourceUrlCondition.class)
    public DataSource defaultDataSource(StandaloneMysqlProperties props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(props.getUrl());
        ds.setUsername(props.getUsername());
        ds.setPassword(props.getPassword());
        ds.setDriverClassName(props.getDriverClassName());
        return ds;
    }
}
