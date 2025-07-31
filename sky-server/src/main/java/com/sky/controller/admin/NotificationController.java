package com.sky.controller.admin;

import com.sky.entity.SystemNotification;
import com.sky.result.Result;
import com.sky.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统通知控制器
 */
@RestController
@RequestMapping("/admin/notifications")
@Api(tags = "系统通知管理")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @ApiOperation("获取未读通知数量")
    public Result<Integer> getUnreadCount() {
        // 假设当前是管理员角色
        Integer count = notificationService.getUnreadCount(1, null);
        return Result.success(count);
    }

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    @ApiOperation("获取通知列表")
    public Result<List<SystemNotification>> getNotifications(@RequestParam(defaultValue = "20") Integer limit) {
        // 假设当前是管理员角色
        List<SystemNotification> notifications = notificationService.getNotifications(1, null, limit);
        return Result.success(notifications);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @ApiOperation("标记通知为已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @ApiOperation("标记所有通知为已读")
    public Result<String> markAllAsRead() {
        // 假设当前是管理员角色
        notificationService.markAllAsRead(1, null);
        return Result.success();
    }

    /**
     * 测试创建通知
     */
    @PostMapping("/test")
    @ApiOperation("测试创建通知")
    public Result<String> createTestNotification() {
        // 创建测试订单通知
        notificationService.createOrderNotification(12345L, "张三", 299.50);
        
        // 创建测试聊天请求通知
        notificationService.createChatRequestNotification(67890L, "李四", "咨询旅游路线");
        
        // 创建测试订单修改通知
        notificationService.createOrderModifyNotification(12345L, "张三", "取消");
        
        return Result.success("测试通知创建成功");
    }
} 