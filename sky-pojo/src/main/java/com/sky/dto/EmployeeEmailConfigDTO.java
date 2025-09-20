package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 员工邮箱配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEmailConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "邮箱密码不能为空")
    private String emailPassword;

    @Builder.Default
    private String emailHost = "smtp.gmail.com";

    @Builder.Default
    private Integer emailPort = 587;

    @Builder.Default
    private Boolean emailEnabled = true;

    @Builder.Default
    private Boolean emailSslEnabled = true;

    @Builder.Default
    private Boolean emailAuthEnabled = true;
}
