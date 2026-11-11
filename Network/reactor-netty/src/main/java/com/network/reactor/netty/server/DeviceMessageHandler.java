package com.network.reactor.netty.server;

import reactor.core.publisher.Mono;
import reactor.netty.Connection;


public class DeviceMessageHandler {


    public Mono<String>  handler(String message, Connection connection){

        try {

            //处理消息 message
            System.out.println("message => " + message);

            return response("SUCCESS","DeviceMessageHandler handler success");

        }catch (Exception e){
            return response("ERROR","消息解析处理失败");
        }
    }


    private Mono<String> response(String type, String message){
        try {
            return Mono.just("{\"type\":"+type+",\"message\":"+message+"}");
        }catch (Exception e){
            return Mono.error(e);
        }
    }


}
