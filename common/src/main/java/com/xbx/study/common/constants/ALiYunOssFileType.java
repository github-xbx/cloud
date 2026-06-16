package com.xbx.study.common.constants;

public enum ALiYunOssFileType {

    IMAGE("image/jpeg", ".jpg"),
    PDF("application/pdf", ".pdf"),
    TXT("text/plain", ".txt"),
    JSON("application/json", ".json");


    public final String type;
    public final String suffix;

    ALiYunOssFileType(String type, String suffix){
        this.type = type;
        this.suffix = suffix;
    }
}
