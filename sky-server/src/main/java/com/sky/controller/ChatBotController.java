package com.sky.controller;

import com.sky.context.BaseContext;
import com.sky.dto.ChatRequest;
import com.sky.entity.ChatMessage;
import com.sky.result.Result;
import com.sky.service.ChatBotService;
import com.sky.vo.ChatResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 聊天机器人控制器
 */
@RestController
@RequestMapping("/chatbot")
@Api(tags = "聊天机器人相关接口")
@Slf4j
public class ChatBotController {
    
    @Autowired
    private ChatBotService chatBotService;
    
    /**
     * 发送消息
     */
    @PostMapping("/message")
    @ApiOperation("发送消息")
    public Result<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request,
                                          @RequestHeader(value = "X-Current-Page", required = false) String currentPage,
                                          @RequestHeader(value = "X-Current-URL", required = false) String currentUrl,
                                          @RequestHeader(value = "X-Request-Priority", required = false) String priority,
                                          @RequestHeader(value = "X-AI-Provider", required = false) String provider) {
        log.info("收到聊天消息: sessionId={}, userType={}, message={}, currentPage={}", 
                request.getSessionId(), request.getUserType(), request.getMessage(), currentPage);
        
        // 从BaseContext获取当前用户信息（由JWT拦截器设置）
        try {
            Long currentUserId = BaseContext.getCurrentId();
            String currentUserType = BaseContext.getCurrentUserType();
            Long agentId = BaseContext.getCurrentAgentId();
            Long operatorId = BaseContext.getCurrentOperatorId();
            
            log.info("从JWT获取用户信息: userId={}, userType={}, agentId={}, operatorId={}", 
                    currentUserId, currentUserType, agentId, operatorId);
            
            // 设置真实的用户信息到请求中
            if (currentUserId != null) {
                request.setUserId(currentUserId.toString());
            }
            
            // 根据JWT中的用户类型设置正确的userType
            if ("agent".equals(currentUserType)) {
                request.setUserType(3); // 代理商主号
            } else if ("agent_operator".equals(currentUserType)) {
                request.setUserType(2); // 操作员
            } else if ("user".equals(currentUserType)) {
                request.setUserType(1); // 普通用户
            }
            
            log.info("设置后的请求信息: userId={}, userType={}", request.getUserId(), request.getUserType());
            
        } catch (Exception e) {
            log.warn("无法从JWT获取用户信息，使用前端传递的信息: {}", e.getMessage());
            // 如果无法从JWT获取用户信息，使用前端传递的信息（可能是游客用户）
        }
        
        // 设置当前页面信息到请求中
        request.setCurrentPage(currentPage);
        request.setCurrentUrl(currentUrl);
        // 仅用于日志分析
        if (priority != null) {
            log.debug("AI请求优先级: {}", priority);
        }
        if (provider != null) {
            log.debug("AI提供商: {}", provider);
        }
        
        ChatResponse response = chatBotService.processMessage(request);
        return Result.success(response);
    }
    
    /**
     * 获取聊天历史
     */
    @GetMapping("/session/{sessionId}/history")
    @ApiOperation("获取聊天历史")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        log.info("获取聊天历史: sessionId={}", sessionId);
        
        List<ChatMessage> history = chatBotService.getChatHistory(sessionId);
        return Result.success(history);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<String> health() {
        return Result.success("ChatBot服务运行正常");
    }
} 