package com.xbx.study.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xbx.study.ai.entity.dto.AgentThread;
import com.xbx.study.ai.entity.dto.RunAgentInput;
import com.xbx.study.ai.entity.vo.AgentThreadsVo;
import com.xbx.study.ai.event.AgUiEvent;
import com.xbx.study.ai.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/copilotkit")
public class AgentController {



    private final AgentService agentService;
    private final ObjectMapper objectMapper;


    public AgentController(AgentService agentService, ObjectMapper objectMapper) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
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
    public Map<String, Object> getRuntimeInfo() {
        // 构建智能体列表，这里只注册了一个名为 "default" 的智能体
       Map<String, Map<String,String>> agents =
                Map.of(
                        "default", Map.of("name","deepseekv4","description","深度求索")
                );

        // 返回标准格式的响应
        return Map.of(
                "agents", agents,
                "a2uiEnabled", false,
                "version","0.0.1",
                "licenseStatus","valid"
        );
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
    public Flux<AgUiEvent> chat(@RequestBody RunAgentInput input, @PathVariable("id") String agentID) {

        System.out.println(agentID);

        return agentService.execute(input);
    }

}
