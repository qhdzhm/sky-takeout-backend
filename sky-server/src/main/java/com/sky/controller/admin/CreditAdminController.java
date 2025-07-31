package com.sky.controller.admin;

import com.sky.dto.AgentCreditDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AdminCreditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 管理员信用额度控制器
 */
@RestController
@RequestMapping("/admin/credits")
@Api(tags = "管理员信用额度相关接口")
@Slf4j
public class CreditAdminController {

    @Autowired
    private AdminCreditService adminCreditService;

    /**
     * 为代理商充值信用额度
     * @param agentId 代理商ID
     * @param amount 金额
     * @param remark 备注
     * @return 操作结果
     */
    @PostMapping("/topup/{agentId}")
    @ApiOperation("为代理商充值信用额度")
    public Result<Boolean> topupAgentCredit(
            @PathVariable Long agentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        log.info("管理员为代理商充值信用额度：agentId={}, amount={}, remark={}", agentId, amount, remark);
        boolean result = adminCreditService.topupCredit(agentId, amount, remark);
        return Result.success(result);
    }

    /**
     * 获取所有代理商信用额度信息
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 代理商信用额度信息列表
     */
    @GetMapping("/agents")
    @ApiOperation("获取所有代理商信用额度信息")
    public Result<PageResult> getAllAgentCredits(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String agentName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取所有代理商信用额度信息：agentId={}, agentName={}, page={}, pageSize={}", 
                agentId, agentName, page, pageSize);
        PageResult pageResult = adminCreditService.getAllAgentCredits(agentId, agentName, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 获取代理商信用额度详情
     * @param agentId 代理商ID
     * @return 代理商信用额度详情
     */
    @GetMapping("/agents/{agentId}")
    @ApiOperation("获取代理商信用额度详情")
    public Result getAgentCreditDetail(@PathVariable Long agentId) {
        log.info("获取代理商信用额度详情：agentId={}", agentId);
        return Result.success(adminCreditService.getAgentCreditDetail(agentId));
    }

    /**
     * 更新代理商信用额度信息
     * @param agentId 代理商ID
     * @param agentCreditDTO 信用额度更新数据
     * @return 操作结果
     */
    @PutMapping("/update/{agentId}")
    @ApiOperation("更新代理商信用额度信息")
    public Result<Boolean> updateAgentCredit(
            @PathVariable Long agentId,
            @RequestBody AgentCreditDTO agentCreditDTO) {
        log.info("更新代理商信用额度信息：agentId={}, data={}", agentId, agentCreditDTO);
        // 确保DTO中的agentId与路径参数一致
        agentCreditDTO.setAgentId(agentId);
        boolean result = adminCreditService.updateAgentCredit(agentCreditDTO);
        return Result.success(result);
    }

    /**
     * 获取信用交易记录
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 交易记录列表
     */
    @GetMapping("/transactions")
    @ApiOperation("获取信用交易记录")
    public Result<PageResult> getCreditTransactions(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String transactionNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取信用交易记录：agentId={}, transactionType={}, transactionNo={}, startDate={}, endDate={}, page={}, pageSize={}",
                agentId, transactionType, transactionNo, startDate, endDate, page, pageSize);
        PageResult pageResult = adminCreditService.getCreditTransactions(
                agentId, transactionType, transactionNo, startDate, endDate, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 获取信用交易统计数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param groupBy 分组依据：day, month, year
     * @return 统计数据
     */
    @GetMapping("/transactions/stats")
    @ApiOperation("获取信用交易统计数据")
    public Result<Map<String, Object>> getCreditTransactionStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") @ApiParam(value = "分组方式", allowableValues = "day,month,year") String groupBy) {
        log.info("获取信用交易统计数据：startDate={}, endDate={}, groupBy={}", startDate, endDate, groupBy);
        Map<String, Object> stats = adminCreditService.getCreditTransactionStats(startDate, endDate, groupBy);
        return Result.success(stats);
    }

    /**
     * 导出信用交易记录
     * @param response HTTP响应对象
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    @GetMapping("/transactions/export")
    @ApiOperation("导出信用交易记录")
    public void exportCreditTransactions(
            HttpServletResponse response,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String transactionNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("导出信用交易记录：agentId={}, transactionType={}, transactionNo={}, startDate={}, endDate={}",
                agentId, transactionType, transactionNo, startDate, endDate);
        adminCreditService.exportCreditTransactions(response, agentId, transactionType, transactionNo, startDate, endDate);
    }
} 