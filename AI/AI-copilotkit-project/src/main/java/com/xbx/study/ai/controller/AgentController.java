package com.xbx.study.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xbx.study.ai.dto.RunAgentInput;
import com.xbx.study.ai.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        List<Map<String, String>> agents = Collections.singletonList(
                Map.of(
                        "name", "default1111",
                        "description", "默认的通用智能体",
                        "id", "default"
                )
        );

        // 返回标准格式的响应
        return Map.of(
                "agents", agents,
                "a2uiEnabled", false
        );
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
