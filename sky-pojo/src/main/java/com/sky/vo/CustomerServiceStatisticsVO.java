package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 客服统计信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceStatisticsVO implements Serializable {

    // 总客服数
    private Integer totalServices;
    
    // 在线客服数
    private Integer onlineServices;
    
    // 活跃会话数
    private Integer activeSessions;
    
    // 等待队列数
    private Integer waitingQueue;
    
    // 今日服务总数
    private Integer todayServices;
    
    // 平均等待时长（秒）
    private Integer avgWaitTime;
    
    // 平均服务时长（秒）
    private Integer avgServiceTime;
    
    // 客户满意度
    private Double satisfaction;
} 