package com.xbx.study.web.controller;

import com.xbx.study.web.script.LuaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("test")
public class TestController {


    private static Logger logger = LoggerFactory.getLogger(TestController.class);


    ThreadLocal<String> threadLocal = new ThreadLocal<>();

    private final LuaScript luaScript;
    private final RedisTemplate<String, Object> redisTemplate;

    public TestController(LuaScript luaScript, RedisTemplate<String, Object> redisTemplate) {
        this.luaScript = luaScript;
        this.redisTemplate = redisTemplate;
    }


    @GetMapping("redis")
    public String redis(){
        Long execute = redisTemplate.execute(luaScript.limitScript(), List.of("test:192.168.1.1-TestController-redis"), 3, 1);
        if (execute > 3){
            return "限流了。。。。。";
        }
        return execute.toString();
    }


    @GetMapping("1")
    public String abc(@RequestParam("param") String a){
        try {
            logger.info("ThreadLocal => {}", threadLocal.get());
            threadLocal.set(a);
            return threadLocal.get();
        }finally {
            threadLocal.remove();
        }


    }





}
