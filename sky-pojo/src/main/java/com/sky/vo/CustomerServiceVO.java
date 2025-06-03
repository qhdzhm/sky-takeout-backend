package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceVO implements Serializable {

    private Long employeeId;
    private String username;
    private String name;
    private String phone;
    private String sex;
    private String serviceNo;
    private Integer onlineStatus;
    private Integer maxConcurrentCustomers;
    private Integer currentCustomerCount;
    private String skillTags;
    private Integer serviceLevel;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastActiveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 统计信息
    private Integer totalSessions;
    private Integer activeSessions;
    private Double avgRating;
    private Integer totalServiceDuration;
    private Double avgServiceDuration;
} 