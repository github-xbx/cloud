package com.xbx.study.ai.controller;

import com.xbx.study.ai.entity.dto.AgentThread;
import com.xbx.study.ai.entity.dto.RunAgentInput;
import com.xbx.study.ai.entity.vo.AgentModeInfoVo;
import com.xbx.study.ai.entity.vo.AgentThreadsVo;
import com.xbx.study.ai.event.AGUIEvent;
import com.xbx.study.ai.event.impl.*;
import com.xbx.study.ai.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/copilotkit")
public class AgentController {



    private final AgentService agentService;


    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }



    /**
     * licenseStatus 所有可选值
     * 值	前端行为
     * "valid"	正式授权 ✅ — 所有功能开启
     * "expiring"	即将到期 ⚠️ — 功能仍可用，但 UI 会显示续费提醒
     * "expired"	已过期 ❌ — 付费功能全部关闭
     * "invalid"	无效 license ❌ — 密钥格式不对，功能关闭
     * "none"	未配置 license — 基础聊天可用，threads 等高级功能关
     * "unknown"	未知状态 — 同上
     * @return
     */
    @GetMapping("/info")
    public AgentModeInfoVo getRuntimeInfo() {
        // 构建智能体列表，这里只注册了一个名为 "default" 的智能体
        // 返回标准格式的响应
        return AgentModeInfoVo.builder()
                .version("1.0.0")
                .agents(new AgentModeInfoVo.Model("default", "千问", "qwen"))
                .agents(new AgentModeInfoVo.Model("deepseek", "深度求索", "deepseekv4"))
                .build();
    }


    /**
     * {
     *     "threads": [
     *         {
     *             "id": "thread_001",
     *             "name": "可选，线程标题",
     *             "createdAt": "2026-07-21T10:00:00.000Z",
     *             "updatedAt": "2026-07-21T10:30:00.000Z",
     *             "lastRunAt": "2026-07-21T10:30:00.000Z"
     *         }
     *     ],
     *     "joinCode": null,
     *     "nextCursor": null
     * }
     *
     *
     字段	类型	必填	说明
     threads	Array	✅	线程列表，首次加载可以为空数组 []
     threads[].id	string	✅	线程唯一 ID
     threads[].name	string	❌	线程显示名称
     threads[].createdAt	string	❌	创建时间（ISO 8601），用于排序
     threads[].updatedAt	string	❌	更新时间，排序权重高于 createdAt
     threads[].lastRunAt	string	❌	最后运行时间，排序权重最高
     threads[].archived	boolean	❌	是否已归档
     joinCode	string|null	❌	WebSocket 实时更新用的 join code
     nextCursor	string|null	❌	分页游标，null 表示没有更多页
     */
    @GetMapping("/threads")
    public AgentThreadsVo threads(@RequestParam("agentId") String agentId){

        Map<String,Object> thread = Map.of("id", "1232321","name","测试名称","createdAt", LocalDateTime.now(),"archived",false);

        AgentThread agentThread = new AgentThread();
        agentThread.setId(UUID.randomUUID().toString());
        agentThread.setName("Thread 对话");
        agentThread.setArchived(false);
        agentThread.setCreateAt(LocalDateTime.now());
        agentThread.setUpdatedAt(LocalDateTime.now());
        agentThread.setLastRunAt(LocalDateTime.now());


        AgentThreadsVo vo = new AgentThreadsVo();
        vo.setThreads(List.of(agentThread));
        vo.setJoinCode("abcdefg");
        vo.setNextCursor("10");
        return vo;

    }


    /**
     * AG-UI 协议端点
     * 返回 SSE 流，每个事件以 "data: " 前缀 + JSON 格式输出
     */
    @PostMapping(value = "/agent/{id}/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AGUIEvent> chat(@RequestBody RunAgentInput input, @PathVariable("id") String agentID) {

        System.out.println(agentID);

        return agentService.execute(input);
    }


    /**
     * connect 接口概述
     * connect 和 run 是 CopilotKit 的两个核心接口：
     *
     * 接口	用途	触发时机
     * POST /agent/{agentId}/run	执行 Agent，处理用户消息并返回回复	用户发送消息时
     * POST /agent/{agentId}/connect	连接已有线程，获取历史/实时事件流	打开或切换对话时
     *
     *
     * 有两种处理方式：
     *
     * 方式一：返回空流（推荐，最简实现）
     * 如果你的后端不需要实时推送能力，只需返回一个空的 SSE 流：
     *
     *
     * data:{"type":"RUN_STARTED","threadId":"xxx","runId":"xxx"}
     *
     * data:{"type":"RUN_FINISHED","threadId":"xxx","runId":"xxx"}
     * 方式二：返回历史消息
     * 如果你想在 connect 时把该线程的历史消息回放给前端：
     * data:{"type":"RUN_STARTED","threadId":"xxx","runId":"xxx"}
     *
     * data:{"type":"TEXT_MESSAGE_START","messageId":"msg-history-1","role":"assistant"}
     * data:{"type":"TEXT_MESSAGE_CONTENT","messageId":"msg-history-1","delta":"你好！有什么可以帮助你的？"}
     * data:{"type":"TEXT_MESSAGE_END","messageId":"msg-history-1"}
     *
     * data:{"type":"RUN_FINISHED","threadId":"xxx","runId":"xxx"}
     *
     */
    @PostMapping(value = "/agent/{id}/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AGUIEvent> connect(@RequestBody RunAgentInput input, @PathVariable("id") String agentID) {
        String runId = input.getRunId();
        String threadId = input.getThreadId();

        return Flux.just(
                new RunStartedEvent(runId, threadId),
                new TextMessageStartEvent("123","assistant"),
                new TextMessageContentEvent("123","你好！有什么可以帮助你的？"),
                new TextMessageEndEvent("123"),
                new RunFinishedEvent(runId, threadId)
        );
    }

}
