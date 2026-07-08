package com.xbx.study.dubbo.service;


import com.xbx.study.dubbo.common.grpc.DubboDubboGrpcServiceTriple.DubboGrpcServiceImplBase;
import com.xbx.study.dubbo.common.grpc.GrpcReq;
import com.xbx.study.dubbo.common.grpc.GrpcResp;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Dubbo Triple 服务端实现
 *
 * 继承 DubboGrpcServiceImplBase（不是直接实现 DubboGrpcService 接口），
 * 基类已提供 helloAsync 等异步方法的默认实现，只需重写同步的 hello() 即可。
 */
@DubboService(protocol = "tri")
public class DubboGrpcImpl extends DubboGrpcServiceImplBase {


    @Override
    public GrpcResp hello(GrpcReq request) {
        System.out.println("GrpcReq => " + request.toString());
        return GrpcResp.newBuilder()
                .setResp("dubbo grpc server response")
                .build();
    }


}
