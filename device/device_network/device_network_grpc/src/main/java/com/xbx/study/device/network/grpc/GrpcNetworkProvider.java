package com.xbx.study.device.network.grpc;

import com.xbx.study.device.network.core.DeviceNetworkProvider;
import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.enums.NetworkProtocol;
import com.xbx.study.device.network.core.message.BaseMessage;
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
    public NetworkServer create(NetworkHandler<BaseMessage> handler) {
        // 如果 handler 是 GrpcNetworkHandler，直接使用
        if (handler instanceof GrpcNetworkHandler) {
            server = new GrpcNetworkServer(port, (GrpcNetworkHandler) handler);
        } else {
            // 包装为 GrpcNetworkHandler
            GrpcNetworkHandler grpcHandler = new GrpcNetworkHandler() {
                @Override
                public void onMessage(GrpcMessage message) {
                    handler.onMessage(message);
                }
                
                @Override
                public void onMessage(String sessionId, GrpcMessage message) {
                    handler.onMessage(sessionId, message);
                }
            };
            server = new GrpcNetworkServer(port, grpcHandler);
        }
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
