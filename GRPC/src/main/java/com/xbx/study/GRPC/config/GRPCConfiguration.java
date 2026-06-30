package com.xbx.study.GRPC.config;

import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GRPCConfiguration {


//    @Bean
//    public GlobalServerInterceptorConfigurer globalServerInterceptorRegistry(ApplicationContext applicationContext){
//        return new GlobalServerInterceptorConfigurer() {
//            @Override
//            public void configureServerInterceptors(List<ServerInterceptor> interceptors) {
//
//            }
//        };
//    }




}
