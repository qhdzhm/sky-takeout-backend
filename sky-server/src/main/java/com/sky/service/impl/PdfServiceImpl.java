package com.sky.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sky.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * PDF生成服务实现类
 */
@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    @Override
    public byte[] generatePdfFromHtml(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // 确保HTML内容包含完整的HTML结构
            if (!htmlContent.contains("<!DOCTYPE html>")) {
                htmlContent = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n</head>\n<body>\n" 
                            + htmlContent + "\n</body>\n</html>";
            }
            
            // 处理logo图片，将占位符替换为实际的base64图片
            htmlContent = processLogoImage(htmlContent);
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            
            // 尝试添加中文字体支持
            try {
                // 使用系统自带的中文字体
                addSystemChineseFonts(builder);
            } catch (Exception fontException) {
                log.warn("添加中文字体失败，将使用默认字体: {}", fontException.getMessage());
            }
            
            builder.toStream(outputStream);
            builder.run();
            
            log.info("PDF生成成功，大小: {} bytes", outputStream.size());
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理logo图片，将占位符替换为实际的base64编码图片
     */
    private String processLogoImage(String htmlContent) {
        try {
            // 读取logo图片文件
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
            if (logoResource.exists()) {
                // 读取图片并转换为base64
                try (InputStream inputStream = logoResource.getInputStream()) {
                    byte[] imageBytes = StreamUtils.copyToByteArray(inputStream);
                    String base64Image = Base64Utils.encodeToString(imageBytes);
                    
                    // 替换HTML中的占位符
                    String logoDataUrl = "data:image/png;base64," + base64Image;
                    htmlContent = htmlContent.replace(
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
                        logoDataUrl
                    );
                    
                    log.info("成功加载logo图片，大小: {} bytes", imageBytes.length);
                }
            } else {
                log.warn("Logo图片文件不存在: static/images/logo.png");
            }
        } catch (Exception e) {
            log.error("处理logo图片失败", e);
        }
        return htmlContent;
    }

    /**
     * 添加系统中文字体支持
     */
    private void addSystemChineseFonts(PdfRendererBuilder builder) {
        try {
            boolean fontLoaded = false;
            
            // Windows系统中文字体路径
            String[] windowsFonts = {
                "C:/Windows/Fonts/msyh.ttc",      // 微软雅黑 (优先)
                "C:/Windows/Fonts/simhei.ttf",    // 黑体
                "C:/Windows/Fonts/simsun.ttc",    // 宋体
                "C:/Windows/Fonts/simkai.ttf",    // 楷体
                "C:/Windows/Fonts/MSYH.TTF",      // 微软雅黑 (备选)
                "C:/Windows/Fonts/SIMHEI.TTF",    // 黑体 (备选)
                "C:/Windows/Fonts/SIMSUN.TTC"     // 宋体 (备选)
            };
            
            // 尝试加载Windows字体
            for (String fontPath : windowsFonts) {
                try {
                    java.io.File fontFile = new java.io.File(fontPath);
                    if (fontFile.exists()) {
                        builder.useFont(fontFile, "Chinese");
                        log.info("成功添加中文字体: {}", fontPath);
                        fontLoaded = true;
                        break; // 只要有一个字体成功加载就够了
                    }
                } catch (Exception e) {
                    log.debug("字体文件加载失败: {}, 错误: {}", fontPath, e.getMessage());
                }
            }
            
            if (!fontLoaded) {
                log.warn("未能加载任何中文字体，中文字符可能显示为乱码");
            }
            
        } catch (Exception e) {
            log.warn("添加系统中文字体失败: {}", e.getMessage());
        }
    }
} 