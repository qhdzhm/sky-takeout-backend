package com.sky.controller.admin;

import com.sky.dto.BatchPermissionDTO;
import com.sky.dto.PermissionAssignDTO;
import com.sky.result.Result;
import com.sky.service.PermissionManagementService;
import com.sky.vo.PermissionManagementVO;
import com.sky.vo.PositionPermissionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理Controller
 */
@RestController
@RequestMapping("/admin/permission-management")
@Api(tags = "权限管理相关接口")
@Slf4j
public class PermissionManagementController {

    @Autowired
    private PermissionManagementService permissionManagementService;

    /**
     * 获取权限管理概览数据
     */
    @GetMapping("/overview")
    @ApiOperation("获取权限管理概览数据")
    public Result<PermissionManagementVO> getPermissionManagementOverview() {
        log.info("获取权限管理概览数据");
        
        PermissionManagementVO result = permissionManagementService.getPermissionManagementOverview();
        return Result.success(result);
    }

    /**
     * 获取指定职位的权限配置
     */
    @GetMapping("/position/{positionId}/permissions")
    @ApiOperation("获取指定职位的权限配置")
    public Result<PositionPermissionVO> getPositionPermissions(
            @ApiParam(name = "positionId", value = "职位ID", required = true)
            @PathVariable Long positionId) {
        log.info("获取职位权限配置，职位ID：{}", positionId);
        
        PositionPermissionVO result = permissionManagementService.getPositionPermissions(positionId);
        return Result.success(result);
    }

    /**
     * 更新职位权限配置
     */
    @PutMapping("/position/{positionId}/permissions")
    @ApiOperation("更新职位权限配置")
    public Result<Void> updatePositionPermissions(
            @ApiParam(name = "positionId", value = "职位ID", required = true)
            @PathVariable Long positionId,
            @RequestBody PermissionAssignDTO permissionAssignDTO) {
        log.info("更新职位权限配置，职位ID：{}，权限配置：{}", positionId, permissionAssignDTO);
        
        permissionAssignDTO.setPositionId(positionId);
        permissionManagementService.updatePositionPermissions(permissionAssignDTO);
        return Result.success();
    }

    /**
     * 批量权限操作
     */
    @PostMapping("/batch-operation")
    @ApiOperation("批量权限操作")
    public Result<Void> batchPermissionOperation(@RequestBody BatchPermissionDTO batchPermissionDTO) {
        log.info("批量权限操作：{}", batchPermissionDTO);
        
        permissionManagementService.batchPermissionOperation(batchPermissionDTO);
        return Result.success();
    }

    /**
     * 复制职位权限
     */
    @PostMapping("/copy-permissions")
    @ApiOperation("复制职位权限")
    public Result<Void> copyPermissions(
            @ApiParam(name = "sourcePositionId", value = "源职位ID", required = true)
            @RequestParam Long sourcePositionId,
            @ApiParam(name = "targetPositionIds", value = "目标职位ID列表", required = true)
            @RequestParam List<Long> targetPositionIds) {
        log.info("复制职位权限，源职位：{}，目标职位：{}", sourcePositionId, targetPositionIds);
        
        permissionManagementService.copyPositionPermissions(sourcePositionId, targetPositionIds, null);
        return Result.success();
    }

    /**
     * 检查页面权限
     */
    @GetMapping("/check-permission")
    @ApiOperation("检查员工页面权限")
    public Result<Boolean> checkPagePermission(
            @ApiParam(name = "employeeId", value = "员工ID")
            @RequestParam(required = false) Long employeeId,
            @ApiParam(name = "pagePath", value = "页面路径", required = true)
            @RequestParam String pagePath) {
        log.info("检查页面权限，员工ID：{}，页面：{}", employeeId, pagePath);
        
        // 如果没有传员工ID，使用当前登录员工ID
        // 这里可以从BaseContext获取
        if (employeeId == null) {
            // employeeId = BaseContext.getCurrentId();
            return Result.error("员工ID不能为空");
        }
        
        Boolean hasPermission = permissionManagementService.checkEmployeePagePermission(employeeId, pagePath);
        return Result.success(hasPermission);
    }

    /**
     * 获取员工页面权限列表
     */
    @GetMapping("/employee/{employeeId}/permissions")
    @ApiOperation("获取员工页面权限列表")
    public Result<List<String>> getEmployeePermissions(
            @ApiParam(name = "employeeId", value = "员工ID", required = true)
            @PathVariable Long employeeId) {
        log.info("获取员工页面权限列表，员工ID：{}", employeeId);
        
        List<String> permissions = permissionManagementService.getEmployeePagePermissions(employeeId);
        return Result.success(permissions);
    }
}

