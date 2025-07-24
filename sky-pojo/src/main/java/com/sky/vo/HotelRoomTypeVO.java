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
 * 酒店房型视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "酒店房型详细信息")
public class HotelRoomTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("房型ID")
    private Integer id;

    @ApiModelProperty("房型名称")
    private String roomType;

    @ApiModelProperty("房型代码")
    private String roomTypeCode;

    @ApiModelProperty("最大入住人数")
    private Integer maxOccupancy;

    @ApiModelProperty("床型")
    private String bedType;

    @ApiModelProperty("房间大小")
    private String roomSize;

    @ApiModelProperty("基础价格")
    private BigDecimal basePrice;

    @ApiModelProperty("房间设施")
    private List<String> amenities;

    @ApiModelProperty("房间描述")
    private String description;

    @ApiModelProperty("房间图片")
    private List<String> images;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("可用数量")
    private Integer availableCount;
} 