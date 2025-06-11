package com.sky.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 邮件确认单DTO
 */
@Data
public class EmailConfirmationDTO implements Serializable {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 接收人类型 (agent/operator)
     */
    private String recipientType;

    /**
     * 代理商主号ID
     */
    private Long agentId;

    /**
     * 操作员ID (可选)
     */
    private Long operatorId;

    /**
     * 订单详情
     */
    private OrderDetails orderDetails;

    @Data
    public static class OrderDetails implements Serializable {
        private String tourName;
        private String tourType;
        private String startDate;
        private String endDate;
        private Integer adultCount;
        private Integer childCount;
        private String totalPrice;
        private String contactPerson;
        private String contactPhone;
        private String pickupLocation;
        private String dropoffLocation;
        private String hotelLevel;
        private String specialRequests;
        private String agentName;
        private String operatorName;
        private String discountRate;
        private List<PassengerInfo> passengers;
        private List<Map<String, Object>> itinerary;
    }

    @Data
    public static class PassengerInfo implements Serializable {
        private String fullName;
        private String phone;
        private String wechatId;
        private Boolean isChild;
        private Integer childAge;
        private Boolean isPrimary;
    }
} 