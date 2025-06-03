package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 解析的订单信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {
    
    /**
     * 服务类型
     */
    private String serviceType;
    
    /**
     * 开始日期
     */
    private String startDate;
    
    /**
     * 结束日期
     */
    private String endDate;
    
    /**
     * 出发地点
     */
    private String departure;
    
    /**
     * 团队人数
     */
    private Integer groupSize;
    
    /**
     * 客户信息列表
     */
    private List<CustomerInfo> customers;
    
    /**
     * 房型
     */
    private String roomType;
    
    /**
     * 酒店级别
     */
    private String hotelLevel;
    
    /**
     * 行程安排
     */
    private String itinerary;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 抵达时间
     */
    private String arrivalTime;
    
    /**
     * 抵达航班
     */
    private String arrivalFlight;
    
    /**
     * 离开航班
     */
    private String departureFlight;
    
    /**
     * 服务车型
     */
    private String vehicleType;
    
    /**
     * 行李数
     */
    private Integer luggage;
    
    /**
     * 客户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String name;
        private String phone;
        private String passport;
    }
    
    /**
     * 航班信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightInfo {
        private String flightNumber;
        private String departureTime;
        private String arrivalTime;
        private String departureAirport;
        private String arrivalAirport;
        private String airline;
        private String status;
    }
} 