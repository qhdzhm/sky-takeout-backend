package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 乘客信息DTO
 */
@Data
@ApiModel(description = "乘客信息数据传输对象")
public class PassengerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("乘客ID")
    private Integer passengerId;
    
    @ApiModelProperty("乘客姓名")
    private String fullName;
    
    @ApiModelProperty("性别(male-男，female-女，other-其他)")
    private String gender;
    
    @ApiModelProperty("出生日期")
    private LocalDate dateOfBirth;
    
    @ApiModelProperty("是否是儿童")
    private Boolean isChild;
    
    @ApiModelProperty("儿童年龄")
    private String childAge;
    
    @ApiModelProperty("护照号码")
    private String passportNumber;
    
    @ApiModelProperty("护照有效期")
    private LocalDate passportExpiry;
    
    @ApiModelProperty("国籍")
    private String nationality;
    
    @ApiModelProperty("电话号码")
    private String phone;
    
    @ApiModelProperty("微信号")
    private String wechatId;
    
    @ApiModelProperty("电子邮箱")
    private String email;
    
    @ApiModelProperty("紧急联系人姓名")
    private String emergencyContactName;
    
    @ApiModelProperty("紧急联系人电话")
    private String emergencyContactPhone;
    
    @ApiModelProperty("饮食需求")
    private String dietaryRequirements;
    
    @ApiModelProperty("健康状况")
    private String medicalConditions;
    
    @ApiModelProperty("行李数量")
    private Integer luggageCount;
    
    @ApiModelProperty("特殊需求")
    private String specialRequests;
    
    @ApiModelProperty("是否为主要乘客")
    private Boolean isPrimary;
    
    @ApiModelProperty("机票号")
    private String ticketNumber;
    
    @ApiModelProperty("座位号")
    private String seatNumber;
    
    @ApiModelProperty("行李标签")
    private String luggageTags;
    
    @ApiModelProperty("登记状态")
    private String checkInStatus;
} 