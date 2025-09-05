package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.AgentPriceResponseDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.PriceModificationService;
import com.sky.vo.PriceModificationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 代理商端价格修改Controller
 */
@RestController
@RequestMapping("/agent/price-modifications")
@Api(tags = "代理商端价格修改接口")
@Slf4j
public class AgentPriceModificationController {

    @Autowired
    private PriceModificationService priceModificationService;

    /**
     * 代理商响应价格修改请求
     */
    @PostMapping("/response")
    @ApiOperation("代理商响应价格修改请求")
    public Result<String> responseToRequest(@RequestBody AgentPriceResponseDTO responseDTO) {
        log.info("代理商响应价格修改请求: {}", responseDTO);
        String result = priceModificationService.agentResponseToRequest(responseDTO);
        return Result.success(result);
    }

    /**
     * 代理商查询特定订单的价格修改请求
     */
    @GetMapping
    @ApiOperation("代理商查询特定订单的价格修改请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bookingId", value = "订单ID", required = true),
            @ApiImplicitParam(name = "status", value = "状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成，auto_processed-自动处理"),
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true)
    })
    public Result<PageResult> getByBookingId(
            @RequestParam Long bookingId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 获取当前代理商ID
        Long agentId = BaseContext.getCurrentAgentId();
        log.info("代理商查询特定订单的价格修改请求: agentId={}, bookingId={}, status={}, page={}, pageSize={}", 
                agentId, bookingId, status, page, pageSize);

        PageResult pageResult = priceModificationService.pageQueryByBookingId(
                agentId, bookingId, status, page, pageSize);

        return Result.success(pageResult);
    }

    /**
     * 代理商分页查询价格修改请求
     */
    @GetMapping("/page")
    @ApiOperation("代理商分页查询价格修改请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成，auto_processed-自动处理"),
            @ApiImplicitParam(name = "startDate", value = "开始日期 yyyy-MM-dd"),
            @ApiImplicitParam(name = "endDate", value = "结束日期 yyyy-MM-dd"),
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true)
    })
    public Result<PageResult> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 获取当前代理商ID
        Long agentId = BaseContext.getCurrentAgentId();
        log.info("代理商分页查询价格修改请求: agentId={}, status={}, startDate={}, endDate={}, page={}, pageSize={}", 
                agentId, status, startDate, endDate, page, pageSize);

        PageResult pageResult = priceModificationService.pageQueryByAgent(
                agentId, status, startDate, endDate, page, pageSize);

        return Result.success(pageResult);
    }

    /**
     * 根据ID查询价格修改请求详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询价格修改请求详情")
    public Result<PriceModificationVO> getById(@PathVariable Long id) {
        log.info("代理商查询价格修改请求详情，ID: {}", id);
        PriceModificationVO vo = priceModificationService.getById(id);
        if (vo != null) {
            return Result.success(vo);
        } else {
            return Result.error("价格修改请求不存在");
        }
    }


}
