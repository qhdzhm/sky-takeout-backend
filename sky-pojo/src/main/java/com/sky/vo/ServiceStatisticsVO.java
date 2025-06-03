package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 会话统计信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatisticsVO implements Serializable {

    // 总会话数
    private Integer totalSessions;
    
    // 活跃会话数
    private Integer activeSessions;
    
    // 平均服务时长（秒）
    private Integer avgServiceDuration;
    
    // 平均用户评分
    private Double avgUserRating;
    
    // 今日服务数量
    private Integer todayServiceCount;
    
    // 等待队列数量
    private Integer waitingQueueCount;
} 