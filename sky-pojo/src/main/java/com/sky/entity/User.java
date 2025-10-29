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
    
    //微信用户统一标识
    private String unionid;
    
    //Google用户唯一标识
    private String googleId;
    
    //Google账号邮箱
    private String googleEmail;
    
    //Google账号名称
    private String googleName;
    
    //Google头像
    private String googleAvatar;
    
    //最后Google登录时间
    private LocalDateTime googleLastLogin;
    
    //微信昵称
    private String wxNickname;
    
    //微信头像
    private String wxAvatar;
    
    //最后微信登录时间
    private LocalDateTime wxLastLogin;

    //用户名和密码字段
    private String username;
    private String password;

    //姓名
    private String name;
    
    //姓
    private String firstName;
    
    //名
    private String lastName;

    //手机号
    private String phone;

    //邮箱
    private String email;

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

    //状态 0-禁用 1-正常
    private Integer status;

    //注册时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
    
    //邀请码
    private String inviteCode;
    
    //推荐人ID
    private Long referredBy;
    
    /**
     * 获取完整姓名，优先返回name字段，如果name为空则返回firstName和lastName的组合
     * @return 完整姓名
     */
    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        
        // 如果没有name，尝试使用firstName和lastName组合
        if (this.firstName != null || this.lastName != null) {
            String fn = this.firstName != null ? this.firstName : "";
            String ln = this.lastName != null ? this.lastName : "";
            return (fn + " " + ln).trim();
        }
        
        // 如果连firstName和lastName都没有，尝试返回wxNickname
        if (this.wxNickname != null) {
            return this.wxNickname;
        }
        
        // 都没有，返回username
        return this.username;
    }
    
    /**
     * 设置姓名
     */
    public void setName(String name) {
        this.name = name;
    }
    
    // Google 字段的 getter/setter 方法（手动添加以确保编译成功）
    public String getGoogleId() {
        return googleId;
    }
    
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
    
    public String getGoogleEmail() {
        return googleEmail;
    }
    
    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
    }
    
    public String getGoogleName() {
        return googleName;
    }
    
    public void setGoogleName(String googleName) {
        this.googleName = googleName;
    }
    
    public String getGoogleAvatar() {
        return googleAvatar;
    }
    
    public void setGoogleAvatar(String googleAvatar) {
        this.googleAvatar = googleAvatar;
    }
    
    public LocalDateTime getGoogleLastLogin() {
        return googleLastLogin;
    }
    
    public void setGoogleLastLogin(LocalDateTime googleLastLogin) {
        this.googleLastLogin = googleLastLogin;
    }
}
