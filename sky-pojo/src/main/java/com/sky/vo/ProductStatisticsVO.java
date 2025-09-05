package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.List;

/**
 * 产品统计数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 产品类型统计
    private Integer dayTourCount;           // 一日游产品数量
    private Integer groupTourCount;         // 团队游产品数量
    private Integer activeDayTourCount;     // 活跃一日游数量
    private Integer activeGroupTourCount;   // 活跃团队游数量

    // 销售统计
    private BigDecimal dayTourRevenue;      // 一日游总收入
    private BigDecimal groupTourRevenue;    // 团队游总收入
    private Integer dayTourBookings;        // 一日游总预订数
    private Integer groupTourBookings;      // 团队游总预订数

    // 热门产品排行（前5名）
    private List<PopularTourVO> topDayTours;    // 热门一日游
    private List<PopularTourVO> topGroupTours;  // 热门团队游

    // 产品性能指标
    private BigDecimal averageDayTourPrice;     // 一日游平均单价
    private BigDecimal averageGroupTourPrice;   // 团队游平均单价
    private Double averageRating;               // 产品平均评分

    /**
     * 热门产品内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularTourVO implements Serializable {
        private Integer tourId;
        private String tourName;
        private Integer bookingCount;
        private BigDecimal revenue;
        private BigDecimal rating;
        private String tourType; // day_tour 或 group_tour
    }
}