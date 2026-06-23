package com.xbx.study.common.oss;

import com.aliyun.sdk.service.oss2.credentials.Credentials;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileCredentialsProvider implements CredentialsProvider {

    /**
     * The held static credentials object
     */
    private final Credentials credential;

    public FileCredentialsProvider() {
        Map<String, String> map = fileRead(null);
        if (map.isEmpty()) {
            throw  new RuntimeException("配置文件 .aliyun.oss.env 内容为空");
        }

        this.credential = new Credentials(map.get("OSS_ACCESS_KEY_ID"), map.get("OSS_ACCESS_KEY_SECRET"));

    }

    public FileCredentialsProvider(String filePath) {
        Map<String, String> map = fileRead(filePath);
        if (map.isEmpty()) {
            throw  new RuntimeException("配置文件 .aliyun.oss.env 内容为空");
        }

        this.credential = new Credentials(map.get("OSS_ACCESS_KEY_ID"), map.get("OSS_ACCESS_KEY_SECRET"));

    }


    private Map<String, String> fileRead(String filePath){
        Map<String, String> map = new HashMap<>();
        String envFilePath = StringUtils.isEmpty(filePath) ? ".aliyun.oss.env" : filePath;
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(envFilePath)) {
            if (is == null) {
                throw new FileNotFoundException("请现在classpath下配置 .aliyun.oss.env");
            }

            //.env 文件格式 KEY=VALUE
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // 跳过空行和注释
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }


        } catch (IOException e) {
            throw new RuntimeException("读取oss配置失败", e);
        }

        return map;
    }



    @Override
    public Credentials getCredentials() {
        return this.credential;
    }
}
