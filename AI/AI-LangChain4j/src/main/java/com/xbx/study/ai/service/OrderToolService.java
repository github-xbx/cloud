// 文件:src/main/java/com/xbx/study/ai/service/OrderToolService.java
package com.xbx.study.ai.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Agent 可调用的业务工具。方法上的 @Tool 描述会被 model 用来决定何时调用,
 * 所以描述要写"做什么",参数用 @P 写清含义。
 */
@Service
public class OrderToolService {

    /** 假数据,真实项目换成 DAO / 远程调用 */
    private static final Map<String, String> ORDERS = Map.of(
            "SO20260101001", "已发货,顺丰 SF123456789,预计明天 18:00 前送达",
            "SO20260101002", "待付款,请在 30 分钟内完成支付",
            "SO20260101003", "已签收,签收时间 2026-01-03 14:22"
    );

    @Tool("根据订单号查询订单的当前状态、物流和预计送达时间")
    public String queryOrder(@P("订单号,形如 SO20260101001") String orderNo) {
        return ORDERS.getOrDefault(orderNo, "未查询到订单:" + orderNo);
    }

    @Tool("对已支付订单发起退款申请,返回退款单号和预计到账时间")
    public String refund(@P("订单号") String orderNo,
                         @P("退款原因,用一句中文描述") String reason) {
        return "订单 " + orderNo + " 已提交退款,退款单号 RF" + System.currentTimeMillis()
                + ",原因:" + reason + ",预计 1-3 个工作日到账";
    }

    @Tool("给用户的手机号发送短信提醒")
    public String sendSms(@P("手机号") String phone,
                          @P("短信内容,不超过 60 字") String content) {
        return "短信已发送至 " + phone + ":" + content;
    }
}
