package com.ai.langgraph.service.langchain;


import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DeployLLMService {


    @UserMessage("""
                你是一位资深 DevOps 工程师。下面是一个 {{projectType}} 项目的构建失败日志，
                请分析失败原因，并给出具体、可操作的修复建议（不超过 300 字，用中文）：

                === 构建日志开始 ===
                {{buildLog}}
                === 构建日志结束 ===
                """)
    String analyzeBuildFailure(@V("projectType") String projectType, @V("buildLog") String buildLog);


    @UserMessage("""
                你是一位运维专家。一个 {{projectType}} 项目在服务器部署后验证失败，错误信息如下：
                请判断可能原因并给出排查建议（不超过 300 字，用中文）：

                === 错误信息 ===
                {{error}}
                """)
    String analyzeDeployFailure(@V("projectType") String projectType, @V("error") String error);

}
