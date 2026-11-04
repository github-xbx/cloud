package com.network.reactor.netty.gateway;


import com.network.reactor.netty.gateway.config.GatewayConfig;
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


public class GrpcGateway {

    // 上游 gRPC 服务地址（此处用 h2c 明文演示）
    private static final Logger logger = LogManager.getLogger(GrpcGateway.class);


    private DisposableServer disposableServer;

    private final RouteManager routeManager = GatewayConfig.createRouteManager();
    private final GrpcForwardClient grpcForwardClient = new GrpcForwardClient();





    public static void main(String[] args) {
        GrpcGateway server = new GrpcGateway();
        server.start();
    }


    public void start(){
        disposableServer = HttpServer.create()
                .host("0.0.0.0")
                .port(8080)
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


}
