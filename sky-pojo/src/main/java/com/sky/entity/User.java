package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //微信用户唯一标识
    private String openid;

    //用户名和密码字段
    private String username;
    private String password;

    //姓名
    private String name;

    //手机号
    private String phone;

    //用户角色
    private String role;

    //用户类型 regular-普通用户，agent-代理商
    private String userType;
    
    //关联的代理商ID
    private Long agentId;

    //性别 0 女 1 男
    private String sex;

    //身份证号
    private String idNumber;

    //头像
    private String avatar;

    //注册时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
}
