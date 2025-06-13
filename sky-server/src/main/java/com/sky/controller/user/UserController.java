package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.entity.User;
import com.sky.entity.Agent;
import com.sky.mapper.AgentMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private AgentMapper agentMapper;

    /**
     * 用户登录 - 仅限普通用户
     *
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        log.info("普通用户登录请求：{}", userLoginDTO.getUsername());

        // 1. 安全检查：防止代理商通过普通用户接口登录
        Agent existingAgent = agentMapper.getByUsername(userLoginDTO.getUsername());
        if (existingAgent != null) {
            log.warn("代理商账号 {} 尝试通过普通用户接口登录，已拒绝", userLoginDTO.getUsername());
            return Result.error("该账号为代理商账号，请使用代理商登录入口");
        }

        // 2. 验证普通用户
        User user;
        try {
            user = userService.wxLogin(userLoginDTO);
        } catch (Exception e) {
            log.error("普通用户登录失败：{}, 错误：{}", userLoginDTO.getUsername(), e.getMessage());
            return Result.error("用户名或密码错误");
        }

        if (user == null) {
            log.warn("普通用户不存在：{}", userLoginDTO.getUsername());
            return Result.error("用户名或密码错误");
        }

        // 3. 确保用户类型为普通用户
        if (!"regular".equals(user.getUserType()) && user.getUserType() != null) {
            log.warn("用户 {} 类型为 {}，不允许通过普通用户接口登录", userLoginDTO.getUsername(), user.getUserType());
            return Result.error("账号类型错误，请使用正确的登录入口");
        }

        // 4. 生成JWT令牌 - 使用用户密钥
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.USER_TYPE, "regular");
        
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        // 5. 设置安全Cookie
        // 设置HttpOnly Cookie用于安全存储refresh token
        Cookie refreshTokenCookie = new Cookie("refreshToken", token);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // 开发环境设为false，生产环境应设为true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7天
        response.addCookie(refreshTokenCookie);

        // 设置访问token Cookie
        Cookie authTokenCookie = new Cookie("authToken", token);
        authTokenCookie.setHttpOnly(true);
        authTokenCookie.setSecure(false);
        authTokenCookie.setPath("/");
        authTokenCookie.setMaxAge(15 * 60); // 15分钟
        response.addCookie(authTokenCookie);

        // 设置用户信息Cookie（非HttpOnly，供前端读取）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("userType", "regular");
        userInfo.put("role", "user");
        userInfo.put("isAuthenticated", true);
        
        String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
        String encodedUserInfo;
        try {
            encodedUserInfo = URLEncoder.encode(userInfoJson, "UTF-8");
        } catch (Exception e) {
            log.error("URL编码失败", e);
            encodedUserInfo = userInfoJson; // 如果编码失败，使用原始值
        }
        Cookie userInfoCookie = new Cookie("userInfo", encodedUserInfo);
        userInfoCookie.setSecure(false);
        userInfoCookie.setPath("/");
        userInfoCookie.setMaxAge(15 * 60); // 15分钟
        response.addCookie(userInfoCookie);

        // 6. 构建响应
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .userType("regular")
                .token(token)
                .build();

        log.info("普通用户登录成功：{}", user.getUsername());
        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<String> register(@RequestBody User user) {
        log.info("用户注册：{}", user);
        
        userService.createUser(user);
        
        return Result.success("注册成功");
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/profile")
    @ApiOperation("获取用户信息")
    public Result<User> getProfile(@RequestParam Long id) {
        log.info("获取用户信息，ID：{}", id);
        
        User user = userService.getById(id);
        
        return Result.success(user);
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @PutMapping("/profile")
    @ApiOperation("更新用户信息")
    public Result<String> updateProfile(@RequestBody User user) {
        log.info("更新用户信息：{}", user);
        
        userService.updateById(user);
        
        return Result.success("更新成功");
    }
} 

