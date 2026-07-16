package com.xbx.study.ai.controller;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.xbx.study.ai.po.prompt.LawPrompt;
import com.xbx.study.ai.service.*;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxImageStyle;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;


import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chatModel")
public class ChatModelController {

    private static final Logger logger = LoggerFactory.getLogger(ChatModelController.class);

    @Value("classpath:static/images/mi.jpg")
    private org.springframework.core.io.Resource resource;//import org.springframework.core.io.Resource;

    @Resource
    private ChatMemoryStore inMemoryChatMemoryStore;



    @Resource(name = "deepseek")
    public ChatModel deepseek;

    @Resource(name = "qwen")
    private ChatModel qwen;

    @Resource(name = "qwenImageModel")
    private ImageModel qwenImageModel;

    @Autowired
    @Qualifier("chatMemoryMessageWindows")
    private ChatMemoryAssistant chatMemoryMessageWindows;

    @Autowired
    @Qualifier("chatMemoryTokenWindows")
    private ChatMemoryAssistant chatMemoryTokenWindows;

    @Resource(name = "lawAssistant")
    private LawAssistant lawAssistant;

    @Resource(name = "localMemoryAssistant")
    private ChatMemoryAssistant localMemoryAssistant;

    @Resource(name = "functionAssistant")
    private FunctionAssistant functionAssistant;


    @Resource(name = "functionHighAssistant")
    private FunctionHighAssistant functionHighAssistant;




    private final StreamingChatModel streamingChatModel;
    private final ChatAssistant chatAssistant;
    private final StreamingChatAssistant streamingChatAssistant;

    public ChatModelController(
            @Qualifier("qwen1") StreamingChatModel streamingChatModel,
            ChatAssistant chatAssistant,
            StreamingChatAssistant streamingChatAssistant) {
        this.streamingChatModel = streamingChatModel;
        this.chatAssistant = chatAssistant;
        this.streamingChatAssistant = streamingChatAssistant;
    }


    @GetMapping("deepseek")
    public String deepseekCall(@RequestParam(name = "prompt", defaultValue = "介绍一下java?") String prompt){

        ChatResponse response = deepseek.chat(UserMessage.from(prompt));

        String text = response.aiMessage().text();

        logger.info("deepseek 回复 =>{}", text);
        TokenUsage tokenUsage = response.tokenUsage();
        logger.info("本次消耗的token => {}",tokenUsage);

        return text;

    }

    @GetMapping("deepseek1")
    public String deepseekCall1(@RequestParam(name = "prompt", defaultValue = "hello?") String prompt){

        String chat = chatAssistant.chat(prompt);

        logger.info("springboot 整合 deepseek  回复 =>{}", chat);
        return chat;

    }



    @GetMapping("qwen_image")
    public String qwenCallIImage() throws IOException {

        byte[] byteArray = resource.getContentAsByteArray();

        String base64Data = Base64.getEncoder().encodeToString(byteArray);

        UserMessage message = UserMessage.from(
                TextContent.from("从一下图片中获取来源网站名称，固件走势和5月30号股价"),
                ImageContent.from(base64Data, "image/jpg")
        );

        ChatResponse chat = qwen.chat(message);
        String text = chat.aiMessage().text();
        logger.info("deepseek images result => {}",text);
        return text;
    }


