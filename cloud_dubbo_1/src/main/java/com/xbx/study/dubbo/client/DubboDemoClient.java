package com.xbx.study.dubbo.client;

import com.xbx.study.dubbo.common.apis.DubboDemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class DubboDemoClient {


    @DubboReference
    private DubboDemoService dubboDemoService;



    public String dubboRpc(){
        String javaProgram = dubboDemoService.hello("java program ");
        return "cloud_dubbo_1 => "+ javaProgram;
    }


}
