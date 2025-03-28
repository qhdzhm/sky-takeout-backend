package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.AgentLoginDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Agent;
import com.sky.entity.User;
import com.sky.mapper.AgentMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.AgentService;
import com.sky.service.DiscountService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.AgentLoginVO;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;

/**
 * 代理商Controller
 */
@RestController
@RequestMapping("/agent")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class AgentController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private DiscountService discountService;
    
    @Autowired
    private HttpServletRequest request;
    
    /**
     * 代理商登录
     * @param agentLoginDTO 登录DTO
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<AgentLoginVO> login(@RequestBody AgentLoginDTO agentLoginDTO) {
        log.info("代理商登录：{}", agentLoginDTO);
        
        try {
            // 1. 直接从代理商表查询用户信息
            Agent agent = agentMapper.getByUsername(agentLoginDTO.getUsername());
            if (agent == null) {
                log.error("代理商账号不存在：{}", agentLoginDTO.getUsername());
                return Result.error("代理商账号不存在");
            }
            
            // 2. 验证代理商密码 - 特殊处理测试账号
            if ("agent1".equals(agentLoginDTO.getUsername()) || 
                "agent2".equals(agentLoginDTO.getUsername()) || 
                "agent3".equals(agentLoginDTO.getUsername())) {
                if (!"123456".equals(agentLoginDTO.getPassword())) {
                    log.error("代理商密码错误");
                    return Result.error("代理商密码错误");
                }
            } else {
                // 正常密码验证（MD5加密验证）
                String password = DigestUtils.md5DigestAsHex(agentLoginDTO.getPassword().getBytes());
                if (!password.equals(agent.getPassword())) {
                    log.error("代理商密码错误");
                    return Result.error("代理商密码错误");
                }
            }
            
            // 3. 检查代理商状态
            if (agent.getStatus() == 0) {
                log.error("代理商账号已被禁用");
                return Result.error("代理商账号已被禁用");
            }
            
            // 4. 创建JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, agent.getId());
            claims.put(JwtClaimsConstant.AGENT_ID, agent.getId());
            claims.put("username", agent.getUsername());
            claims.put("userType", "agent");
            
            String token = JwtUtil.createJWT(
                    jwtProperties.getAgentSecretKey(),
                    jwtProperties.getAgentTtl(),
                    claims);
            
            // 5. 设置BaseContext中的用户信息
            BaseContext.setCurrentId(agent.getId());
            BaseContext.setCurrentUsername(agent.getUsername());
            BaseContext.setCurrentUserType("agent");
            BaseContext.setCurrentAgentId(agent.getId());
            
            // 6. 构建返回结果对象
            AgentLoginVO agentLoginVO = AgentLoginVO.builder()
                    .id(agent.getId())
                    .companyName(agent.getCompanyName())
                    .contactPerson(agent.getContactPerson())
                    .username(agent.getUsername())
                    .discountRate(agent.getDiscountRate())
                    .token(token)
                    .build();
            
            log.info("代理商登录成功：{}，折扣率：{}", agent.getUsername(), agent.getDiscountRate());
            
            // 7. 返回结果
            return Result.success(agentLoginVO);
            
        } catch (Exception e) {
            log.error("代理商登录异常", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取代理商折扣率
     * @return 折扣率
     */
    @GetMapping("/discount-rate")
    public Result<BigDecimal> getDiscountRate(@RequestParam(required = false) Long agentId) {
        log.info("获取代理商折扣率，请求参数 agentId={}", agentId);
        
        try {
            // 首先尝试使用请求参数中的agentId
            if (agentId != null) {
                Agent agent = agentMapper.getById(agentId);
                if (agent == null) {
                    return Result.error("代理商不存在");
                }
                
                BigDecimal discountRate = agent.getDiscountRate();
                if (discountRate == null) {
                    discountRate = new BigDecimal("0.9"); // 默认折扣率
                }
                
                log.info("获取代理商折扣率成功，代理商ID={}, 折扣率={}", agentId, discountRate);
                return Result.success(discountRate);
            }
            
            // 从JWT令牌中获取代理商ID
            String token = extractToken(request);
            if (token == null) {
                return Result.error("未提供有效的认证信息");
            }
            
            try {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getAgentSecretKey(), token);
                // 从JWT中获取代理商ID
                Object agentIdObj = claims.get(JwtClaimsConstant.AGENT_ID);
                if (agentIdObj != null) {
                    Long agentIdFromToken = Long.valueOf(agentIdObj.toString());
                    Agent agent = agentMapper.getById(agentIdFromToken);
                    if (agent == null) {
                        return Result.error("代理商不存在");
                    }
                    
                    BigDecimal discountRate = agent.getDiscountRate();
                    if (discountRate == null) {
                        discountRate = new BigDecimal("0.9"); // 默认折扣率
                    }
                    
                    log.info("通过JWT获取代理商折扣率成功，代理商ID={}, 折扣率={}", agentIdFromToken, discountRate);
                    return Result.success(discountRate);
                }
            } catch (Exception e) {
                log.error("解析JWT失败", e);
                return Result.error("无效的令牌");
            }
            
            // 尝试从BaseContext获取
            Long currentAgentId = BaseContext.getCurrentAgentId();
            if (currentAgentId != null) {
                Agent agent = agentMapper.getById(currentAgentId);
                if (agent != null) {
                    BigDecimal discountRate = agent.getDiscountRate();
                    if (discountRate == null) {
                        discountRate = new BigDecimal("0.9"); // 默认折扣率
                    }
                    
                    log.info("通过BaseContext获取代理商折扣率成功，代理商ID={}, 折扣率={}", currentAgentId, discountRate);
                    return Result.success(discountRate);
                }
            }
            
            return Result.error("无法获取代理商信息");
        } catch (Exception e) {
            log.error("获取代理商折扣率异常", e);
            return Result.error("获取折扣率失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据代理商ID获取折扣率
     * @param agentId 代理商ID
     * @return 折扣率
     */
    @GetMapping("/{agentId}/discount-rate")
    public Result<Map<String, Object>> getDiscountRateById(@PathVariable Long agentId) {
        log.info("根据ID获取代理商折扣率，代理商ID={}", agentId);
        
        try {
            // 查询代理商信息
            Agent agent = agentMapper.getById(agentId);
            if (agent == null) {
                log.warn("代理商不存在，ID={}", agentId);
                return Result.error("代理商不存在");
            }
            
            // 获取折扣率，默认为0.9（9折）
            BigDecimal discountRate = agent.getDiscountRate();
            if (discountRate == null) {
                discountRate = new BigDecimal("0.9");
            }
            
            // 计算折扣百分比（用于前端显示）
            BigDecimal savedPercent = BigDecimal.ONE.subtract(discountRate).multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("discountRate", discountRate);
            result.put("savedPercent", savedPercent);
            result.put("agentId", agentId);
            
            log.info("获取代理商折扣率成功，代理商ID={}，折扣率={}，节省百分比={}%", agentId, discountRate, savedPercent);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取代理商折扣率异常", e);
            return Result.error("获取折扣率失败：" + e.getMessage());
        }
    }
    
    /**
     * 从请求中提取token
     * @param request HTTP请求
     * @return 提取的token或null
     */
    private String extractToken(HttpServletRequest request) {
        // 按照优先级从不同的请求头中获取token
        String token = request.getHeader("token");
        if (token != null) return token;
        
        token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        
        token = request.getHeader("Authentication");
        if (token != null) return token;
        
        token = request.getHeader(jwtProperties.getAgentTokenName());
        return token;
    }
    
    /**
     * 计算特定价格的折扣价
     * @param originalPrice 原始价格
     * @param tourId 旅游产品ID（可选）
     * @param tourType 旅游产品类型（可选，day_tour或group_tour）
     * @return 折扣后的价格
     */
    @PostMapping("/calculate-discount")
    public Result<Map<String, Object>> calculateDiscount(
            @RequestParam BigDecimal originalPrice,
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false) String tourType) {
        
        log.info("计算折扣价格，原价：{}，产品ID：{}，产品类型：{}", originalPrice, tourId, tourType);
        
        // 验证价格有效性
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("价格无效");
        }
        
        try {
            // 获取当前代理商ID
            Long agentId = BaseContext.getCurrentAgentId();
            if (agentId == null) {
                // 尝试从当前用户上下文获取
                Long userId = BaseContext.getCurrentId();
                if (userId == null) {
                    log.warn("未登录用户尝试计算折扣价格");
                    return Result.error("未登录，无法计算折扣价格");
                }
                
                // 查询用户信息
                User user = userService.getById(userId);
                if (user == null || !"agent".equals(user.getUserType())) {
                    log.warn("非代理商用户尝试计算折扣价格");
                    return Result.error("非代理商用户无法享受折扣价");
                }
                
                // 获取关联的代理商ID
                agentId = user.getAgentId();
                if (agentId == null) {
                    log.warn("代理商用户未关联代理商ID");
                    return Result.error("未找到关联的代理商信息");
                }
            }
            
            // 查询代理商信息获取折扣率
            Agent agent = agentMapper.getById(agentId);
            if (agent == null) {
                log.warn("代理商不存在，ID：{}", agentId);
                return Result.error("代理商不存在");
            }
            
            // 获取折扣率，默认为0.9（9折）
            BigDecimal discountRate = agent.getDiscountRate();
            if (discountRate == null) {
                discountRate = new BigDecimal("0.9");
            }
            
            // 计算折扣价格（四舍五入保留两位小数）
            BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal savedAmount = originalPrice.subtract(discountedPrice).setScale(2, RoundingMode.HALF_UP);
            
            log.info("折扣计算完成，代理商：{}，原价：{}，折扣率：{}，折扣价：{}，节省：{}", 
                    agent.getUsername(), originalPrice, discountRate, discountedPrice, savedAmount);
            
            // 构建响应结果
            Map<String, Object> result = new HashMap<>();
            result.put("originalPrice", originalPrice);
            result.put("discountRate", discountRate);
            result.put("discountedPrice", discountedPrice);
            result.put("savedAmount", savedAmount);
            result.put("tourId", tourId);
            result.put("tourType", tourType);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("计算折扣价格异常", e);
            return Result.error("折扣计算失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量计算价格的折扣价
     * @param prices 原始价格列表
     * @return 折扣后的价格列表
     */
    @PostMapping("/calculate-discounts")
    public Result<Map<String, BigDecimal>> calculateDiscounts(@RequestBody Map<String, BigDecimal> prices) {
        log.info("批量计算折扣价格，原价列表：{}", prices);
        
        // 从上下文中获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("未登录，无法计算折扣价格");
        }
        
        // 查询用户信息
        User user = userService.getById(userId);
        if (user == null || !"agent".equals(user.getUserType())) {
            return Result.error("非代理商账号，无法计算折扣价格");
        }
        
        // 获取代理商ID
        Long agentId = user.getAgentId();
        
        // 计算每个价格的折扣价
        Map<String, BigDecimal> discountedPrices = new HashMap<>();
        
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            BigDecimal originalPrice = entry.getValue();
            BigDecimal discountedPrice = discountService.getDiscountedPrice(originalPrice, agentId);
            discountedPrices.put(entry.getKey(), discountedPrice);
        }
        
        log.info("批量计算折扣价格结果：{}", discountedPrices);
        return Result.success(discountedPrices);
    }
    
    /**
     * 计算旅游产品详情页的折扣价格
     * 此接口不需要JWT验证，直接通过agentId参数计算
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day-tour, group-tour)
     * @param originalPrice 原始价格
     * @param agentId 代理商ID
     * @return 折扣价格及折扣率信息
     */
    @GetMapping("/calculate-tour-discount")
    public Result<Map<String, Object>> calculateTourDiscount(
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false) String tourType,
            @RequestParam BigDecimal originalPrice,
            @RequestParam Long agentId) {
        
        log.info("计算旅游产品折扣价格，旅游ID：{}，类型：{}，原价：{}，代理商ID：{}", 
                tourId, tourType, originalPrice, agentId);
        
        try {
            // 获取代理商信息
            Agent agent = agentMapper.getById(agentId);
            if (agent == null) {
                return Result.error("代理商不存在");
            }
            
            String agentName = agent.getUsername();
            
            // 获取折扣率
            BigDecimal discountRate = agent.getDiscountRate();
            if (discountRate == null) {
                discountRate = new BigDecimal("0.9"); // 默认折扣率
            }
            
            // 计算折扣价格
            BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            
            // 计算节省金额
            BigDecimal savedAmount = originalPrice.subtract(discountedPrice);
            
            log.info("折扣计算详细信息 - 旅游产品ID: {}, 类型: {}, 代理商ID: {}, 代理商名: {}, " +
                    "折扣率: {}, 原价: {}, 折扣价: {}, 节省金额: {}",
                    tourId, tourType, agentId, agentName, discountRate, 
                    originalPrice, discountedPrice, savedAmount);
            
            // 记录折扣计算历史
            try {
                // 这里应该调用一个service方法来保存折扣计算历史
                // discountService.saveDiscountHistory(tourId, tourType, agentId, originalPrice, discountedPrice, discountRate);
            } catch (Exception e) {
                log.warn("保存折扣计算历史失败", e);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("tourId", tourId);
            result.put("tourType", tourType);
            result.put("originalPrice", originalPrice);
            result.put("discountedPrice", discountedPrice);
            result.put("discountRate", discountRate);
            result.put("savedAmount", savedAmount);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("计算旅游折扣价格失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("tourId", tourId);
            errorResult.put("tourType", tourType);
            errorResult.put("originalPrice", originalPrice);
            errorResult.put("discountedPrice", originalPrice); // 出错时返回原价
            errorResult.put("discountRate", BigDecimal.ONE);
            errorResult.put("savedAmount", BigDecimal.ZERO);
            errorResult.put("error", "计算折扣价格失败: " + e.getMessage());
            
            return Result.success(errorResult); // 返回成功但包含错误信息，避免前端异常
        }
    }

    /**
     * 批量计算旅游产品折扣价格
     * 优化接口，减少前端请求次数，提高性能
     * 
     * @param requests 请求列表，每个请求包含产品ID、类型、原价和代理商ID
     * @return 每个产品的折扣价格信息列表
     */
    @PostMapping("/calculate-tour-discounts-batch")
    public Result<List<Map<String, Object>>> calculateTourDiscountsBatch(
            @RequestBody List<Map<String, Object>> requests) {
        
        log.info("批量计算旅游产品折扣价格，请求数量：{}", requests.size());
        
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        for (Map<String, Object> request : requests) {
            try {
                // 提取请求参数
                Integer id = request.get("id") != null ? Integer.valueOf(request.get("id").toString()) : null;
                Long tourId = request.get("tourId") != null ? Long.valueOf(request.get("tourId").toString()) : null;
                String tourType = (String) request.get("tourType");
                BigDecimal originalPrice = new BigDecimal(request.get("originalPrice").toString());
                Long agentId = Long.valueOf(request.get("agentId").toString());
                
                // 计算折扣价格
                BigDecimal discountedPrice = discountService.getDiscountedPrice(originalPrice, agentId);
                
                // 获取折扣率
                BigDecimal discountRate = discountService.getAgentDiscountRate(agentId);
                
                // 计算节省金额
                BigDecimal savedAmount = originalPrice.subtract(discountedPrice);
                
                // 构建结果
                Map<String, Object> result = new HashMap<>();
                if (id != null) {
                    result.put("id", id); // 保留原始请求的ID，用于前端匹配
                }
                result.put("tourId", tourId);
                result.put("tourType", tourType);
                result.put("originalPrice", originalPrice);
                result.put("discountedPrice", discountedPrice);
                result.put("discountRate", discountRate);
                result.put("savedAmount", savedAmount);
                
                resultList.add(result);
            } catch (Exception e) {
                log.error("计算旅游折扣价格失败", e);
                // 创建错误结果
                Map<String, Object> errorResult = new HashMap<>();
                if (request.get("id") != null) {
                    errorResult.put("id", request.get("id"));
                }
                errorResult.put("error", "计算折扣价格失败: " + e.getMessage());
                errorResult.put("originalPrice", request.get("originalPrice"));
                errorResult.put("discountedPrice", request.get("originalPrice")); // 发生错误时使用原价
                errorResult.put("discountRate", BigDecimal.ONE);
                errorResult.put("savedAmount", BigDecimal.ZERO);
                
                resultList.add(errorResult);
            }
        }
        
        log.info("批量计算旅游产品折扣价格完成，结果数量：{}", resultList.size());
        return Result.success(resultList);
    }
} 