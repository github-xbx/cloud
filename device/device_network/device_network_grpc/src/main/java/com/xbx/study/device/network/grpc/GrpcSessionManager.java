package com.xbx.study.device.network.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC 会话管理器（单例）
 * 负责管理设备连接、设备ID与会话的映射
 */
public class GrpcSessionManager {

    private static final Logger log = LoggerFactory.getLogger(GrpcSessionManager.class);

    private GrpcSessionManager() {
    }

    private static class Holder {
        private static final GrpcSessionManager INSTANCE = new GrpcSessionManager();
    }

    public static GrpcSessionManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * sessionId -> StreamObserver 映射（用于发送下行消息）
     */
    private final Map<String, StreamObserver<?>> sessionObservers = new ConcurrentHashMap<>();

    /**
     * deviceId -> sessionId 映射
     */
    private final Map<String, Set<String>> deviceSessions = new ConcurrentHashMap<>();

    /**
     * sessionId -> deviceId 映射
     */
    private final Map<String, String> sessionDevices = new ConcurrentHashMap<>();

    /**
     * 注册新的会话
     * 
     * @param sessionId 会话ID
     * @param observer StreamObserver
     */
    public void registerSession(String sessionId, StreamObserver<?> observer) {
        sessionObservers.put(sessionId, observer);
        log.debug("注册gRPC会话: sessionId={}", sessionId);
    }

    /**
     * 注销会话
     * 
     * @param sessionId 会话ID
     */
    public void unregisterSession(String sessionId) {
        // 移除设备绑定
        String deviceId = sessionDevices.remove(sessionId);
        if (deviceId != null) {
            Set<String> sessions = deviceSessions.get(deviceId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    deviceSessions.remove(deviceId);
                }
            }
            log.debug("注销设备绑定: deviceId={}, sessionId={}", deviceId, sessionId);
        }

        // 移除会话
        sessionObservers.remove(sessionId);
        log.debug("注销gRPC会话: sessionId={}", sessionId);
    }

    /**
     * 绑定设备ID到会话
     * 
     * @param sessionId 会话ID
     * @param deviceId 设备ID
     */
    public void bindDevice(String sessionId, String deviceId) {
        sessionDevices.put(sessionId, deviceId);
        deviceSessions.computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        log.info("设备绑定成功: deviceId={}, sessionId={}", deviceId, sessionId);
    }

    /**
     * 获取会话的 StreamObserver
     * 
     * @param sessionId 会话ID
     * @return StreamObserver
     */
    public StreamObserver<?> getObserver(String sessionId) {
        return sessionObservers.get(sessionId);
    }

    /**
     * 获取会话对应的设备ID
     * 
     * @param sessionId 会话ID
     * @return 设备ID
     */
    public String getDeviceId(String sessionId) {
        return sessionDevices.get(sessionId);
    }

    /**
     * 获取设备的所有会话ID
     * 
     * @param deviceId 设备ID
     * @return 会话ID集合
     */
    public Set<String> getSessionIds(String deviceId) {
        return deviceSessions.get(deviceId);
    }

    /**
     * 获取设备的第一个会话ID
     * 
     * @param deviceId 设备ID
     * @return 会话ID
     */
    public String getFirstSessionId(String deviceId) {
        Set<String> sessions = deviceSessions.get(deviceId);
        if (sessions != null && !sessions.isEmpty()) {
            return sessions.iterator().next();
        }
        return null;
    }

    /**
     * 检查设备是否在线
     * 
     * @param deviceId 设备ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(String deviceId) {
        Set<String> sessions = deviceSessions.get(deviceId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        return sessions.stream()
                .map(sessionObservers::get)
                .anyMatch(observer -> observer != null);
    }

    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean hasSession(String sessionId) {
        return sessionObservers.containsKey(sessionId);
    }

    /**
     * 获取所有在线设备ID
     * 
     * @return 设备ID集合
     */
    public Set<String> getOnlineDeviceIds() {
        return deviceSessions.keySet();
    }

    /**
     * 获取所有活动会话ID
     * 
     * @return 会话ID集合
     */
    public Set<String> getActiveSessionIds() {
        return sessionObservers.keySet();
    }

    /**
     * 获取在线设备数量
     * 
     * @return 设备数量
     */
    public int getOnlineDeviceCount() {
        return deviceSessions.size();
    }
}
