# Device Network gRPC 模块

## 概述

本模块实现了基于 gRPC 的设备网络通信，支持：

- ✅ 一元调用（设备注册、心跳）
- ✅ 客户端流式（设备数据上报）
- ✅ 服务端流式（服务端下发命令）
- ✅ 双向流式（实时双向通信）
- ✅ 会话管理和设备绑定
- ✅ 消息类型安全（GrpcMessage）

## 项目结构

```
device_network_grpc/
├── src/main/proto/
│   └── device.proto                        # Protobuf 定义文件
├── src/main/java/.../grpc/
│   ├── GrpcNetworkProvider.java            # Spring Bean 提供者
│   ├── GrpcNetworkServer.java              # gRPC 服务器实现
│   ├── GrpcSessionManager.java             # 会话管理器（单例）
│   ├── GrpcNetworkHandler.java             # gRPC 处理器接口
│   ├── DefaultGrpcNetworkHandler.java      # 默认处理器实现
│   ├── DeviceServiceImpl.java              # gRPC 服务实现
│   ├── GrpcCommunicationService.java       # 业务层服务示例
│   └── message/
│       └── GrpcMessage.java                # gRPC 消息类
└── pom.xml
```

## 核心组件

### 1. GrpcMessage

gRPC 消息类，实现 `BaseMessage` 接口：

```java
public class GrpcMessage implements BaseMessage {
    private String messageId;
    private String deviceId;
    private String sessionId;
    private String type;
    private String payload;
    private long timestamp;
    
    // getter/setter 方法
}
```

### 2. GrpcNetworkHandler

gRPC 网络处理器接口，业务层实现此接口：

```java
public interface GrpcNetworkHandler extends NetworkHandler<GrpcMessage> {
    
    /**
     * 处理设备注册
     */
    boolean onDeviceRegister(String sessionId, String deviceId, String token, String deviceType);
    
    /**
     * 处理心跳
     */
    boolean onHeartbeat(String sessionId, String deviceId);
    
    /**
     * 设备上线回调
     */
    void onDeviceOnline(String sessionId, String deviceId);
    
    /**
     * 设备离线回调
     */
    void onDeviceOffline(String sessionId, String deviceId);
}
```

### 3. GrpcSessionManager (单例)

管理 gRPC 会话和设备绑定：

```java
GrpcSessionManager sessionManager = GrpcSessionManager.getInstance();

// 注册会话
sessionManager.registerSession(sessionId, streamObserver);

// 绑定设备
sessionManager.bindDevice(sessionId, deviceId);

// 检查设备是否在线
boolean isOnline = sessionManager.isDeviceOnline(deviceId);
```

### 4. GrpcNetworkServer

实现 `NetworkServer` 接口：

```java
GrpcNetworkServer server = new GrpcNetworkServer(9090, handler);
server.start();

// 向指定设备发送消息
server.sendToDevice("device_001", "Hello!");

// 广播消息
server.broadcast("Hello all devices!");
```

## 使用方式

### 1. 实现 GrpcNetworkHandler

```java
@Component
public class MyGrpcHandler implements GrpcNetworkHandler {
    
    @Override
    public void onMessage(String sessionId, GrpcMessage message) {
        // 处理消息
        log.info("收到消息: sessionId={}, deviceId={}, type={}", 
                sessionId, message.getDeviceId(), message.getType());
    }
    
    @Override
    public boolean onDeviceRegister(String sessionId, String deviceId, String token, String deviceType) {
        // 验证设备
        return true;
    }
    
    @Override
    public void onDeviceOnline(String sessionId, String deviceId) {
        // 设备上线处理
        log.info("设备上线: {}", deviceId);
    }
    
    @Override
    public void onDeviceOffline(String sessionId, String deviceId) {
        // 设备离线处理
        log.info("设备离线: {}", deviceId);
    }
}
```

### 2. 配置端口

```yaml
# application.yml
grpc:
  server:
    port: 9090
```

### 3. 使用 GrpcNetworkServer

```java
@Service
public class DeviceService {
    
    @Autowired
    private GrpcNetworkServer grpcServer;
    
    public void sendCommand(String deviceId, String command) {
        if (grpcServer.isDeviceOnline(deviceId)) {
            grpcServer.sendToDevice(deviceId, command);
        }
    }
    
    public void broadcastAlarm(String alarm) {
        grpcServer.broadcast("ALARM:" + alarm);
    }
}
```

### 4. 使用 GrpcCommunicationService

```java
@Service
public class BusinessService {
    
    @Autowired
    private GrpcCommunicationService grpcService;
    
    public void sendToDevice(String deviceId, String message) {
        grpcService.sendToDevice(deviceId, message);
    }
    
    public List<String> getOnlineDevices() {
        return grpcService.getOnlineDevices();
    }
}
```

## 消息流转

```
设备连接 → gRPC 调用
    ↓
DeviceServiceImpl 接收请求
    ↓
转换为 GrpcMessage
    ↓
调用 GrpcNetworkHandler.onMessage(sessionId, message)
    ↓
业务层处理消息
    ↓
通过 GrpcNetworkServer 发送响应
    ↓
设备接收响应
```

## 与 TCP 模块的对比

| 特性 | TCP 模块 | gRPC 模块 |
|------|---------|----------|
| 协议 | 自定义文本协议 | Protobuf 二进制 |
| 流式支持 | 无 | 双向流、客户端流、服务端流 |
| 类型安全 | 弱（BaseMessage 接口） | 强（GrpcMessage 类） |
| 消息类 | BaseMessage | GrpcMessage |
| 处理器 | NetworkHandler | GrpcNetworkHandler |
| 跨语言 | 困难 | 原生支持多语言 |

## 注意事项

1. **Proto 编译**：运行 `mvn compile` 自动生成 gRPC 存根类
2. **类型转换**：DeviceServiceImpl 自动将 Proto 消息转换为 GrpcMessage
3. **会话管理**：一元调用没有持久连接，使用 null observer
4. **设备生命周期**：通过 onDeviceOnline/onDeviceOffline 回调通知

## 扩展建议

1. **添加认证拦截器**：实现 gRPC 拦截器进行设备认证
2. **实现健康检查**：添加 gRPC 健康检查服务
3. **添加监控指标**：统计消息数量、在线设备数等
4. **实现重试机制**：客户端自动重试失败的请求
5. **TLS 支持**：添加 SSL/TLS 加密通信

## 测试

### 编译 Proto 文件

```bash
cd device_network_grpc
mvn compile
```

### 运行测试

```java
// 创建处理器
GrpcNetworkHandler handler = new DefaultGrpcNetworkHandler();

// 创建服务器
GrpcNetworkServer server = new GrpcNetworkServer(9090, handler);
server.start();

// 使用客户端测试
// 参考 GrpcClientExample
```
