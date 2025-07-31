package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String cdnDomain;
    
    /**
     * 文件上传
     *
     * @param bytes      文件字节数组
     * @param objectName 对象名称
     * @return 访问URL
     */
    public String upload(byte[] bytes, String objectName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建上传Object的Metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setCacheControl("no-cache");
            metadata.setHeader("Pragma", "no-cache");
            metadata.setContentType(getContentType(objectName.substring(objectName.lastIndexOf("."))));
            metadata.setContentDisposition("inline;filename=" + objectName);
            
            // 上传文件
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes), metadata);
            
            // 如果配置了CDN域名，返回CDN地址；否则返回OSS地址
            if (cdnDomain != null && !cdnDomain.isEmpty()) {
                // 确保CDN域名以https://或http://开头
                String domain = cdnDomain;
                if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                    domain = "https://" + domain;
                }
                // 确保域名末尾没有斜杠
                if (domain.endsWith("/")) {
                    domain = domain.substring(0, domain.length() - 1);
                }
                return domain + "/" + objectName;
            } else {
                // 原有逻辑：设置URL过期时间为100年，相当于永久
                Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 100);
                // 生成URL
                URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
                return url.toString();
            }
        } catch (Exception e) {
            log.error("上传文件失败，{}", e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
    
    /**
     * 上传MultipartFile到OSS
     *
     * @param file      上传的文件
     * @param directory 上传目录，例如 "images/"或"images/user/"
     * @return 访问URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }
        
        // 构建文件名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;
        
        // 构建日期目录，如：images/2025/04/06/
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
        String objectName = directory + datePath + fileName;
        
        try {
            return upload(file.getBytes(), objectName);
        } catch (IOException e) {
            log.error("文件上传失败，{}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }
    
    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 删除文件
            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            log.error("删除文件失败，{}", e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 从URL中提取对象名称
     * 
     * @param url 文件URL
     * @return 对象名称
     */
    public String getObjectNameFromUrl(String url) {
        // 移除查询参数
        String cleanUrl = url;
        if (url.contains("?")) {
            cleanUrl = url.substring(0, url.indexOf("?"));
        }
        
        // 基本URL（不含协议和域名）
        String ossEndpoint = this.endpoint;
        if (ossEndpoint.startsWith("http")) {
            ossEndpoint = ossEndpoint.substring(ossEndpoint.indexOf("://") + 3);
        }
        
        String urlWithoutProtocol = cleanUrl;
        if (cleanUrl.startsWith("http")) {
            urlWithoutProtocol = cleanUrl.substring(cleanUrl.indexOf("://") + 3);
        }
        
        // 移除域名和bucket部分
        if (urlWithoutProtocol.contains(ossEndpoint)) {
            // aliyun oss 标准URL
            String afterEndpoint = urlWithoutProtocol.substring(urlWithoutProtocol.indexOf(ossEndpoint) + ossEndpoint.length());
            if (afterEndpoint.startsWith("/")) {
                afterEndpoint = afterEndpoint.substring(1);
            }
            // 移除bucket名称
            if (afterEndpoint.startsWith(bucketName + "/")) {
                return afterEndpoint.substring((bucketName + "/").length());
            }
        } else if (urlWithoutProtocol.contains(bucketName + ".")) {
            // bucket在域名中的形式: bucket.endpoint/object
            String afterBucket = urlWithoutProtocol.substring(urlWithoutProtocol.indexOf("/") + 1);
            return afterBucket;
        }
        
        return null;
    }
    
    /**
     * 获取文件后缀对应的MIME类型
     */
    private String getContentType(String filenameExtension) {
        if (filenameExtension.equalsIgnoreCase(".jpg") || filenameExtension.equalsIgnoreCase(".jpeg")) {
            return "image/jpeg";
        }
        if (filenameExtension.equalsIgnoreCase(".png")) {
            return "image/png";
        }
        if (filenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (filenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (filenameExtension.equalsIgnoreCase(".webp")) {
            return "image/webp";
        }
        if (filenameExtension.equalsIgnoreCase(".pdf")) {
            return "application/pdf";
        }
        if (filenameExtension.equalsIgnoreCase(".doc") || filenameExtension.equalsIgnoreCase(".docx")) {
            return "application/msword";
        }
        if (filenameExtension.equalsIgnoreCase(".xls") || filenameExtension.equalsIgnoreCase(".xlsx")) {
            return "application/vnd.ms-excel";
        }
        return "application/octet-stream";
    }
} 