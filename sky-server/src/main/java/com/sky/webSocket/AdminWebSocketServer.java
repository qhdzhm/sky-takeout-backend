package com.sky.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Collections;

/**
 * 管理端WebSocket服务器
 * 用于客服工作台的实时通信
 */
@Component
@ServerEndpoint("/ws/admin/{serviceId}")
@Slf4j
public class AdminWebSocketServer {

    // 存储客服连接，key为客服ID，value为WebSocket连接
    private static Map<Long, Session> serviceConnections = new ConcurrentHashMap<>();
    
    // 存储会话ID到客服ID的映射
    private static Map<Long, Long> sessionServiceMapping = new ConcurrentHashMap<>();
    
    // 简化的ObjectMapper，不需要处理LocalDateTime
    private static ObjectMapper objectMapper = new ObjectMapper();

    // 静态注入EmployeeMapper用于更新在线状态
    private static EmployeeMapper employeeMapper;

    @Autowired
    public void setEmployeeMapper(EmployeeMapper employeeMapper) {
        AdminWebSocketServer.employeeMapper = employeeMapper;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("serviceId") Long serviceId) {
        try {
            serviceConnections.put(serviceId, session);
            log.info("✅ 客服 {} 成功连接到管理端WebSocket，当前在线客服数: {}", serviceId, serviceConnections.size());
            
            // 🔧 新增：更新数据库中的在线状态
            if (employeeMapper != null) {
                try {
                    employeeMapper.updateCustomerServiceOnlineStatus(serviceId, 1);
                    log.info("✅ 已更新客服 {} 的数据库在线状态为：上线", serviceId);
                } catch (Exception e) {
                    log.error("❌ 更新客服 {} 在线状态失败：{}", serviceId, e.getMessage());
                }
            } else {
                log.warn("⚠️ EmployeeMapper未注入，无法更新在线状态");
            }
            
            // 发送连接成功消息
            sendMessage(serviceId, createMessage("connected", "连接成功", null));
            
            // 输出连接映射情况用于调试
            log.info("🔍 当前WebSocket连接映射: {}", serviceConnections.keySet());
            
        } catch (Exception e) {
            log.error("❌ 客服连接建立失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("serviceId") Long serviceId) {
        try {
            serviceConnections.remove(serviceId);
            log.info("❌ 客服 {} 断开管理端WebSocket连接，剩余在线客服数: {}", serviceId, serviceConnections.size());
            
            // 🔧 新增：更新数据库中的在线状态
            if (employeeMapper != null) {
                try {
                    employeeMapper.updateCustomerServiceOnlineStatus(serviceId, 0);
                    log.info("✅ 已更新客服 {} 的数据库在线状态为：离线", serviceId);
                } catch (Exception e) {
                    log.error("❌ 更新客服 {} 离线状态失败：{}", serviceId, e.getMessage());
                }
            } else {
                log.warn("⚠️ EmployeeMapper未注入，无法更新离线状态");
            }
            
            // 清理相关的会话映射
            sessionServiceMapping.entrySet().removeIf(entry -> entry.getValue().equals(serviceId));
            
        } catch (Exception e) {
            log.error("❌ 客服连接关闭处理失败：{}", e.getMessage());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("serviceId") Long serviceId) {
        try {
            log.info("📨 收到客服 {} 消息：{}", serviceId, message);
            
            // 解析消息并处理
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
            String type = (String) messageData.get("type");
            
            // 根据消息类型处理
            switch (type) {
                case "ping":
                    // 心跳响应
                    sendMessage(serviceId, createMessage("pong", "心跳响应", null));
                    break;
                default:
                    log.warn("⚠️ 未知消息类型：{}", type);
                    break;
            }
            
        } catch (Exception e) {
            log.error("❌ 处理客服消息失败：{}", e.getMessage());
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("serviceId") Long serviceId) {
        log.error("❌ 客服 {} WebSocket发生错误：{}", serviceId, error.getMessage(), error);
    }

    /**
     * 向指定客服发送消息
     */
    public static void sendMessage(Long serviceId, String message) {
        try {
            Session session = serviceConnections.get(serviceId);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
                log.info("✅ 成功向客服 {} 发送消息", serviceId);
            } else {
                log.warn("⚠️ 客服 {} 连接不存在或已关闭，无法发送消息", serviceId);
            }
        } catch (IOException e) {
            log.error("❌ 向客服 {} 发送消息失败：{}", serviceId, e.getMessage());
        }
    }

    /**
     * 向所有在线客服广播消息
     */
    public static void broadcastToAllServices(String message) {
        serviceConnections.forEach((serviceId, session) -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                log.error("❌ 向客服 {} 广播消息失败：{}", serviceId, e.getMessage());
            }
        });
    }

    /**
     * 通知新会话分配
     */
    public static void notifyNewSession(Long sessionId, Map<String, Object> sessionData) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("session", sessionData);
            
            String message = createMessage("new_session", "新会话分配", data);
            
            // 广播给所有在线客服
            broadcastToAllServices(message);
            
        } catch (Exception e) {
            log.error("❌ 通知新会话失败：{}", e.getMessage());
        }
    }

    /**
     * 通知会话消息
     */
    public static void notifySessionMessage(Long sessionId, Map<String, Object> messageData) {
        try {
            Long serviceId = sessionServiceMapping.get(sessionId);
            log.info("🔍 尝试向会话 {} 的客服推送消息，映射的客服ID: {}", sessionId, serviceId);
            log.info("🔍 当前会话映射: {}", sessionServiceMapping);
            log.info("🔍 当前连接映射: {}", serviceConnections.keySet());
            
            if (serviceId != null) {
                Session session = serviceConnections.get(serviceId);
                log.info("🔍 客服 {} 的WebSocket连接状态: {}", serviceId, 
                    session != null ? (session.isOpen() ? "已连接" : "连接已关闭") : "未连接");
                
                Map<String, Object> data = new HashMap<>();
                data.put("sessionId", sessionId);
                data.put("messageId", messageData.get("id"));
                data.put("content", messageData.get("content"));
                data.put("senderType", messageData.get("senderType"));
                // 转换LocalDateTime为字符串
                Object createTime = messageData.get("createTime");
                data.put("createTime", createTime != null ? createTime.toString() : null);
                
                String message = createMessage("session_message", "新消息", data);
                
                sendMessage(serviceId, message);
                log.info("✅ 已向客服 {} 推送会话 {} 的消息", serviceId, sessionId);
            } else {
                log.warn("⚠️ 会话 {} 没有分配的客服，无法推送消息", sessionId);
            }
            
        } catch (Exception e) {
            log.error("❌ 通知会话消息失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 通知会话结束
     */
    public static void notifySessionEnded(Long sessionId, Long serviceId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            
            String message = createMessage("session_ended", "用户已结束会话", data);
            
            if (serviceId != null) {
                sendMessage(serviceId, message);
                log.info("✅ 已通知客服 {} 会话 {} 结束", serviceId, sessionId);
            } else {
                log.warn("⚠️ 会话 {} 没有分配的客服，无法发送结束通知", sessionId);
            }
            
            // 清除映射
            sessionServiceMapping.remove(sessionId);
            
        } catch (Exception e) {
            log.error("❌ 通知会话结束失败：{}", e.getMessage());
        }
    }

    /**
     * 绑定会话和客服的关系
     */
    public static void bindSessionToService(Long sessionId, Long serviceId) {
        sessionServiceMapping.put(sessionId, serviceId);
        log.info("🔗 会话 {} 绑定到客服 {}，当前映射数: {}", sessionId, serviceId, sessionServiceMapping.size());
        log.info("🔍 完整映射表: {}", sessionServiceMapping);
    }

    /**
     * 获取在线客服数量
     */
    public static int getOnlineServiceCount() {
        return serviceConnections.size();
    }

    /**
     * 检查客服是否在线
     */
    public static boolean isServiceOnline(Long serviceId) {
        Session session = serviceConnections.get(serviceId);
        return session != null && session.isOpen();
    }

    /**
     * 创建消息
     */
    private static String createMessage(String type, String content, Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("content", content);
            message.put("data", data != null ? data : Collections.emptyMap());
            message.put("timestamp", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("创建消息失败：{}", e.getMessage());
            return "{\"type\":\"error\",\"content\":\"消息创建失败\"}";
        }
    }

    /**
     * 创建通知消息
     */
    public static String createNotificationMessage(String type, String title, Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("title", title);
            message.put("data", data != null ? data : Collections.emptyMap());
            message.put("timestamp", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("创建通知消息失败：{}", e.getMessage());
            return "{\"type\":\"error\",\"content\":\"通知消息创建失败\"}";
        }
    }
} 