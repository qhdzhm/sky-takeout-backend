package com.sky.service;

import com.sky.dto.ChatRequest;
import com.sky.entity.ChatMessage;
import com.sky.vo.ChatResponse;

import java.util.List;

/**
 * 聊天机器人服务接口
 */
public interface ChatBotService {
    
    /**
     * 处理用户消息
     */
    ChatResponse processMessage(ChatRequest request);
    
    /**
     * 获取聊天历史
     */
    List<ChatMessage> getChatHistory(String sessionId);
    
    /**
     * 检查用户是否被限流
     */
    boolean checkRateLimit(String sessionId, Long userId);
} 