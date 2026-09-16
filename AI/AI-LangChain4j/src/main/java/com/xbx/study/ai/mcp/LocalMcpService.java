package com.xbx.study.ai.mcp;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalMcpService {


    /**
     * 定义 mcp 客户端
     * @return
     */
    public McpClient codeReviewClient(){

        StdioMcpTransport transport = StdioMcpTransport.builder()
                .command(List.of("python", "-m", "code_review_mcp.server")) // // 启动命令
                .logEvents(true)  //开启日志
                .build();

        return new DefaultMcpClient.Builder().transport(transport).build();
    }




}
