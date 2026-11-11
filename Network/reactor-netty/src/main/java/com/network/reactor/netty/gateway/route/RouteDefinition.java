package com.network.reactor.netty.gateway.route;

public class RouteDefinition {

    private final String id;

    //gRPC 服务的全名
    private final String serviceName;

    //后端ip
    private final String ip;

    //后端端口
    private final  int port;

    //权重
    private final int weight;


    public RouteDefinition(String id, String serviceName, String ip, int port, int weight) {
        this.id = id;
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getWeight() {
        return weight;
    }
}
