package com.sky.vo;

import com.sky.entity.ServiceSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 客服工作台数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkbenchDataVO implements Serializable {

    // 活跃会话列表
    private List<ServiceSessionVO> activeSessions;
    
    // 等待队列列表
    private List<ServiceSessionVO> waitingQueue;
    
    // 统计信息
    private WorkbenchStatistics statistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkbenchStatistics implements Serializable {
        // 今日服务数量
        private Integer todayServiceCount;
        
        // 平均评分
        private Double avgRating;
        
        // 当前活跃会话数
        private Integer activeSessionCount;
        
        // 等待队列数量
        private Integer waitingQueueCount;
    }
} 