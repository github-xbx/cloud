package rpc.gRPC.service.rpc;


import grpc.proto.netty.GreeterGrpc;
import grpc.proto.netty.HelloReply;
import grpc.proto.netty.HelloRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class HelloWordRpcService extends GreeterGrpc.GreeterImplBase {


    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

        System.out.println("say hello 请求数据=》"+request);


        HelloReply hello = HelloReply.newBuilder().setMessage("hello").build();

        responseObserver.onNext(hello);
        responseObserver.onCompleted();

        //super.sayHello(request, responseObserver);
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloClientStream(StreamObserver<HelloReply> responseObserver) {


        return  new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest helloRequest) {
                //处理客户端的请求
                System.out.println("say Hello Client Stream 请求数据=》"+helloRequest);
            }

            @Override
            public void onError(Throwable throwable) {
                // 异常处理，清理会话
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                HelloReply response = HelloReply.newBuilder().setMessage("sayHelloClientStream response").build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };


        //return super.sayHelloClientStream(responseObserver);
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloSCStream(StreamObserver<HelloReply> responseObserver) {




        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest fileClientMsg) {
                //处理客户端的请求
                System.out.println("say Hello Server Client Stream 请求数据=》"+fileClientMsg);
                HelloReply response = HelloReply.newBuilder().setMessage("sayHelloSCStream response").build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {
                // 异常处理，清理会话
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void sayHelloServerStream(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

        System.out.println("say Hello Server Stream 请求数据=》"+request);
        String name = request.getName();

        for (int i = 1; i <= 5; i++) {

            HelloReply reply = HelloReply.newBuilder()
                    .setMessage("sayHelloServerStream " + name + "，第 " + i + " 条消息")
                    .build();

            responseObserver.onNext(reply);
        }

        responseObserver.onCompleted();
    }

}
