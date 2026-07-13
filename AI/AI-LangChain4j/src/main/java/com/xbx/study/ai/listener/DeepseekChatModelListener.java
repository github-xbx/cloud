package com.xbx.study.ai.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeepseekChatModelListener implements ChatModelListener {

    private static final Logger logger = LoggerFactory.getLogger(DeepseekChatModelListener.class);

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        //onRequest 配置的k:v键值对，在onResponse阶段可以获得，上下文 传递参数好用
        String uuidValue = UUID.randomUUID().toString();
        requestContext.attributes().put("TraceId",uuidValue);
        logger.info("请求参数requestContext:{}", requestContext+"\t"+uuidValue);
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Object traceId = responseContext.attributes().get("TraceId");
        logger.info("返回结果responseContext:{}", traceId);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        logger.error(errorContext.error().toString());
        logger.error("{}",errorContext);
    }
}
