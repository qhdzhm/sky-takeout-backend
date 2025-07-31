package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.SendMessageDTO;
import com.sky.entity.ServiceMessage;
import com.sky.entity.ServiceSession;
import com.sky.entity.Employee;
import com.sky.mapper.ServiceMessageMapper;
import com.sky.mapper.ServiceSessionMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.ServiceMessageService;
import com.sky.webSocket.AdminWebSocketServer;
import com.sky.webSocket.UserWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务消息Service实现类
 */
@Service
@Slf4j
public class ServiceMessageServiceImpl implements ServiceMessageService {

    @Autowired
    private ServiceMessageMapper serviceMessageMapper;

    @Autowired
    private ServiceSessionMapper serviceSessionMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 发送消息
     */
    @Override
    public ServiceMessage sendMessage(SendMessageDTO sendMessageDTO) {
        Long sessionId = sendMessageDTO.getSessionId();
        Long senderId = BaseContext.getCurrentId();
        
        // 获取会话信息
        ServiceSession session = serviceSessionMapper.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 确定接收者ID和发送者类型
        Long receiverId;
        Integer senderType;
        Integer newSessionStatus = session.getSessionStatus(); // 默认保持原状态
        boolean statusChanged = false; // 标记状态是否改变
        
        if (senderId.equals(session.getUserId())) {
            // 用户发送消息
            receiverId = session.getEmployeeId();
            senderType = 1; // 用户
        } else if (session.getEmployeeId() != null && senderId.equals(session.getEmployeeId())) {
            // 客服发送消息（会话已分配）
            receiverId = session.getUserId();
            senderType = 2; // 客服
            // 确保会话状态为进行中
            if (session.getSessionStatus() != 1) {
                newSessionStatus = 1;
                statusChanged = true;
            }
        } else if (session.getEmployeeId() == null) {
            // 🔧 修复权限控制：会话未分配时，允许管理员、操作员、客服接管
            log.info("会话 {} 未分配客服，员工 {} 尝试接管该会话", sessionId, senderId);
            
            // 检查当前员工角色是否有权限接管会话
            Employee currentEmployee = employeeMapper.getById(senderId.intValue());
            if (currentEmployee == null) {
                log.error("❌ 权限检查失败: 员工 {} 不存在", senderId);
                throw new RuntimeException("员工不存在，无法接管会话");
            }
            
            // 允许管理员(role=1)、操作员(role=2)、客服(role=3)接管会话，禁止导游(role=4)
            if (currentEmployee.getRole() == 4) {
                log.error("❌ 权限检查失败: 导游 {} 无权接管客服会话", senderId);
                throw new RuntimeException("导游无权接管客服会话");
            }
            
            String roleText = getRoleText(currentEmployee.getRole());
            log.info("✅ {} {} 自动接管未分配的会话 {}", roleText, senderId, sessionId);
            
            // 自动分配会话给当前员工（可能是管理员、操作员或客服）
            serviceSessionMapper.assignService(sessionId, senderId, LocalDateTime.now());
            
            // 更新会话状态为进行中
            serviceSessionMapper.updateSessionStatus(sessionId, 1, null, null, LocalDateTime.now());
            
            // 绑定会话和客服关系到WebSocket
            AdminWebSocketServer.bindSessionToService(sessionId, senderId);
            
            receiverId = session.getUserId();
            senderType = 2; // 客服
            newSessionStatus = 1; // 设置为进行中
            statusChanged = true;
        } else {
            log.error("权限检查失败: senderId={}, session.getUserId()={}, session.getEmployeeId()={}", 
                senderId, session.getUserId(), session.getEmployeeId());
            throw new RuntimeException("无权限发送消息");
        }

        // 创建消息
        ServiceMessage serviceMessage = ServiceMessage.builder()
                .sessionId(sessionId)
                .senderId(senderId)
                .receiverId(receiverId)
                .messageType(sendMessageDTO.getMessageType())
                .senderType(senderType)
                .content(sendMessageDTO.getContent())
                .mediaUrl(sendMessageDTO.getMediaUrl())
                .messageStatus(1) // 已发送
                .isFromAi(false)
                .createTime(LocalDateTime.now())
                .sendTime(LocalDateTime.now())
                .build();

        serviceMessageMapper.insert(serviceMessage);

        // 更新会话状态和最后更新时间
        serviceSessionMapper.updateSessionStatus(sessionId, newSessionStatus, 
                session.getEndTime(), session.getServiceDuration(), LocalDateTime.now());

        // WebSocket推送逻辑
        if (senderType == 2) { // 客服发送消息
            // 1. 向用户推送消息
            try {
                // 根据用户类型确定用户ID和推送方式
                Long targetUserId = session.getUserId();
                Integer userType = session.getUserType(); // 用户类型：1-普通用户 2-代理商 3-代理商操作员
                
                // 使用UserWebSocketServer向指定用户推送消息
                UserWebSocketServer.sendServiceMessage(targetUserId, sendMessageDTO.getContent(), sessionId);
                
                log.info("已向用户推送消息: userId={}, content={}, sessionId={}", targetUserId, sendMessageDTO.getContent(), sessionId);
            } catch (Exception e) {
                log.error("向用户推送消息失败: {}", e.getMessage());
            }
            
            // 2. 如果状态改变，向用户推送状态更新
            if (statusChanged) {
                try {
                    Long targetUserId = session.getUserId();
                    Map<String, Object> statusData = new HashMap<>();
                    statusData.put("sessionId", sessionId);
                    statusData.put("status", newSessionStatus);
                    statusData.put("message", "客服已接入");
                    
                    String statusMessage = UserWebSocketServer.createMessage("status_change", "客服已接入", statusData);
                    UserWebSocketServer.sendMessage(targetUserId, statusMessage);
                    
                    log.info("已向用户推送状态变化: userId={}, sessionId={}, status={}", targetUserId, sessionId, newSessionStatus);
                } catch (Exception e) {
                    log.error("向用户推送状态变化失败: {}", e.getMessage());
                }
            }
        } else if (senderType == 1) { // 用户发送消息
            // 向客服端推送消息
            try {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("type", "service_message");
                messageData.put("id", serviceMessage.getId());
                messageData.put("sessionId", sessionId);
                messageData.put("content", sendMessageDTO.getContent());
                messageData.put("senderType", senderType);
                messageData.put("createTime", serviceMessage.getCreateTime().toString());
                
                // 推送给管理端WebSocket
                AdminWebSocketServer.notifySessionMessage(sessionId, messageData);
                
                log.info("已向客服推送消息: sessionId={}, content={}, senderType={}", 
                    sessionId, sendMessageDTO.getContent(), senderType);
            } catch (Exception e) {
                log.error("向客服推送消息失败: {}", e.getMessage());
            }
        }

        log.info("消息发送成功，会话ID: {}, 发送者: {}, 内容: {}, 新状态: {}", sessionId, senderId, sendMessageDTO.getContent(), newSessionStatus);

        return serviceMessage;
    }

