package com.xbx.study.netty.server.service;

public interface TestService {


    /**
     * 发送 同步消息
     * @param id 客户端id
     * @return 返回消息
     */
    String sendSyncMessage(Integer id) throws Exception;
}
