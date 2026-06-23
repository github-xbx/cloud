package com.xbx.study.GRPC.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TestInterceptor.class);

    /**
     * 服务端拦截器
     */
    public static class HeaderServerInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata requestHeaders, ServerCallHandler<ReqT, RespT> serverCallHandler) {

            logger.info("server 拦截器,接收到客户端请求,请求头信息 =>:{}", requestHeaders);

            // server 的请求增加请求头信息
            return serverCallHandler.startCall(
                    new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
                        @Override
                        public void sendHeaders(Metadata responseHeaders) {
                            responseHeaders.put(
                                    Metadata.Key.of("server_header", Metadata.ASCII_STRING_MARSHALLER),
                                    "1212121212"
                            );
                            super.sendHeaders(responseHeaders);
                        }

            },requestHeaders);
        }
    }


    /**
     * 客户端拦截器
     */
    public static class HeaderClientInterceptor implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {

           //客户端 请求增加请求头信息
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    headers.put(Metadata.Key.of("client_header", Metadata.ASCII_STRING_MARSHALLER), "66666666");


                    super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        @Override
                        public void onHeaders(Metadata headers) {
                            logger.info("client 拦截器,接收到服务端请求,请求头 =>:{}", headers);
                            super.onHeaders(headers);

                        }
                    }, headers);
                }
            };
        }
    }


}
