package com.sky.service;

/**
 * PDF生成服务接口
 */
public interface PdfService {
    
    /**
     * 将HTML内容转换为PDF字节数组
     *
     * @param htmlContent HTML内容
     * @return PDF字节数组
     */
    byte[] generatePdfFromHtml(String htmlContent);
} 