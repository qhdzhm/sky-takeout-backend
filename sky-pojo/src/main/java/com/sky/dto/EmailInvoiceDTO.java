package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 邮件Invoice DTO
 */
@Data
public class EmailInvoiceDTO implements Serializable {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 代理商主号ID
     */
    private Long agentId;

    /**
     * 操作员ID (可选)
     */
    private Long operatorId;

        /**
         * logo偏好：agent/company（可选）
         * null 或其他取值则按自动策略：优先代理商头像，失败回退公司logo
         */
        private String logoPreference;

    /**
     * Invoice详情
     */
    private InvoiceDetails invoiceDetails;

    @Data
    public static class InvoiceDetails implements Serializable {
        private String tourName;
        private String tourType;
        private String startDate;
        private String endDate;
        private Integer adultCount;
        private Integer childCount;
        private Double totalPrice;  // 改为Double类型以支持数学计算
        private String agentName;
        private String operatorName;
        private Double discountRate;  // 改为Double类型以支持数学计算
    }
} 