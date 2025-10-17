package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店客人统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "酒店客人统计VO")
public class HotelCustomerStatisticsVO {

    @ApiModelProperty("酒店名称")
    private String hotelName;

    @ApiModelProperty("统计日期")
    private LocalDate tourDate;

    @ApiModelProperty("总客人数")
    private Integer totalCustomers;

    @ApiModelProperty("按导游分组的客人信息")
    private List<GuideCustomerGroup> guideGroups;

    /**
     * 导游客人分组
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "导游客人分组")
    public static class GuideCustomerGroup {

        @ApiModelProperty("导游姓名")
        private String guideName;

        @ApiModelProperty("车辆信息")
        private String vehicleInfo;

        @ApiModelProperty("当天目的地")
        private String destination;

        @ApiModelProperty("该导游负责的客人数")
        private Integer customerCount;

        @ApiModelProperty("客人详细信息列表")
        private List<CustomerDetail> customers;
    }

    /**
     * 客人详细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "客人详细信息")
    public static class CustomerDetail {

        @ApiModelProperty("订单号")
        private String orderNumber;

        @ApiModelProperty("联系人姓名")
        private String contactPerson;

        @ApiModelProperty("联系电话")
        private String contactPhone;

        @ApiModelProperty("成人数")
        private Integer adultCount;

        @ApiModelProperty("儿童数")
        private Integer childCount;

        @ApiModelProperty("接客地点")
        private String pickupLocation;

        @ApiModelProperty("送客地点")
        private String dropoffLocation;

        @ApiModelProperty("特殊要求")
        private String specialRequests;

        @ApiModelProperty("预订ID")
        private Integer bookingId;
        
        @ApiModelProperty("乘客列表")
        private List<PassengerInfo> passengers;
    }
    
    /**
     * 乘客信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "乘客信息")
    public static class PassengerInfo {
        
        @ApiModelProperty("乘客姓名")
        private String fullName;
        
        @ApiModelProperty("联系电话")
        private String phone;
        
        @ApiModelProperty("微信号")
        private String wechatId;
        
        @ApiModelProperty("是否为儿童")
        private Boolean isChild;
    }
} 