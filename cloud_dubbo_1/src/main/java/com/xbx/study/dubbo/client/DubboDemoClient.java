package com.xbx.study.dubbo.client;

import com.xbx.study.dubbo.common.apis.DubboDemoService;
import com.xbx.study.dubbo.common.grpc.DubboGrpcProto;
import com.xbx.study.dubbo.common.grpc.DubboGrpcServiceGrpc;
import com.xbx.study.dubbo.common.grpc.GrpcReq;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class DubboDemoClient {


    @DubboReference
    private DubboDemoService dubboDemoService;


    @DubboReference(protocol = "tri")
    private DubboGrpcServiceGrpc


    public String dubboRpc(){
        String javaProgram = dubboDemoService.hello("java program ");
        return "cloud_dubbo_1 => "+ javaProgram;
    }

    public String dubboGrpc(){

        dubboGrpcService.hello(GrpcReq.newBuilder().build());
    }

}
