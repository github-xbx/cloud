package com.xbx.study.ai.service;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface FunctionAssistant {


    //客户指令： 出差住宿发票开票
    //开票信息： 公司名称
    //税号： xxxxx
    //开票金额：123.00元
    String chat(@UserMessage String prompt, @MemoryId Long memoryId);

}
