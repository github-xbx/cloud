package com.xbx.database.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * 让 {@code @PropertySource} 支持加载 .yml 文件。
 *
 * <p>Spring 默认的 {@code @PropertySource} 只支持 .properties / .xml，
 * 传入本工厂即可加载 .yml。
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        Properties properties = Objects.requireNonNull(factory.getObject());
        return new PropertiesPropertySource(
                name != null ? name : Objects.requireNonNull(resource.getResource().getFilename()),
                properties);
    }
}
