package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "酒店详细信息")
public class HotelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("酒店ID")
    private Integer id;

    @ApiModelProperty("酒店名称")
    private String hotelName;

    @ApiModelProperty("酒店等级")
    private String hotelLevel;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("区域位置")
    private String locationArea;

    @ApiModelProperty("电话")
    private String phone;

    @ApiModelProperty("设施列表")
    private List<String> facilities;

    @ApiModelProperty("酒店描述")
    private String description;

    @ApiModelProperty("酒店图片")
    private List<String> images;

    @ApiModelProperty("评分")
    private BigDecimal rating;

    @ApiModelProperty("供应商名称")
    private String supplierName;

    @ApiModelProperty("可用房型列表")
    private List<HotelRoomTypeVO> roomTypes;

    @ApiModelProperty("最低价格")
    private BigDecimal minPrice;

    @ApiModelProperty("状态")
    private String status;
} 