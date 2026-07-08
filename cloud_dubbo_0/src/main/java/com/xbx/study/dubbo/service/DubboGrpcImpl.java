package com.xbx.study.dubbo.service;

import com.xbx.study.dubbo.common.grpc.DubboGrpcServiceGrpc;
import com.xbx.study.dubbo.common.grpc.GrpcReq;
import com.xbx.study.dubbo.common.grpc.GrpcResp;
import io.grpc.stub.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * dubbo 实现 gRPC 协议接口
 */
@DubboService(protocol = "tri") //指定triple协议，tri协议兼容gRPC协议
public class DubboGrpcImpl extends DubboGrpcServiceGrpc.DubboGrpcServiceImplBase {

    @Override
    public void hello(GrpcReq request, StreamObserver<GrpcResp> responseObserver) {


        System.out.println("GrpcReq => " + request.toString());

        responseObserver.onNext(GrpcResp.newBuilder().setResp("dubbo grpc server response").build());
        responseObserver.onCompleted();

    }
}
