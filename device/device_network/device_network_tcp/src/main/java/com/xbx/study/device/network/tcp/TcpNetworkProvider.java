package com.xbx.study.device.network.tcp;

import com.xbx.study.device.network.core.DeviceNetworkProvider;
import com.xbx.study.device.network.core.NetworkHandler;
import com.xbx.study.device.network.core.NetworkServer;
import com.xbx.study.device.network.core.enums.NetworkProtocol;
import org.springframework.stereotype.Component;

@Component
public class TcpNetworkProvider implements DeviceNetworkProvider {
    @Override
    public NetworkProtocol protocol() {
        return NetworkProtocol.TCP;
    }

    @Override
    public NetworkServer create(NetworkHandler<?> handler) {
        return new TcpNetworkServer(8081,handler);
    }
}
