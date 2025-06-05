package com.sky.controller;

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
                                          @RequestHeader(value = "X-Current-URL", required = false) String currentUrl) {
        log.info("收到聊天消息: sessionId={}, userType={}, message={}, currentPage={}", 
                request.getSessionId(), request.getUserType(), request.getMessage(), currentPage);
        
        // 设置当前页面信息到请求中
        request.setCurrentPage(currentPage);
        request.setCurrentUrl(currentUrl);
        
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