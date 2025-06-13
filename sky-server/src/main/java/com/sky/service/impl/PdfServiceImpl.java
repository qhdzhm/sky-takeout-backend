package com.sky.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sky.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
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
            
            // 确保HTML内容包含完整的HTML结构和正确的编码声明
            if (!htmlContent.contains("<!DOCTYPE html>")) {
                htmlContent = "<!DOCTYPE html>\n<html>\n<head>\n" +
                    "<meta charset=\"UTF-8\">\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                    "</head>\n<body>\n" + htmlContent + "\n</body>\n</html>";
            }
            
            // 处理logo图片，将占位符替换为实际的base64图片
            htmlContent = processLogoImage(htmlContent);
            
            // 确保HTML内容使用UTF-8编码
            byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            String processedHtml = new String(htmlBytes, StandardCharsets.UTF_8);
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(processedHtml, null);
            
            // 设置默认字体和字符编码
            builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
            
            // 添加中文字体支持
            addChineseFontSupport(builder);
            
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
     * 添加中文字体支持 - 改进版本
     */
    private void addChineseFontSupport(PdfRendererBuilder builder) {
        try {
            log.info("开始添加中文字体支持...");
            
            // 方案1: 尝试加载内置字体资源
            boolean resourceFontLoaded = loadResourceFonts(builder);
            
            // 方案2: 如果内置字体加载失败，尝试系统字体
            if (!resourceFontLoaded) {
                boolean systemFontLoaded = loadSystemFonts(builder);
                
                if (!systemFontLoaded) {
                    log.warn("未能加载任何中文字体，PDF可能出现中文乱码");
                    // 设置默认后备字体
                    setFallbackFont(builder);
                }
            }
            
        } catch (Exception e) {
            log.error("添加中文字体支持失败: {}", e.getMessage(), e);
            setFallbackFont(builder);
        }
    }
    
    /**
     * 加载资源文件中的字体
     */
    private boolean loadResourceFonts(PdfRendererBuilder builder) {
        try {
            // 尝试从resources目录加载字体文件
            String[] resourceFonts = {
                "fonts/simhei.ttf",
                "fonts/simsun.ttf", 
                "fonts/microsoftyahei.ttf",
                "fonts/NotoSansCJK-Regular.ttc"
            };
            
            for (String fontPath : resourceFonts) {
                try {
                    ClassPathResource fontResource = new ClassPathResource(fontPath);
                    if (fontResource.exists()) {
                        try (InputStream fontStream = fontResource.getInputStream()) {
                            byte[] fontBytes = StreamUtils.copyToByteArray(fontStream);
                            builder.useFont(() -> new java.io.ByteArrayInputStream(fontBytes), "Chinese");
                            log.info("成功加载资源字体: {}", fontPath);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.debug("资源字体加载失败: {}, 错误: {}", fontPath, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("加载资源字体时出错: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 加载系统字体
     */
    private boolean loadSystemFonts(PdfRendererBuilder builder) {
        try {
            // Windows系统字体路径
            String[] windowsFonts = {
                "C:/Windows/Fonts/msyh.ttc",      // 微软雅黑
                "C:/Windows/Fonts/msyhbd.ttc",    // 微软雅黑粗体
                "C:/Windows/Fonts/simhei.ttf",    // 黑体
                "C:/Windows/Fonts/simsun.ttc",    // 宋体
                "C:/Windows/Fonts/simkai.ttf",    // 楷体
                "C:/Windows/Fonts/MSYH.TTF",      // 微软雅黑备选
                "C:/Windows/Fonts/SIMHEI.TTF",    // 黑体备选
                "C:/Windows/Fonts/SIMSUN.TTC"     // 宋体备选
            };
            
            // Linux系统字体路径
            String[] linuxFonts = {
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                "/System/Library/Fonts/PingFang.ttc",  // macOS
                "/System/Library/Fonts/STHeiti Light.ttc" // macOS
            };
            
            // 检测操作系统
            String osName = System.getProperty("os.name").toLowerCase();
            String[] fontPaths = osName.contains("windows") ? windowsFonts : linuxFonts;
            
            for (String fontPath : fontPaths) {
                try {
                    File fontFile = new File(fontPath);
                    if (fontFile.exists() && fontFile.canRead()) {
                        builder.useFont(fontFile, "Chinese");
                        log.info("成功加载系统字体: {}", fontPath);
                        return true;
                    }
                } catch (Exception e) {
                    log.debug("系统字体加载失败: {}, 错误: {}", fontPath, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("加载系统字体时出错: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 设置后备字体配置
     */
    private void setFallbackFont(PdfRendererBuilder builder) {
        try {
            // 尝试使用Java内置的字体
            builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
            log.info("已设置后备字体配置");
        } catch (Exception e) {
            log.warn("设置后备字体配置失败: {}", e.getMessage());
        }
    }
}