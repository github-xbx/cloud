package com.xbx.study.GRPC.collect.grpc.session;


import com.google.protobuf.Message;
import com.xbx.study.GRPC.collect.proto.FileClientMsg;
import com.xbx.study.GRPC.collect.proto.FileServerMsg;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClientSession<REQ extends Message,RESP extends Message> {


    /**
     * 向客户端发送消息，并异步等待回复。
     * @param msg 要发送的 ServerMessage（业务层无需关心 request_id，由本方法自动填充）
     * @return CompletableFuture，当客户端回复时完成，可调用 get() 阻塞等待
     */
    CompletableFuture<REQ> sendMsgAndWait(RESP msg, String connectionId);

    /**
     * 处理客户端发来的消息（由 GrpcServiceImpl 的 onNext() 调用）
     */
    void onClientMessage(REQ msg, String connectionId);


    CompletableFuture<FileClientMsg> sendMsgAndWait(FileServerMsg msg);

    void onClientMessage(FileClientMsg msg);

}
