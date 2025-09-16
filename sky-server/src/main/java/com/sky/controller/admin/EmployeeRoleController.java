package com.sky.controller.admin;

import com.sky.annotation.RequireOperatorPermission;
import com.sky.dto.EmployeeRoleUpdateDTO;
import com.sky.entity.Employee;
import com.sky.result.Result;
import com.sky.service.EmployeeRoleService;
import com.sky.vo.OperatorStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 员工角色职责管理Controller
 */
@RestController
@RequestMapping("/admin/employee/role")
@Api(tags = "员工角色职责管理接口")
@Slf4j
public class EmployeeRoleController {

    @Autowired
    private EmployeeRoleService employeeRoleService;

    /**
     * 更新员工操作员类型
     */
    @PutMapping("/operator-type")
    @ApiOperation("更新员工操作员类型")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以分配操作员类型")
    public Result<String> updateOperatorType(@Valid @RequestBody EmployeeRoleUpdateDTO roleUpdateDTO) {
        log.info("更新员工操作员类型：{}", roleUpdateDTO);
        
        employeeRoleService.updateOperatorType(roleUpdateDTO.getEmployeeId(), roleUpdateDTO.getOperatorType());
        return Result.success("操作员类型更新成功");
    }

    /**
     * 设置排团主管
     */
    @PutMapping("/set-tour-master/{employeeId}")
    @ApiOperation("设置排团主管")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有当前排团主管可以转移职责")
    public Result<String> setTourMaster(@PathVariable Long employeeId, 
                                       @RequestParam(required = false) String reason) {
        log.info("设置排团主管：employeeId={}, reason={}", employeeId, reason);
        
        employeeRoleService.setTourMaster(employeeId, reason);
        return Result.success("排团主管设置成功");
    }

    /**
     * 批量更新员工分配权限
     */
    @PutMapping("/assign-permission")
    @ApiOperation("批量更新员工分配权限")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以管理分配权限")
    public Result<String> updateAssignPermission(@RequestBody List<Long> employeeIds, 
                                                @RequestParam Boolean canAssignOrders) {
        log.info("批量更新员工分配权限：employeeIds={}, canAssignOrders={}", employeeIds, canAssignOrders);
        
        employeeRoleService.batchUpdateAssignPermission(employeeIds, canAssignOrders);
        return Result.success("分配权限更新成功");
    }

    /**
     * 获取所有操作员统计信息
     */
    @GetMapping("/operator-statistics")
    @ApiOperation("获取操作员统计信息")
    public Result<List<OperatorStatisticsVO>> getOperatorStatistics() {
        log.info("获取操作员统计信息");
        
        List<OperatorStatisticsVO> statistics = employeeRoleService.getOperatorStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取可设置为排团主管的员工列表
     */
    @GetMapping("/tour-master-candidates")
    @ApiOperation("获取可设置为排团主管的员工列表")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以查看候选人")
    public Result<List<Employee>> getTourMasterCandidates() {
        log.info("获取可设置为排团主管的员工列表");
        
        List<Employee> candidates = employeeRoleService.getTourMasterCandidates();
        return Result.success(candidates);
    }

    /**
     * 获取酒店操作员列表
     */
    @GetMapping("/hotel-operators")
    @ApiOperation("获取酒店操作员列表")
    public Result<List<Employee>> getHotelOperators() {
        log.info("获取酒店操作员列表");
        
        List<Employee> hotelOperators = employeeRoleService.getHotelOperators();
        return Result.success(hotelOperators);
    }

    /**
     * 重置员工职责（设为普通员工）
     */
    @PutMapping("/reset-role/{employeeId}")
    @ApiOperation("重置员工职责")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以重置职责")
    public Result<String> resetEmployeeRole(@PathVariable Long employeeId) {
        log.info("重置员工职责：employeeId={}", employeeId);
        
        employeeRoleService.resetEmployeeRole(employeeId);
        return Result.success("员工职责重置成功");
    }

    /**
     * 获取员工角色变更历史
     */
    @GetMapping("/change-history/{employeeId}")
    @ApiOperation("获取员工角色变更历史")
    public Result<List<Object>> getRoleChangeHistory(@PathVariable Long employeeId) {
        log.info("获取员工角色变更历史：employeeId={}", employeeId);
        
        List<Object> history = employeeRoleService.getRoleChangeHistory(employeeId);
        return Result.success(history);
    }
}
