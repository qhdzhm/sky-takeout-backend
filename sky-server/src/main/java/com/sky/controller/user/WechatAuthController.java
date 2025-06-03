package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import com.sky.context.BaseContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信授权登录相关接口
 */
@RestController
@RequestMapping("/user/wechat")
@Slf4j
@Api(tags = "微信授权相关接口")
public class WechatAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 微信扫码登录
     * 
     * @param code 微信授权码
     * @return 登录结果
     */
    @GetMapping("/login")
    @ApiOperation("微信扫码登录")
    public Result<UserLoginVO> login(@RequestParam("code") String code) {
        log.info("微信扫码登录，授权码：{}", code);
        
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setCode(code);
        
        try {
            User user = userService.wxLogin(userLoginDTO);
            
            // 验证用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                return Result.error("账号已被禁用");
            }
            
            // 准备JWT中的claims
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.USER_TYPE, "regular"); // 微信登录用户为普通用户
            
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
                    .name(user.getWxNickname() != null ? user.getWxNickname() : user.getName())
                    .userType("regular")
                    .discountRate(BigDecimal.ONE) // 普通用户折扣率为1.0（无折扣）
                    .build();
            
            log.info("微信用户登录成功，生成token: {}", token);
            
            return Result.success(userLoginVO);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 生成微信扫码登录的URL
     * 
     * @return 微信登录URL
     */
    @GetMapping("/qrcode-url")
    @ApiOperation("生成微信扫码登录的URL")
    public Result<String> getQrcodeUrl() {
        // 配置为前端回调地址
        String redirectUri = "http://localhost:3001/wx-callback"; // 修改为实际的前端地址
        
        // 使用snsapi_base范围，这个范围不需要用户授权，只获取用户的openid
        String scope = "snsapi_base"; 
        
        log.info("生成微信扫码登录URL，appid: {}, redirectUri: {}", weChatProperties.getAppid(), redirectUri);
        
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + weChatProperties.getAppid() +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope +
                "&state=STATE#wechat_redirect";
        
        return Result.success(url);
    }
    
    /**
     * 【开发测试用】模拟微信扫码登录成功
     * 仅用于开发和测试环境，生产环境应禁用！
     * 
     * @return 模拟的授权码
     */
    @GetMapping("/mock-scan")
    @ApiOperation("模拟微信扫码登录（仅用于开发测试）")
    public Result<Map<String, String>> mockWechatScan() {
        // 生成一个随机的模拟授权码
        String mockCode = "test_" + UUID.randomUUID().toString().substring(0, 8);
        
        // 生成模拟的回调URL
        String callbackUrl = "http://localhost:3001/wx-callback?code=" + mockCode + "&state=STATE";
        
        log.info("生成模拟微信授权码: {}", mockCode);
        
        // 在用户服务中提前注册这个授权码，确保wxLogin可以识别
        // 这步非常重要，否则wxLogin无法处理这个模拟码
        try {
            // 创建一个模拟的openid
            String mockOpenid = "mock_wx_" + mockCode;
            
            // 将模拟授权码和openid的映射存储在一个静态Map中
            WechatMockData.addMockCode(mockCode, mockOpenid);
            
            log.info("模拟授权码已注册: {} -> {}", mockCode, mockOpenid);
            
            Map<String, String> result = new HashMap<>();
            result.put("code", mockCode);
            result.put("callback_url", callbackUrl);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建模拟授权码失败", e);
            return Result.error("创建模拟授权码失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存测试用的微信模拟数据
     */
    public static class WechatMockData {
        private static final Map<String, String> mockCodes = new HashMap<>();
        
        public static void addMockCode(String code, String openid) {
            mockCodes.put(code, openid);
        }
        
        public static String getOpenidByCode(String code) {
            return mockCodes.get(code);
        }
        
        public static boolean hasMockCode(String code) {
            return mockCodes.containsKey(code);
        }
    }
} 