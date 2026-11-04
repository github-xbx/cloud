package com.network.reactor.netty.gateway.route;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RouteManager {

    private static final Logger logger = LogManager.getLogger(RouteManager.class);

    private final Map<String, RouteDefinition> routes  = new ConcurrentHashMap<>();


    /**
     * 路由注册
     * @param route
     */
    public void register(RouteDefinition route){
        routes.put(route.getServiceName(), route);
        logger.info("注册路由: {} -> {}:{}", route.getServiceName(),route.getIp(), route.getPort());
    }


    public RouteDefinition getRoute(String serviceName){
        return routes.get(serviceName);
    }



    public void remove(String serviceName){
        routes.remove(serviceName);
    }


}
