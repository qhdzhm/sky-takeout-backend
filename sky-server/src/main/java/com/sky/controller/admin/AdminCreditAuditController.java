package com.sky.controller.admin;

import com.sky.mapper.PaymentAuditLogMapper;
import com.sky.entity.PaymentAuditLog;
import com.sky.result.PageResult;
import com.sky.result.Result;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/admin/audit")
@Api(tags = "支付审计-信用支付")
@Slf4j
public class AdminCreditAuditController {

    @Autowired
    private PaymentAuditLogMapper paymentAuditLogMapper;

    @GetMapping("/credit-logs")
    @ApiOperation("分页查询信用支付审计日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "agentId", value = "代理商ID"),
            @ApiImplicitParam(name = "action", value = "动作类型，如credit_payment/refund"),
            @ApiImplicitParam(name = "startDate", value = "开始日期 yyyy-MM-dd"),
            @ApiImplicitParam(name = "endDate", value = "结束日期 yyyy-MM-dd"),
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true)
    })
    public Result<PageResult> page(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LocalDateTime startTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        int offset = (page - 1) * pageSize;

        List<PaymentAuditLog> list = paymentAuditLogMapper.selectPage(agentId, action, startTime, endTime, pageSize, offset);
        int total = paymentAuditLogMapper.countPage(agentId, action, startTime, endTime);
        return Result.success(new PageResult(total, list));
    }

    @GetMapping("/credit-logs/sum")
    @ApiOperation("统计某代理商在时间区间内的信用支付总额")
    public Result<BigDecimal> sum(
            @RequestParam Long agentId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LocalDateTime startTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        BigDecimal sum = paymentAuditLogMapper.sumAmountByAgent(agentId, startTime, endTime);
        return Result.success(sum);
    }
}













