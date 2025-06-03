package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.SendMessageDTO;
import com.sky.entity.ServiceMessage;
import com.sky.entity.ServiceSession;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ServiceMessageService;
import com.sky.service.ServiceSessionService;
import com.sky.vo.ServiceSessionVO;
import com.sky.vo.ServiceStatisticsVO;
import com.sky.vo.WorkbenchDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端会话Controller
 */
@RestController
@RequestMapping("/admin/service-sessions")
@Slf4j
@Api(tags = "管理端会话相关接口")
public class ServiceSessionController {

    @Autowired
    private ServiceSessionService serviceSessionService;

    @Autowired
    private ServiceMessageService serviceMessageService;

    /**
     * 分页查询会话列表
     */
    @GetMapping
    @ApiOperation("分页查询会话列表")
    public Result<PageResult> getSessionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {
        
        log.info("分页查询会话列表：page={}, pageSize={}, status={}, startDate={}, endDate={}, keyword={}", 
                page, pageSize, status, startDate, endDate, keyword);
        
        PageResult pageResult = serviceSessionService.getSessionList(page, pageSize, status, startDate, endDate, keyword);
        return Result.success(pageResult);
    }

    /**
     * 获取等待队列
     */
    @GetMapping("/waiting-queue")
    @ApiOperation("获取等待队列")
    public Result<List<ServiceSession>> getWaitingQueue() {
        List<ServiceSession> sessions = serviceSessionService.getWaitingQueue();
        return Result.success(sessions);
    }

    /**
     * 获取客服工作台数据
     */
    @GetMapping("/workbench/{serviceId}")
    @ApiOperation("获取客服工作台数据")
    public Result<WorkbenchDataVO> getWorkbenchData(@PathVariable Long serviceId) {
        WorkbenchDataVO workbenchData = serviceSessionService.getWorkbenchData(serviceId);
        return Result.success(workbenchData);
    }

    /**
     * 接受会话
     */
    @PutMapping("/{sessionId}/accept")
    @ApiOperation("接受会话")
    public Result<String> acceptSession(@PathVariable Long sessionId) {
        // 从JWT中获取客服ID
        Long serviceId = BaseContext.getCurrentId();
        
        if (serviceId == null) {
            log.warn("无法获取当前客服ID，使用默认ID进行测试");
            serviceId = 18L; // 使用你在日志中看到的管理员ID
        }
        
        log.info("客服 {} 接受会话 {}", serviceId, sessionId);
        serviceSessionService.acceptSession(sessionId, serviceId);
        return Result.success();
    }

    /**
     * 分配会话给客服
     */
    @PutMapping("/{sessionId}/assign")
    @ApiOperation("分配会话给客服")
    public Result<String> assignSession(@PathVariable Long sessionId, @RequestBody AssignSessionDTO assignSessionDTO) {
        serviceSessionService.assignSession(sessionId, assignSessionDTO.getServiceId());
        return Result.success();
    }

    /**
     * 强制结束会话
     */
    @PutMapping("/{sessionId}/end")
    @ApiOperation("强制结束会话")
    public Result<String> endSession(@PathVariable Long sessionId, @RequestBody EndSessionDTO endSessionDTO) {
        serviceSessionService.endSession(sessionId, 5, endSessionDTO.getReason()); // 5-管理员强制结束
        return Result.success();
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{sessionId}")
    @ApiOperation("获取会话详情")
    public Result<ServiceSessionVO> getSessionDetail(@PathVariable Long sessionId) {
        ServiceSessionVO sessionVO = serviceSessionService.getSessionDetail(sessionId);
        return Result.success(sessionVO);
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/{sessionId}/messages")
    @ApiOperation("获取会话消息")
    public Result<List<ServiceMessage>> getSessionMessages(@PathVariable Long sessionId) {
        List<ServiceMessage> messages = serviceMessageService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    /**
     * 发送消息（客服端）
     */
    @PostMapping("/message/send")
    @ApiOperation("发送消息")
    public Result<ServiceMessage> sendMessage(@RequestBody SendMessageDTO sendMessageDTO) {
        log.info("客服发送消息：{}", sendMessageDTO);
        
        // 设置发送者类型为客服
        sendMessageDTO.setSenderType(2);
        ServiceMessage message = serviceMessageService.sendMessage(sendMessageDTO);
        
        return Result.success(message);
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/{sessionId}/read")
    @ApiOperation("标记消息已读")
    public Result<String> markMessagesRead(@PathVariable Long sessionId) {
        // 这里应该从JWT中获取客服ID，暂时使用固定值
        // Long serviceId = BaseContext.getCurrentId();
        serviceMessageService.markAsReadByService(sessionId);
        return Result.success();
    }

    /**
     * 获取客服的活跃会话
     */
    @GetMapping("/service/{serviceId}/active")
    @ApiOperation("获取客服的活跃会话")
    public Result<List<ServiceSession>> getActiveSessionsByService(@PathVariable Long serviceId) {
        List<ServiceSession> sessions = serviceSessionService.getActiveSessionsByService(serviceId);
        return Result.success(sessions);
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取会话统计信息")
    public Result<ServiceStatisticsVO> getStatistics() {
        ServiceStatisticsVO statistics = serviceSessionService.getStatistics();
        return Result.success(statistics);
    }

    // DTO 类定义
    public static class AssignSessionDTO {
        private Long serviceId;
        
        public Long getServiceId() {
            return serviceId;
        }
        
        public void setServiceId(Long serviceId) {
            this.serviceId = serviceId;
        }
    }

    public static class EndSessionDTO {
        private String reason;
        private String endType;
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public String getEndType() {
            return endType;
        }
        
        public void setEndType(String endType) {
            this.endType = endType;
        }
    }
} 