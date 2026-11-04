package com.xbx.study.ai.decorator;


import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 *
 * 每个 operator 的职责边界：
 *
 * operator	语义	容易踩坑
 * filter	丢弃不满足条件的元素	空 Flux 直接 onComplete，客户端看不到任何数据
 * map	1 → 1 转换	惰性，异常在订阅时抛
 * concatMap	1 → N，保序	不要用 flatMap（并发 + 乱序）
 * buffer(n)	N 条合并为 1 条	流结束时不完整的批次也会发出，需要处理
 * timeout(d)	相邻元素间隔超 d 秒失败	与装饰器层 Mono.timeout 语义完全不同
 * distinct	去重（有内存增长风险）	长流会 OOM，需配合窗口
 * onErrorResume	错误转元素	一旦兜底，"失败"变成"成功"，上游重试就失效了
 *
 *
 *
 *
 */


public class DataPipeline {


    public static Flux<String> build(Flux<String> source, Config cfg) {

        Flux<String> f = source;

        //1.基础清洗
        if (cfg.filterEmpty()){
            f = f.filter(s -> s != null && !s.isEmpty());
        }

        // 2. 逐条转换（脱敏 / 格式化）—— map
        if (cfg.mask()) {
            f = f.map(DataPipeline::mask);
        }

        // 3. 逐条转成"可能多条"—— flatMap / concatMap
        if (cfg.splitByNewline()) {
            f = f.concatMap(s -> Flux.fromArray(s.split("\n")));
        }

        // 4. 加信封（逐条）—— map
        if (cfg.wrapJson()) {
            f = f.map(s -> "{\"data\":\"" + escape(s) + "\"}");
        }

        // 5. 跨条批量—— buffer
        if (cfg.batchSize() > 0) {
            f = f.buffer(cfg.batchSize())
                    .map(list -> String.join("\n", list));
        }

        // 6. 流空闲超时—— Flux.timeout（区别于装饰器层的"整个调用超时"）
        if (cfg.idleTimeout() != null) {
            f = f.timeout(cfg.idleTimeout());
        }

        // 7. 去重—— 需要跨条状态
        if (cfg.distinct()) {
            f = f.distinct();
        }

        // 8. 错误兜底—— 流中错误转成一条错误事件，优雅结束
        if (cfg.gracefulError()) {
            f = f.onErrorResume(ex ->
                    Flux.just("{\"error\":\"" + escape(ex.getMessage()) + "\"}"));
        }

        return f;
    }


    public static String mask(String s){
        // 示例：邮箱脱敏  a***@b.com
        int at = s.indexOf('@');
        if (at <= 1) return s;
        return s.charAt(0) + "***" + s.substring(at);
    }

    public static String escape(String s){
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }




    public record Config(
            boolean filterEmpty,
            boolean mask,
            boolean splitByNewline,
            boolean wrapJson,
            int batchSize,          // 0 = 不批处理
            Duration idleTimeout,
            boolean distinct,
            boolean gracefulError
    ) {
        public static Config defaultCfg() {
            return new Config(true, false, false, false, 0, null, false, true);
        }
    }
}
