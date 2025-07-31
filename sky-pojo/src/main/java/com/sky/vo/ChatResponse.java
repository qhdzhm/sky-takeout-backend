package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 回复消息
     */
    private String message;
    
    /**
     * 消息类型 1-普通问答 2-订单信息
     */
    private Integer messageType;
    
    /**
     * 订单数据 (JSON格式)
     */
    private String orderData;
    
    /**
     * 跳转URL
     */
    private String redirectUrl;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 创建成功响应
     */
    public static ChatResponse success(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(1)
                .build();
    }
    
    /**
     * 创建订单响应
     */
    public static ChatResponse orderSuccess(String message, String orderData, String redirectUrl) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(2)
                .orderData(orderData)
                .redirectUrl(redirectUrl)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static ChatResponse error(String message) {
        return ChatResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static ChatResponse error(String message, String errorCode) {
        return ChatResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
} 