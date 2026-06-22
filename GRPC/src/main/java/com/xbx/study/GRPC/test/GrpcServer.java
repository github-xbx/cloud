package com.xbx.study.GRPC.test;

import com.xbx.study.GRPC.proto.GreeterGrpc;
import com.xbx.study.GRPC.proto.HelloReply;
import com.xbx.study.GRPC.proto.HelloRequest;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GrpcServer {

    //private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);


    public static void main(String[] args) throws IOException, InterruptedException {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        Server server = Grpc.newServerBuilderForPort(50051, InsecureServerCredentials.create())
                .executor(poolExecutor)
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
            responseObserver.onNext(helloReply);
            responseObserver.onCompleted();

        }
    }

}