    /**
     * 获取会话消息列表
     */
    @Override
    public List<ServiceMessage> getSessionMessages(Long sessionId) {
        return serviceMessageMapper.getBySessionId(sessionId);
    }

    /**
     * 标记消息为已读
     */
    @Override
    public void markAsRead(Long sessionId, Long receiverId) {
        serviceMessageMapper.markAsRead(sessionId, receiverId, LocalDateTime.now());
    }

    /**
     * 获取未读消息数量
     */
    @Override
    public Integer getUnreadCount(Long sessionId, Long receiverId) {
        return serviceMessageMapper.getUnreadCount(sessionId, receiverId);
    }

    /**
     * 根据AI上下文ID获取历史消息
     */
    @Override
    public List<ServiceMessage> getMessagesByAiContextId(String aiContextId) {
        return serviceMessageMapper.getByAiContextId(aiContextId);
    }

    // ========== 管理端新增方法实现 ==========

    /**
     * 客服端标记消息为已读
     */
    @Override
    public void markAsReadByService(Long sessionId) {
        // 临时实现：暂时跳过，避免编译错误
        log.info("标记会话 {} 的消息为已读（临时实现）", sessionId);
    }

    
    private String getRoleText(Integer role) {
        switch (role) {
            case 1:
                return "管理员";
            case 2:
                return "操作员";
            case 3:
                return "客服";
            default:
                return "未知";
        }
    }
} 