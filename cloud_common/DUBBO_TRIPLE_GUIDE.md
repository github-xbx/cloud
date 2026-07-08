# Dubbo Triple + Protobuf 集成指南

## 1. 概述

`cloud_common` 模块使用 **dubbo-maven-plugin**（Dubbo 3.3+ 内置插件），从一个 `.proto` 文件自动生成三套代码：

- **Protobuf 消息类** — `GrpcReq`, `GrpcResp`（序列化/反序列化）
- **服务接口** — `DubboGrpcService`（业务接口，服务端实现它，客户端引用它）
- **Triple 桩代码** — `DubboDubboGrpcServiceTriple`（协议层，包含 `ServiceImplBase` 和 `Stub`）

### 生成文件一览

```
target/generated-sources/protobuf/java/com/xbx/study/dubbo/common/grpc/
├── GrpcReq.java                          # 请求消息
├── GrpcReqOrBuilder.java                 # 请求消息 Builder 接口
├── GrpcResp.java                         # 响应消息
├── GrpcRespOrBuilder.java                # 响应消息 Builder 接口
├── DubboGrpcProto.java                   # Proto 注册类
├── DubboGrpcService.java                 # ★ 服务接口（服务端实现它）
└── DubboDubboGrpcServiceTriple.java      # ★ Triple 桩代码
    ├── DubboGrpcServiceImplBase          #    服务端基类（继承它）
    └── DubboGrpcServiceStub             #    客户端桩（直接调用）
```

---

## 2. 服务端实现

### 2.1 实现方式一：同步风格（推荐，最简单）

继承 `DubboGrpcServiceImplBase`，重写同步方法，直接返回结果：

```java
package com.xbx.study.dubbo.service;

import com.xbx.study.dubbo.common.grpc.DubboDubboGrpcServiceTriple.DubboGrpcServiceImplBase;
import com.xbx.study.dubbo.common.grpc.DubboGrpcService;
import com.xbx.study.dubbo.common.grpc.GrpcReq;
import com.xbx.study.dubbo.common.grpc.GrpcResp;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Dubbo Triple 服务端实现
 *
 * 关键点：
 * 1. extends DubboGrpcServiceImplBase（不是 DubboGrpcServiceGrpc.xxxBase！）
 * 2. 重写 hello(GrpcReq) 同步方法，直接 return GrpcResp
 * 3. @DubboService(protocol = "tri") 指定 triple 协议
 */
@DubboService(protocol = "tri")
public class DubboGrpcImpl extends DubboGrpcServiceImplBase {

    @Override
    public GrpcResp hello(GrpcReq request) {
        System.out.println("收到请求: " + request.getReq());

        return GrpcResp.newBuilder()
                .setResp("服务端响应: " + request.getReq())
                .build();
    }
}
```

### 2.2 实现方式二：异步/流式风格

如果需要手动控制 StreamObserver（比如服务端流式推送），重写带 StreamObserver 的版本：

```java
@DubboService(protocol = "tri")
public class DubboGrpcStreamImpl extends DubboGrpcServiceImplBase {

    @Override
    public void hello(GrpcReq request,
                      org.apache.dubbo.common.stream.StreamObserver<GrpcResp> responseObserver) {
        // 发送响应
        responseObserver.onNext(GrpcResp.newBuilder().setResp("resp").build());
        // 标记完成
        responseObserver.onCompleted();
    }
}
```

### 2.3 服务端配置 (`application.yml`)

```yaml
server:
  port: 10011

dubbo:
  application:
    name: cloud_dubbo_0         # 应用名
  protocol:
    name: tri                   # 使用 Triple 协议（兼容 gRPC）
    port: 20880                 # 监听端口
  registry:
    address: N/A                # 直连模式（不注册到注册中心）
  scan:
    base-packages: com.xbx.study.dubbo.service   # 扫描 @DubboService
```

---

## 3. 客户端调用

### 3.1 注入接口直接调用（推荐）

```java
package com.xbx.study.dubbo.client;

import com.xbx.study.dubbo.common.grpc.DubboGrpcService;
import com.xbx.study.dubbo.common.grpc.GrpcReq;
import com.xbx.study.dubbo.common.grpc.GrpcResp;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class DubboGrpcClient {

    // 引用远程服务
    // 注意：类型是 DubboGrpcService（生成的接口），不是 DubboGrpcServiceGrpc
    @DubboReference(protocol = "tri")
    private DubboGrpcService dubboGrpcService;

    public String callHello(String msg) {
        GrpcReq request = GrpcReq.newBuilder()
                .setReq(msg)
                .build();

        // 同步调用，直接拿到返回值
        GrpcResp response = dubboGrpcService.hello(request);

        return response.getResp();
    }
}
```

