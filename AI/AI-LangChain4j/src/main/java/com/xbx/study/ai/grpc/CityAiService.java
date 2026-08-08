package com.xbx.study.ai.grpc;

import com.xbx.study.ai.service.ChatAssistant;
import grpc.proto.ai.CityServiceGrpc;
import grpc.proto.ai.ReqMessage;
import grpc.proto.ai.RespMessage;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class CityAiService extends CityServiceGrpc.CityServiceImplBase {

    private final ChatAssistant chatAssistant;

    public CityAiService(ChatAssistant chatAssistant) {
        this.chatAssistant = chatAssistant;
    }


    @Override
    public void addressToCity(ReqMessage request, StreamObserver<RespMessage> responseObserver) {

        String address = request.getAddress();
        if (address == null || address.isEmpty()){
            responseObserver.onCompleted(); //直接结束
        }

        String city = chatAssistant.addressToCity(address);

        responseObserver.onNext(RespMessage.newBuilder().setCity(city).build());
        responseObserver.onCompleted();
    }
}
