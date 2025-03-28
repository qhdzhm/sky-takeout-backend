package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预订数据传输对象
 */
@Data
public class BookingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 预订ID
    private Integer id;

    // 用户ID
    private Integer userId;

    // 旅游产品ID
    private Integer tourId;

    // 旅游产品类型（day_tour或group_tour）
    private String tourType;

    // 旅游产品名称
    private String tourName;

    // 预订开始日期
    private LocalDate startDate;

    // 预订结束日期（对于团体游）
    private LocalDate endDate;

    // 成人数量
    private Integer adults;

    // 儿童数量
    private Integer children;

    // 总价
    private BigDecimal totalPrice;

    // 联系人姓名
    private String contactName;

    // 联系人电话
    private String contactPhone;

    // 联系人邮箱
    private String contactEmail;

    // 特殊要求
    private String specialRequests;

    // 预订状态（pending, confirmed, cancelled, completed）
    private String status;

    // 支付状态（unpaid, paid, refunded）
    private String paymentStatus;

    // 支付方式
    private String paymentMethod;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
} 