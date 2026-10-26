package com.xbx.study.ai.decorator;

import com.xbx.study.ai.decorator.channel.ErrorMapChannel;
import com.xbx.study.ai.decorator.channel.RetryChannel;
import com.xbx.study.ai.decorator.channel.TimerChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 组装：装饰器链 + 数据管道
 */
public class EndpointHandler {
    private static final Logger log = LoggerFactory.getLogger(EndpointHandler.class);
    private final BizService biz;

    public EndpointHandler(BizService biz) { this.biz = biz; }

    public void handle(Transport raw, DataPipeline.Config cfg, boolean streaming) {
        // ===== 装饰器层：包裹"发送动作" =====
        // 顺序：ErrorMap 在外（最终兜底）→ Retry 居中（失败重试）→ Timed 在内（统计每次尝试）
        Transport channel = new ErrorMapChannel(
                new RetryChannel(
                        new TimerChannel(raw),
                        2
                ),
                ex -> "{\"error\":\"" + ex.getClass().getSimpleName() + "\"}"
        );

        if (streaming) {
            // ===== operator 层：包裹"数据流" =====
            Flux<String> pipeline = DataPipeline.build(biz.subscribe(), cfg);

            channel.sendStream(pipeline,null)
                    .doOnError(ex -> log.error("stream failed", ex))
                    .onErrorResume(ex -> Mono.empty())   // 最终消费，防未处理异常
                    .subscribe();
        } else {
            channel.sendUnary(biz.fetch(),null)
                    .doOnError(ex -> log.error("unary failed", ex))
                    .onErrorResume(ex -> Mono.empty())
                    .subscribe();
        }
    }
}


//BizService.subscribe()  →  Flux<String>
//        │
//                ▼  DataPipeline: filter → map(脱敏) → buffer(n) → timeout → onErrorResume
//        │  （operator 层，逐条/跨条转换，惰性执行）
//        ▼
//TimedChannel   → 计时开始
//        ▼
//RetryChannel   → 失败重试 2 次（重新订阅上游 Flux）
//        ▼
//ErrorMapChannel→ 最终失败转错误消息
//        ▼
//HttpChannel / WebSocketChannel / GrpcChannel → 实际发送
//        │
//                ▼  Mono<Void> 完成信号向上传播
//        ▼
//TimedChannel.doFinally 记录耗时（含重试总耗时）













//场景 1：HTTP SSE + 批量推送
//        java
//DataPipeline.Config cfg = new DataPipeline.Config(
//        /* filterEmpty */     true,
//        /* mask */            true,
//        /* splitByNewline */  false,
//        /* wrapJson */        true,
//        /* batchSize */       20,
//        /* idleTimeout */     Duration.ofSeconds(30),
//        /* distinct */        false,
//        /* gracefulError */   true
//);
//
//handler.handle(new HttpChannel(response), cfg, true);
//场景 2：WebSocket 逐条推送，不批处理
//        java
//DataPipeline.Config cfg = new DataPipeline.Config(
//        true, true, false, true, 0, Duration.ofSeconds(60), false, true
//);
//handler.handle(new WebSocketChannel(session), cfg, true);
//场景 3：gRPC 强类型转换
//java
//handler.handle(
//    new GrpcChannel<>(orderObserver, OrderProto::fromString),
//cfg,
//        true
//        );
//场景 4：同一个业务，不同协议用不同配置
//        java
//// HTTP 需要批量（降低 TCP 开销）+ JSON 信封
//handler.handle(new HttpChannel(resp), httpCfg, true);
//
//// WebSocket 逐条低延迟
//        handler.handle(new WebSocketChannel(session), wsCfg, true);
//
//// gRPC 保持原始字符串，让 proto 层处理
//        handler.handle(new GrpcChannel<>(obs, OrderProto::fromString), grpcCfg, true);