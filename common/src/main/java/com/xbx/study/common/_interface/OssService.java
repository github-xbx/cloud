package com.xbx.study.common._interface;


import com.xbx.study.common.constants.ALiYunOssFileType;

import java.io.InputStream;

/**
 * OSS（对象存储服务）统一操作接口。
 * <p>定义了文件上传、分片上传、下载和删除等核心操作，各云存储厂商需实现此接口。</p>
 *
 * @author xingbingxuan
 * @since 1.0.0
 */
public interface OssService {

    /**
     * 上传文件到 OSS。
     * <p>将本地文件或文件流上传至指定的存储桶（Bucket）中。</p>
     */
    String uploadFile(String bucket, InputStream in, ALiYunOssFileType fileType);

    /**
     * 分片上传文件到 OSS。
     * <p>适用于大文件上传场景，将文件分割为多个分片并行上传，提高上传效率和可靠性。</p>
     */
    void shardUploadFile();

    /**
     * 从 OSS 下载文件。
     * <p>根据文件标识从存储桶中获取文件内容。</p>
     */
    void downloadFile();

    /**
     * 从 OSS 删除文件。
     * <p>根据文件标识从存储桶中移除指定文件。</p>
     */
    void deleteFile();

}