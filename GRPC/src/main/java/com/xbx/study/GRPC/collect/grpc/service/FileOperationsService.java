package com.xbx.study.GRPC.collect.grpc.service;

import com.xbx.study.GRPC.collect.grpc.session.FileOperationsClientSession;
import com.xbx.study.GRPC.collect.manager.RpcConnectionManager;
import com.xbx.study.GRPC.collect.proto.FileClientMsg;
import com.xbx.study.GRPC.collect.proto.FileOperationsGrpc;
import com.xbx.study.GRPC.collect.proto.FileServerMsg;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class FileOperationsService extends FileOperationsGrpc.FileOperationsImplBase {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationsService.class);

    private final RpcConnectionManager manager;

    public FileOperationsService(RpcConnectionManager rpcConnectionManager) {
        this.manager = rpcConnectionManager;
    }


    @Override
    public StreamObserver<FileClientMsg> fileOperationStream(StreamObserver<FileServerMsg> responseObserver) {

        FileOperationsClientSession session = new FileOperationsClientSession(responseObserver);



        return new StreamObserver<FileClientMsg>() {
            @Override
            public void onNext(FileClientMsg fileClientMsg) {

                session.onClientMessage(fileClientMsg);
                manager.register(session.getSessionId(),session);
            }

            @Override
            public void onError(Throwable throwable) {
                // 异常处理，清理会话
                manager.unregister(session.getSessionId());
                session.close();
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                manager.unregister(session.getSessionId());
                session.close();
                responseObserver.onCompleted();
            }
        };
    }
}
