package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserPageQueryDTO;
import com.sky.dto.UserStatusDTO;
import com.sky.dto.UserPasswordResetDTO;
import com.sky.dto.PasswordChangeDTO;
import com.sky.entity.Agent;
import com.sky.entity.User;
import com.sky.exception.BusinessException;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.AgentMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.UserService;
import com.sky.vo.UserSimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: UserServiceImpl
 * Package: com.sky.service.impl
 * Description:
 *
 * @Author Tangshifu
 * @Create 2024/7/15 15:48
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private com.sky.utils.WeChatUtil weChatUtil;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        try {
            // 检查是否有用户名和密码，优先使用普通登录
            if (userLoginDTO.getUsername() != null && userLoginDTO.getPassword() != null) {
                log.info("执行用户名密码登录: {}", userLoginDTO.getUsername());
                
                // 处理用户名为user1的测试账号，但使用数据库中的实际密码进行验证
                if ("user1".equals(userLoginDTO.getUsername())) {
                    log.info("检测到测试用户user1登录，正在处理...");
                    
                    // 查询用户是否存在
                    User user = userMapper.getUserByUsername("user1");
                    
                    // 如果用户不存在，则创建测试用户
                    if (user == null) {
                        log.info("测试用户user1不存在，自动创建测试用户");
                        user = User.builder()
                                .username("user1")
                                .password("123456") // 明文密码，实际应该加密
                                .phone("1234567890")
                                .userType("regular") // 设置为普通用户
                                .status(StatusConstant.ENABLE) // 设置为启用状态
                                .build();
                        
                        userMapper.addUser(user);
                        log.info("测试用户user1创建成功，ID: {}", user.getId());
                    }
                    
                    // 使用数据库中的实际密码进行验证
                    if (userLoginDTO.getPassword().equals(user.getPassword())) {
                        log.info("测试用户user1登录成功");
                        return user;
                    } else {
                        log.info("测试用户user1密码错误");
                        throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
                    }
                }

                // 🚨 安全检查：拒绝代理商通过普通用户接口登录
                Agent agent = agentMapper.getByUsername(userLoginDTO.getUsername());
                if (agent != null) {
                    log.warn("🚫 代理商账号 {} 尝试通过普通用户接口登录，已拒绝", userLoginDTO.getUsername());
                    throw new LoginFailedException("该账号为代理商账号，请使用代理商登录入口");
                }
                
                // 常规用户名密码登录流程
                User user = userMapper.getUserByUsername(userLoginDTO.getUsername());
                
                // 如果用户不存在或密码不匹配，抛出登录失败异常
                if (user == null) {
                    log.error("用户不存在：{}", userLoginDTO.getUsername());
                    throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
                }
                
                // 检查用户状态
                if (user.getStatus() != null && user.getStatus() == StatusConstant.DISABLE) {
                    log.error("用户账号已被禁用：{}", userLoginDTO.getUsername());
                    throw new LoginFailedException(MessageConstant.ACCOUNT_LOCKED);
                }
                
                // 生产环境应该使用MD5或更安全的方式验证密码
                if (!userLoginDTO.getPassword().equals(user.getPassword())) {
                    log.error("密码错误：{}", userLoginDTO.getUsername());
                    throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
                }
                
                // 设置用户类型为普通用户（如果没有设置）
                if (user.getUserType() == null) {
                    user.setUserType("regular");
                }
                
                log.info("普通用户登录成功：{}", user.getUsername());
                return user;
            } else if (userLoginDTO.getCode() != null) {
                // 微信登录处理
                log.info("执行微信登录：{}", userLoginDTO.getCode());
                
                // 检查是否为模拟授权码
                String code = userLoginDTO.getCode();
                if (code != null && code.startsWith("test_")) {
                    log.info("检测到测试模拟的微信授权码: {}", code);
                    
                    // 从WechatAuthController.WechatMockData获取模拟的openid
                    String mockOpenid = com.sky.controller.user.WechatAuthController.WechatMockData.getOpenidByCode(code);
                    
                    if (mockOpenid != null) {
                        log.info("使用模拟的openid: {}", mockOpenid);
                        
                        // 查询用户是否已存在
                        User user = userMapper.getUserByOpenid(mockOpenid);
                        
                        // 如果用户不存在，创建新用户
                        if (user == null) {
                            log.info("模拟微信用户首次登录，创建新用户");
                            
                            user = User.builder()
                                    .openid(mockOpenid)
                                    .wxNickname("测试微信用户")
                                    .wxAvatar("https://placeholder.com/150") // 使用占位图
                                    .username("wx_test_" + mockOpenid.substring(0, 8))
                                    .password(DigestUtils.md5DigestAsHex(mockOpenid.getBytes()))
                                    .userType("regular")
                                    .role("customer")
                                    .status(StatusConstant.ENABLE)
                                    .wxLastLogin(LocalDateTime.now())
                                    .build();
                            
                            userMapper.addUser(user);
                            log.info("模拟微信用户创建成功，ID: {}", user.getId());
                        } else {
                            // 更新用户的微信登录信息
                            log.info("模拟微信用户已存在，更新登录信息");
                            user.setWxLastLogin(LocalDateTime.now());
                            userMapper.updateWxLoginInfo(user);
                        }
                        
                        return user;
                    } else {
                        log.error("无效的模拟授权码: {}", code);
                        throw new LoginFailedException("无效的模拟授权码");
                    }
                }
                
                // 正常微信登录处理流程
                Map<String, String> wxResultMap = weChatUtil.getWxLoginInfo(userLoginDTO.getCode());
                
                if (wxResultMap.containsKey("errcode")) {
                    log.error("微信登录失败：{}", wxResultMap.get("errmsg"));
                    throw new LoginFailedException("微信登录失败：" + wxResultMap.get("errmsg"));
                }
                
                String openid = wxResultMap.get("openid");
                String unionid = wxResultMap.get("unionid");
                String nickname = wxResultMap.get("nickname");
                String headimgurl = wxResultMap.get("headimgurl");
                
                if (openid == null || openid.isEmpty()) {
                    log.error("未获取到微信openid");
                    throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
                }
                
                // 根据openid查询用户
                User user = userMapper.getUserByOpenid(openid);
                
                // 如果用户不存在，创建新用户
                if (user == null) {
                    log.info("微信用户首次登录，创建新用户");
                    
                    // 使用微信返回的昵称，或默认值
                    String wxNickname = nickname != null ? nickname : "微信用户";
                    
                    // 创建新用户，使用微信用户信息
                    user = User.builder()
                            .openid(openid)
                            .unionid(unionid)
                            .wxNickname(wxNickname)
                            .wxAvatar(headimgurl)
                            .username("wx_" + openid.substring(0, 10)) // 生成一个基于openid的用户名
                            .password(DigestUtils.md5DigestAsHex(openid.getBytes())) // 生成一个基于openid的密码
                            .userType("regular") // 设置为普通用户
                            .role("customer")
                            .status(StatusConstant.ENABLE) // 设置为启用状态
                            .wxLastLogin(LocalDateTime.now())
                            .build();
                    
                    userMapper.addUser(user);
                    log.info("微信用户创建成功，ID: {}", user.getId());
                } else {
                    // 更新用户的微信登录信息
                    log.info("微信用户已存在，更新登录信息");
                    user.setWxLastLogin(LocalDateTime.now());
                    userMapper.updateWxLoginInfo(user);
                }
                
                // 确保用户类型为普通用户
                if (user.getUserType() == null) {
                    user.setUserType("regular");
                }
                
                log.info("微信用户登录成功：{}", user.getUsername());
                return user;
            } else {
                // 没有提供登录凭证
                log.error("登录失败：未提供有效的登录凭证");
                throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
            }
        } catch (Exception e) {
            log.error("登录失败: ", e);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
    }

    @Override
    public User getById(Long id) {
        log.info("根据ID获取用户信息：{}", id);
        return userMapper.getById(id);
    }

    @Override
    public void updateById(User user) {
        log.info("更新用户信息：{}", user);
        
        // 将name拆分为firstName和lastName
        if (user.getName() != null && !user.getName().isEmpty()) {
            String[] nameParts = user.getName().trim().split("\\s+");
            if (nameParts.length > 1) {
                user.setLastName(nameParts[nameParts.length - 1]);
                
                // 将除了最后一个部分之外的所有部分作为firstName
                StringBuilder firstName = new StringBuilder();
                for (int i = 0; i < nameParts.length - 1; i++) {
                    if (i > 0) {
                        firstName.append(" ");
                    }
                    firstName.append(nameParts[i]);
                }
                user.setFirstName(firstName.toString());
            } else {
                // 如果只有一个部分，则全部作为firstName
                user.setFirstName(nameParts[0]);
                user.setLastName("");
            }
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    @Override
    public PageResult pageQuery(UserPageQueryDTO userPageQueryDTO) {
        // 设置分页参数
        userPageQueryDTO.setPage((userPageQueryDTO.getPage() - 1) * userPageQueryDTO.getPageSize());
        
        // 查询用户总数
        Integer total = userMapper.countUser(userPageQueryDTO);
        
        // 查询用户列表
        List<User> list = userMapper.pageQuery(userPageQueryDTO);
        
        return new PageResult(total, list);
    }
    
    @Override
    public void deleteById(Long id) {
        log.info("删除用户：{}", id);
        
        // 验证用户是否存在
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        userMapper.deleteById(id);
    }
    
    @Override
    public void updateStatus(UserStatusDTO userStatusDTO) {
        log.info("修改用户状态：{}", userStatusDTO);
        
        // 验证用户是否存在
        User user = userMapper.getById(userStatusDTO.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 修改用户状态
        userMapper.updateStatus(userStatusDTO.getId(), userStatusDTO.getStatus());
    }
    
    @Override
    public void resetPassword(UserPasswordResetDTO userPasswordResetDTO) {
        log.info("重置用户密码：{}", userPasswordResetDTO);
        
        Long id = userPasswordResetDTO.getId();
        
        // 验证用户是否存在
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 使用提供的密码，如果未提供则使用默认密码
        String password = userPasswordResetDTO.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = PasswordConstant.DEFAULT_PASSWORD;
        }
        
        // 重置密码
        userMapper.updatePassword(id, password);
    }
    
    @Override
    public void createUser(User user) {
        log.info("创建新用户：{}", user);
        
        // 验证用户名是否已存在
        User existUser = userMapper.getUserByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 将name拆分为firstName和lastName
        if (user.getName() != null && !user.getName().isEmpty() && 
            (user.getFirstName() == null || user.getLastName() == null)) {
            String[] nameParts = user.getName().trim().split("\\s+");
            if (nameParts.length > 1) {
                user.setLastName(nameParts[nameParts.length - 1]);
                
                // 将除了最后一个部分之外的所有部分作为firstName
                StringBuilder firstName = new StringBuilder();
                for (int i = 0; i < nameParts.length - 1; i++) {
                    if (i > 0) {
                        firstName.append(" ");
                    }
                    firstName.append(nameParts[i]);
                }
                user.setFirstName(firstName.toString());
            } else {
                // 如果只有一个部分，则全部作为firstName
                user.setFirstName(nameParts[0]);
                user.setLastName("");
            }
        }
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(StatusConstant.ENABLE);
        }
        
        if (user.getUserType() == null) {
            user.setUserType("regular");
        }
        
        if (user.getRole() == null) {
            user.setRole("customer");
        }
        
        // 设置默认密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456"); // 实际应用中可能需要更复杂的默认密码
        }
        
        // 设置默认邮箱
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            // 使用用户名生成默认邮箱
            user.setEmail(user.getUsername() + "@example.com");
        }
        
        // 添加用户
        userMapper.addUser(user);
    }

    /**
     * 获取用户下拉选项列表（支持名称模糊搜索）
     * @param name 用户名称关键字（可选）
     * @param id 用户ID（可选，用于精确查询）
     * @return 用户简略信息列表
     */
    @Override
    public List<UserSimpleVO> getUserOptions(String name, Long id) {
        // 如果指定了ID，则直接通过ID查询
        if (id != null) {
            User user = userMapper.getById(id);
            if (user != null) {
                UserSimpleVO vo = UserSimpleVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .build();
                return Collections.singletonList(vo);
            }
            return Collections.emptyList();
        }
        
        // 否则通过名称关键字模糊查询
        List<User> users = userMapper.getUsersByNameKeyword(name);
        return users.stream().map(user -> UserSimpleVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 用户修改自己的密码
     * @param userId 用户ID
     * @param passwordChangeDTO 密码修改信息
     */
    @Override
    public void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        log.info("用户修改密码：userId={}", userId);
        
        // 1. 验证用户是否存在
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 验证旧密码是否正确 - 简单直接比较，因为用户密码在数据库中以明文存储
        String oldPassword = passwordChangeDTO.getOldPassword();
        String storedPassword = user.getPassword();
        
        log.debug("输入的旧密码: {}, 存储的密码: {}", oldPassword, storedPassword);
        
        if (!oldPassword.equals(storedPassword)) {
            throw new BusinessException("旧密码不正确");
        }
        
        // 3. 验证新密码有效性
        String newPassword = passwordChangeDTO.getNewPassword();
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码不能少于6位");
        }
        
        // 4. 更新密码
        userMapper.updatePassword(userId, newPassword);
        
        // 5. 记录密码已修改，可用于后续处理（如强制重新登录）
        log.info("用户 {} 密码修改成功", userId);
    }

    /**
     * 根据邀请码获取用户
     * @param inviteCode 邀请码
     * @return 用户信息
     */
    @Override
    public User getUserByInviteCode(String inviteCode) {
        log.info("根据邀请码查询用户：{}", inviteCode);
        return userMapper.getUserByInviteCode(inviteCode);
    }
}
