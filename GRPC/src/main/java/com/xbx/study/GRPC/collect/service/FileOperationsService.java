package com.xbx.study.GRPC.collect.service;

import com.xbx.study.GRPC.collect.common.proto.ServerMessage;
import com.xbx.study.GRPC.collect.manager.RpcConnectionManager;
import com.xbx.study.GRPC.collect.proto.FileClientMsg;
import com.xbx.study.GRPC.collect.proto.FileOperationsGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class FileOperationsService extends FileOperationsGrpc.FileOperationsImplBase {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationsService.class);


    private final RpcConnectionManager  connectionManager;

    public FileOperationsService(RpcConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public StreamObserver<FileClientMsg> fileOperationStream(StreamObserver<ServerMessage> responseObserver) {

        return new StreamObserver<FileClientMsg>() {
            @Override
            public void onNext(FileClientMsg fileClientMsg) {

                logger.info("客户端文件处理的结果 =》 {}",fileClientMsg.toString());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

                connectionManager.unregister();
            }
        };
    }
}
