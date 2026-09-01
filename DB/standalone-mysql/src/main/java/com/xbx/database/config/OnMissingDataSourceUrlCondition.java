package com.xbx.database.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 当未配置 {@code spring.datasource.url} 时匹配。
 *
 * <p>用于默认数据源 bean 的条件：消费者未配置数据源时创建内置默认数据源，
 * 已配置时本条件不成立、默认数据源退避，改由 Spring Boot 使用消费者配置创建。
 */
public class OnMissingDataSourceUrlCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !StringUtils.hasText(context.getEnvironment().getProperty("spring.datasource.url"));
    }
}
