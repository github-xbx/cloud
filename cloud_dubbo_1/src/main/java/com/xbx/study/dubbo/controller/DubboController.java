package com.xbx.study.dubbo.controller;

import com.xbx.study.dubbo.client.DubboDemoClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dubbo")
public class DubboController {


    private final DubboDemoClient dubboDemoClient;

    public DubboController(DubboDemoClient dubboDemoClient) {
        this.dubboDemoClient = dubboDemoClient;
    }


    @GetMapping("rpc")
    public String rpc(){
        return dubboDemoClient.dubboRpc();
    }


    @GetMapping("grpc")
    public String grpc(){
        return dubboDemoClient.dubboGrpc();
    }


}
