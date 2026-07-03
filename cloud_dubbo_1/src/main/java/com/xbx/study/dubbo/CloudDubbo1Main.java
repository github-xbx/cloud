package com.xbx.study.dubbo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class CloudDubbo1Main {

    public static void main(String[] args) {
        SpringApplication.run(CloudDubbo1Main.class, args);
    }
}
