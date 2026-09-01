package com.xbx.database.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * standalone-mysql 内置默认数据源配置。
 *
 * <p>默认值在模块资源 {@code standalone-mysql-defaults.yml} 中定义（前缀 standalone.mysql）；
 * 消费者零配置时使用这些默认值，如需覆盖，在自身 application.yml 中配
 * {@code standalone.mysql.*} 或标准 {@code spring.datasource.*} 即可。
 */
@ConfigurationProperties(prefix = "standalone.mysql")
public class StandaloneMysqlProperties {

    private String url;

    private String username;

    private String password;

    private String driverClassName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}
