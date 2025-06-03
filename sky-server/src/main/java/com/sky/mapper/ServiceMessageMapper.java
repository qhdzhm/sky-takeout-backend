package com.sky.mapper;

import com.sky.entity.ServiceMessage;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务消息Mapper
 */
@Mapper
public interface ServiceMessageMapper {

    /**
     * 插入消息
     */
    @Insert("insert into service_message (session_id, sender_id, receiver_id, message_type, sender_type, content, media_url, message_status, is_from_ai, ai_context_id, create_time, send_time) " +
            "values (#{sessionId}, #{senderId}, #{receiverId}, #{messageType}, #{senderType}, #{content}, #{mediaUrl}, #{messageStatus}, #{isFromAi}, #{aiContextId}, #{createTime}, #{sendTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceMessage serviceMessage);

    /**
     * 根据会话ID获取消息列表
     */
    @Select("select * from service_message where session_id = #{sessionId} order by create_time asc")
    List<ServiceMessage> getBySessionId(Long sessionId);

    /**
     * 更新消息状态
     */
    @Update("update service_message set message_status = #{status}, deliver_time = #{deliverTime}, read_time = #{readTime} where id = #{messageId}")
    void updateMessageStatus(Long messageId, Integer status, LocalDateTime deliverTime, LocalDateTime readTime);

    /**
     * 获取会话未读消息数量
     */
    @Select("select count(*) from service_message where session_id = #{sessionId} and receiver_id = #{receiverId} and message_status < 3")
    Integer getUnreadCount(Long sessionId, Long receiverId);

    /**
     * 标记消息为已读
     */
    @Update("update service_message set message_status = 3, read_time = #{readTime} where session_id = #{sessionId} and receiver_id = #{receiverId} and message_status < 3")
    void markAsRead(Long sessionId, Long receiverId, LocalDateTime readTime);

    /**
     * 获取会话最后一条消息
     */
    @Select("select * from service_message where session_id = #{sessionId} order by create_time desc limit 1")
    ServiceMessage getLastMessage(Long sessionId);

    /**
     * 根据AI上下文ID获取消息
     */
    @Select("select * from service_message where ai_context_id = #{aiContextId} order by create_time asc")
    List<ServiceMessage> getByAiContextId(String aiContextId);
} 