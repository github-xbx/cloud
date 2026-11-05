package com.network.reactor.netty.spring;

import com.network.reactor.netty.gateway.IGateway;
import org.springframework.beans.factory.DisposableBean;

public class ReactorGatewayProxy implements DisposableBean {

    private final IGateway gateway;

    public ReactorGatewayProxy(IGateway gateway){
        this.gateway = gateway;
    }

    /**
     * 独立线程运行
     */
    public void run() {
        new Thread(gateway::start,"gateway-thread").start();
    }


    /**
     * Bean 销毁方法
     */
    @Override
    public void destroy() throws Exception {
        gateway.stop();
    }
}
