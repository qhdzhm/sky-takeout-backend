package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserPageQueryDTO;
import com.sky.dto.UserStatusDTO;
import com.sky.dto.UserPasswordResetDTO;
import com.sky.dto.PasswordChangeDTO;
import com.sky.entity.User;
import com.sky.result.PageResult;
import com.sky.vo.UserLoginVO;
import com.sky.vo.UserSimpleVO;

import java.util.List;

/**
 * ClassName: UserService
 * Package: com.sky.service
 * Description:
 *
 * @Author Tangshifu
 * @Create 2024/7/15 15:26
 * @Version 1.0
 */
public interface UserService {
    /**
     * 用户登录（包括普通用户和微信登录）
     * @param userLoginDTO 登录信息
     * @return 用户信息
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);

    /**
     * 更新用户信息
     * @param user 用户信息
     */
    void updateById(User user);
    
    /**
     * 分页查询用户
     * @param userPageQueryDTO
     * @return
     */
    PageResult pageQuery(UserPageQueryDTO userPageQueryDTO);
    
    /**
     * 删除用户
     * @param id
     */
    void deleteById(Long id);
    
    /**
     * 修改用户状态
     * @param userStatusDTO
     */
    void updateStatus(UserStatusDTO userStatusDTO);
    
    /**
     * 重置用户密码
     * @param userPasswordResetDTO 包含用户ID和新密码的DTO
     */
    void resetPassword(UserPasswordResetDTO userPasswordResetDTO);
    
    /**
     * 创建新用户
     * @param user
     */
    void createUser(User user);

    /**
     * 获取用户下拉选项列表（支持名称模糊搜索）
     * @param name 用户名称关键字（可选）
     * @param id 用户ID（可选，用于精确查询）
     * @return 用户简略信息列表
     */
    List<UserSimpleVO> getUserOptions(String name, Long id);
    
    /**
     * 用户修改自己的密码
     * @param userId 用户ID
     * @param passwordChangeDTO 密码修改信息
     */
    void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO);
    
    /**
     * 根据邀请码获取用户
     * @param inviteCode 邀请码
     * @return 用户信息
     */
    User getUserByInviteCode(String inviteCode);
}
