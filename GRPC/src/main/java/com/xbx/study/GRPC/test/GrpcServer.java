package com.xbx.study.GRPC.test;


import com.xbx.study.GRPC.interceptor.TestInterceptor;
import com.xbx.study.GRPC.proto.GreeterGrpc;
import com.xbx.study.GRPC.proto.HelloReply;
import com.xbx.study.GRPC.proto.HelloRequest;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);


    public static void main(String[] args) throws IOException, InterruptedException {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        Server server = Grpc.newServerBuilderForPort(50051, InsecureServerCredentials.create())
                .executor(poolExecutor)
                .intercept(new TestInterceptor.HeaderServerInterceptor()) //拦截器全局注册
                .addService(new GreeterImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + server.getPort());

        // 阻塞主线程，让 Server 持续监听请求，直到 JVM 被终止
        server.awaitTermination();
    }



    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            System.out.println("======="+request+"=======");
            HelloReply helloReply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
            responseObserver.onNext(helloReply); //发送一条响应消息给客户端
            responseObserver.onCompleted(); //告诉客户端"我发完了"

        }

        // 客户端流式：返回值是 StreamObserver<请求>，用来逐条接收客户端发来的消息
        @Override
        public StreamObserver<HelloRequest> sayHelloClientStream(StreamObserver<HelloReply> responseObserver) {

            return new StreamObserver<HelloRequest>() {
                private final StringBuilder allNames = new StringBuilder();

                @Override
                public void onNext(HelloRequest request) {
                    // 客户端每发一条，这里就被调用一次
                    System.out.println("收到客户端消息: " + request.getName());
                    allNames.append(request.getName()).append(", ");
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("客户端流出错: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    // 客户端说"我发完了"，服务端汇总后一次性响应
                    String msg = allNames.length() > 0
                            ? allNames.substring(0, allNames.length() - 2)  // 去掉末尾逗号
                            : "(空)";
                    HelloReply reply = HelloReply.newBuilder()
                            .setMessage("全部收到: [" + msg + "]")
                            .build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                }
            };
        }


        @Override
        public void sayHelloServerStream(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            String name = request.getName();
            for (int i = 0; i < 10; i++) {
                HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + name + " 第" + i + "条消息。").build();
                responseObserver.onNext(reply);  //发送一条
            }
            responseObserver.onCompleted();       //发完了
        }

        // 双向流式：双方都可以独立发消息
        @Override
        public StreamObserver<HelloRequest> sayHelloSCStream(StreamObserver<HelloReply> responseObserver) {

            return new StreamObserver<HelloRequest>() {

                @Override
                public void onNext(HelloRequest request) {
                    // 客户端发来一条，服务端立刻回复一条（不用等 onCompleted）
                    System.out.println("收到客户端消息: " + request.getName());
                    HelloReply reply = HelloReply.newBuilder().setMessage("已收到: " + request.getName()).build();
                    responseObserver.onNext(reply);   // 每收到一条就回一条
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("双向流出错: " + t.getMessage());
                    responseObserver.onError(t);
                }

                @Override
                public void onCompleted() {
                    // 客户端说"我发完了"
                    System.out.println("客户端发送完毕");
                    responseObserver.onCompleted();   // 服务端也关闭
                }
            };
        }
    }

}
