package com.sky.service;

import com.sky.entity.SystemNotification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建新订单通知
     */
    void createOrderNotification(Long orderId, String customerName, Double amount);

    /**
     * 创建聊天请求通知
     */
    void createChatRequestNotification(Long sessionId, String customerName, String subject);

    /**
     * 创建订单修改通知
     */
    void createOrderModifyNotification(Long orderId, String customerName, String modifyType);

    /**
     * 创建用户注册通知
     */
    void createUserRegisterNotification(Long userId, String username);

    /**
     * 创建退款申请通知
     */
    void createRefundRequestNotification(Long orderId, String customerName, Double amount);

    /**
     * 创建投诉建议通知
     */
    void createComplaintNotification(Long complaintId, String customerName, String subject);

    /**
     * 创建自定义通知
     */
    void createCustomNotification(SystemNotification notification);

    /**
     * 获取未读通知数量
     */
    Integer getUnreadCount(Integer receiverRole, Long receiverId);

    /**
     * 获取通知列表
     */
    List<SystemNotification> getNotifications(Integer receiverRole, Long receiverId, Integer limit);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Integer receiverRole, Long receiverId);

    /**
     * 清理过期通知
     */
    void cleanExpiredNotifications();

    /**
     * 创建详细的订单操作通知（新增方法）
     * @param orderId 订单ID  
     * @param operatorName 操作者姓名（中介/操作员/用户）
     * @param operatorType 操作者类型（agent/operator/user）
     * @param contactPerson 订单联系人
     * @param orderNumber 订单号
     * @param actionType 操作类型（payment/modify/create/cancel等）
     * @param actionDetail 操作详情
     */
    void createDetailedOrderNotification(Long orderId, String operatorName, String operatorType, 
                                       String contactPerson, String orderNumber, 
                                       String actionType, String actionDetail);

    /**
     * 代理商端订单变更通知（定向给agent或operator）
     * @param agentId 代理商ID（必填）
     * @param operatorId 操作员ID（可选，为空表示主号）
     * @param orderId 订单ID
     * @param orderNumber 订单号
     * @param changeTitle 标题
     * @param changeDetail 变更详情
     */
    void createAgentOrderChangeNotification(Long agentId, Long operatorId, Long orderId,
                                            String orderNumber, String changeTitle, String changeDetail);
} 