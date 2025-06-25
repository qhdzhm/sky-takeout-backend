package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.CreditRepaymentDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AgentCreditService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理商信用额度控制器
 */
@RestController
@RequestMapping("/agent/credit")
@Api(tags = "代理商信用额度相关接口")
@Slf4j
public class AgentCreditController {

    @Autowired
    private AgentCreditService agentCreditService;

    /**
     * 获取当前代理商的信用额度信息
     * @return 信用额度信息
     */
    @GetMapping("/info")
    @ApiOperation("获取信用额度信息")
    public Result<Map<String, Object>> getCreditInfo(@RequestParam(required = false) Long agentId) {
        String userType = BaseContext.getCurrentUserType();
        Long targetAgentId;
        
        // 如果是操作员，获取所属代理商的信用额度信息
        if ("agent_operator".equals(userType)) {
            targetAgentId = BaseContext.getCurrentAgentId();
            if (targetAgentId == null) {
                return Result.error("无法获取代理商信息");
            }
            log.info("操作员获取代理商信用额度信息, 操作员ID: {}, 代理商ID: {}", BaseContext.getCurrentId(), targetAgentId);
        } else {
            // 代理商主账号，使用传入的agentId或当前用户ID
            targetAgentId = agentId != null ? agentId : BaseContext.getCurrentId();
            log.info("代理商获取信用额度信息, 代理商ID: {}", targetAgentId);
        }
        
        AgentCreditVO creditInfo = agentCreditService.getCreditInfo(targetAgentId);
        
        // 构建响应数据 - 根据用户类型返回不同的数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", creditInfo.getId());
        responseData.put("agentId", creditInfo.getAgentId());
        responseData.put("agentName", creditInfo.getAgentName());
        responseData.put("usagePercentage", creditInfo.getUsagePercentage());
        responseData.put("isFrozen", creditInfo.getIsFrozen());
        responseData.put("lastUpdated", creditInfo.getLastUpdated());
        
        // 操作员只能看到使用率和基本状态，不能看到具体金额
        if (!"agent_operator".equals(userType)) {
            // 代理商主账号可以看到所有信息
            responseData.put("totalCredit", creditInfo.getTotalCredit());
            responseData.put("usedCredit", creditInfo.getUsedCredit());
            responseData.put("availableCredit", creditInfo.getAvailableCredit());
            responseData.put("depositBalance", creditInfo.getDepositBalance());
            responseData.put("creditRating", creditInfo.getCreditRating());
            responseData.put("interestRate", creditInfo.getInterestRate());
            responseData.put("billingCycleDay", creditInfo.getBillingCycleDay());
            responseData.put("lastSettlementDate", creditInfo.getLastSettlementDate());
            responseData.put("overdraftCount", creditInfo.getOverdraftCount());
            responseData.put("createdAt", creditInfo.getCreatedAt());
        } else {
            // 操作员可以看到基本信用信息以支持支付功能，但隐藏敏感的财务详情
            responseData.put("totalCredit", creditInfo.getTotalCredit());     // 显示总额度
            responseData.put("availableCredit", creditInfo.getAvailableCredit()); // 显示可用额度（支付功能需要）
            responseData.put("usedCredit", null);                            // 隐藏已用额度
            responseData.put("depositBalance", null);                        // 隐藏押金余额
            responseData.put("creditRating", null);                          // 隐藏信用评级
            responseData.put("interestRate", null);                          // 隐藏利率
            responseData.put("billingCycleDay", null);                       // 隐藏账单周期
            responseData.put("lastSettlementDate", null);                    // 隐藏结算日期
            responseData.put("overdraftCount", null);                        // 隐藏透支次数
            responseData.put("createdAt", null);                             // 隐藏创建时间
            log.info("操作员访问，显示基本额度信息以支持支付功能，已隐藏敏感财务详情");
        }
        
        // 如果是代理商主账号，添加最近交易记录
        if (!"agent_operator".equals(userType)) {
            try {
                List<CreditTransactionVO> recentTransactions = agentCreditService.getCreditTransactions(
                    targetAgentId, null, null, null, 1, 5);
                responseData.put("recentTransactions", recentTransactions);
            } catch (Exception e) {
                log.warn("获取最近交易记录失败: {}", e.getMessage());
                responseData.put("recentTransactions", new ArrayList<>());
            }
        } else {
            // 操作员不能看到交易记录
            responseData.put("recentTransactions", null);
            log.info("操作员访问，已隐藏交易记录信息");
        }
        
        return Result.success(responseData);
    }

