package com.xbx.study.web.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 自定义 定时任务
 */
@Component
public class HelloTask {


    @Value("${server.port}")
    private Integer port;



    public void hello(){
        System.out.println("application port => "+port+" time=>"+  LocalDateTime.now());
    }

    public void hello2(){
        System.out.println("spring application port => "+port+" time=>"+  LocalDateTime.now());
    }

}
