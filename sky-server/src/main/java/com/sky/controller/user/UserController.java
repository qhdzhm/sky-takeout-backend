package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.PasswordChangeDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import com.sky.context.BaseContext;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 普通用户相关接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户注册
     * 
     * @param userRegisterDTO 用户注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);
        
        try {
            // 设置默认姓名为用户名
            if (userRegisterDTO.getName() == null || userRegisterDTO.getName().isEmpty()) {
                userRegisterDTO.setName(userRegisterDTO.getUsername());
            }
            
            // 创建用户实体
            User user = User.builder()
                    .username(userRegisterDTO.getUsername())
                    .password(userRegisterDTO.getPassword())
                    .name(userRegisterDTO.getName())
                    .phone(userRegisterDTO.getPhone())
                    .email(userRegisterDTO.getEmail())
                    .userType("regular")
                    .role("customer")
                    .build();
            
            // 处理邀请码 - 查找推荐人
            String inviteCode = userRegisterDTO.getInviteCode();
            if (inviteCode != null && !inviteCode.isEmpty()) {
                User referrer = userService.getUserByInviteCode(inviteCode);
                if (referrer != null) {
                    user.setReferredBy(referrer.getId());
                }
            }
            
            // 生成唯一邀请码
            String newInviteCode = UUID.randomUUID().toString().substring(0, 8);
            user.setInviteCode(newInviteCode);
            
            // 创建用户
            userService.createUser(user);
            
            // 准备JWT中的claims
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.USER_TYPE, "regular");
            
            // 生成token
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
            
            // 将用户信息存入线程本地变量
            BaseContext.setCurrentUsername(user.getUsername());
            BaseContext.setCurrentId(user.getId());
            BaseContext.setCurrentUserType("regular");
            
            // 构建登录响应VO
            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .token(token)
                    .username(user.getUsername())
                    .name(user.getName())
                    .userType("regular")
                    .discountRate(BigDecimal.ONE) // 普通用户折扣率为1.0（无折扣）
                    .build();
            
            log.info("用户注册成功：{}", user.getUsername());
            
            return Result.success(userLoginVO);
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     * 
     * @param userLoginDTO 登录数据传输对象
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 根据登录方式记录不同的日志
        if (userLoginDTO.getUsername() != null && userLoginDTO.getPassword() != null) {
            log.info("用户名密码登录:{}", userLoginDTO.getUsername());
        } else if (userLoginDTO.getCode() != null) {
            log.info("微信登录code:{}", userLoginDTO.getCode());
        } else {
            log.info("登录参数不完整");
            return Result.error("登录参数不完整");
        }

        try {
            User user = userService.wxLogin(userLoginDTO);

            // 验证用户类型，如果是代理商则拒绝
            if (user.getUserType() != null && "agent".equals(user.getUserType())) {
                return Result.error("代理商用户请使用代理商登录接口");
            }

            // 准备JWT中的claims
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.USER_TYPE, "regular"); // 明确设置为普通用户
            
            // 生成token
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

            // 将用户信息存入线程本地变量
            BaseContext.setCurrentUsername(user.getUsername());
            BaseContext.setCurrentId(user.getId());
            BaseContext.setCurrentUserType("regular");

            // 构建登录响应VO
            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .token(token)
                    .username(user.getUsername())
                    .name(user.getName())
                    .userType("regular") // 明确设置为普通用户
                    .discountRate(BigDecimal.ONE) // 普通用户折扣率为1.0（无折扣）
                    .build();

            // 记录日志
            log.info("普通用户登录成功，生成token: {}", token);

            return Result.success(userLoginVO);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户个人信息
     * 
     * @return 用户信息
     */
    @GetMapping("/profile")
    @ApiOperation("获取用户个人信息")
    public Result<User> getProfile() {
        log.info("获取用户个人信息");
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        return Result.success(user);
    }

    /**
     * 更新用户个人信息
     * 
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/profile")
    @ApiOperation("更新用户个人信息")
    public Result<String> updateProfile(@RequestBody User user) {
        log.info("更新用户个人信息：{}", user);
        Long userId = BaseContext.getCurrentId();
        user.setId(userId);
        userService.updateById(user);
        return Result.success();
    }
    
    /**
     * 修改密码
     * 
     * @param passwordChangeDTO 密码修改信息
     * @return 修改结果
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<String> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO) {
        log.info("用户修改密码");
        Long userId = BaseContext.getCurrentId();
        userService.changePassword(userId, passwordChangeDTO);
        
        // 返回特殊状态码，通知前端需要重新登录
        return Result.success("密码修改成功，请重新登录");
    }
}
