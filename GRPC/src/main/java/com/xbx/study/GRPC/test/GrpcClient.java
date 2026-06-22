package com.xbx.study.GRPC.test;

import com.xbx.study.GRPC.proto.GreeterGrpc;
import com.xbx.study.GRPC.proto.HelloReply;
import com.xbx.study.GRPC.proto.HelloRequest;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    public static void main(String[] args) {


        ManagedChannel channel = Grpc.newChannelBuilderForAddress("localhost", 50051, InsecureChannelCredentials.create()).build();

        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);


        HelloRequest request = HelloRequest.newBuilder().setName("hello").build();
        HelloReply helloReply = null;
        try {
            helloReply = blockingStub.sayHello(request);
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(helloReply);
    }






}
