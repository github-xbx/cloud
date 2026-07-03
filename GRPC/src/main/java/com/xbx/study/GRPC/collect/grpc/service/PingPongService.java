package com.xbx.study.GRPC.collect.grpc.service;

import com.xbx.study.GRPC.collect.common.proto.ServerMessage;
import com.xbx.study.GRPC.collect.proto.Ping;
import com.xbx.study.GRPC.collect.proto.PingPongGrpc;
import com.xbx.study.GRPC.collect.proto.Pong;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class PingPongService extends PingPongGrpc.PingPongImplBase {




    @Override
    public void connect(Ping request, StreamObserver<ServerMessage> responseObserver) {

        String deviceId = request.getDeviceId();


        responseObserver.onNext(ServerMessage.newBuilder().setContent(Pong.newBuilder().build().toByteString()).build());
        responseObserver.onCompleted();
    }
}
