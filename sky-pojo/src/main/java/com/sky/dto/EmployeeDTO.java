package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EmployeeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @JsonIgnore // 响应时忽略密码字段
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;
    
    private String avatar;

    private String sex;
    @Pattern(regexp = "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", message = "身份证格式不正确")
    private String idNumber;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "角色不能为空")
    private Integer role;

    private Integer workStatus; // 工作状态：0-空闲，1-忙碌，2-休假，3-出团，4-待命

    private String licensePlate; // 分配的车牌号

    private VehicleDTO assignedVehicle; // 分配的车辆信息

    private Integer isPrimary; // 是否为主驾驶：0-副驾驶，1-主驾驶

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createUser; // 可改为创建人姓名（需要联查）
    private String updateUser; // 可改为修改人姓名（需要联查）


}
