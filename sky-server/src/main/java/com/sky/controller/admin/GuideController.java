package com.sky.controller.admin;

import com.sky.dto.GuidePageQueryDTO;
import com.sky.entity.Guide;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.GuideService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 导游管理
 */
@RestController
@RequestMapping("/admin/guides")
@Api(tags = "导游管理相关接口")
@Slf4j
public class GuideController {

    @Autowired
    private GuideService guideService;

    /**
     * 导游分页查询
     */
    @GetMapping
    @ApiOperation("导游分页查询")
    public Result<PageResult> pageQuery(GuidePageQueryDTO guidePageQueryDTO) {
        log.info("导游分页查询：{}", guidePageQueryDTO);
        
        PageResult pageResult = guideService.pageQuery(guidePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 通过员工ID获取导游信息
     */
    @GetMapping("/by-employee/{employeeId}")
    @ApiOperation("通过员工ID获取导游信息")
    public Result<Guide> getGuideByEmployeeId(@PathVariable Long employeeId) {
        log.info("通过员工ID获取导游信息，员工ID：{}", employeeId);
        
        Guide guide = guideService.getGuideByEmployeeId(employeeId);
        return Result.success(guide);
    }

    /**
     * 修复导游和员工的关联关系（临时接口）
     */
    @PostMapping("/fix-employee-relation")
    @ApiOperation("修复导游和员工的关联关系")
    public Result fixEmployeeRelation() {
        log.info("开始修复导游和员工的关联关系");
        
        try {
            guideService.fixEmployeeRelation();
            return Result.success("修复成功");
        } catch (Exception e) {
            log.error("修复失败", e);
            return Result.error("修复失败：" + e.getMessage());
        }
    }

    /**
     * 同步导游表数据到员工表（临时接口）
     */
    @PostMapping("/sync-to-employees")
    @ApiOperation("同步导游表数据到员工表")
    public Result syncGuidesToEmployees() {
        log.info("开始同步导游表数据到员工表");
        
        try {
            guideService.syncGuidesToEmployees();
            return Result.success("同步成功");
        } catch (Exception e) {
            log.error("同步失败", e);
            return Result.error("同步失败：" + e.getMessage());
        }
    }
} 