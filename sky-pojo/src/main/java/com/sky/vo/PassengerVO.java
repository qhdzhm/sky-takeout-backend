package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 乘客VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "乘客信息")
public class PassengerVO {
    
    @ApiModelProperty("乘客ID")
    private Integer passengerId;
    
    @ApiModelProperty("全名")
    private String fullName;
    
    @ApiModelProperty("性别")
    private String gender;
    
    @ApiModelProperty("出生日期")
    private Date dateOfBirth;
    
    @ApiModelProperty("是否是儿童")
    private Boolean isChild;
    
    @ApiModelProperty("儿童年龄")
    private String childAge;
    
    @ApiModelProperty("护照号码")
    private String passportNumber;
    
    @ApiModelProperty("护照有效期")
    private Date passportExpiry;
    
    @ApiModelProperty("国籍")
    private String nationality;
    
    @ApiModelProperty("电话")
    private String phone;
    
    @ApiModelProperty("微信ID")
    private String wechatId;
    
    @ApiModelProperty("邮箱")
    private String email;
    
    @ApiModelProperty("紧急联系人姓名")
    private String emergencyContactName;
    
    @ApiModelProperty("紧急联系人电话")
    private String emergencyContactPhone;
    
    @ApiModelProperty("饮食要求")
    private String dietaryRequirements;
    
    @ApiModelProperty("健康状况")
    private String medicalConditions;
    
    @ApiModelProperty("行李数量")
    private Integer luggageCount;
    
    @ApiModelProperty("特殊要求")
    private String specialRequests;
    
    // 以下是订单乘客关联表中的字段
    
    @ApiModelProperty("是否是主要联系人")
    private Boolean isPrimary;
    
    @ApiModelProperty("票号")
    private String ticketNumber;
    
    @ApiModelProperty("座位号")
    private String seatNumber;
    
    @ApiModelProperty("行李标签")
    private String luggageTags;
    
    @ApiModelProperty("登记状态")
    private String checkInStatus;
} 