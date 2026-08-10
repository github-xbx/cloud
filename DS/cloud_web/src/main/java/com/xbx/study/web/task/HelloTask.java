package com.xbx.study.web.task;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 自定义 定时任务
 */
@Component
public class HelloTask {





    public void hello(){
        System.out.println("hello"+  LocalDateTime.now());
    }

    public void hello2(String name, Integer status){

    }

}
