package com.sky.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 员工-导游管理DTO
 */
@Data
public class EmployeeGuideDTO {
    
    /**
     * 员工ID
     */
    private Long employeeId;
    
    /**
     * 导游ID
     */
    private Long guideId;
    
    /**
     * 语言能力
     */
    private String languages;
    
    /**
     * 经验年数
     */
    private Integer experienceYears;
    
    /**
     * 小时费率
     */
    private Double hourlyRate;
    
    /**
     * 日费率
     */
    private Double dailyRate;
    
    /**
     * 最大接团数
     */
    private Integer maxGroups;
    
    /**
     * 导游证号
     */
    private String licenseNumber;
    
    /**
     * 导游证到期日期
     */
    private LocalDate licenseExpiry;
    
    /**
     * 专业特长
     */
    private String specialties;
    
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    
    /**
     * 紧急联系电话
     */
    private String emergencyPhone;
    
    /**
     * 导游级别
     */
    private String guideLevel;
    
    /**
     * 是否激活
     */
    private Boolean isActive;
    
    /**
     * 分页查询参数
     */
    private Integer page;
    private Integer pageSize;
    
    /**
     * 查询条件
     */
    private String name;
    private String department;
    private Boolean isGuide;
    private String status;
} 