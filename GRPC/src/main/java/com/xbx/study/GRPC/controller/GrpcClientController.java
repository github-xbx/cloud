package com.xbx.study.GRPC.controller;

import com.xbx.study.GRPC.dto.UserInfoDto;
import com.xbx.study.GRPC.proto.UserInfo;
import com.xbx.study.GRPC.service.UserInfoGrpcClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("grpc/client")
@RestController
public class GrpcClientController {


    private final UserInfoGrpcClientService clientService;

    public GrpcClientController(UserInfoGrpcClientService clientService) {
        this.clientService = clientService;
    }



    @GetMapping("list")
    public  List<UserInfoDto> getList(){
        List<UserInfo> list = clientService.getList();
        return list.stream().map(UserInfoDto::from).collect(Collectors.toList());
    }
}
