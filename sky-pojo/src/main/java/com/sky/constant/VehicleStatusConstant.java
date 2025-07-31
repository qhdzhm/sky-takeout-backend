package com.sky.constant;

/**
 * 车辆状态常量
 */
public class VehicleStatusConstant {
    
    // 基础状态
    public static final Integer DISABLED = 0;    // 送修中
    public static final Integer AVAILABLE = 1;   // 可用/空闲
    public static final Integer OCCUPIED = 2;    // 已占用（未满）
    public static final Integer FULL = 3;        // 已满
    
    // 扩展状态
    public static final Integer REGO_EXPIRED = 4;       // 注册过期
    public static final Integer INSPECTION_EXPIRED = 5; // 车检过期
    
    // 车辆可用性状态（对应vehicle_availability表的status字段）
    public static final String AVAILABILITY_AVAILABLE = "available";        // 可用
    public static final String AVAILABILITY_IN_USE = "in_use";             // 使用中
    public static final String AVAILABILITY_MAINTENANCE = "maintenance";    // 维护中
    public static final String AVAILABILITY_OUT_OF_SERVICE = "out_of_service"; // 停用
    
    // 状态描述
    public static final String[] STATUS_DESC = {
        "送修中",      // 0
        "可用",        // 1
        "已占用",      // 2
        "已满",        // 3
        "注册过期",    // 4
        "车检过期"     // 5
    };
    
    /**
     * 获取状态描述
     */
    public static String getStatusDesc(Integer status) {
        if (status == null || status < 0 || status >= STATUS_DESC.length) {
            return "未知";
        }
        return STATUS_DESC[status];
    }
    
    /**
     * 获取可用性状态描述
     */
    public static String getAvailabilityStatusDesc(String status) {
        if (status == null) return "未知";
        
        switch (status) {
            case AVAILABILITY_AVAILABLE:
                return "可用";
            case AVAILABILITY_IN_USE:
                return "使用中";
            case AVAILABILITY_MAINTENANCE:
                return "维护中";
            case AVAILABILITY_OUT_OF_SERVICE:
                return "停用";
            default:
                return "未知";
        }
    }
} 