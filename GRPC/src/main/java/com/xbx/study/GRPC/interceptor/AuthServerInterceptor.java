package com.xbx.study.GRPC.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.interceptors.AuthenticatingServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端拦截器 auth 验证，验证token
 */
@GrpcGlobalServerInterceptor
public class AuthServerInterceptor  implements AuthenticatingServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthServerInterceptor.class);

    private final GrpcAuthReader reader = new GrpcAuthReader();


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        logger.info("服务端拦截器 auth 验证，验证token。");

        Boolean readAuthentication = reader.readAuthentication(serverCall, metadata);
        if (readAuthentication){
            //验证token成功
            //传递给下一个拦截器处理
            return serverCallHandler.startCall(serverCall, metadata);
        }

        //验证token失败
        serverCall.close(Status.UNAUTHENTICATED, metadata);
        return new ServerCall.Listener<ReqT>() {};
    }
}
