package com.xbx.study.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("test")
public class TestController {


    private static Logger logger = LoggerFactory.getLogger(TestController.class);


    ThreadLocal<String> threadLocal = new ThreadLocal<>();



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
