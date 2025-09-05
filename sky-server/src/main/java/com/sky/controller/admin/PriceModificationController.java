package com.sky.controller.admin;

import com.sky.dto.PriceModificationRequestDTO;
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
 * 管理端价格修改Controller
 */
@RestController
@RequestMapping("/admin/price-modification")
@Api(tags = "管理端价格修改接口")
@Slf4j
public class PriceModificationController {

    @Autowired
    private PriceModificationService priceModificationService;

    /**
     * 创建价格修改请求
     */
    @PostMapping("/create")
    @ApiOperation("创建价格修改请求")
    public Result<String> createPriceModificationRequest(@RequestBody PriceModificationRequestDTO requestDTO) {
        log.info("管理员创建价格修改请求: {}", requestDTO);
        String result = priceModificationService.createPriceModificationRequest(requestDTO);
        return Result.success(result);
    }

    /**
     * 分页查询价格修改请求
     */
    @GetMapping("/page")
    @ApiOperation("分页查询价格修改请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成，auto_processed-自动处理"),
            @ApiImplicitParam(name = "modificationType", value = "修改类型：increase-涨价，decrease-降价"),
            @ApiImplicitParam(name = "startDate", value = "开始日期 yyyy-MM-dd"),
            @ApiImplicitParam(name = "endDate", value = "结束日期 yyyy-MM-dd"),
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true)
    })
    public Result<PageResult> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String modificationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("管理端分页查询价格修改请求: status={}, modificationType={}, startDate={}, endDate={}, page={}, pageSize={}", 
                status, modificationType, startDate, endDate, page, pageSize);

        PageResult pageResult = priceModificationService.pageQuery(
                status, modificationType, startDate, endDate, page, pageSize);

        return Result.success(pageResult);
    }

    /**
     * 根据ID查询价格修改请求详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询价格修改请求详情")
    public Result<PriceModificationVO> getById(@PathVariable Long id) {
        log.info("查询价格修改请求详情，ID: {}", id);
        PriceModificationVO vo = priceModificationService.getById(id);
        if (vo != null) {
            return Result.success(vo);
        } else {
            return Result.error("价格修改请求不存在");
        }
    }
}
