package com.sky.mapper;

import com.sky.entity.SystemNotification;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知Mapper
 */
@Mapper
public interface SystemNotificationMapper {

    /**
     * 插入通知
     */
    @Insert("insert into system_notification (type, title, content, icon, related_id, related_type, level, is_read, receiver_role, receiver_id, create_time, expire_time) " +
            "values (#{type}, #{title}, #{content}, #{icon}, #{relatedId}, #{relatedType}, #{level}, #{isRead}, #{receiverRole}, #{receiverId}, #{createTime}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SystemNotification notification);

    /**
     * 获取未读通知数量
     */
    @Select("select count(*) from system_notification where is_read = 0 and receiver_role = #{receiverRole} " +
            "and (receiver_id is null or receiver_id = #{receiverId}) and (expire_time is null or expire_time > now())")
    Integer getUnreadCount(@Param("receiverRole") Integer receiverRole, @Param("receiverId") Long receiverId);

    /**
     * 获取通知列表
     */
    @Select("select * from system_notification where receiver_role = #{receiverRole} " +
            "and (receiver_id is null or receiver_id = #{receiverId}) and (expire_time is null or expire_time > now()) " +
            "order by create_time desc limit #{limit}")
    List<SystemNotification> getNotifications(@Param("receiverRole") Integer receiverRole, 
                                            @Param("receiverId") Long receiverId, 
                                            @Param("limit") Integer limit);

    /**
     * 标记通知为已读
     */
    @Update("update system_notification set is_read = 1, read_time = #{readTime} where id = #{id}")
    void markAsRead(@Param("id") Long id, @Param("readTime") LocalDateTime readTime);

    /**
     * 批量标记为已读
     */
    @Update("update system_notification set is_read = 1, read_time = #{readTime} " +
            "where receiver_role = #{receiverRole} and (receiver_id is null or receiver_id = #{receiverId}) and is_read = 0")
    void markAllAsRead(@Param("receiverRole") Integer receiverRole, 
                      @Param("receiverId") Long receiverId, 
                      @Param("readTime") LocalDateTime readTime);

    /**
     * 删除过期通知
     */
    @Delete("delete from system_notification where expire_time < now()")
    void deleteExpiredNotifications();
} 