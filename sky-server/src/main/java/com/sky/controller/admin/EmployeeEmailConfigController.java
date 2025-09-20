package com.sky.controller.admin;

import com.sky.dto.EmployeeEmailConfigDTO;
import com.sky.entity.Employee;
import com.sky.result.Result;
import com.sky.service.EmployeeEmailConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 员工邮箱配置管理控制器
 */
@RestController
@RequestMapping("/admin/employee-email")
@Api(tags = "员工邮箱配置管理")
@Slf4j
public class EmployeeEmailConfigController {

    @Autowired
    private EmployeeEmailConfigService employeeEmailConfigService;

    /**
     * 配置员工邮箱
     */
    @PostMapping("/config")
    @ApiOperation("配置员工邮箱")
    public Result configEmployeeEmail(@Valid @RequestBody EmployeeEmailConfigDTO configDTO) {
        log.info("配置员工邮箱请求: {}", configDTO);
        
        try {
            employeeEmailConfigService.configEmployeeEmail(configDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("配置员工邮箱失败", e);
            return Result.error("配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工邮箱配置
     */
    @GetMapping("/config/{employeeId}")
    @ApiOperation("获取员工邮箱配置")
    public Result<Employee> getEmployeeEmailConfig(@PathVariable Long employeeId) {
        log.info("获取员工邮箱配置: employeeId={}", employeeId);
        
        try {
            Employee employee = employeeEmailConfigService.getEmployeeEmailConfig(employeeId);
            if (employee != null) {
                // 不返回密码字段，保护安全
                employee.setEmailPassword(null);
            }
            return Result.success(employee);
        } catch (Exception e) {
            log.error("获取员工邮箱配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 测试员工邮箱连接
     */
    @PostMapping("/test/{employeeId}")
    @ApiOperation("测试员工邮箱连接")
    public Result testEmployeeEmailConnection(@PathVariable Long employeeId) {
        log.info("测试员工邮箱连接: employeeId={}", employeeId);
        
        try {
            boolean success = employeeEmailConfigService.testEmployeeEmailConnection(employeeId);
            if (success) {
                return Result.success("邮箱连接测试成功");
            } else {
                return Result.error("邮箱连接测试失败，请检查配置");
            }
        } catch (Exception e) {
            log.error("测试员工邮箱连接失败", e);
            return Result.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用员工邮箱发送
     */
    @PutMapping("/toggle/{employeeId}")
    @ApiOperation("启用/禁用员工邮箱发送")
    public Result updateEmailEnabled(@PathVariable Long employeeId, 
                                   @RequestParam Boolean enabled) {
        log.info("更新员工邮箱启用状态: employeeId={}, enabled={}", employeeId, enabled);
        
        try {
            employeeEmailConfigService.updateEmailEnabled(employeeId, enabled);
            return Result.success(enabled ? "已启用员工邮箱发送" : "已禁用员工邮箱发送");
        } catch (Exception e) {
            log.error("更新员工邮箱启用状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}
