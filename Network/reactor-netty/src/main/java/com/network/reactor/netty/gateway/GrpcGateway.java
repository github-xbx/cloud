package com.network.reactor.netty.gateway;


import com.network.reactor.netty.gateway.forward.GrpcForwardClient;
import com.network.reactor.netty.gateway.route.RouteDefinition;
import com.network.reactor.netty.gateway.route.RouteManager;
import com.network.reactor.netty.gateway.route.RouteMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;


public class GrpcGateway implements IGateway {

    // 上游 gRPC 服务地址（此处用 h2c 明文演示）
    private static final Logger logger = LogManager.getLogger(GrpcGateway.class);


    public static Builder builder(){
        return new Builder();
    }


    private DisposableServer disposableServer;

    private final RouteManager routeManager;
    private final GrpcForwardClient grpcForwardClient;
    private final String host;
    private final int port;


    private GrpcGateway(Builder builder){
        this.routeManager = builder.routeManager;
        this.grpcForwardClient = builder.grpcForwardClient;
        this.host = builder.host;
        this.port = builder.port;
    }





    public static void main(String[] args) {
        GrpcGateway gateway = GrpcGateway.builder().build();
        gateway.start();
    }


    public void start(){
        disposableServer = HttpServer.create()
                .host(host)
                .port(port)
                .protocol(HttpProtocol.H2C)
                .doOnConnection(connection -> {
                    logger.info("[Gateway-Inbound] 新连接: {}", connection.channel().remoteAddress());
                    connection.onDispose(() -> {
                        logger.info("[Gateway-Inbound] 连接关闭: {}", connection.channel().remoteAddress());
                    });
                })
                .handle((request, response)->{

                    logger.info("========== Gateway Request ==========");
                    logger.info("Method: {}", request.method());
                    logger.info("URI: {}", request.uri());
                    logger.info("Path: {}", request.path());
                    logger.info("Content-Type: {}", request.requestHeaders().get("content-type"));
                    logger.info("TE: {}", request.requestHeaders().get("te"));
                    logger.info("HTTP Protocol: {}", request.protocol());
                    logger.info("=====================================");

                    String path = request.uri();
                    String method = request.method().name();

                    String serviceName = RouteMatcher.getServiceName(path);
                    if (serviceName == null || method == null) {
                        return request.receive().then(response.status(400).sendString(Mono.just("Invalid gRPC path")).then());
                    }
                    RouteDefinition route = routeManager.getRoute(serviceName);
                    if (route == null) {
                        return request.receive().then(response.status(404).sendString(Mono.just("Route not found")).then());
                    }

                    // 执行后端转发
                    return grpcForwardClient.forward(request, response, route);

                }).bindNow();

        logger.info("==============>Gateway started: {}<================", disposableServer.port());

        disposableServer.onDispose().block();
    }


    public void stop(){
        if (disposableServer != null){
            disposableServer.disposeNow(Duration.ofSeconds(5));
        }
    }


    public static final class Builder {
        private RouteManager routeManager;
        private GrpcForwardClient grpcForwardClient = new GrpcForwardClient();
        private String host = "0.0.0.0";
        private int port = 8080;
        private Builder() {}                         // 构造器私有，只能 builder() 进



        public Builder routeManager(RouteManager routeManager) {
            this.routeManager = routeManager;
            return this;
        }

        public Builder grpcForwardClient(GrpcForwardClient grpcForwardClient) {
            this.grpcForwardClient = grpcForwardClient;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }


        public Builder port(int port) {
            this.port = port;
            return this;
        }

        // 6. 校验集中在 build()，构造出的对象一定合法
        public GrpcGateway build() {

            return new GrpcGateway(this);
        }
    }

}
