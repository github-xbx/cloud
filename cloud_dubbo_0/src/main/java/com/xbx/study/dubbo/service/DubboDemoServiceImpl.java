package com.xbx.study.dubbo.service;

import com.xbx.study.dubbo.common.apis.DubboDemoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;



@DubboService
public class DubboDemoServiceImpl implements DubboDemoService {
    @Override
    public String hello(String name) {
        return "hello word =>" + name;
    }
}
