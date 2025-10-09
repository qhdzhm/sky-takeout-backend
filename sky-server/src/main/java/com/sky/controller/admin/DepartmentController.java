package com.sky.controller.admin;

import com.sky.dto.DepartmentDTO;
import com.sky.result.Result;
import com.sky.service.DepartmentService;
import com.sky.vo.DepartmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理Controller
 */
@RestController
@RequestMapping("/admin/departments")
@Api(tags = "部门管理相关接口")
@Slf4j
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取所有部门列表
     */
    @GetMapping
    @ApiOperation("获取所有部门列表")
    public Result<List<DepartmentVO>> getAllDepartments() {
        log.info("获取所有部门列表");
        
        List<DepartmentVO> result = departmentService.getAllDepartments();
        return Result.success(result);
    }

    /**
     * 根据ID获取部门信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取部门信息")
    public Result<DepartmentVO> getDepartmentById(
            @ApiParam(name = "id", value = "部门ID", required = true)
            @PathVariable Long id) {
        log.info("根据ID获取部门信息：{}", id);
        
        DepartmentVO result = departmentService.getDepartmentById(id);
        return Result.success(result);
    }

    /**
     * 创建部门
     */
    @PostMapping
    @ApiOperation("创建部门")
    public Result<Long> createDepartment(@RequestBody DepartmentDTO departmentDTO) {
        log.info("创建部门：{}", departmentDTO);
        
        Long departmentId = departmentService.createDepartment(departmentDTO);
        return Result.success(departmentId);
    }

    /**
     * 更新部门信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新部门信息")
    public Result<Void> updateDepartment(
            @ApiParam(name = "id", value = "部门ID", required = true)
            @PathVariable Long id,
            @RequestBody DepartmentDTO departmentDTO) {
        log.info("更新部门信息，ID：{}，数据：{}", id, departmentDTO);
        
        departmentDTO.setId(id);
        departmentService.updateDepartment(departmentDTO);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除部门")
    public Result<Void> deleteDepartment(
            @ApiParam(name = "id", value = "部门ID", required = true)
            @PathVariable Long id) {
        log.info("删除部门：{}", id);
        
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    /**
     * 根据部门代码查询部门
     */
    @GetMapping("/by-code/{deptCode}")
    @ApiOperation("根据部门代码查询部门")
    public Result<DepartmentVO> getDepartmentByCode(
            @ApiParam(name = "deptCode", value = "部门代码", required = true)
            @PathVariable String deptCode) {
        log.info("根据部门代码查询部门：{}", deptCode);
        
        DepartmentVO result = departmentService.getDepartmentByCode(deptCode);
        return Result.success(result);
    }

    /**
     * 获取部门统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取部门统计信息")
    public Result<List<DepartmentVO>> getDepartmentStatistics() {
        log.info("获取部门统计信息");
        
        List<DepartmentVO> result = departmentService.getDepartmentStatistics();
        return Result.success(result);
    }
}

