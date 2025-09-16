package com.sky.scheduler;

import com.sky.service.VehicleAvailabilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 车辆状态定时任务调度器
 * 负责定期检查和同步车辆过期状态到可用性表
 * 
 * @author System
 * @since 2025-01-06
 */
@Component
@Slf4j
public class VehicleStatusScheduler {

    @Autowired
    private VehicleAvailabilityService vehicleAvailabilityService;

    /**
     * 每天凌晨2点执行车辆过期状态同步
     * 检查rego和车检过期的车辆，自动更新可用性表状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncExpiredVehicleStatusDaily() {
        log.info("🚗 开始执行每日车辆过期状态同步任务 - {}", LocalDateTime.now());
        
        try {
            vehicleAvailabilityService.syncExpiredVehicleStatus();
            log.info("✅ 每日车辆过期状态同步任务执行完成 - {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("❌ 每日车辆过期状态同步任务执行失败 - {}", LocalDateTime.now(), e);
        }
    }

    /**
     * 每4小时执行一次车辆状态检查（白天时间）
     * 8:00, 12:00, 16:00, 20:00
     */
    @Scheduled(cron = "0 0 8,12,16,20 * * ?")
    public void syncExpiredVehicleStatusRegular() {
        log.info("🔄 开始执行定时车辆状态同步任务 - {}", LocalDateTime.now());
        
        try {
            vehicleAvailabilityService.syncExpiredVehicleStatus();
            log.info("✅ 定时车辆状态同步任务执行完成 - {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("❌ 定时车辆状态同步任务执行失败 - {}", LocalDateTime.now(), e);
        }
    }

    /**
     * 手动触发车辆状态同步（可通过管理接口调用）
     */
    public void manualSyncExpiredVehicleStatus() {
        log.info("🔧 手动触发车辆状态同步任务 - {}", LocalDateTime.now());
        
        try {
            vehicleAvailabilityService.syncExpiredVehicleStatus();
            log.info("✅ 手动车辆状态同步任务执行完成 - {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("❌ 手动车辆状态同步任务执行失败 - {}", LocalDateTime.now(), e);
            throw e;
        }
    }
}

