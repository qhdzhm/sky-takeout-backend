package com.sky.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 聊天响应VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 提取的数据（用于订单跳转）
     */
    private String extractedData;
    
    /**
     * 跳转URL
     */
    private String redirectUrl;
    
    /**
     * 订单/产品操作数据（JSON格式）
     */
    private String orderData;
    
    /**
     * 消息类型
     * 1: 普通对话
     * 2: 订单查询
     * 3: 产品推荐
     * 4: 天气查询
     * 5: 汇率查询
     * 6: 新闻查询
     * 7: 交通查询
     * 8: 攻略查询
     */
    private Integer messageType;
    
    // Setter methods for compatibility
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public void setOrderData(String orderData) {
        this.orderData = orderData;
    }
    
    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    /**
     * 创建成功响应
     */
    public static ChatResponse success(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setMessageType(1);
        return r;
    }
    
    /**
     * 创建错误响应
     */
    public static ChatResponse error(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(false);
        r.setMessage(message);
        r.setMessageType(1);
        return r;
    }
    
    /**
     * 创建错误响应（带错误代码）
     */
    public static ChatResponse error(String message, String errorCode) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(false);
        r.setMessage(message);
        r.setErrorCode(errorCode);
        r.setMessageType(1);
        return r;
    }
    
    /**
     * 创建订单处理成功响应
     */
    public static ChatResponse orderSuccess(String message, String extractedData, String redirectUrl) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setExtractedData(extractedData);
        r.setRedirectUrl(redirectUrl);
        r.setMessageType(2);
        return r;
    }
    
    /**
     * 创建产品推荐响应
     */
    public static ChatResponse productRecommendation(String message, String productData) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setOrderData(productData);
        r.setMessageType(3);
        return r;
    }
    
    /**
     * 创建天气查询响应
     */
    public static ChatResponse weatherResponse(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setMessageType(4);
        return r;
    }
    
    /**
     * 创建汇率查询响应
     */
    public static ChatResponse exchangeRateResponse(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setMessageType(5);
        return r;
    }
    
    /**
     * 创建新闻查询响应
     */
    public static ChatResponse newsResponse(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setMessageType(6);
        return r;
    }
    
    /**
     * 创建交通查询响应
     */
    public static ChatResponse trafficResponse(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setMessageType(7);
        return r;
    }
    
    /**
     * 创建攻略查询响应
     */
    public static ChatResponse travelGuideResponse(String message) {
        ChatResponse r = new ChatResponse();
        r.setSuccess(true);
        r.setMessage(message);
        r.setMessageType(8);
        return r;
    }
} 