package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    
    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    
    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    /**
     * 用户类型 1-普通客户 2-中介操作员
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
    
    /**
     * 用户ID (可选) - 支持数字ID和字符串ID（如guest用户）
     */
    private String userId;
    
    /**
     * 当前页面路径 (可选)
     */
    private String currentPage;
    
    /**
     * 当前完整URL (可选)
     */
    private String currentUrl;
} 