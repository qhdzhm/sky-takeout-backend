package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.entity.Agent;
import com.sky.mapper.AgentMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.service.GoogleOAuthService;
import com.sky.utils.JwtUtil;
import com.sky.utils.CookieUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    @Autowired
    private GoogleOAuthService googleOAuthService;

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

        // 5. 设置安全Cookie - 正确的双Token模式
        // 🧪 临时测试：Access Token（10秒）- 快速测试自动刷新机制
        CookieUtil.setCookieWithMultiplePaths(response, "authToken", token, true, 900); // 15分钟
        
        // Refresh Token（长期，7天）- 生成不同的Token
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put(JwtClaimsConstant.USER_ID, user.getId());
        refreshClaims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        refreshClaims.put(JwtClaimsConstant.USER_TYPE, "regular");
        
        String refreshToken = JwtUtil.createRefreshJWT(
            jwtProperties.getUserSecretKey(),
            jwtProperties.getRefreshTokenTtl(),
            refreshClaims
        );
        
        CookieUtil.setCookieWithMultiplePaths(response, "refreshToken", refreshToken, true, 7 * 24 * 60 * 60);

        // 设置用户信息Cookie（非HttpOnly，供前端读取）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("userType", "regular");
        userInfo.put("role", "user");
        userInfo.put("isAuthenticated", true);
        
        String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
        CookieUtil.setUserInfoCookie(response, userInfoJson, 604800); // 7天，与refreshToken同步

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
     * 用户登出
     */
    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<String> logout(HttpServletResponse response) {
        log.info("普通用户退出登录");
        
        try {
            // 使用统一的Cookie工具类清理所有用户相关Cookie
            CookieUtil.clearAllUserCookies(response);
            
            log.info("普通用户退出登录成功，已清理所有路径下的Cookie");
            return Result.success("退出登录成功");
        } catch (Exception e) {
            log.error("普通用户退出登录失败", e);
            return Result.error("退出登录失败：" + e.getMessage());
        }
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
    
    /**
     * Google 登录 - 验证 Google ID Token
     *
     * @param requestBody 包含 Google ID Token 的请求体
     * @param response HTTP 响应对象
     * @return 登录结果
     */
    @PostMapping("/google/verify")
    @ApiOperation("Google 登录验证")
    public Result<UserLoginVO> googleLogin(@RequestBody Map<String, String> requestBody, HttpServletResponse response) {
        String credential = requestBody.get("credential");
        
        if (credential == null || credential.trim().isEmpty()) {
            log.error("❌ Google 登录失败：未提供 credential");
            return Result.error("Google 登录失败：未提供凭证");
        }
        
        log.info("🔐 开始处理 Google 登录请求");
        
        try {
            // 验证 Google Token 并获取用户信息
            User user = googleOAuthService.verifyGoogleToken(credential);
            
            if (user == null) {
                log.error("❌ Google 登录失败：无法获取用户信息");
                return Result.error("Google 登录失败");
            }
            
            // 确保用户类型为普通用户
            if (user.getUserType() == null) {
                user.setUserType("regular");
            }
            
            // 生成 JWT 令牌 - 使用用户密钥
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.USER_TYPE, "regular");
            
            String token = JwtUtil.createJWT(
                    jwtProperties.getUserSecretKey(),
                    jwtProperties.getUserTtl(),
                    claims);
            
            // 设置安全Cookie
            CookieUtil.setCookieWithMultiplePaths(response, "authToken", token, true, 900); // 15分钟
            
            // Refresh Token（7天）
            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put(JwtClaimsConstant.USER_ID, user.getId());
            refreshClaims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            refreshClaims.put(JwtClaimsConstant.USER_TYPE, "regular");
            
            String refreshToken = JwtUtil.createRefreshJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getRefreshTokenTtl(),
                refreshClaims
            );
            
            CookieUtil.setCookieWithMultiplePaths(response, "refreshToken", refreshToken, true, 7 * 24 * 60 * 60);
            
            // 设置用户信息Cookie（非HttpOnly，供前端读取）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getName());
            userInfo.put("userType", "regular");
            userInfo.put("role", "user");
            userInfo.put("isAuthenticated", true);
            
            String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
            CookieUtil.setUserInfoCookie(response, userInfoJson, 604800); // 7天
            
            // 构建响应
            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .userType("regular")
                    .token(token)
                    .build();
            
            log.info("✅ Google 登录成功：用户 {}, ID: {}", user.getUsername(), user.getId());
            return Result.success(userLoginVO);
            
        } catch (Exception e) {
            log.error("❌ Google 登录失败", e);
            return Result.error("Google 登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取 Google OAuth 授权 URL
     *
     * @return 授权 URL
     */
    @GetMapping("/google/auth-url")
    @ApiOperation("获取 Google OAuth 授权 URL")
    public Result<Map<String, String>> getGoogleAuthUrl() {
        try {
            String state = "google_oauth_" + System.currentTimeMillis();
            String authUrl = googleOAuthService.generateAuthUrl(state);
            
            Map<String, String> result = new HashMap<>();
            result.put("authUrl", authUrl);
            result.put("state", state);
            
            log.info("✅ 生成 Google 授权 URL 成功");
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("❌ 生成 Google 授权 URL 失败", e);
            return Result.error("获取授权 URL 失败: " + e.getMessage());
        }
    }
} 