    /**
     * 获取信用交易记录
     * @param page 页码
     * @param pageSize 每页记录数
     * @param type 交易类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易记录分页结果
     */
    @GetMapping("/transactions")
    @ApiOperation("获取信用交易记录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "java.lang.Integer", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true, dataType = "java.lang.Integer", paramType = "query"),
        @ApiImplicitParam(name = "type", value = "交易类型(payment/topup)", dataType = "java.lang.String", paramType = "query"),
        @ApiImplicitParam(name = "startDate", value = "开始日期(yyyy-MM-dd)", dataType = "java.time.LocalDate", paramType = "query"),
        @ApiImplicitParam(name = "endDate", value = "结束日期(yyyy-MM-dd)", dataType = "java.time.LocalDate", paramType = "query")
    })
    public Result<PageResult> getTransactionHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // 检查当前用户是否为操作员
        String userType = BaseContext.getCurrentUserType();
        if ("agent_operator".equals(userType)) {
            return Result.error("操作员无权限查看信用交易记录");
        }
        
        // 从线程上下文中获取当前登录代理商ID
        Long agentId = BaseContext.getCurrentId();
        log.info("获取信用交易记录, 代理商ID: {}, 页码: {}, 每页记录数: {}, 类型: {}, 开始日期: {}, 结束日期: {}", 
                agentId, page, pageSize, type, startDate, endDate);
        
        List<CreditTransactionVO> records = agentCreditService.getCreditTransactions(
                agentId, type, startDate, endDate, page, pageSize);
        int total = agentCreditService.countCreditTransactions(agentId, type, startDate, endDate);
        
        PageResult pageResult = new PageResult(total, records);
        return Result.success(pageResult);
    }

    /**
     * 使用信用额度支付订单
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    @PostMapping("/payment")
    @ApiOperation("使用信用额度支付订单")
    public Result<CreditPaymentResultVO> payWithCredit(@RequestBody CreditPaymentDTO paymentDTO) {
        // 操作员可以使用信用支付，但使用的是代理商的credit
        // 从线程上下文中获取代理商ID（对于操作员，这是所属代理商的ID）
        Long agentId = BaseContext.getCurrentAgentId();
        String userType = BaseContext.getCurrentUserType();
        
        if (agentId == null) {
            return Result.error("无法获取代理商信息");
        }
        
        log.info("使用信用额度支付订单, 用户类型: {}, 代理商ID: {}, 支付信息: {}", userType, agentId, paymentDTO);
        
        CreditPaymentResultVO result = agentCreditService.payWithCredit(agentId, paymentDTO);
        return Result.success(result);
    }

    /**
     * 检查信用额度是否足够支付
     * @param bookingId 订单ID
     * @param amount 金额
     * @return 检查结果
     */
    @GetMapping("/payment/check")
    @ApiOperation("检查信用额度是否足够支付")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "bookingId", value = "订单ID", required = true, dataType = "java.lang.Long", paramType = "query"),
        @ApiImplicitParam(name = "amount", value = "支付金额", required = true, dataType = "java.math.BigDecimal", paramType = "query")
    })
    public Result<CreditCheckResultVO> checkCreditBalance(
            @RequestParam Long bookingId,
            @RequestParam BigDecimal amount) {
        // 操作员可以检查信用额度，但检查的是代理商的credit
        Long agentId = BaseContext.getCurrentAgentId();
        String userType = BaseContext.getCurrentUserType();
        
        if (agentId == null) {
            return Result.error("无法获取代理商信息");
        }
        
        log.info("检查信用额度是否足够支付, 用户类型: {}, 代理商ID: {}, 订单ID: {}, 金额: {}", userType, agentId, bookingId, amount);
        
        CreditCheckResultVO result = agentCreditService.checkCreditPayment(agentId, bookingId, amount);
        return Result.success(result);
    }
    
    /**
     * 信用额度还款
     * @param repaymentDTO 还款信息
     * @return 还款结果
     */
    @PostMapping("/repayment")
    @ApiOperation("信用额度还款")
    public Result<CreditRepaymentResultVO> repayCredit(@RequestBody CreditRepaymentDTO repaymentDTO) {
        // 检查当前用户是否为操作员
        String userType = BaseContext.getCurrentUserType();
        if ("agent_operator".equals(userType)) {
            return Result.error("操作员无权限进行信用额度还款");
        }
        
        // 从线程上下文中获取当前登录代理商ID
        Long agentId = BaseContext.getCurrentId();
        log.info("信用额度还款, 代理商ID: {}, 还款信息: {}", agentId, repaymentDTO);
        
        CreditRepaymentResultVO result = agentCreditService.repayCredit(agentId, repaymentDTO);
        return Result.success(result);
    }
} 