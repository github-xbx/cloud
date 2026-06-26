package com.xbx.study.GRPC.collect.manager;

import com.google.protobuf.Message;
import com.xbx.study.GRPC.collect.grpc.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RpcConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(RpcConnectionManager.class);


    private final Map<String, ClientSession<? extends Message, ? extends Message>> _MAP = new ConcurrentHashMap<>();


    public void register(String id, ClientSession<? extends Message, ? extends Message> session) {
        _MAP.put(id,session);
        logger.info("device register : {} ",id);
    }

    public void unregister(String id) {
        _MAP.remove(id);
        logger.info("device unregister : {}", id);
    }

}
