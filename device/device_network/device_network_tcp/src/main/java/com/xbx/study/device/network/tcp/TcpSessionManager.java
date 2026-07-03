package com.xbx.study.device.network.tcp;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP 会话管理器（Spring 单例 Bean）
 * 负责管理设备连接、设备ID与会话的映射
 * 
 * 使用 @Component 确保在整个应用中只有一个实例
 */

public class TcpSessionManager {

    private static final Logger log = LoggerFactory.getLogger(TcpSessionManager.class);

    /**
     * 用于存储设备ID的属性键
     */
    public static final AttributeKey<String> DEVICE_ID_KEY = AttributeKey.valueOf("deviceId");

    /**
     * sessionId -> Channel 映射
     */
    private final Map<String, Channel> sessionChannels = new ConcurrentHashMap<>();

    /**
     * deviceId -> sessionId 映射（一个设备可能有多个连接）
     */
    private final Map<String, Set<String>> deviceSessions = new ConcurrentHashMap<>();

    /**
     * sessionId -> deviceId 映射
     */
    private final Map<String, String> sessionDevices = new ConcurrentHashMap<>();



    private TcpSessionManager(){}

    private static class Holder{
        private static final TcpSessionManager INSTANCE  = new TcpSessionManager();
    }

    public static TcpSessionManager  getInstance(){
        return Holder.INSTANCE;
    }


    /**
     * 注册新的连接
     * 
     * @param sessionId 会话ID
     * @param channel 通道
     */
    public void registerSession(String sessionId, Channel channel) {
        sessionChannels.put(sessionId, channel);
        log.debug("注册会话: sessionId={}, remoteAddress={}", sessionId, channel.remoteAddress());
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
        sessionChannels.remove(sessionId);
        log.debug("注销会话: sessionId={}", sessionId);
    }

    /**
     * 绑定设备ID到会话
     * 
     * @param sessionId 会话ID
     * @param deviceId 设备ID
     */
    public void bindDevice(String sessionId, String deviceId) {
        Channel channel = sessionChannels.get(sessionId);
        if (channel == null) {
            log.warn("绑定设备失败：会话不存在, sessionId={}", sessionId);
            return;
        }

        // 设置通道属性
        channel.attr(DEVICE_ID_KEY).set(deviceId);

        // 更新映射关系
        sessionDevices.put(sessionId, deviceId);
        deviceSessions.computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        log.info("设备绑定成功: deviceId={}, sessionId={}, remoteAddress={}", 
                deviceId, sessionId, channel.remoteAddress());
    }

    /**
     * 解绑设备
     * 
     * @param sessionId 会话ID
     */
    public void unbindDevice(String sessionId) {
        String deviceId = sessionDevices.remove(sessionId);
        if (deviceId != null) {
            Set<String> sessions = deviceSessions.get(deviceId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    deviceSessions.remove(deviceId);
                }
            }
            log.info("设备解绑: deviceId={}, sessionId={}", deviceId, sessionId);
        }
    }

    /**
     * 获取会话对应的通道
     * 
     * @param sessionId 会话ID
     * @return 通道，如果不存在返回null
     */
    public Channel getChannel(String sessionId) {
        return sessionChannels.get(sessionId);
    }

    /**
     * 获取会话对应的设备ID
     * 
     * @param sessionId 会话ID
     * @return 设备ID，如果未绑定返回null
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
     * 获取设备的第一个会话ID（通常一个设备只有一个连接）
     * 
     * @param deviceId 设备ID
     * @return 会话ID，如果设备不在线返回null
     */
    public String getFirstSessionId(String deviceId) {
        Set<String> sessions = deviceSessions.get(deviceId);
        if (sessions != null && !sessions.isEmpty()) {
            return sessions.iterator().next();
        }
        return null;
    }

    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean hasSession(String sessionId) {
        return sessionChannels.containsKey(sessionId);
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
        // 检查至少有一个会话是活跃的
        return sessions.stream()
                .map(sessionChannels::get)
                .anyMatch(channel -> channel != null && channel.isActive());
    }

    /**
     * 检查会话是否活跃
     * 
     * @param sessionId 会话ID
     * @return 是否活跃
     */
    public boolean isSessionActive(String sessionId) {
        Channel channel = sessionChannels.get(sessionId);
        return channel != null && channel.isActive();
    }

    /**
     * 获取所有在线的设备ID
     * 
     * @return 设备ID集合
     */
    public Set<String> getOnlineDeviceIds() {
        return deviceSessions.keySet();
    }

    /**
     * 获取所有活动的会话ID
     * 
     * @return 会话ID集合
     */
    public Set<String> getActiveSessionIds() {
        return sessionChannels.keySet();
    }

    /**
     * 获取在线设备数量
     * 
     * @return 设备数量
     */
    public int getOnlineDeviceCount() {
        return deviceSessions.size();
    }

    /**
     * 获取活动会话数量
     * 
     * @return 会话数量
     */
    public int getActiveSessionCount() {
        return sessionChannels.size();
    }
}
