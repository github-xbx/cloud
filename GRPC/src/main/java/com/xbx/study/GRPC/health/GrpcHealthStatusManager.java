package com.xbx.study.GRPC.health;

import io.grpc.BindableService;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自动将所有 @GrpcService Bean 注册到 gRPC Health 检查中
 */
@Component
public class GrpcHealthStatusManager {

    private static final Logger log = LoggerFactory.getLogger(GrpcHealthStatusManager.class);

    private final HealthStatusManager healthStatusManager;
    private final ApplicationContext applicationContext;

    public GrpcHealthStatusManager(HealthStatusManager healthStatusManager, ApplicationContext applicationContext) {
        this.healthStatusManager = healthStatusManager;
        this.applicationContext = applicationContext;
    }

    /**
     * 应用启动完成后，自动扫描所有 @GrpcService 并注册健康状态
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerAllGrpcServices() {
        Map<String, Object> grpcBeans = applicationContext.getBeansWithAnnotation(GrpcService.class);

        for (Map.Entry<String, Object> entry : grpcBeans.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof BindableService service) {
                // 获取 proto 定义的完整服务名，如 "UserService"
                String serviceName = service.bindService().getServiceDescriptor().getName();
                healthStatusManager.setStatus(serviceName, ServingStatus.SERVING);
                log.info("HealthCheck 已注册: {} → SERVING", serviceName);
            }
        }
    }
}