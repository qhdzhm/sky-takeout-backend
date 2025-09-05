package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

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
    @JsonProperty("passenger_id")
    private Integer passengerId;
    
    @ApiModelProperty("乘客姓名")
    @JsonProperty("full_name")
    @JsonAlias({"fullName", "full_name"})
    private String fullName;
    
    @ApiModelProperty("性别(male-男，female-女，other-其他)")
    private String gender;
    
    @ApiModelProperty("出生日期")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    
    @ApiModelProperty("是否是儿童")
    @JsonProperty("is_child")
    @JsonAlias({"isChild", "is_child"})
    private Boolean isChild;
    
    @ApiModelProperty("儿童年龄")
    @JsonProperty(value = "child_age", access = JsonProperty.Access.WRITE_ONLY)
    private String childAge;
    
    // 前端可能发送age字段，映射到childAge
    @JsonProperty("age")
    public void setAge(Object age) {
        if (age != null) {
            this.childAge = age.toString();
        }
    }
    
    @ApiModelProperty("护照号码")
    @JsonProperty("passport_number")
    private String passportNumber;
    
    @ApiModelProperty("护照有效期")
    @JsonProperty("passport_expiry")
    private LocalDate passportExpiry;
    
    @ApiModelProperty("国籍")
    private String nationality;
    
    @ApiModelProperty("电话号码")
    private String phone;
    
    @ApiModelProperty("微信号")
    @JsonProperty("wechat_id")
    @JsonAlias({"wechatId", "wechat_id"})
    private String wechatId;
    
    @ApiModelProperty("电子邮箱")
    private String email;
    
    @ApiModelProperty("紧急联系人姓名")
    @JsonProperty("emergency_contact_name")
    private String emergencyContactName;
    
    @ApiModelProperty("紧急联系人电话")
    @JsonProperty("emergency_contact_phone")
    private String emergencyContactPhone;
    
    @ApiModelProperty("饮食需求")
    @JsonProperty("dietary_requirements")
    private String dietaryRequirements;
    
    @ApiModelProperty("健康状况")
    @JsonProperty("medical_conditions")
    private String medicalConditions;
    
    @ApiModelProperty("行李数量")
    @JsonProperty("luggage_count")
    private Integer luggageCount;
    
    @ApiModelProperty("特殊需求")
    @JsonProperty("special_requests")
    private String specialRequests;
    
    @ApiModelProperty("是否为主要乘客")
    @JsonProperty("is_primary")
    @JsonAlias({"isPrimary", "is_primary"})
    private Boolean isPrimary;
    
    @ApiModelProperty("机票号")
    @JsonProperty("ticket_number")
    private String ticketNumber;
    
    @ApiModelProperty("座位号")
    @JsonProperty("seat_number")
    private String seatNumber;
    
    @ApiModelProperty("行李标签")
    @JsonProperty("luggage_tags")
    private String luggageTags;
    
    @ApiModelProperty("登记状态")
    @JsonProperty("check_in_status")
    private String checkInStatus;
} 