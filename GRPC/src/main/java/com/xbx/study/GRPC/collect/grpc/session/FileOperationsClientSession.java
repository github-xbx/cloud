package com.xbx.study.GRPC.collect.grpc.session;

import com.xbx.study.GRPC.collect.proto.FileClientMsg;
import com.xbx.study.GRPC.collect.proto.FileServerMsg;
import io.grpc.stub.StreamObserver;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FileOperationsClientSession extends RpcClientSession<FileClientMsg, FileServerMsg>{


    public FileOperationsClientSession(StreamObserver<FileServerMsg> responseObserver){

        setResponseObserver(responseObserver);
    }


    public CompletableFuture<FileClientMsg> sendMsgAndWait(FileServerMsg msg) {
        String connectionId = UUID.randomUUID().toString();
        return sendMsgAndWait(msg, connectionId);
    }

    public void onClientMessage(FileClientMsg msg) {
        String connectionId = msg.getGrpcId();
        this.onClientMessage(msg, connectionId);
    }
}
