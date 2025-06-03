package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.PasswordChangeDTO;
import com.sky.entity.Agent;
import com.sky.entity.AgentOperator;
import com.sky.entity.User;
import com.sky.mapper.AgentMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.service.AgentService;
import com.sky.service.AgentOperatorService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
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
    private AgentService agentService;

    @Autowired
    private AgentOperatorService agentOperatorService;

    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private AgentMapper agentMapper;

    /**
     * 代理商用户登录（支持代理商主账号和操作员账号）
     * 
     * @param userLoginDTO 登录数据传输对象
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation("代理商用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("代理商登录：{}", userLoginDTO);

        try {
            // 首先尝试代理商主账号登录
            Agent agent = null;
            AgentOperator operator = null;
            boolean isOperator = false;
            
            // 先检查是否为代理商主账号
            agent = agentMapper.getByUsername(userLoginDTO.getUsername());
            if (agent != null) {
                // 验证代理商密码
                String inputPassword = DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes());
                if (inputPassword.equals(agent.getPassword())) {
                    // 检查代理商状态
                    if (agent.getStatus() == 0) {
                        return Result.error("代理商账号已被禁用");
                    }
                    log.info("代理商主账号登录成功: {}", userLoginDTO.getUsername());
                } else {
                    log.error("代理商密码错误：{}", userLoginDTO.getUsername());
                    agent = null; // 密码错误，置空
                }
            }
            
            // 如果代理商主账号登录失败，尝试操作员登录
            if (agent == null) {
                log.info("代理商主账号登录失败，尝试操作员登录: {}", userLoginDTO.getUsername());
                try {
                    operator = agentOperatorService.login(userLoginDTO);
                    if (operator != null) {
                        isOperator = true;
                        agent = agentMapper.getById(operator.getAgentId());
                        log.info("操作员登录成功: {}, 所属代理商ID: {}", userLoginDTO.getUsername(), operator.getAgentId());
                    }
                } catch (Exception e) {
                    log.error("操作员登录失败: {}", e.getMessage());
                }
            }
            
            if (agent == null) {
                log.error("代理商账号不存在：{}", userLoginDTO.getUsername());
                return Result.error("账号或密码错误");
            }

            // 准备JWT中的claims
            Map<String, Object> claims = new HashMap<>();
            
            if (isOperator) {
                // 操作员登录
                claims.put(JwtClaimsConstant.USER_ID, operator.getId());
                claims.put(JwtClaimsConstant.USERNAME, operator.getUsername());
                claims.put(JwtClaimsConstant.USER_TYPE, "agent_operator");
                claims.put(JwtClaimsConstant.AGENT_ID, operator.getAgentId());
                claims.put(JwtClaimsConstant.OPERATOR_ID, operator.getId());
            } else {
                // 代理商主账号登录
                claims.put(JwtClaimsConstant.USER_ID, agent.getId());
                claims.put(JwtClaimsConstant.USERNAME, agent.getUsername());
                claims.put(JwtClaimsConstant.USER_TYPE, "agent");
                claims.put(JwtClaimsConstant.AGENT_ID, agent.getId());
            }

            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

            // 设置上下文
            if (isOperator) {
                BaseContext.setCurrentId(operator.getId());
                BaseContext.setCurrentUsername(operator.getUsername());
                BaseContext.setCurrentUserType("agent_operator");
                BaseContext.setCurrentAgentId(operator.getAgentId());
                BaseContext.setCurrentOperatorId(operator.getId());
            } else {
                BaseContext.setCurrentId(agent.getId());
                BaseContext.setCurrentUsername(agent.getUsername());
                BaseContext.setCurrentUserType("agent");
                BaseContext.setCurrentAgentId(agent.getId());
            }

            // 获取折扣率
            BigDecimal discountRate = (agent.getDiscountRate() != null) 
                    ? agent.getDiscountRate() 
                    : new BigDecimal("0.90"); // 默认9折

            // 构建响应
            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(isOperator ? operator.getId() : agent.getId())
                    .token(token)
                    .username(isOperator ? operator.getUsername() : agent.getUsername())
                    .name(isOperator ? operator.getName() : agent.getCompanyName())
                    .userType(isOperator ? "agent_operator" : "agent")
                    .agentId(agent.getId().longValue())
                    .operatorId(isOperator ? operator.getId() : null)
                    .discountRate(discountRate)
                    .canSeeDiscount(!isOperator) // 操作员不能看到折扣
                    .canSeeCredit(!isOperator)   // 操作员不能看到信用额度
                    .build();

            log.info("登录成功，用户类型: {}, 生成token: {}", isOperator ? "操作员" : "代理商主账号", token);
            return Result.success(userLoginVO);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
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
        // 检查当前用户是否为操作员
        String userType = BaseContext.getCurrentUserType();
        if ("agent_operator".equals(userType)) {
            return Result.error("无权限查看折扣信息");
        }
        
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

    /**
     * 代理商修改密码
     * 
     * @param passwordChangeDTO 密码修改信息
     * @return 修改结果
     */
    @PutMapping("/password")
    @ApiOperation("代理商修改密码")
    public Result<String> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO) {
        log.info("代理商修改密码");
        
        String userType = BaseContext.getCurrentUserType();
        if ("agent_operator".equals(userType)) {
            // 操作员修改密码 - 这里可以扩展操作员密码修改逻辑
            return Result.error("操作员密码修改功能暂未开放");
        } else {
            // 代理商主账号修改密码
            Long agentId = BaseContext.getCurrentAgentId();
            if (agentId == null) {
                return Result.error("未找到代理商信息");
            }
            agentService.changePassword(agentId, passwordChangeDTO);
            return Result.success();
        }
    }
} 