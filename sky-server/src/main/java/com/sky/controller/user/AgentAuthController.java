package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Agent;
import com.sky.entity.User;
import com.sky.mapper.AgentMapper;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理商用户身份认证相关接口
 */
@RestController
@RequestMapping("/user/agent")
@Slf4j
@Api(tags = "代理商用户认证接口")
public class AgentAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private AgentMapper agentMapper;

    /**
     * 代理商用户登录
     * 
     * @param userLoginDTO 登录数据传输对象
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation("代理商用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("代理商用户登录:{}", userLoginDTO.getUsername());

        try {
            User user = userService.wxLogin(userLoginDTO);

            // 验证用户类型是否为代理商
            if (user.getUserType() == null || !"agent".equals(user.getUserType())) {
                return Result.error("请使用代理商账号登录");
            }

            // 准备JWT中的claims
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USER_TYPE, "agent");
            claims.put(JwtClaimsConstant.AGENT_ID, user.getAgentId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());

            // 创建token
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

            // 将相关信息存入BaseContext
            BaseContext.setCurrentId(user.getId());
            BaseContext.setCurrentUsername(user.getUsername());
            BaseContext.setCurrentUserType("agent");
            BaseContext.setCurrentAgentId(user.getAgentId());

            // 获取折扣率
            Agent agent = agentMapper.getById(user.getAgentId());
            BigDecimal discountRate = (agent != null && agent.getDiscountRate() != null) 
                    ? agent.getDiscountRate() 
                    : new BigDecimal("0.90"); // 默认9折

            // 构建登录响应VO
            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .token(token)
                    .username(user.getUsername())
                    .name(user.getName())
                    .userType("agent")
                    .agentId(user.getAgentId())
                    .discountRate(discountRate)
                    .build();

            log.info("代理商用户登录成功，生成token: {}", token);
            return Result.success(userLoginVO);
        } catch (Exception e) {
            log.error("代理商用户登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取代理商用户折扣率
     * 
     * @return 折扣率
     */
    @GetMapping("/discount-rate")
    @ApiOperation("获取代理商用户折扣率")
    public Result<BigDecimal> getDiscountRate(@RequestParam(required = false) Long agentId) {
        // 如果没有传入agentId，则从当前上下文中获取
        if (agentId == null) {
            agentId = BaseContext.getCurrentAgentId();
            if (agentId == null) {
                return Result.error("未找到代理商信息");
            }
        }
        
        // 获取代理商信息
        Agent agent = agentMapper.getById(agentId);
        if (agent == null) {
            return Result.error("代理商不存在");
        }
        
        // 返回折扣率
        BigDecimal discountRate = agent.getDiscountRate();
        if (discountRate == null) {
            discountRate = new BigDecimal("0.90"); // 默认9折
        }
        
        return Result.success(discountRate);
    }
} 