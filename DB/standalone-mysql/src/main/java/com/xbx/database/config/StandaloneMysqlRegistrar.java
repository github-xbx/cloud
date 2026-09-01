package com.xbx.database.config;

import com.xbx.database.annotation.EnableStandaloneMysql;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 根据 {@link EnableStandaloneMysql} 注解注册 Mapper 扫描。
 *
 * <p>显式指定 basePackages 时注册 {@link MapperScannerConfigurer}；
 * 未指定时交由 MyBatis-Plus 默认扫描消费者主配置类所在包。
 */
public class StandaloneMysqlRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String SCANNER_BEAN_NAME = "standaloneMysqlMapperScannerConfigurer";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(
                EnableStandaloneMysql.class.getName());
        if (attrs == null) {
            return;
        }

        Set<String> basePackages = new LinkedHashSet<>();
        for (String pkg : (String[]) attrs.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attrs.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        if (basePackages.isEmpty()) {
            // 未显式指定包时，交由 MyBatis-Plus 默认扫描消费者主配置类所在包
            return;
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("basePackage",
                StringUtils.collectionToCommaDelimitedString(basePackages));
        registry.registerBeanDefinition(SCANNER_BEAN_NAME, builder.getBeanDefinition());
    }
}
