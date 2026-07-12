package com.xbx.study.dubbo.client;

import com.xbx.study.dubbo.common.apis.DubboDemoService;
import com.xbx.study.dubbo.common.grpc.DubboGrpcService;
import com.xbx.study.dubbo.common.grpc.GrpcReq;
import com.xbx.study.dubbo.common.grpc.GrpcResp;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class DubboDemoClient {


    @DubboReference(protocol = "dubbo", url = "dubbo://localhost:20880")
    private DubboDemoService dubboDemoService;


    @DubboReference(protocol = "tri", url = "tri://localhost:20880", mock = "")
    private DubboGrpcService  dubboGrpcService;


    public String dubboRpc(){
        String javaProgram = dubboDemoService.hello("java program ");
        return "cloud_dubbo_1 => "+ javaProgram;
    }

    public String dubboGrpc(){

        GrpcResp hello = dubboGrpcService.hello(GrpcReq.newBuilder().build());
        return "cloud_dubbo_1 => "+ hello.toString();
    }

}
