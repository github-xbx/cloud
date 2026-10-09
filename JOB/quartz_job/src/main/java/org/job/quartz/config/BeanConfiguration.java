package org.job.quartz.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;


import javax.sql.DataSource;
import java.io.IOException;


@Configuration
public class BeanConfiguration {




    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource){

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setConfigLocation(new ClassPathResource("quartz.properties"));
        factory.setDataSource(dataSource);
        return factory;

    }




}
