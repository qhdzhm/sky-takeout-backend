package com.sky.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工-导游管理VO
 */
@Data
public class EmployeeGuideVO {
    
    /**
     * 员工信息
     */
    private Long employeeId;
    private String username;
    private String employeeName;
    private String phone;
    private String sex;
    private Integer employeeStatus;
    private Integer workStatus;
    private String department;
    private String guideLevel;
    private LocalDate hireDate;
    
    /**
     * 导游信息
     */
    private Long guideId;
    private String guideName;
    private String bio;
    private Integer experienceYears;
    private String languages;
    private String certification;
    private Double hourlyRate;
    private Double dailyRate;
    private Boolean guideActive;
    private Integer guideStatus;
    private Integer maxGroups;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private String specialties;
    private String emergencyContact;
    private String emergencyPhone;
    private LocalDateTime guideCreatedTime;
    private LocalDateTime guideUpdatedTime;
    
    /**
     * 是否为导游
     */
    private Boolean isGuide;
    
    /**
     * 可用性统计信息
     */
    private Integer currentGroups;
    private Integer remainingCapacity;
    private String availabilityStatus;
    private String availabilityNotes;
    
    /**
     * 综合状态
     */
    private String overallStatus;
    private String statusDescription;
} 