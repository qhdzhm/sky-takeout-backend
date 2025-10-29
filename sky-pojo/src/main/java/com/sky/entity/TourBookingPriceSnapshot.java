package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单价格快照
 * 记录订单创建时的完整价格计算明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBookingPriceSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Integer bookingId;

    // ==================== 基础价格信息 ====================
    /**
     * 基础单价（元/人）
     */
    private BigDecimal baseUnitPrice;

    /**
     * 代理商折扣率（如0.7表示70%折扣）
     */
    private BigDecimal discountRate;

    /**
     * 折扣后单价（元/人）
     */
    private BigDecimal discountedUnitPrice;

    // ==================== 人员信息 ====================
    /**
     * 成人数量
     */
    private Integer adultCount;

    /**
     * 成人单价
     */
    private BigDecimal adultUnitPrice;

    /**
     * 成人总价
     */
    private BigDecimal adultTotalPrice;

    /**
     * 儿童数量
     */
    private Integer childCount;

    /**
     * 儿童年龄（逗号分隔）
     */
    private String childrenAges;

    /**
     * 儿童价格明细（JSON格式）
     */
    private String childrenDetails;

    /**
     * 儿童总价
     */
    private BigDecimal childTotalPrice;

    // ==================== 住宿信息 ====================
    /**
     * 酒店星级
     */
    private String hotelLevel;

    /**
     * 星级差价总额
     */
    private BigDecimal hotelPriceDiffTotal;

    /**
     * 房间数量
     */
    private Integer roomCount;

    /**
     * 房型列表（JSON格式）
     */
    private String roomTypes;

    /**
     * 房型费用总额
     */
    private BigDecimal roomFees;

    /**
     * 单房差
     */
    private BigDecimal singleRoomSupplement;

    /**
     * 住宿夜数
     */
    private Integer accommodationNights;

    // ==================== 可选行程 ====================
    /**
     * 可选行程（JSON格式）
     */
    private String optionalTours;

    /**
     * 可选行程总价
     */
    private BigDecimal optionalToursPrice;

    // ==================== 总价 ====================
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;

    /**
     * 非代理商价格（零售价）
     */
    private BigDecimal nonAgentPrice;

    // ==================== 配置快照 ====================
    /**
     * 价格配置快照（JSON格式，保存计算时使用的所有配置）
     */
    private String priceConfigSnapshot;

    // ==================== 元数据 ====================
    /**
     * 价格计算时间
     */
    private LocalDateTime calculationDate;

    /**
     * 行程开始日期
     */
    private LocalDate tourStartDate;

    /**
     * 行程结束日期
     */
    private LocalDate tourEndDate;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;
}







