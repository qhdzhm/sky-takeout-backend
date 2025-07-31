package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.SendMessageDTO;
import com.sky.dto.TransferToServiceDTO;
import com.sky.entity.ServiceMessage;
import com.sky.entity.ServiceSession;
import com.sky.result.Result;
import com.sky.service.ServiceMessageService;
import com.sky.service.ServiceSessionService;
import com.sky.vo.ServiceSessionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端客服Controller
 */
@RestController
@RequestMapping("/user/service")
@Slf4j
@Api(tags = "用户客服相关接口")
public class ServiceController {

    @Autowired
    private ServiceSessionService serviceSessionService;

    @Autowired
    private ServiceMessageService serviceMessageService;

    /**
     * 转人工服务
     */
    @PostMapping("/transfer")
    @ApiOperation("转人工服务")
    public Result<ServiceSession> transferToService(@RequestBody TransferToServiceDTO transferToServiceDTO) {
        log.info("用户申请转人工服务：{}", transferToServiceDTO);
        
        ServiceSession session = serviceSessionService.transferToService(transferToServiceDTO);
        
        return Result.success(session);
    }

    /**
     * 获取用户的活跃会话
     */
    @GetMapping("/session/active")
    @ApiOperation("获取用户的活跃会话")
    public Result<ServiceSession> getActiveSession() {
        // 从JWT中获取用户ID
        Long userId = BaseContext.getCurrentId();
        
        if (userId == null) {
            log.warn("无法获取当前用户ID，使用默认用户ID 4 进行测试");
            userId = 4L; // 使用测试用户ID
        }
        
        log.info("获取用户 {} 的活跃会话", userId);
        ServiceSession session = serviceSessionService.getActiveSessionByUserId(userId);
        
        if (session != null) {
            log.info("找到活跃会话：ID={}, 状态={}", session.getId(), session.getSessionStatus());
        } else {
            log.info("用户 {} 没有活跃会话", userId);
        }
        
        return Result.success(session);
    }

    /**
     * 发送消息
     */
    @PostMapping("/message/send")
    @ApiOperation("发送消息")
    public Result<ServiceMessage> sendMessage(@RequestBody SendMessageDTO sendMessageDTO) {
        log.info("发送消息：{}", sendMessageDTO);
        
        ServiceMessage message = serviceMessageService.sendMessage(sendMessageDTO);
        
        return Result.success(message);
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/message/{sessionId}")
    @ApiOperation("获取会话消息列表")
    public Result<List<ServiceMessage>> getSessionMessages(@PathVariable Long sessionId) {
        List<ServiceMessage> messages = serviceMessageService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/message/read/{sessionId}")
    @ApiOperation("标记消息为已读")
    public Result<String> markAsRead(@PathVariable Long sessionId) {
        // 这里应该从JWT中获取用户ID，暂时使用固定值
        // Long userId = BaseContext.getCurrentId();
        // serviceMessageService.markAsRead(sessionId, userId);
        
        return Result.success();
    }

    /**
     * 结束会话
     */
    @PutMapping("/session/end/{sessionId}")
    @ApiOperation("结束会话")
    public Result<String> endSession(@PathVariable Long sessionId) {
        serviceSessionService.endSession(sessionId, 2); // 2-用户结束
        return Result.success();
    }

    /**
     * 评价会话
     */
    @PutMapping("/session/rate/{sessionId}")
    @ApiOperation("评价会话")
    public Result<String> rateSession(@PathVariable Long sessionId, 
                                     @RequestParam Integer rating, 
                                     @RequestParam(required = false) String comment) {
        serviceSessionService.rateSession(sessionId, rating, comment);
        return Result.success();
    }
} 