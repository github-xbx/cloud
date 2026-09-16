package com.xbx.study.ai.utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BailianTemporaryStorage {

    // 你的 API Key
    private static final String API_KEY = System.getenv("java_qwen_apikey");
    // 文件要绑定的模型名称，需与后续调用的模型一致
    private static final String MODEL_NAME = "happyhorse-1.1-r2v";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String localFilePath = "E:\\file\\87270530-d130-4238-b294-70c9e359d258.jpg"; // 替换为你的本地图片路径

        String tempUrl = uploadFileAndGetUrl(localFilePath);
        System.out.println("获取到的临时 URL: " + tempUrl);
        // 输出示例: oss://dashscope-instant/xxx/2024-07-18/xxxx/image.png
    }

    /**
     * 上传本地文件到百炼临时存储，并返回 oss:// 格式的 URL
     */
    public static String uploadFileAndGetUrl(String localFilePath) throws IOException {
        // 1. 获取上传凭证
        JsonNode policyData = getUploadPolicy(API_KEY, MODEL_NAME);

        // 2. 上传文件到 OSS
        String ossUrl = uploadFileToOss(policyData, localFilePath);

        return ossUrl;
    }

    /**
     * 步骤1：获取文件上传凭证
     */
    private static JsonNode getUploadPolicy(String apiKey, String modelName) throws IOException {
        HttpUrl url = HttpUrl.parse("https://dashscope.aliyuncs.com/api/v1/uploads")
                .newBuilder()
                .addQueryParameter("action", "getPolicy")
                .addQueryParameter("model", modelName)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取上传凭证失败，HTTP 状态码: " + response.code() + ", 响应: " + response.body().string());
            }
            String responseBody = response.body().string();
            JsonNode rootNode = mapper.readTree(responseBody);
            return rootNode.path("data");
        }
    }

    /**
     * 步骤2：使用凭证将文件上传到临时 OSS
     */
    private static String uploadFileToOss(JsonNode policyData, String localFilePath) throws IOException {
        File file = new File(localFilePath);
        String fileName = file.getName();

        // 构造 OSS 的上传 Key（路径）
        String uploadDir = policyData.path("upload_dir").asText();
        String key = uploadDir + "/" + fileName;

        // 构造 multipart/form-data 请求体[reference:4][reference:5]
        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("OSSAccessKeyId", policyData.path("oss_access_key_id").asText())
                .addFormDataPart("Signature", policyData.path("signature").asText())
                .addFormDataPart("policy", policyData.path("policy").asText())
                .addFormDataPart("x-oss-object-acl", policyData.path("x_oss_object_acl").asText())
                .addFormDataPart("x-oss-forbid-overwrite", policyData.path("x_oss_forbid_overwrite").asText())
                .addFormDataPart("key", key)
                .addFormDataPart("success_action_status", "200")
                .addFormDataPart("file", fileName, RequestBody.create(file, MediaType.parse("application/octet-stream")));

        // 获取 OSS 上传地址
        String ossHost = policyData.path("oss_host").asText();
        // 注意：此处使用 HTTP，而非 HTTPS[reference:6][reference:7]
        String uploadUrl = "http://" + ossHost;

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(formBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("上传文件到 OSS 失败，HTTP 状态码: " + response.code() + ", 响应: " + response.body().string());
            }
            // 上传成功后，拼接 oss:// 格式的 URL
            // 格式: oss://{bucket}/{key}
            String bucket = policyData.path("bucket").asText();
            return "oss://" + bucket + "/" + key;
        }
    }
}