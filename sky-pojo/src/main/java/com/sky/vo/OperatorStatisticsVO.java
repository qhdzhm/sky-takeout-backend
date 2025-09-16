package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作员统计信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorStatisticsVO {
    
    private Long employeeId;
    private String employeeName;
    private String operatorType;
    private String operatorTypeDesc;
    private Boolean isTourMaster;
    private Boolean canAssignOrders;
    private Integer workStatus;
    private String workStatusDesc;
    
    // 统计数据
    private Integer assignedOrderCount;      // 分配的订单数
    private Integer completedOrderCount;     // 完成的订单数
    private Integer pendingOrderCount;       // 待处理订单数
    private Double completionRate;           // 完成率
    
    // 最近活动
    private LocalDateTime lastAssignedTime;  // 最后分配时间
    private LocalDateTime lastCompletedTime; // 最后完成时间
    
    // 工作负荷
    private String workloadLevel;            // 工作负荷等级：light/medium/heavy
    private Integer maxConcurrentOrders;     // 最大并发处理订单数
    
    // 状态信息
    private Boolean isOnline;                // 是否在线
    private LocalDateTime lastActiveTime;    // 最后活跃时间
}

