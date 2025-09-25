package com.sky.controller.agent;

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
 * 代理商聊天机器人控制器
 */
@RestController
@RequestMapping("/agent/chatbot")
@Api(tags = "代理商聊天机器人相关接口")
@Slf4j
public class AgentChatBotController {
    
    @Autowired
    private ChatBotService chatBotService;
    
    /**
     * 代理商发送消息
     */
    @PostMapping("/message")
    @ApiOperation("代理商发送ChatBot消息")
    public Result<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request,
                                          @RequestHeader(value = "X-Current-Page", required = false) String currentPage,
                                          @RequestHeader(value = "X-Current-URL", required = false) String currentUrl,
                                          @RequestHeader(value = "X-Request-Priority", required = false) String priority,
                                          @RequestHeader(value = "X-AI-Provider", required = false) String provider) {
        log.info("收到代理商聊天消息: sessionId={}, userType={}, message={}, currentPage={}", 
                request.getSessionId(), request.getUserType(), request.getMessage(), currentPage);
        
        // 从BaseContext获取代理商用户信息（由JwtTokenAgentInterceptor设置）
        Long currentUserId = BaseContext.getCurrentId();
        String currentUserType = BaseContext.getCurrentUserType();
        Long agentId = BaseContext.getCurrentAgentId();
        Long operatorId = BaseContext.getCurrentOperatorId();
        
        log.info("从JWT获取代理商信息: userId={}, userType={}, agentId={}, operatorId={}", 
                currentUserId, currentUserType, agentId, operatorId);
        
        // 设置代理商用户信息到请求中
        if (currentUserId != null) {
            request.setUserId(currentUserId.toString());
        }
        
        // 根据JWT中的用户类型设置正确的userType
        if ("agent".equals(currentUserType)) {
            request.setUserType(3); // 代理商主号
            log.info("✅ 识别为代理商主号，设置userType=3");
        } else if ("agent_operator".equals(currentUserType)) {
            request.setUserType(2); // 操作员
            log.info("✅ 识别为代理商操作员，设置userType=2");
        } else if ("operator".equals(currentUserType)) {
            request.setUserType(2); // 操作员
            log.info("✅ 识别为操作员，设置userType=2");
        } else {
            log.warn("⚠️ 未知的代理商用户类型: {}, 默认设置为代理商主号", currentUserType);
            request.setUserType(3); // 默认为代理商主号
        }
        
        log.info("✅ 代理商ChatBot请求处理: userId={}, userType={}, agentId={}", 
                request.getUserId(), request.getUserType(), agentId);
        
        // 设置当前页面信息到请求中
        request.setCurrentPage(currentPage);
        request.setCurrentUrl(currentUrl);
        
        // 日志信息
        if (priority != null) {
            log.debug("代理商AI请求优先级: {}", priority);
        }
        if (provider != null) {
            log.debug("代理商AI提供商: {}", provider);
        }
        
        // 调用通用的ChatBot服务
        ChatResponse response = chatBotService.processMessage(request);
        return Result.success(response);
    }
    
    /**
     * 获取代理商聊天历史
     */
    @GetMapping("/session/{sessionId}/history")
    @ApiOperation("获取代理商聊天历史")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        log.info("获取代理商聊天历史: sessionId={}", sessionId);
        
        // 验证代理商身份
        Long currentUserId = BaseContext.getCurrentId();
        String currentUserType = BaseContext.getCurrentUserType();
        
        log.info("代理商聊天历史查询: userId={}, userType={}", currentUserId, currentUserType);
        
        List<ChatMessage> history = chatBotService.getChatHistory(sessionId);
        return Result.success(history);
    }
    
    /**
     * 代理商ChatBot健康检查
     */
    @GetMapping("/health")
    @ApiOperation("代理商ChatBot健康检查")
    public Result<String> health() {
        return Result.success("代理商ChatBot服务运行正常");
    }
}
