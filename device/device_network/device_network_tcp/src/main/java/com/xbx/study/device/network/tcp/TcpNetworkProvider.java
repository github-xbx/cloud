package com.xbx.study.device.network.tcp;

import com.xbx.study.device.network.core.DeviceNetworkProvider;
import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.enums.NetworkProtocol;
import com.xbx.study.device.network.tcp.handler.TcpNettyNetworkHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TcpNetworkProvider implements DeviceNetworkProvider, DisposableBean {

    @Value("${tcp.server.port:8081}")
    private int port;

    private NetworkServer server;

    @Override
    public NetworkProtocol protocol() {
        return NetworkProtocol.TCP;
    }

    @Override
    public NetworkServer create(NetworkHandler handler) {

        if (!(handler instanceof TcpNettyNetworkHandler tcpHandler)){
            throw new RuntimeException();
        }

        server = new TcpNetworkServer(port, tcpHandler);
        return server;
    }

    @Override
    public void destroy() throws Exception {
        if (server != null){
            try {
                server.stop();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
