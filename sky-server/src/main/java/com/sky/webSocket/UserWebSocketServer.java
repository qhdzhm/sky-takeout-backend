package com.sky.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户端WebSocket服务器
 * 用于向用户端推送会话相关的实时消息
 */
@Component
@ServerEndpoint("/ws/service")
@Slf4j
public class UserWebSocketServer {

    // 存储用户ID -> WebSocket会话的映射（支持真实用户ID）
    private static Map<Long, Session> userConnections = new ConcurrentHashMap<>();
    
    // 存储临时用户ID -> WebSocket会话的映射（支持临时用户ID）
    private static Map<String, Session> tempUserConnections = new ConcurrentHashMap<>();
    
    // 存储会话ID -> 用户ID的映射，用于会话结束时找到对应用户
    private static Map<Long, Long> sessionUserMapping = new ConcurrentHashMap<>();
    
    // 存储会话ID -> 临时用户ID的映射，用于临时用户
    private static Map<Long, String> sessionTempUserMapping = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        try {
            // 从查询参数中获取userId和sessionId
            URI uri = session.getRequestURI();
            String query = uri.getQuery();
            
            String userIdStr = null;
            Long sessionId = null;
            
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        if ("userId".equals(keyValue[0])) {
                            userIdStr = keyValue[1];
                        } else if ("sessionId".equals(keyValue[0])) {
                            sessionId = Long.parseLong(keyValue[1]);
                        }
                    }
                }
            }
            
            if (userIdStr != null) {
                // 判断是临时用户ID还是真实用户ID
                if (userIdStr.startsWith("temp_")) {
                    // 临时用户ID
                    tempUserConnections.put(userIdStr, session);
                    if (sessionId != null) {
                        sessionTempUserMapping.put(sessionId, userIdStr);
                    }
                    log.info("✅ 临时用户 {} 连接到WebSocket，会话ID: {}, 当前在线临时用户数: {}", 
                            userIdStr, sessionId, tempUserConnections.size());
                } else {
                    // 真实用户ID
                    try {
                        Long userId = Long.parseLong(userIdStr);
                        userConnections.put(userId, session);
                        if (sessionId != null) {
                            sessionUserMapping.put(sessionId, userId);
                        }
                        log.info("✅ 用户 {} 连接到WebSocket，会话ID: {}, 当前在线用户数: {}", 
                                userId, sessionId, userConnections.size());
                    } catch (NumberFormatException e) {
                        log.error("❌ 无效的用户ID格式: {}", userIdStr);
                        session.close();
                        return;
                    }
                }
            } else {
                log.warn("⚠️ WebSocket连接缺少userId参数");
                session.close();
            }
        } catch (Exception e) {
            log.error("❌ 用户WebSocket连接处理失败: {}", e.getMessage(), e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("📥 收到用户消息: {}", message);
        // 可以在这里处理用户发送的消息
    }

    @OnClose
    public void onClose(Session session) {
        try {
            // 找到对应的用户ID并移除
            Long userIdToRemove = null;
            String tempUserIdToRemove = null;
            Long sessionIdToRemove = null;
            
            // 检查真实用户连接
            for (Map.Entry<Long, Session> entry : userConnections.entrySet()) {
                if (entry.getValue().equals(session)) {
                    userIdToRemove = entry.getKey();
                    break;
                }
            }
            
            // 检查临时用户连接
            if (userIdToRemove == null) {
                for (Map.Entry<String, Session> entry : tempUserConnections.entrySet()) {
                    if (entry.getValue().equals(session)) {
                        tempUserIdToRemove = entry.getKey();
                        break;
                    }
                }
            }
            
            if (userIdToRemove != null) {
                // 清理真实用户连接
                userConnections.remove(userIdToRemove);
                
                // 查找并移除对应的会话映射
                for (Map.Entry<Long, Long> entry : sessionUserMapping.entrySet()) {
                    if (entry.getValue().equals(userIdToRemove)) {
                        sessionIdToRemove = entry.getKey();
                        break;
                    }
                }
                
                if (sessionIdToRemove != null) {
                    sessionUserMapping.remove(sessionIdToRemove);
                }
                
                log.info("❌ 用户 {} 断开WebSocket连接，剩余在线用户数: {}", userIdToRemove, userConnections.size());
                
            } else if (tempUserIdToRemove != null) {
                // 清理临时用户连接
                tempUserConnections.remove(tempUserIdToRemove);
                
                // 查找并移除对应的会话映射
                for (Map.Entry<Long, String> entry : sessionTempUserMapping.entrySet()) {
                    if (entry.getValue().equals(tempUserIdToRemove)) {
                        sessionIdToRemove = entry.getKey();
                        break;
                    }
                }
                
                if (sessionIdToRemove != null) {
                    sessionTempUserMapping.remove(sessionIdToRemove);
                }
                
                log.info("❌ 临时用户 {} 断开WebSocket连接，剩余在线临时用户数: {}", tempUserIdToRemove, tempUserConnections.size());
            }
        } catch (Exception e) {
            log.error("❌ 用户WebSocket断开处理失败: {}", e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("❌ 用户WebSocket发生错误: {}", error.getMessage(), error);
    }

    /**
     * 向指定用户发送消息
     */
    public static void sendMessage(Long userId, String message) {
        try {
            Session session = userConnections.get(userId);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
                log.info("✅ 成功向用户 {} 发送消息", userId);
            } else {
                log.warn("⚠️ 用户 {} 未连接或连接已关闭", userId);
            }
        } catch (IOException e) {
            log.error("❌ 向用户 {} 发送消息失败: {}", userId, e.getMessage());
        }
    }

    /**
     * 向指定临时用户发送消息
     */
    public static void sendMessageToTempUser(String tempUserId, String message) {
        try {
            Session session = tempUserConnections.get(tempUserId);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
                log.info("✅ 成功向临时用户 {} 发送消息", tempUserId);
            } else {
                log.warn("⚠️ 临时用户 {} 未连接或连接已关闭", tempUserId);
            }
        } catch (IOException e) {
            log.error("❌ 向临时用户 {} 发送消息失败: {}", tempUserId, e.getMessage());
        }
    }

    /**
     * 通过会话ID发送消息（自动识别真实用户或临时用户）
     */
    public static void sendMessageBySessionId(Long sessionId, String message) {
        // 先尝试真实用户
        Long userId = sessionUserMapping.get(sessionId);
        if (userId != null) {
            sendMessage(userId, message);
            return;
        }
        
        // 再尝试临时用户
        String tempUserId = sessionTempUserMapping.get(sessionId);
        if (tempUserId != null) {
            sendMessageToTempUser(tempUserId, message);
            return;
        }
        
        log.warn("⚠️ 会话 {} 没有对应的用户连接", sessionId);
    }

    /**
     * 通知用户会话已结束
     */
    public static void notifySessionEnded(Long sessionId, String reason) {
        try {
            // 先尝试真实用户
            Long userId = sessionUserMapping.get(sessionId);
            if (userId != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("sessionId", sessionId);
                data.put("reason", reason);
                
                String message = createMessage("session_ended", "会话已结束", data);
                sendMessage(userId, message);
                
                // 清除真实用户映射
                sessionUserMapping.remove(sessionId);
                
                log.info("✅ 已通知真实用户 {} 会话 {} 结束，原因: {}", userId, sessionId, reason);
                return;
            }
            
            // 再尝试临时用户
            String tempUserId = sessionTempUserMapping.get(sessionId);
            if (tempUserId != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("sessionId", sessionId);
                data.put("reason", reason);
                
                String message = createMessage("session_ended", "会话已结束", data);
                sendMessageToTempUser(tempUserId, message);
                
                // 清除临时用户映射
                sessionTempUserMapping.remove(sessionId);
                
                log.info("✅ 已通知临时用户 {} 会话 {} 结束，原因: {}", tempUserId, sessionId, reason);
                return;
            }
            
            log.warn("⚠️ 会话 {} 没有对应的用户连接，无法发送结束通知", sessionId);
        } catch (Exception e) {
            log.error("❌ 通知用户会话结束失败: {}", e.getMessage());
        }
    }

    /**
     * 创建WebSocket消息
     */
    public static String createMessage(String type, String message, Map<String, Object> data) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("type", type);
        messageMap.put("message", message);
        messageMap.put("data", data);
        messageMap.put("timestamp", System.currentTimeMillis());
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(messageMap);
        } catch (Exception e) {
            log.error("❌ 创建WebSocket消息失败: {}", e.getMessage());
            return "{\"type\":\"error\",\"message\":\"消息格式错误\"}";
        }
    }

    /**
     * 向指定用户发送客服消息
     */
    public static void sendServiceMessage(Long userId, String content, Long sessionId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("content", content);
            data.put("senderType", 2); // 客服
            
            String message = createMessage("service_message", "收到客服消息", data);
            
            // 优先通过会话ID发送消息（支持临时用户）
            sendMessageBySessionId(sessionId, message);
            
            log.info("✅ 已向会话 {} 推送客服消息", sessionId);
        } catch (Exception e) {
            log.error("❌ 推送客服消息失败: {}", e.getMessage());
        }
    }

    /**
     * 将临时用户连接升级为真实用户连接（用户登录时调用）
     */
    public static void upgradeToRealUser(String tempUserId, Long realUserId) {
        try {
            Session session = tempUserConnections.get(tempUserId);
            if (session != null && session.isOpen()) {
                // 移除临时用户连接
                tempUserConnections.remove(tempUserId);
                
                // 添加到真实用户连接
                userConnections.put(realUserId, session);
                
                // 更新会话映射
                Long sessionId = null;
                for (Map.Entry<Long, String> entry : sessionTempUserMapping.entrySet()) {
                    if (entry.getValue().equals(tempUserId)) {
                        sessionId = entry.getKey();
                        break;
                    }
                }
                
                if (sessionId != null) {
                    sessionTempUserMapping.remove(sessionId);
                    sessionUserMapping.put(sessionId, realUserId);
                }
                
                log.info("✅ 临时用户 {} 升级为真实用户 {}，会话ID: {}", tempUserId, realUserId, sessionId);
            } else {
                log.warn("⚠️ 临时用户 {} 的连接不存在或已关闭", tempUserId);
            }
        } catch (Exception e) {
            log.error("❌ 升级用户连接失败: {}", e.getMessage());
        }
    }

    /**
     * 获取在线用户数量（包括临时用户）
     */
    public static int getOnlineUserCount() {
        return userConnections.size() + tempUserConnections.size();
    }

    /**
     * 检查用户是否在线（支持临时用户）
     */
    public static boolean isUserOnline(Long userId) {
        Session session = userConnections.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 检查临时用户是否在线
     */
    public static boolean isTempUserOnline(String tempUserId) {
        Session session = tempUserConnections.get(tempUserId);
        return session != null && session.isOpen();
    }

    /**
     * 更新真实用户的会话映射
     */
    public static void updateSessionMapping(Long userId, Long newSessionId) {
        try {
            // 清除该用户的旧会话映射
            sessionUserMapping.entrySet().removeIf(entry -> entry.getValue().equals(userId));
            
            // 添加新的会话映射
            sessionUserMapping.put(newSessionId, userId);
            
            log.info("✅ 已更新真实用户 {} 的会话映射到会话 {}", userId, newSessionId);
        } catch (Exception e) {
            log.error("❌ 更新真实用户会话映射失败: {}", e.getMessage());
        }
    }

    /**
     * 通过用户ID更新会话映射（处理临时用户和真实用户）
     */
    public static void updateSessionMappingByUserId(Long userId, Long newSessionId) {
        try {
            boolean updated = false;
            
            // 先尝试更新真实用户映射
            Long oldSessionId = null;
            for (Map.Entry<Long, Long> entry : sessionUserMapping.entrySet()) {
                if (entry.getValue().equals(userId)) {
                    oldSessionId = entry.getKey();
                    break;
                }
            }
            
            if (oldSessionId != null) {
                sessionUserMapping.remove(oldSessionId);
                sessionUserMapping.put(newSessionId, userId);
                updated = true;
                log.info("✅ 已更新真实用户 {} 的会话映射: {} -> {}", userId, oldSessionId, newSessionId);
            }
            
            // 再尝试更新临时用户映射（通过查找可能的临时用户ID）
            String tempUserId = null;
            Long oldTempSessionId = null;
            for (Map.Entry<Long, String> entry : sessionTempUserMapping.entrySet()) {
                String tempId = entry.getValue();
                // 检查这个临时用户是否对应当前用户（这里可能需要额外的逻辑来关联）
                if (tempId.contains(String.valueOf(userId)) || isCurrentUserTempId(tempId, userId)) {
                    tempUserId = tempId;
                    oldTempSessionId = entry.getKey();
                    break;
                }
            }
            
            if (tempUserId != null && oldTempSessionId != null) {
                sessionTempUserMapping.remove(oldTempSessionId);
                sessionTempUserMapping.put(newSessionId, tempUserId);
                updated = true;
                log.info("✅ 已更新临时用户 {} 的会话映射: {} -> {}", tempUserId, oldTempSessionId, newSessionId);
            }
            
            if (!updated) {
                log.warn("⚠️ 未找到用户 {} 的现有WebSocket连接映射", userId);
            }
        } catch (Exception e) {
            log.error("❌ 更新用户会话映射失败: {}", e.getMessage());
        }
    }

    /**
     * 检查临时用户ID是否属于当前用户（简单实现）
     */
    private static boolean isCurrentUserTempId(String tempUserId, Long userId) {
        // 简单的检查逻辑，可以根据实际需求改进
        // 这里假设临时用户在最近时间内创建，且与当前用户相关联
        return tempUserConnections.containsKey(tempUserId);
    }
} 