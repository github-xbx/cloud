package com.network.reactor.netty.gateway.loadBalancer;

import com.network.reactor.netty.gateway.route.RouteDefinition;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机 负载均衡
 */
public final class WeightedRandomLoadBalancer implements ILoadBalancer<RouteDefinition> {

    private final List<RouteDefinition> routes;
    private final int totalWeight;


    public WeightedRandomLoadBalancer(List<RouteDefinition> list){
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("route must not be empty");
        }

        this.routes = List.copyOf(list);

        //计算总权重
        int sum = 0;
        for (RouteDefinition route : this.routes) {
            if (route == null || route.getWeight()<= 0) {
                throw new IllegalArgumentException("route and weight must be valid; weight must be > 0");
            }
            sum = Math.addExact(sum, route.getWeight());
        }
        this.totalWeight = sum;

    }



    public RouteDefinition select(){
        //再总权重 之中 获取一个随机整数
        int ticket = ThreadLocalRandom.current().nextInt(this.totalWeight);

        //遍历 路由表，随机数做比较 如果 route.weight < ticket 则落到当前route，否则判断下一个route
        for (RouteDefinition route : this.routes) {
            ticket -= route.getWeight();
            if (ticket < 0 ){
                return route;
            }
        }
        throw new IllegalStateException("No backend selected");
    }





}
