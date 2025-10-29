package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * 价格计算请求DTO
 * 包含完整的参数验证
 */
@Data
@ApiModel(description = "价格计算请求")
public class PriceCalculationDTO {

    @ApiModelProperty(value = "产品ID", required = true)
    @NotNull(message = "产品ID不能为空")
    @Min(value = 1, message = "产品ID必须大于0")
    private Integer tourId;

    @ApiModelProperty(value = "产品类型(day_tour/group_tour)", required = true)
    @NotBlank(message = "产品类型不能为空")
    @Pattern(regexp = "^(day_tour|group_tour)$", message = "产品类型只能是day_tour或group_tour")
    private String tourType;

    @ApiModelProperty(value = "成人数量", required = true)
    @NotNull(message = "成人数量不能为空")
    @Min(value = 0, message = "成人数量不能小于0")
    @Max(value = 100, message = "成人数量不能超过100")
    private Integer adultCount;

    @ApiModelProperty(value = "儿童数量")
    @Min(value = 0, message = "儿童数量不能小于0")
    @Max(value = 50, message = "儿童数量不能超过50")
    private Integer childCount = 0;

    @ApiModelProperty(value = "是否包含酒店")
    private Boolean includeHotel = true;

    @ApiModelProperty(value = "酒店星级")
    @Pattern(regexp = "^(3星|4星|4\\.5星|5星)?$", message = "酒店星级格式不正确")
    private String hotelLevel = "4星";

    @ApiModelProperty(value = "房间数量")
    @Min(value = 1, message = "房间数量至少为1")
    @Max(value = 20, message = "房间数量不能超过20")
    private Integer roomCount = 1;

    @ApiModelProperty(value = "用户ID")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @ApiModelProperty(value = "代理商ID")
    @Min(value = 1, message = "代理商ID必须大于0")
    private Long agentId;

    @ApiModelProperty(value = "儿童年龄(逗号分隔)")
    @Pattern(regexp = "^([0-9]{1,2})(,[0-9]{1,2})*$|^$", message = "儿童年龄格式不正确，应为逗号分隔的数字")
    private String childrenAges;

    @ApiModelProperty(value = "房间类型(单个)")
    private String roomType;

    @ApiModelProperty(value = "房间类型数组(JSON)")
    private String roomTypes;

    @ApiModelProperty(value = "可选行程(JSON)")
    private String selectedOptionalTours;

    @ApiModelProperty(value = "行程出发日期(yyyy-MM-dd)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @ApiModelProperty(value = "行程返回日期(yyyy-MM-dd)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @ApiModelProperty(value = "是否小团")
    private Boolean isSmallGroup = false;

    /**
     * 自定义验证：儿童年龄数量应该与儿童数量匹配
     */
    @AssertTrue(message = "儿童年龄数量与儿童数量不匹配")
    public boolean isChildrenAgesValid() {
        if (childCount == null || childCount == 0) {
            return true; // 没有儿童，不需要验证
        }
        if (childrenAges == null || childrenAges.trim().isEmpty()) {
            return true; // 允许不提供年龄（使用默认计算）
        }
        String[] ages = childrenAges.split(",");
        // 验证年龄数量
        if (ages.length != childCount) {
            return false;
        }
        // 验证每个年龄在合理范围内
        try {
            for (String ageStr : ages) {
                int age = Integer.parseInt(ageStr.trim());
                if (age < 0 || age > 18) {
                    return false; // 年龄应该在0-18岁之间
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 自定义验证：日期范围合理性
     */
    @AssertTrue(message = "结束日期必须晚于开始日期")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true; // 日期可选
        }
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }

    /**
     * 自定义验证：总人数不能为0
     */
    @AssertTrue(message = "成人和儿童总人数不能为0")
    public boolean isTotalPeopleValid() {
        if (adultCount == null) {
            return true;
        }
        int total = adultCount + (childCount != null ? childCount : 0);
        return total > 0;
    }
}