### 3.2 使用 Stub 调用（不依赖 Spring 注入）

```java
import com.xbx.study.dubbo.common.grpc.DubboDubboGrpcServiceTriple;
import com.xbx.study.dubbo.common.grpc.DubboGrpcService;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.TriRpcUtil;

// 创建 stub
DubboGrpcService stub = DubboDubboGrpcServiceTriple.newStub(invoker);

// 调用
GrpcResp resp = stub.hello(GrpcReq.newBuilder().setReq("hello").build());
```

### 3.3 客户端配置 (`application.yml`)

```yaml
server:
  port: 10010

dubbo:
  application:
    name: cloud_dubbo_1         # 应用名
  protocol:
    name: tri
    port: 20881
```

### 3.4 直连方式（不依赖注册中心）

如果在本地开发测试，服务端和客户端直连，在 `dubbo-consumer` 端配置：

```java
@DubboReference(protocol = "tri", url = "tri://localhost:20880")
private DubboGrpcService dubboGrpcService;
```

或者通过配置文件指定：

```yaml
dubbo:
  consumer:
    check: false
  cloud:
    subscribed-services: ""
```

---

## 4. 从旧 gRPC 风格迁移（重要）

如果你之前用 `protobuf-maven-plugin` + `protoc-gen-grpc-java` 生成代码，新旧 API 对比如下：

| 项目 | 旧风格 (gRPC) | 新风格 (Triple) |
|------|--------------|-----------------|
| 服务接口 | `DubboGrpcServiceGrpc` | `DubboGrpcService` |
| 服务端基类 | `DubboGrpcServiceGrpc.DubboGrpcServiceImplBase` | `DubboDubboGrpcServiceTriple.DubboGrpcServiceImplBase` |
| 客户端桩 | `DubboGrpcServiceGrpc.DubboGrpcServiceStub` | `DubboDubboGrpcServiceTriple.DubboGrpcServiceStub` |
| StreamObserver | `io.grpc.stub.StreamObserver` | `org.apache.dubbo.common.stream.StreamObserver` |
| 服务端方法签名 | `void hello(Req, StreamObserver<Resp>)` | 直接 `Resp hello(Req)` ✨ |
| 客户端调用 | 需要 `StreamObserver` 回调 | 直接拿到返回值 ✨ |

**核心变化**：生成的服务接口 `DubboGrpcService` 变成了**同步风格**，服务端直接 `return` 返回值，客户端直接拿到结果，不再需要回调。

---

## 5. 模块依赖关系

```
cloud_common (proto 定义 + 代码生成)
    ├── protobuf-java
    ├── dubbo (provided)
    └── dubbo-maven-plugin (代码生成)

cloud_dubbo_0 (服务端)
    ├── cloud_common
    ├── dubbo-spring-boot-starter
    └── dubbo-nacos-spring-boot-starter

cloud_dubbo_1 (客户端)
    ├── cloud_common
    ├── dubbo-spring-boot-starter
    └── dubbo-nacos-spring-boot-starter
```

> `cloud_common` 中的 dubbo 依赖建议用 `<scope>provided</scope>`，因为它只在生成代码编译时需要，运行时由 `cloud_dubbo_0` / `cloud_dubbo_1` 提供完整环境。

---

## 6. 添加新 RPC 方法

1. 编辑 `cloud_common/src/main/proto/DubboGrpc.proto`：

```protobuf
service DubboGrpcService {
  rpc hello(GrpcReq) returns(GrpcResp) {}

  // 新增一个方法
  rpc sayGoodbye(GrpcReq) returns(GrpcResp) {}
}
```

2. 执行 `mvn clean compile`，自动生成对应的接口方法和桩代码。

3. 服务端 `DubboGrpcImpl` 中新增对应实现：

```java
@Override
public GrpcResp sayGoodbye(GrpcReq request) {
    return GrpcResp.newBuilder()
            .setResp("Goodbye: " + request.getReq())
            .build();
}
```

4. 客户端直接调用新方法：

```java
GrpcResp resp = dubboGrpcService.sayGoodbye(request);
```