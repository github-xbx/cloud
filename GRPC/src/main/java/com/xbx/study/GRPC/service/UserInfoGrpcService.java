package com.xbx.study.GRPC.service;



import com.xbx.study.GRPC.proto.Request;
import com.xbx.study.GRPC.proto.Response;
import com.xbx.study.GRPC.proto.UserInfo;
import com.xbx.study.GRPC.proto.UserServiceGrpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * grpc 整合进spring 中 grpc server java bean
 */
@GrpcService
//@Service
public class UserInfoGrpcService extends UserServiceGrpc.UserServiceImplBase {




    /**
     * server grpc 接口实现
     * @param request 请求 message
     * @param responseObserver 响应
     */

    @Override
    public void getAllUserInfo(Request request, StreamObserver<Response> responseObserver) {

        List<UserInfo> userInfos = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            UserInfo userInfo = UserInfo.newBuilder().setUserId(i).setUsername("用户 => " + i).build();
            userInfos.add(userInfo);
        }

        Response response = Response.newBuilder().setResponseId(request.getRequestId()).addAllUserinfo(userInfos).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
