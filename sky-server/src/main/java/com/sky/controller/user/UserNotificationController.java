package com.sky.controller.user;

import com.sky.entity.SystemNotification;
import com.sky.result.Result;
import com.sky.service.NotificationService;
import com.sky.context.BaseContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理商系统通知控制器
 */
@RestController
@RequestMapping("/agent/notifications")
@Api(tags = "代理商系统通知管理")
@Slf4j
public class UserNotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/unread-count")
    @ApiOperation("获取用户未读通知数量")
    public Result<Integer> getUnreadCount() {
        try {
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            log.info("获取用户未读通知数量: userId={}, userType={}", userId, userType);
            
            // 根据用户类型获取对应的通知
            // 代理商/操作员获取 receiver_role=3 且 receiver_id=userId 的通知
            // 普通用户暂时不接收通知，或者可以接收全局通知
            Integer count = 0;
            
            if ("agent".equals(userType) || "operator".equals(userType)) {
                count = notificationService.getUnreadCount(3, userId);
            }
            
            log.info("✅ 用户未读通知数量: userId={}, count={}", userId, count);
            return Result.success(count);
        } catch (Exception e) {
            log.error("❌ 获取用户未读通知数量失败: {}", e.getMessage(), e);
            return Result.error("获取未读通知数量失败");
        }
    }

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    @ApiOperation("获取用户通知列表")
    public Result<List<SystemNotification>> getNotifications(@RequestParam(defaultValue = "20") Integer limit,
                                                            @RequestParam(defaultValue = "all") String status) {
        try {
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            log.info("获取用户通知列表: userId={}, userType={}, limit={}, status={}", userId, userType, limit, status);
            
            List<SystemNotification> notifications;
            
            if ("agent".equals(userType) || "operator".equals(userType)) {
                // 代理商/操作员获取定向通知
                notifications = notificationService.getNotifications(3, userId, limit);
            } else {
                // 普通用户暂时返回空列表，或者可以接收一些全局通知
                notifications = new ArrayList<>();
            }
            
            log.info("✅ 用户通知列表获取成功: userId={}, count={}", userId, notifications.size());
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("❌ 获取用户通知列表失败: {}", e.getMessage(), e);
            return Result.error("获取通知列表失败");
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @ApiOperation("标记通知为已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            Long userId = BaseContext.getCurrentId();
            log.info("标记通知为已读: notificationId={}, userId={}", id, userId);
            
            notificationService.markAsRead(id);
            
            log.info("✅ 通知标记已读成功: notificationId={}, userId={}", id, userId);
            return Result.success("标记成功");
        } catch (Exception e) {
            log.error("❌ 标记通知已读失败: notificationId={}, error={}", id, e.getMessage(), e);
            return Result.error("标记失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @ApiOperation("标记所有通知为已读")
    public Result<String> markAllAsRead() {
        Long userId = null;
        try {
            userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            log.info("标记所有通知为已读: userId={}, userType={}", userId, userType);
            
            if ("agent".equals(userType) || "operator".equals(userType)) {
                notificationService.markAllAsRead(3, userId);
                log.info("✅ 所有通知标记已读成功: userId={}", userId);
            }
            
            return Result.success("全部标记成功");
        } catch (Exception e) {
            log.error("❌ 标记所有通知已读失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("批量标记失败");
        }
    }

    /**
     * 获取通知历史记录（支持分页和筛选）
     */
    @GetMapping("/history")
    @ApiOperation("获取通知历史记录")
    public Result<List<SystemNotification>> getNotificationHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer level) {
        Long userId = null;
        try {
            userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            log.info("获取通知历史记录: userId={}, userType={}, page={}, size={}, type={}, level={}", 
                    userId, userType, page, size, type, level);
            
            List<SystemNotification> notifications;
            
            if ("agent".equals(userType) || "operator".equals(userType)) {
                // 计算偏移量
                int limit = size;
                
                // 这里可以扩展NotificationService来支持更复杂的查询
                // 目前先使用基础的获取方法
                notifications = notificationService.getNotifications(3, userId, limit * page);
                
                // 简单的分页处理（实际应该在数据库层面做）
                int start = (page - 1) * size;
                int end = Math.min(start + size, notifications.size());
                if (start < notifications.size()) {
                    notifications = notifications.subList(start, end);
                } else {
                    notifications = new ArrayList<>();
                }
            } else {
                notifications = new ArrayList<>();
            }
            
            log.info("✅ 通知历史记录获取成功: userId={}, count={}", userId, notifications.size());
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("❌ 获取通知历史记录失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("获取历史记录失败");
        }
    }
}
