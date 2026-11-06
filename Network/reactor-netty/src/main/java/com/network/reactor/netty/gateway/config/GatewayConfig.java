package com.network.reactor.netty.gateway.config;

import com.network.reactor.netty.gateway.route.RouteDefinition;
import com.network.reactor.netty.gateway.route.RouteManager;

public class GatewayConfig {

    public static RouteManager createRouteManager(){
        RouteManager manager = new RouteManager();

        manager.register(new RouteDefinition(
                "greeter",
                "Greeter",
                "127.0.0.1",
                9000,
                1
        ));

        manager.register(new RouteDefinition(
                "order-route",
                "order.OrderService",
                "127.0.0.1",
                9002,
                1
        ));

        return manager;
    }


}
