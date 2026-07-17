package com.xbx.study.device.network.grpc;

import com.xbx.study.device.network.core.DeviceNetworkProvider;
import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.enums.NetworkProtocol;
import com.xbx.study.device.network.core.message.BaseMessage;
import com.xbx.study.device.network.grpc.handler.GrpcNetworkHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcNetworkProvider implements DeviceNetworkProvider, DisposableBean {

    @Value("${grpc.server.port:9090}")
    private int port;

    private NetworkServer server;

    @Override
    public NetworkProtocol protocol() {
        return NetworkProtocol.GRPC;
    }

    @Override
    public NetworkServer create(NetworkHandler handler) {
        if (!(handler instanceof GrpcNetworkHandler grpcHandler)) {
            throw new IllegalArgumentException("handler must implement GrpcNetworkHandler, got: " + handler.getClass().getName());
        }
       // @SuppressWarnings("unchecked")

        server = new GrpcNetworkServer(port, grpcHandler);
        return server;
    }

    @Override
    public void destroy() throws Exception {
        if (server != null) {
            try {
                server.stop();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
