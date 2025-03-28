package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

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
}
