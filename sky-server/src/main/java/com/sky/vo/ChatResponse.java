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
    
    /**
     * 创建成功响应
     */
    public static ChatResponse success(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(1) // 默认为普通对话
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static ChatResponse error(String message) {
        return ChatResponse.builder()
                .success(false)
                .message(message)
                .messageType(1)
                .build();
    }
    
    /**
     * 创建错误响应（带错误代码）
     */
    public static ChatResponse error(String message, String errorCode) {
        return ChatResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .messageType(1)
                .build();
    }
    
    /**
     * 创建订单处理成功响应
     */
    public static ChatResponse orderSuccess(String message, String extractedData, String redirectUrl) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .extractedData(extractedData)
                .redirectUrl(redirectUrl)
                .messageType(2) // 订单处理消息
                .build();
    }
    
    /**
     * 创建产品推荐响应
     */
    public static ChatResponse productRecommendation(String message, String productData) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .orderData(productData)
                .messageType(3) // 产品推荐消息
                .build();
    }
    
    /**
     * 创建天气查询响应
     */
    public static ChatResponse weatherResponse(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(4) // 天气查询消息
                .build();
    }
    
    /**
     * 创建汇率查询响应
     */
    public static ChatResponse exchangeRateResponse(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(5) // 汇率查询消息
                .build();
    }
    
    /**
     * 创建新闻查询响应
     */
    public static ChatResponse newsResponse(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(6) // 新闻查询消息
                .build();
    }
    
    /**
     * 创建交通查询响应
     */
    public static ChatResponse trafficResponse(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(7) // 交通查询消息
                .build();
    }
    
    /**
     * 创建攻略查询响应
     */
    public static ChatResponse travelGuideResponse(String message) {
        return ChatResponse.builder()
                .success(true)
                .message(message)
                .messageType(8) // 攻略查询消息
                .build();
    }
} 