package com.xbx.study.common.oss;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.models.PutObjectResult;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.xbx.study.common._interface.OssService;
import com.xbx.study.common.constants.ALiYunOssFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;


@Component
public class ALiYunOssService implements OssService, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ALiYunOssService.class);
    private static final String ENDPOINT = "https://oss-cn-beijing.aliyuncs.com";


    private OSSClient ossClient;


    private String baseUrl(String bucket, String uploadFileName){
        return "https://" + bucket + ".oss-cn-beijing.aliyuncs.com/"+ uploadFileName;
    }


    @Override
    public String uploadFile(String bucket, InputStream in,  ALiYunOssFileType fileType) {


        String uploadFileName =  LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "/"
                + UUID.randomUUID().toString() + fileType.suffix;

        PutObjectRequest request = PutObjectRequest
                .newBuilder()
                .bucket(bucket)

                .key(uploadFileName)
                .body(BinaryData.fromStream(in))
                .contentType(fileType.type)
                .build();

        PutObjectResult result = ossClient.putObject(request);
        logger.debug(request.toString());
        if (result.statusCode() == 200) {
            return baseUrl(bucket, uploadFileName);
        }else  {
            throw new RuntimeException("oss upload failed");
        }


    }

    @Override
    public void shardUploadFile() {

    }

    @Override
    public void downloadFile() {

    }

    @Override
    public void deleteFile() {

    }


    /**
     * 属性注入后
     */
    @Override
    public void afterPropertiesSet()  {

        ossClient = OSSClient.newBuilder()
                //地域
                .region("cn-beijing")
                //端点 地域节点
                .endpoint(ENDPOINT)
                //令牌token 存放在 .aliyun.oss.env
                .credentialsProvider(new FileCredentialsProvider())
                //连接5秒超时
                .connectTimeout(Duration.ofSeconds(5))
                //读写30秒超时
                .readWriteTimeout(Duration.ofSeconds(30))
                .build();
        logger.debug("aliyun oss cline init...");

    }

    /**
     * bean 销毁前
     */
    @Override
    public void destroy() throws Exception {

        if (ossClient != null) {
            ossClient.close();
            logger.debug("aliyun oss cline destroy...");
        }
    }


}
