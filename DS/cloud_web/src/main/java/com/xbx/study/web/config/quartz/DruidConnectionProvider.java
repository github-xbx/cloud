package com.xbx.study.web.config.quartz;

import com.alibaba.druid.pool.DruidDataSource;
import org.quartz.SchedulerException;
import org.quartz.utils.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Quartz 的 Druid 连接池提供者
 * 用于替换 Quartz 默认的 C3P0 连接池
 * 当再 quartz.properties 中配置数据源时使用
 */
public class DruidConnectionProvider implements ConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(DruidConnectionProvider.class);

    // ========== 以下属性与 quartz.properties 配置对应 ==========
    // 注意：属性名必须和配置文件中去掉前缀后的 key 完全一致
    private String driver;
    private String URL;
    private String user;
    private String password;
    private int maxConnections = 10;           // 最大连接数
    private String validationQuery;            // 验证查询语句，如 "SELECT 1"
    private boolean validateOnCheckout = false;
    private int idleConnectionValidationSeconds = 50;
    private String maxCachedStatementsPerConnection = "120";
    private String discardIdleConnectionsSeconds = "0";
    // =====================================================

    private DruidDataSource datasource;

    @Override
    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    @Override
    public void shutdown() throws SQLException {
        if (datasource != null) {
            datasource.close();
        }
    }

    @Override
    public void initialize() throws SQLException {
        // 1. 基础参数校验
        if (this.URL == null) {
            throw new SQLException("DB URL cannot be null");
        }
        if (this.driver == null) {
            throw new SQLException("DB driver class name cannot be null");
        }
        if (this.maxConnections < 0) {
            throw new SQLException("Max connections must be greater than 0");
        }

        // 2. 创建并配置 DruidDataSource
        datasource = new DruidDataSource();
        datasource.setDriverClassName(driver);
        datasource.setUrl(URL);
        datasource.setUsername(user);
        datasource.setPassword(password);
        datasource.setMaxActive(maxConnections);
        datasource.setInitialSize(1);               // 初始连接数
        datasource.setMinIdle(1);                   // 最小空闲连接数
        datasource.setMaxWait(60000);               // 获取连接超时时间（毫秒）

        // 3. 设置验证查询
        if (validationQuery != null) {
            datasource.setValidationQuery(validationQuery);
            datasource.setTestOnBorrow(validateOnCheckout);
            datasource.setTestWhileIdle(true);
            datasource.setTimeBetweenEvictionRunsMillis(idleConnectionValidationSeconds * 1000L);
        }

        // 4. 设置 PSCache（预编译语句缓存）
        try {
            int maxCacheStatements = Integer.parseInt(maxCachedStatementsPerConnection);
            if (maxCacheStatements > 0) {
                datasource.setPoolPreparedStatements(true);
                datasource.setMaxPoolPreparedStatementPerConnectionSize(maxCacheStatements);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid maxCachedStatementsPerConnection value: {}", maxCachedStatementsPerConnection);
        }

        // 5. 设置连接回收策略
        try {
            int idleSeconds = Integer.parseInt(discardIdleConnectionsSeconds);
            if (idleSeconds > 0) {
                datasource.setRemoveAbandoned(true);
                datasource.setRemoveAbandonedTimeout(idleSeconds);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid discardIdleConnectionsSeconds value: {}", discardIdleConnectionsSeconds);
        }

        // 6. 初始化数据源
        datasource.init();

        logger.info("DruidConnectionProvider initialized successfully. URL: {}", URL);
    }

    // ========== Setter 方法（Quartz 通过反射调用这些方法注入配置值） ==========
    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public void setValidateOnCheckout(boolean validateOnCheckout) {
        this.validateOnCheckout = validateOnCheckout;
    }

    public void setIdleConnectionValidationSeconds(int idleConnectionValidationSeconds) {
        this.idleConnectionValidationSeconds = idleConnectionValidationSeconds;
    }

    public void setMaxCachedStatementsPerConnection(String maxCachedStatementsPerConnection) {
        this.maxCachedStatementsPerConnection = maxCachedStatementsPerConnection;
    }

    public void setDiscardIdleConnectionsSeconds(String discardIdleConnectionsSeconds) {
        this.discardIdleConnectionsSeconds = discardIdleConnectionsSeconds;
    }
}
