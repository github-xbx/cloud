package com.xbx.database.annotation;

import com.xbx.database.config.MybatisConfig;
import com.xbx.database.config.StandaloneMysqlConfig;
import com.xbx.database.config.StandaloneMysqlRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 standalone-mysql 模块（单机 MySQL + MyBatis-Plus）。
 *
 * <p>加在消费者的启动类或配置类上，指定 mapper 接口所在包即可：
 * <pre>{@code
 * @EnableStandaloneMysql("com.xxx.mapper")
 * @SpringBootApplication
 * public class MyApp {
 * }
 * }</pre>
 *
 * <p>数据源默认使用模块内置配置（见 {@link StandaloneMysqlConfig}），
 * 消费者可通过标准 {@code spring.datasource.*} 覆盖。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({StandaloneMysqlRegistrar.class, MybatisConfig.class, StandaloneMysqlConfig.class})
public @interface EnableStandaloneMysql {

    /** mapper 接口所在包（与 {@link #basePackages()} 互取别名）。 */
    @AliasFor("basePackages")
    String[] value() default {};

    /** mapper 接口所在包（与 {@link #value()} 互取别名）。 */
    @AliasFor("value")
    String[] basePackages() default {};
}
