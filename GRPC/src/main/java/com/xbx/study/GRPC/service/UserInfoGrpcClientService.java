package com.xbx.study.GRPC.service;

import com.xbx.study.GRPC.proto.Request;
import com.xbx.study.GRPC.proto.Response;
import com.xbx.study.GRPC.proto.UserInfo;
import com.xbx.study.GRPC.proto.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoGrpcClientService {

    @GrpcClient(value = "local-rpc")
    private UserServiceGrpc.UserServiceBlockingStub blockingStub;



    public List<UserInfo> getList(){

        Response response = blockingStub.getAllUserInfo(Request.newBuilder().setRequestId(1000).build());

        return response.getUserinfoList();
    }


}
