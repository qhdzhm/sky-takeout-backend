package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.AgentLoginDTO;
import com.sky.entity.Agent;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.AgentService;
import com.sky.utils.JwtUtil;
import com.sky.vo.AgentLoginVO;
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
 * 代理商认证控制器
 */
@RestController
@RequestMapping("/user/agent")
@Api(tags = "代理商认证接口")
@Slf4j
public class AgentAuthController {

    @Autowired
    private AgentService agentService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 代理商登录
     *
     * @param agentLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("代理商登录")
    public Result<AgentLoginVO> login(@RequestBody AgentLoginDTO agentLoginDTO, HttpServletResponse response) {
        log.info("代理商登录：{}", agentLoginDTO);

        Agent agent = agentService.login(agentLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, agent.getId());
        claims.put(JwtClaimsConstant.AGENT_ID, agent.getId());
        claims.put(JwtClaimsConstant.USERNAME, agent.getUsername());
        claims.put(JwtClaimsConstant.USER_TYPE, "agent");
        
        String token = JwtUtil.createJWT(
                jwtProperties.getAgentSecretKey(),
                jwtProperties.getAgentTtl(),
                claims);

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
        userInfo.put("id", agent.getId());
        userInfo.put("username", agent.getUsername());
        userInfo.put("name", agent.getCompanyName());
        userInfo.put("userType", "agent");
        userInfo.put("role", "agent");
        userInfo.put("agentId", agent.getId());
        userInfo.put("isAuthenticated", true);
        userInfo.put("discountRate", agent.getDiscountRate() != null ? agent.getDiscountRate() : 0.95);
        userInfo.put("canSeeDiscount", true);
        userInfo.put("canSeeCredit", true);
        
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

        AgentLoginVO agentLoginVO = AgentLoginVO.builder()
                .id(agent.getId())
                .username(agent.getUsername())
                .companyName(agent.getCompanyName())
                .token(token)
                .build();

        return Result.success(agentLoginVO);
    }
} 

