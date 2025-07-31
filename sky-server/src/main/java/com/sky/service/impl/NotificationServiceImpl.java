package com.sky.service.impl;

import com.sky.entity.SystemNotification;
import com.sky.mapper.SystemNotificationMapper;
import com.sky.service.NotificationService;
import com.sky.webSocket.AdminWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现类
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SystemNotificationMapper notificationMapper;

    @Override
    public void createOrderNotification(Long orderId, String customerName, Double amount) {
        SystemNotification notification = SystemNotification.builder()
                .type(1)
                .title("新订单提醒")
                .content(String.format("客户 %s 下了一个新订单，金额 ¥%.2f", customerName, amount))
                .icon("💰")
                .relatedId(orderId)
                .relatedType("order")
                .level(2) // 重要
                .isRead(0)
                .receiverRole(1) // 管理员
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(7))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("new_order", notification);
        
        log.info("🔔 创建新订单通知: 订单ID={}, 客户={}, 金额={}", orderId, customerName, amount);
    }

    @Override
    public void createChatRequestNotification(Long sessionId, String customerName, String subject) {
        SystemNotification notification = SystemNotification.builder()
                .type(2)
                .title("客服请求")
                .content(String.format("客户 %s 发起了客服请求: %s", customerName, subject))
                .icon("💬")
                .relatedId(sessionId)
                .relatedType("session")
                .level(2) // 重要
                .isRead(0)
                .receiverRole(2) // 客服
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusHours(2))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("chat_request", notification);
        
        log.info("🔔 创建客服请求通知: 会话ID={}, 客户={}, 主题={}", sessionId, customerName, subject);
    }

    @Override
    public void createOrderModifyNotification(Long orderId, String customerName, String modifyType) {
        SystemNotification notification = SystemNotification.builder()
                .type(3)
                .title("订单变更")
                .content(String.format("客户 %s 对订单进行了%s操作", customerName, modifyType))
                .icon("📝")
                .relatedId(orderId)
                .relatedType("order")
                .level(1) // 普通
                .isRead(0)
                .receiverRole(1) // 管理员
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(3))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("order_modify", notification);
        
        log.info("🔔 创建订单变更通知: 订单ID={}, 客户={}, 变更类型={}", orderId, customerName, modifyType);
    }

    @Override
    public void createUserRegisterNotification(Long userId, String username) {
        SystemNotification notification = SystemNotification.builder()
                .type(4)
                .title("新用户注册")
                .content(String.format("新用户 %s 注册了账号", username))
                .icon("👤")
                .relatedId(userId)
                .relatedType("user")
                .level(1) // 普通
                .isRead(0)
                .receiverRole(1) // 管理员
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(30))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("user_register", notification);
        
        log.info("🔔 创建用户注册通知: 用户ID={}, 用户名={}", userId, username);
    }

    @Override
    public void createRefundRequestNotification(Long orderId, String customerName, Double amount) {
        SystemNotification notification = SystemNotification.builder()
                .type(5)
                .title("退款申请")
                .content(String.format("客户 %s 申请退款，金额 ¥%.2f", customerName, amount))
                .icon("💸")
                .relatedId(orderId)
                .relatedType("order")
                .level(3) // 紧急
                .isRead(0)
                .receiverRole(1) // 管理员
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(15))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("refund_request", notification);
        
        log.info("🔔 创建退款申请通知: 订单ID={}, 客户={}, 金额={}", orderId, customerName, amount);
    }

    @Override
    public void createComplaintNotification(Long complaintId, String customerName, String subject) {
        SystemNotification notification = SystemNotification.builder()
                .type(6)
                .title("投诉建议")
                .content(String.format("客户 %s 提交了投诉建议: %s", customerName, subject))
                .icon("⚠️")
                .relatedId(complaintId)
                .relatedType("complaint")
                .level(3) // 紧急
                .isRead(0)
                .receiverRole(1) // 管理员
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(10))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("complaint", notification);
        
        log.info("🔔 创建投诉建议通知: 投诉ID={}, 客户={}, 主题={}", complaintId, customerName, subject);
    }

    @Override
    public void createDetailedOrderNotification(Long orderId, String operatorName, String operatorType, 
                                              String contactPerson, String orderNumber, 
                                              String actionType, String actionDetail) {
        // 格式化操作者信息
        String operatorInfo;
        String operatorIcon;
        switch (operatorType.toLowerCase()) {
            case "agent":
                operatorInfo = String.format("中介 %s", operatorName);
                operatorIcon = "🏢";
                break;
            case "operator":
                operatorInfo = String.format("操作员 %s", operatorName);
                operatorIcon = "👤";
                break;
            case "user":
                operatorInfo = String.format("客户 %s", operatorName);
                operatorIcon = "👥";
                break;
            default:
                operatorInfo = operatorName;
                operatorIcon = "📝";
        }

        // 格式化订单信息（优先显示联系人，没有则显示订单号）
        String orderInfo = contactPerson != null && !contactPerson.trim().isEmpty() 
                          ? String.format("订单联系人 %s", contactPerson)
                          : String.format("订单 %s", orderNumber);

        // 格式化操作类型
        String actionTypeChinese;
        int priority = 1; // 默认普通级别
        switch (actionType.toLowerCase()) {
            case "payment":
                actionTypeChinese = "完成支付";
                operatorIcon = "💰";
                priority = 2; // 重要
                break;
            case "create":
                actionTypeChinese = "下订单";
                operatorIcon = "📋";
                priority = 2;
                break;
            case "modify":
                actionTypeChinese = "修改订单";
                operatorIcon = "✏️";
                break;
            case "cancel":
                actionTypeChinese = "取消订单";
                operatorIcon = "❌";
                priority = 2;
                break;
            case "confirm":
                actionTypeChinese = "确认订单";
                operatorIcon = "✅";
                priority = 2;
                break;
            case "complete":
                actionTypeChinese = "完成订单";
                operatorIcon = "🎉";
                priority = 2;
                break;
            default:
                actionTypeChinese = actionType;
                operatorIcon = "📝";
        }

        // 构建通知内容
        String content = String.format("%s 对 %s 进行了%s操作", 
                                     operatorInfo, orderInfo, actionTypeChinese);
        
        if (actionDetail != null && !actionDetail.trim().isEmpty()) {
            content += String.format(" (%s)", actionDetail);
        }

        SystemNotification notification = SystemNotification.builder()
                .type(3) // 订单变更类型
                .title("订单操作通知")
                .content(content)
                .icon(operatorIcon)
                .relatedId(orderId)
                .relatedType("order")
                .level(priority)
                .isRead(0)
                .receiverRole(1) // 管理员
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(3))
                .build();

        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("detailed_order_action", notification);
        
        log.info("🔔 创建详细订单操作通知: 订单ID={}, 操作者={} ({}), 订单={}, 操作={}, 详情={}", 
                orderId, operatorName, operatorType, orderInfo, actionTypeChinese, actionDetail);
    }

    @Override
    public void createCustomNotification(SystemNotification notification) {
        if (notification.getCreateTime() == null) {
            notification.setCreateTime(LocalDateTime.now());
        }
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        
        notificationMapper.insert(notification);
        
        // 发送WebSocket实时通知
        sendRealTimeNotification("custom", notification);
        
        log.info("🔔 创建自定义通知: 类型={}, 标题={}", notification.getType(), notification.getTitle());
    }

    @Override
    public Integer getUnreadCount(Integer receiverRole, Long receiverId) {
        return notificationMapper.getUnreadCount(receiverRole, receiverId);
    }

    @Override
    public List<SystemNotification> getNotifications(Integer receiverRole, Long receiverId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return notificationMapper.getNotifications(receiverRole, receiverId, limit);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId, LocalDateTime.now());
    }

    @Override
    public void markAllAsRead(Integer receiverRole, Long receiverId) {
        notificationMapper.markAllAsRead(receiverRole, receiverId, LocalDateTime.now());
    }

    @Override
    public void cleanExpiredNotifications() {
        notificationMapper.deleteExpiredNotifications();
        log.info("🧹 清理过期通知完成");
    }

    /**
     * 发送实时WebSocket通知
     */
    private void sendRealTimeNotification(String notificationType, SystemNotification notification) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("notificationId", notification.getId());
            data.put("type", notification.getType());
            data.put("title", notification.getTitle());
            data.put("content", notification.getContent());
            data.put("icon", notification.getIcon());
            data.put("level", notification.getLevel());
            data.put("relatedId", notification.getRelatedId());
            data.put("relatedType", notification.getRelatedType());
            data.put("createTime", notification.getCreateTime().toString());

            // 根据接收者角色发送通知
            if (notification.getReceiverRole() == 1) {
                // 发送给所有管理员
                AdminWebSocketServer.broadcastToAllServices(
                    AdminWebSocketServer.createNotificationMessage("system_notification", notification.getTitle(), data)
                );
            } else if (notification.getReceiverRole() == 2) {
                // 发送给所有客服
                AdminWebSocketServer.broadcastToAllServices(
                    AdminWebSocketServer.createNotificationMessage("system_notification", notification.getTitle(), data)
                );
            } else if (notification.getReceiverRole() == 3 && notification.getReceiverId() != null) {
                // 发送给特定用户
                AdminWebSocketServer.sendMessage(notification.getReceiverId(),
                    AdminWebSocketServer.createNotificationMessage("system_notification", notification.getTitle(), data)
                );
            }

            log.info("✅ 发送实时通知成功: 类型={}, 接收者角色={}", notificationType, notification.getReceiverRole());
        } catch (Exception e) {
            log.error("❌ 发送实时通知失败: {}", e.getMessage(), e);
        }
    }
} 