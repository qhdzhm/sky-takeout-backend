package com.sky.service;

import com.sky.dto.SendMessageDTO;
import com.sky.entity.ServiceMessage;

import java.util.List;

/**
 * 服务消息Service接口
 */
public interface ServiceMessageService {

    /**
     * 发送消息
     */
    ServiceMessage sendMessage(SendMessageDTO sendMessageDTO);

    /**
     * 获取会话消息列表
     */
    List<ServiceMessage> getSessionMessages(Long sessionId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long sessionId, Long receiverId);

    /**
     * 获取未读消息数量
     */
    Integer getUnreadCount(Long sessionId, Long receiverId);

    /**
     * 根据AI上下文ID获取历史消息
     */
    List<ServiceMessage> getMessagesByAiContextId(String aiContextId);

    // ========== 管理端新增方法 ==========

    /**
     * 客服端标记消息为已读
     */
    void markAsReadByService(Long sessionId);
} 