    @GetMapping("qwen_image_create")
    public String qwenCallIImageCreate() throws IOException {

        Response<Image> response = qwenImageModel.generate("美女");


        logger.info("qwenImageModel result => {}",response.content().url());

        return response.content().url().toString();

    }
    @GetMapping("qwen_image_create_1")
    public String qwenCallIImageCreate1() {
        String prompt = "近景镜头，18岁的中国女孩，古代服饰，圆脸，正面看着镜头"
                + "民族优雅的服饰，商业摄影，室外，电影级光照，半身特写，精致的淡妆，锐利的边缘。";
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(System.getenv("java_qwen_apikey"))
                .model("wanx2.0-t2i-turbo")
                .prompt(prompt)
                .style(WanxImageStyle.CARTOON_3D.getStyle())
                .n(2)
                //.size("1920*1080")
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;

        try {
            System.out.println("--- sync call, please wait a moment ---");
            result = imageSynthesis.call(param);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        return  result.toString();
    }



    @GetMapping("stream/chat0")
    public Flux<String> streamChat0(@RequestParam("prompt") String prompt){
        return Flux.create(stringFluxSink -> {
            streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String s) {
                    stringFluxSink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    stringFluxSink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    stringFluxSink.error(throwable);
                }
            });
        });
    }



    @GetMapping("stream/chat1")
    public void streamChat1(@RequestParam(value = "prompt", defaultValue = "北京有什么好吃的") String prompt){
        logger.info("----- come in chat2 -----");
        streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                logger.info("=== [{}] ===",partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                logger.info("-------- response over: {}", chatResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }

    @GetMapping("stream/chat2")
    public Flux<String> streamChat2(@RequestParam(value = "prompt", defaultValue = "南京有什么好吃的") String prompt){
        logger.info("----- come in chat3 -----");

        return streamingChatAssistant.chatFlux(prompt);
    }


    /**
     * 没有缓存记忆功能
     * @return
     */
    @GetMapping("memory/chat0")
    public String chat0(){
        String chat1 = chatAssistant.chat("你好，我的名字交张三");
        logger.info("chat1 => {}",chat1);

        String chat2 = chatAssistant.chat("我的名字叫什么");
        logger.info("chat2 => {}",chat2);
        return "success : "+ LocalDateTime.now() +"<br> \n\n answer01: "+chat1+"<br> \n\n answer02: "+chat2;
    }

    /**
     * 以消息缓存的聊天
     * @return
     */
    @GetMapping("memory/chat1")
    public String chat1(){

        String chat1 = chatMemoryMessageWindows.chatWithChatMemory(1L, "你好，我的名字是李四");
        logger.info("userid = 1L, chat1 => {}",chat1);
        String chat2 = chatMemoryMessageWindows.chatWithChatMemory(1L, "我的名字是什么");
        logger.info("userid = 1L, chat2 => {}",chat2);

        String chat3 = chatMemoryMessageWindows.chatWithChatMemory(3L, "你好，我的名字是hahaha");
        logger.info("userid = 3L, chat1 => {}",chat3);
        String chat4 = chatMemoryMessageWindows.chatWithChatMemory(3L, "我的名字是什么");
        logger.info("userid = 3L, chat2 => {}",chat4);

        return "chatMessageWindowChatMemory success : "
                + LocalDateTime.now()+"<br> \n\n " +
                "userId=1L,chat1: "+chat1+"<br> \n\n userId=1L,chat2: "+chat2+"<br> \n\n" +
                "userId=3L,chat3: "+chat3+"<br> \n\n userId=3L,chat4: "+chat4+"<br> \\n\\n";
    }


    /**
     * 以 token缓存的 聊天
     * @return
     */
    @GetMapping("memory/chat2")
    public String chat2(){

        String chat1 = chatMemoryTokenWindows.chatWithChatMemory(1L, "你好，我的名字是java");
        logger.info("userid = 1L, chat1 => {}",chat1);
        String chat2 = chatMemoryTokenWindows.chatWithChatMemory(1L, "我的名字是什么");
        logger.info("userid = 1L, chat2 => {}",chat2);

        String chat3 = chatMemoryTokenWindows.chatWithChatMemory(3L, "你好，我的名字是spring boot");
        logger.info("userid = 3L, chat1 => {}",chat3);
        String chat4 = chatMemoryTokenWindows.chatWithChatMemory(3L, "我的名字是什么");
        logger.info("userid = 3L, chat2 => {}",chat4);

        return "chatTokenWindowChatMemory success : "
                + LocalDateTime.now()+"<br> \n\n " +
                "userId=1L,chat1: "+chat1+"<br> \n\n userId=1L,chat2: "+chat2+"<br> \n\n" +
                "userId=3L,chat3: "+chat3+"<br> \n\n userId=3L,chat4: "+chat4+"<br> \\n\\n";
    }


    @GetMapping(value = "/chatprompt/test1")
    public String test1() {
        String chat = lawAssistant.chat(1L,"什么是知识产权？",2000);
        System.out.println(chat);

        String chat2 = lawAssistant.chat(1L,"什么是java？",2000);
        System.out.println(chat2);

        String chat3 = lawAssistant.chat(1L,"介绍下西瓜和芒果",2000);
        System.out.println(chat3);

        String chat4 = lawAssistant.chat(1L,"飞机发动机原理",2000);
        System.out.println(chat4);

        return "success : "+ LocalDateTime.now()+"<br> \n\n chat: "+chat+"<br> \n\n chat2: "+chat2;
    }


    /**
     * TRIPS协议（与贸易有关的知识产权协议）：
     * 这是世界贸易组织（WTO）成员间的一个重要协议，
     * 它规定了最低标准的知识产权保护要求，并适用于所有WTO成员。
     * @return
     */
    @GetMapping(value = "/chatprompt/test2")
    public String test2() {
        LawPrompt prompt = new LawPrompt();

        prompt.setLegal("知识产权");
        prompt.setQuestion("TRIPS协议?");

        String chat = lawAssistant.chat(prompt);

        System.out.println(chat);

        return "success : "+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"<br> \n\n chat: "+chat;
    }


    @GetMapping(value = "chatprompt/test3")
    public String test3(){
        // 默认 PromptTemplate 构造使用it属性作为默认占位符
        String role = "外科医生";
        String question = "牙疼";

        //1.构造PromptTemplate模板
        PromptTemplate template = PromptTemplate.from("你是一个{{it}}助手，{{question}}怎么办");
        //2.有PromptTemplate 生成Prompt
        Prompt prompt = template.apply(Map.of("it", role, "question", question));
        //3. Prompt提示词变为UserMessage
        UserMessage userMessage = prompt.toUserMessage();
        //4.调用大模型
        ChatResponse chat = qwen.chat(userMessage);
        logger.info(chat.aiMessage().text());

        return chat.toString();
    }

    /**
     * 使用缓存的方式对话
     * @param prompt
     * @return
     */
    @GetMapping(value = "/memory/db")
    public String memoryDb(@RequestParam(value = "prompt") String prompt){

        String chat = localMemoryAssistant.chatWithChatMemory(1L, prompt);
        logger.info(chat);
        return chat;
    }

    /**
     * 获取存储内容
     * @param memoryId
     * @return
     */
    @GetMapping("get/memoryDb")
    public String getMemoryDb(@RequestParam(value = "memoryId") Long memoryId){

        List<ChatMessage> messages = inMemoryChatMemoryStore.getMessages(memoryId);

        return messages.toString();
    }

    /**
     *  使用tools 工具
     * @return
     */
    @GetMapping("tools/chat0")
    public String toolsChat0(){
        String chat = functionAssistant.chat("开张发票，公司：哈哈哈科技有限公司 税号：xxxxxxxxxx 金额：2345.555543", 1L);
        logger.info("tools chat result => {}",chat);
        String chat1 = functionAssistant.chat("你好",1L);
        logger.info(" chat1 => {}",chat1);
        return "success:"+chat;
    }

    @GetMapping("tools/chat1")
    public String toolsChat1(){
        String chat = functionHighAssistant.chat(1L, "最近15天 天津的天气情况");
        logger.info("chat => {}",chat);
        return chat;
    }
}
