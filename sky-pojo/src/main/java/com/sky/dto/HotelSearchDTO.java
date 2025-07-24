package com.sky.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 酒店搜索DTO
 */
@Data
@ApiModel(description = "酒店搜索条件")
public class HotelSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("区域位置")
    private String locationArea;

    @ApiModelProperty("酒店等级")
    private String hotelLevel;

    @ApiModelProperty("入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @ApiModelProperty("退房日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @ApiModelProperty("房间数量")
    private Integer roomCount;

    @ApiModelProperty("成人数量")
    private Integer adultCount;

    @ApiModelProperty("儿童数量")
    private Integer childCount;

    @ApiModelProperty("房型偏好")
    private String roomTypePreference;

    @ApiModelProperty("最低价格")
    private java.math.BigDecimal minPrice;

    @ApiModelProperty("最高价格")
    private java.math.BigDecimal maxPrice;

    @ApiModelProperty("排序方式（price_asc：价格升序，price_desc：价格降序，rating_desc：评分降序）")
    private String sortBy;
} 