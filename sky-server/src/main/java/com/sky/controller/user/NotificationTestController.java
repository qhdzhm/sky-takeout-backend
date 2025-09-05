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

import java.time.LocalDateTime;

/**
 * 用户端通知测试控制器
 * 仅用于开发和测试阶段
 */
@RestController
@RequestMapping("/user/notifications/test")
@Api(tags = "用户端通知测试")
@Slf4j
public class NotificationTestController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 创建测试通知（仅开发环境）
     */
    @PostMapping("/create")
    @ApiOperation("创建测试通知")
    public Result<String> createTestNotification(@RequestParam(defaultValue = "订单状态通知") String title,
                                                @RequestParam(defaultValue = "这是一条测试通知") String content,
                                                @RequestParam(defaultValue = "2") Integer level) {
        try {
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            if (!"agent".equals(userType) && !"operator".equals(userType)) {
                return Result.error("仅支持代理商和操作员测试");
            }
            
            log.info("创建测试通知: userId={}, userType={}, title={}", userId, userType, title);
            
            SystemNotification notification = SystemNotification.builder()
                    .type(31) // 代理订单变更通知
                    .title(title)
                    .content(content)
                    .icon("🧪")
                    .relatedId(999L) // 测试订单ID
                    .relatedType("test_order")
                    .level(level)
                    .isRead(0)
                    .receiverRole(3) // 特定用户
                    .receiverId(userId)
                    .createTime(LocalDateTime.now())
                    .expireTime(LocalDateTime.now().plusDays(7))
                    .build();
            
            notificationService.createCustomNotification(notification);
            
            log.info("✅ 测试通知创建成功: userId={}, notificationId={}", userId, notification.getId());
            return Result.success("测试通知创建成功！请查看通知中心");
        } catch (Exception e) {
            log.error("❌ 创建测试通知失败: {}", e.getMessage(), e);
            return Result.error("创建测试通知失败");
        }
    }

    /**
     * 批量创建测试通知
     */
    @PostMapping("/batch-create")
    @ApiOperation("批量创建测试通知")
    public Result<String> batchCreateTestNotifications(@RequestParam(defaultValue = "5") Integer count) {
        try {
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            if (!"agent".equals(userType) && !"operator".equals(userType)) {
                return Result.error("仅支持代理商和操作员测试");
            }
            
            log.info("批量创建测试通知: userId={}, userType={}, count={}", userId, userType, count);
            
            String[] titles = {
                "新订单通知", "订单确认通知", "支付成功通知", "订单取消通知", "系统维护通知"
            };
            
            String[] contents = {
                "您有一个新的旅游订单，订单号：HT20250101001",
                "您的订单已被管理员确认，请及时安排付款",
                "订单支付成功，感谢您的信任！",
                "订单已被客户取消，请注意处理",
                "系统将于今晚进行维护，请提前做好准备"
            };
            
            Integer[] levels = {2, 2, 1, 2, 3};
            String[] icons = {"💰", "✅", "🎉", "❌", "🔧"};
            
            for (int i = 0; i < Math.min(count, titles.length); i++) {
                SystemNotification notification = SystemNotification.builder()
                        .type(31)
                        .title(titles[i])
                        .content(contents[i])
                        .icon(icons[i])
                        .relatedId((long) (1000 + i))
                        .relatedType("test_order")
                        .level(levels[i])
                        .isRead(0)
                        .receiverRole(3)
                        .receiverId(userId)
                        .createTime(LocalDateTime.now().minusMinutes(i * 5)) // 错开时间
                        .expireTime(LocalDateTime.now().plusDays(7))
                        .build();
                
                notificationService.createCustomNotification(notification);
                
                // 避免过快创建导致时间戳相同
                Thread.sleep(100);
            }
            
            log.info("✅ 批量测试通知创建成功: userId={}, count={}", userId, count);
            return Result.success(String.format("成功创建 %d 条测试通知！", count));
        } catch (Exception e) {
            log.error("❌ 批量创建测试通知失败: {}", e.getMessage(), e);
            return Result.error("批量创建测试通知失败");
        }
    }

    /**
     * 清除测试通知
     */
    @DeleteMapping("/clear")
    @ApiOperation("清除测试通知")
    public Result<String> clearTestNotifications() {
        Long userId = null;
        try {
            userId = BaseContext.getCurrentId();
            log.info("清除测试通知: userId={}", userId);
            
            // 注意：这里实际上需要在NotificationService中添加删除方法
            // 目前只是标记为已读
            notificationService.markAllAsRead(3, userId);
            
            log.info("✅ 测试通知清理完成: userId={}", userId);
            return Result.success("测试通知已清理（标记为已读）");
        } catch (Exception e) {
            log.error("❌ 清除测试通知失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("清除测试通知失败");
        }
    }
}
