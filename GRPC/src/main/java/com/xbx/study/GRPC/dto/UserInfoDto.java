package com.xbx.study.GRPC.dto;

import com.xbx.study.GRPC.proto.UserInfo;

public class UserInfoDto {
    private long userId;
    private String username;

    public UserInfoDto(long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public static UserInfoDto from(UserInfo proto) {
        return new UserInfoDto(proto.getUserId(), proto.getUsername());
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
