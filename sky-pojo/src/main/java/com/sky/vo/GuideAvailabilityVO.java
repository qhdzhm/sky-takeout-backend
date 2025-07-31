package com.sky.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 导游可用性VO
 */
@Data
public class GuideAvailabilityVO {
    
    private Long guideId;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 导游姓名
     */
    private String guideName;
    
    /**
     * 语言能力
     */
    private String languages;
    
    /**
     * 经验年数
     */
    private Integer experienceYears;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 日费率
     */
    private Double dailyRate;
    
    /**
     * 小时费率
     */
    private Double hourlyRate;
    
    /**
     * 可用开始时间
     */
    private LocalTime availableStartTime;
    
    /**
     * 可用结束时间
     */
    private LocalTime availableEndTime;
    
    /**
     * 最大接团数
     */
    private Integer maxGroups;
    
    /**
     * 当前已分配团数
     */
    private Integer currentGroups;
    
    /**
     * 可用状态
     */
    private String status;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 是否推荐
     */
    private Boolean recommended;
    
    /**
     * 推荐原因
     */
    private String recommendReason;
    
    /**
     * 剩余容量
     */
    private Integer remainingCapacity;
} 