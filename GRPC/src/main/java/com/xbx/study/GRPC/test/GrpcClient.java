package com.xbx.study.GRPC.test;

import com.xbx.study.GRPC.interceptor.TestInterceptor;
import com.xbx.study.GRPC.proto.GreeterGrpc;
import com.xbx.study.GRPC.proto.HelloReply;
import com.xbx.study.GRPC.proto.HelloRequest;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    private static GreeterGrpc.GreeterBlockingStub blockingStub;
    private static GreeterGrpc.GreeterStub asyncStub;         // 异步桩，流式调用必须用它

    private static void init(){
        ManagedChannel channel = Grpc.newChannelBuilderForAddress("localhost", 50051, InsecureChannelCredentials.create())
                .intercept(new TestInterceptor.HeaderClientInterceptor()) //拦截器全局注册
                .build();


        blockingStub = GreeterGrpc.newBlockingStub(channel);
        asyncStub = GreeterGrpc.newStub(channel);             // 创建异步桩
    }




    public static void main(String[] args) throws InterruptedException {

        init();

        client();               // 一发一收
        clientStream();      // 多发一收（客户端流式）
        serverStream();      // 一发多收（服务端流式）
        bidiStream();        // 多发多收（双向流式）
    }




    // === 一元：一发一收 ===
    public static void client(){
        HelloRequest request = HelloRequest.newBuilder().setName("hello").build();
        HelloReply helloReply = blockingStub.sayHello(request);
        System.out.println(helloReply);
    }


    // === 客户端流式：多发一收 ===
    public static void clientStream() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);  // 等最终响应到达

        // 1. 调用 asyncStub，拿到 requestObserver
        StreamObserver<HelloRequest> requestObserver = asyncStub.sayHelloClientStream(
            new StreamObserver<HelloReply>() {
                @Override
                public void onNext(HelloReply reply) {
                    // 服务端汇总后回的唯一一条
                    System.out.println("服务端汇总回复: " + reply.getMessage());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    System.out.println("调用完成");
                    latch.countDown();
                }
            }
        );

        // 2. 用 requestObserver 逐条发送
        requestObserver.onNext(HelloRequest.newBuilder().setName("消息A").build());
        requestObserver.onNext(HelloRequest.newBuilder().setName("消息B").build());
        requestObserver.onNext(HelloRequest.newBuilder().setName("消息C").build());

        // 3. 告诉服务端"我发完了"
        requestObserver.onCompleted();

        // 4. 等最终响应
        latch.await(5, TimeUnit.SECONDS);
    }

    // === 服务端流式：一发多收 ===
    public static void serverStream() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        HelloRequest request = HelloRequest.newBuilder().setName("服务端流式").build();

        asyncStub.sayHelloServerStream(request,
                new StreamObserver<HelloReply>() {
                    @Override
                    public void onNext(HelloReply reply) {

                        System.out.println("服务端发送消息: " + reply.getMessage());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        latch.countDown();

                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("服务端传输完成");
                        latch.countDown();
                    }
                }
        );

        // 4. 等最终响应
        latch.await(5, TimeUnit.SECONDS);
    }


    // === 双端流式：多发多收 ===
    private static void bidiStream() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<HelloRequest> streamObserver = asyncStub.sayHelloSCStream(
                new StreamObserver<HelloReply>() {
                    @Override
                    public void onNext(HelloReply helloReply) {
                        System.out.println("双向传输，服务端响应数据: " + helloReply.getMessage());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("双向传输完成");
                        latch.countDown();
                    }
                }
        );

        streamObserver.onNext(HelloRequest.newBuilder().setName("<UNK>A").build());
        streamObserver.onNext(HelloRequest.newBuilder().setName("<UNK>B").build());
        streamObserver.onNext(HelloRequest.newBuilder().setName("<UNK>C").build());
        streamObserver.onNext(HelloRequest.newBuilder().setName("<UNK>D").build());
        streamObserver.onCompleted();

        // 4. 等最终响应
        latch.await(5, TimeUnit.SECONDS);
    }

}