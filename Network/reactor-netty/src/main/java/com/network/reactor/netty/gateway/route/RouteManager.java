package com.network.reactor.netty.gateway.route;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import com.network.reactor.netty.gateway.loadBalancer.WeightedRandomLoadBalancer;

public class RouteManager {

    private static final Logger logger = LogManager.getLogger(RouteManager.class);

    private final Map<String, List<RouteDefinition>> routes = new ConcurrentHashMap<>();
    private final Map<String, WeightedRandomLoadBalancer> loadBalancers = new ConcurrentHashMap<>();


    /**
     * 路由注册
     * @param route
     */
    public void register(RouteDefinition route){
        routes.compute(route.getServiceName(), (service, current) -> {
            List<RouteDefinition> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);
            updated.removeIf(existing -> existing.getId().equals(route.getId()));
            updated.add(route);
            return List.copyOf(updated);
        });
        loadBalancers.compute(route.getServiceName(), (service, ignored) -> new WeightedRandomLoadBalancer(routes.get(service)));
        logger.info("注册路由: {} -> {}:{}", route.getServiceName(),route.getIp(), route.getPort());
    }

    /**
     * 路由查询
     */
    public RouteDefinition selectRoute(String serviceName) {
        WeightedRandomLoadBalancer balancer = loadBalancers.get(serviceName);
        return balancer == null ? null : balancer.select();
    }



    public void remove(String serviceName){
        routes.remove(serviceName);
        loadBalancers.remove(serviceName);
    }


}
