package com.sky.controller.admin;

import com.sky.dto.EmployeeGuideDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeGuideService;
import com.sky.vo.EmployeeGuideVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工-导游管理控制器
 */
@RestController
@RequestMapping("/admin/employee-guide")
@Api(tags = "员工-导游管理接口")
@Slf4j
public class EmployeeGuideController {

    @Autowired
    private EmployeeGuideService employeeGuideService;

    /**
     * 分页查询员工-导游信息
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工-导游信息")
    public Result<PageResult> page(EmployeeGuideDTO queryDTO) {
        log.info("分页查询员工-导游信息：{}", queryDTO);
        
        // 设置默认分页参数
        if (queryDTO.getPage() == null) {
            queryDTO.setPage(1);
        }
        if (queryDTO.getPageSize() == null) {
            queryDTO.setPageSize(10);
        }
        
        PageResult pageResult = employeeGuideService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 将员工设置为导游
     */
    @PostMapping("/set-guide")
    @ApiOperation("将员工设置为导游")
    public Result<Long> setEmployeeAsGuide(@RequestBody EmployeeGuideDTO employeeGuideDTO) {
        log.info("将员工设置为导游：{}", employeeGuideDTO);
        
        Long guideId = employeeGuideService.setEmployeeAsGuide(employeeGuideDTO);
        return Result.success(guideId);
    }

    /**
     * 取消员工的导游身份
     */
    @DeleteMapping("/remove-guide/{employeeId}")
    @ApiOperation("取消员工的导游身份")
    public Result<String> removeGuideRole(@PathVariable Long employeeId) {
        log.info("取消员工的导游身份：employeeId={}", employeeId);
        
        employeeGuideService.removeGuideRole(employeeId);
        return Result.success("取消导游身份成功");
    }

    /**
     * 更新导游信息
     */
    @PutMapping("/update-guide")
    @ApiOperation("更新导游信息")
    public Result<String> updateGuideInfo(@RequestBody EmployeeGuideDTO employeeGuideDTO) {
        log.info("更新导游信息：{}", employeeGuideDTO);
        
        employeeGuideService.updateGuideInfo(employeeGuideDTO);
        return Result.success("更新导游信息成功");
    }

    /**
     * 根据员工ID获取导游信息
     */
    @GetMapping("/guide/{employeeId}")
    @ApiOperation("根据员工ID获取导游信息")
    public Result<EmployeeGuideVO> getGuideByEmployeeId(@PathVariable Long employeeId) {
        log.info("根据员工ID获取导游信息：employeeId={}", employeeId);
        
        EmployeeGuideVO employeeGuideVO = employeeGuideService.getGuideByEmployeeId(employeeId);
        return Result.success(employeeGuideVO);
    }

    /**
     * 获取所有导游员工列表
     */
    @GetMapping("/guides")
    @ApiOperation("获取所有导游员工列表")
    public Result<List<EmployeeGuideVO>> getAllGuideEmployees() {
        log.info("获取所有导游员工列表");
        
        List<EmployeeGuideVO> guideEmployees = employeeGuideService.getAllGuideEmployees();
        return Result.success(guideEmployees);
    }

    /**
     * 批量设置导游可用性
     */
    @PostMapping("/availability/batch")
    @ApiOperation("批量设置导游可用性")
    public Result<String> batchSetGuideAvailability(
            @RequestParam Long guideId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        
        log.info("批量设置导游可用性：guideId={}, startDate={}, endDate={}, status={}", 
                 guideId, startDate, endDate, status);
        
        employeeGuideService.batchSetGuideAvailability(guideId, startDate, endDate, 
                                                       startTime, endTime, status, notes);
        return Result.success("批量设置可用性成功");
    }

    /**
     * 获取导游可用性统计
     */
    @GetMapping("/availability/stats")
    @ApiOperation("获取导游可用性统计")
    public Result<List<EmployeeGuideVO>> getGuideAvailabilityStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        log.info("获取导游可用性统计：date={}", date);
        
        List<EmployeeGuideVO> stats = employeeGuideService.getGuideAvailabilityStats(date);
        return Result.success(stats);
    }
} 