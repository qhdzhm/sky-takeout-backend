package com.sky.mapper;

import com.sky.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper {
    
    /**
     * 插入聊天消息
     */
    @Insert("INSERT INTO chat_message (session_id, user_id, user_message, bot_response, message_type, extracted_data, user_type, create_time, update_time) " +
            "VALUES (#{sessionId}, #{userId}, #{userMessage}, #{botResponse}, #{messageType}, #{extractedData}, #{userType}, #{createTime}, #{updateTime})")
    void insert(ChatMessage chatMessage);
    
    /**
     * 根据会话ID查询聊天历史
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> selectBySessionId(String sessionId);
    
    /**
     * 根据用户ID查询最近的聊天记录
     */
    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> selectRecentByUserId(Long userId, Integer limit);
    
    /**
     * 统计用户今日聊天次数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE user_id = #{userId} AND DATE(create_time) = CURDATE()")
    Integer countTodayMessages(Long userId);
} 