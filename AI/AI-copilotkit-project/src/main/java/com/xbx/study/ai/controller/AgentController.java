package com.xbx.study.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xbx.study.ai.dto.RunAgentInput;
import com.xbx.study.ai.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
                "a2uiEnabled", false
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
    public Map<String, Object> threads(@RequestParam("agentId") String agentId){

        Map<String,Object> thread = Map.of("id", "1232321","name","测试名称","createdAt", LocalDateTime.now(),"archived",true);


        Map<String, Object> map = new HashMap<>();
        map.put("threads", List.of(thread));
        map.put("joinCode",null);
        map.put("nextCursor",null);
        return map;

    }


    /**
     * AG-UI 协议端点
     * 返回 SSE 流，每个事件以 "data: " 前缀 + JSON 格式输出
     */
    @PostMapping(value = "/agent/{id}/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody RunAgentInput input, @PathVariable("id") String agentID) {
        SseEmitter emitter = new SseEmitter(600_000L); // 10分钟超时
        System.out.println(agentID);

        // 异步执行 Agent，避免阻塞主线程
        new Thread(() -> {
            try {
                agentService.execute(input, event -> {
                    try {
                        // AG-UI 协议要求每个事件以 "data: " 开头
                        String json = objectMapper.writeValueAsString(event);
                        emitter.send(SseEmitter.event().data(json, MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

}
