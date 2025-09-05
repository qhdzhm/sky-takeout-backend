package com.sky.controller.admin;

import com.sky.annotation.RequireOperatorPermission;
import com.sky.context.BaseContext;
import com.sky.dto.AssignOrderDTO;
import com.sky.dto.OperatorAssignmentPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OperatorAssignmentService;
import com.sky.vo.OperatorAssignmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 操作员分配管理Controller
 */
@RestController
@RequestMapping("/admin/operator-assignment")
@Api(tags = "操作员分配管理接口")
@Slf4j
public class OperatorAssignmentController {

    @Autowired
    private OperatorAssignmentService operatorAssignmentService;

    /**
     * 分配订单给酒店专员
     */
    @PostMapping("/assign")
    @ApiOperation("分配订单给酒店专员")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以分配订单")
    public Result<String> assignOrder(@Valid @RequestBody AssignOrderDTO assignOrderDTO) {
        log.info("分配订单：{}", assignOrderDTO);

        Long currentEmployeeId = BaseContext.getCurrentId();
        operatorAssignmentService.assignOrder(assignOrderDTO, currentEmployeeId);

        return Result.success("订单分配成功");
    }

    /**
     * 重新分配订单
     */
    @PutMapping("/reassign/{bookingId}")
    @ApiOperation("重新分配订单")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以重新分配订单")
    public Result<String> reassignOrder(@PathVariable Integer bookingId,
                                       @RequestParam Long newOperatorId,
                                       @RequestParam(required = false) String notes) {
        log.info("重新分配订单：{} -> 操作员：{}", bookingId, newOperatorId);

        Long currentEmployeeId = BaseContext.getCurrentId();
        operatorAssignmentService.reassignOrder(bookingId, newOperatorId, currentEmployeeId, notes);

        return Result.success("订单重新分配成功");
    }

    /**
     * 取消订单分配
     */
    @DeleteMapping("/cancel/{bookingId}")
    @ApiOperation("取消订单分配")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以取消分配")
    public Result<String> cancelAssignment(@PathVariable Integer bookingId) {
        log.info("取消订单分配：{}", bookingId);

        Long currentEmployeeId = BaseContext.getCurrentId();
        operatorAssignmentService.cancelAssignment(bookingId, currentEmployeeId);

        return Result.success("订单分配已取消");
    }

    /**
     * 完成分配任务
     */
    @PutMapping("/complete/{bookingId}")
    @ApiOperation("完成分配任务")
    @RequireOperatorPermission(checkOrderPermission = true, bookingIdParam = "bookingId", 
                             description = "只能完成分配给自己的订单任务")
    public Result<String> completeAssignment(@PathVariable Integer bookingId) {
        log.info("完成分配任务：{}", bookingId);

        Long currentEmployeeId = BaseContext.getCurrentId();
        operatorAssignmentService.completeAssignment(bookingId, currentEmployeeId);

        return Result.success("任务已完成");
    }

    /**
     * 获取订单的分配信息
     */
    @GetMapping("/booking/{bookingId}")
    @ApiOperation("获取订单的分配信息")
    public Result<OperatorAssignmentVO> getAssignmentByBookingId(@PathVariable Integer bookingId) {
        log.info("获取订单分配信息：{}", bookingId);

        OperatorAssignmentVO assignment = operatorAssignmentService.getAssignmentByBookingId(bookingId);
        return Result.success(assignment);
    }

    /**
     * 获取当前操作员的分配任务
     */
    @GetMapping("/my-assignments")
    @ApiOperation("获取当前操作员的分配任务")
    public Result<List<OperatorAssignmentVO>> getMyAssignments() {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("获取操作员分配任务：{}", currentEmployeeId);

        List<OperatorAssignmentVO> assignments = operatorAssignmentService.getAssignmentsByOperatorId(currentEmployeeId);
        return Result.success(assignments);
    }

    /**
     * 获取指定操作员的分配任务
     */
    @GetMapping("/operator/{operatorId}")
    @ApiOperation("获取指定操作员的分配任务")
    public Result<List<OperatorAssignmentVO>> getAssignmentsByOperatorId(@PathVariable Long operatorId) {
        log.info("获取操作员分配任务：{}", operatorId);

        // 只有排团主管可以查看其他操作员的任务
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId) && !operatorId.equals(currentEmployeeId)) {
            return Result.error("权限不足：只能查看自己的分配任务");
        }

        List<OperatorAssignmentVO> assignments = operatorAssignmentService.getAssignmentsByOperatorId(operatorId);
        return Result.success(assignments);
    }

    /**
     * 分页查询分配记录
     */
    @GetMapping("/page")
    @ApiOperation("分页查询分配记录")
    public Result<PageResult> pageQuery(OperatorAssignmentPageQueryDTO queryDTO) {
        log.info("分页查询分配记录：{}", queryDTO);

        // 只有排团主管可以查看所有分配记录
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId)) {
            // 普通操作员只能查看自己的记录
            queryDTO.setOperatorId(currentEmployeeId);
        }

        PageResult pageResult = operatorAssignmentService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取所有有效的分配记录
     */
    @GetMapping("/active")
    @ApiOperation("获取所有有效的分配记录")
    public Result<List<OperatorAssignmentVO>> getAllActiveAssignments() {
        log.info("获取所有有效的分配记录");

        // 只有排团主管可以查看所有分配记录
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId)) {
            return Result.error("权限不足：只有排团主管可以查看所有分配记录");
        }

        List<OperatorAssignmentVO> assignments = operatorAssignmentService.getAllActiveAssignments();
        return Result.success(assignments);
    }

    /**
     * 检查对订单的操作权限
     */
    @GetMapping("/check-permission/{bookingId}")
    @ApiOperation("检查对订单的操作权限")
    public Result<Boolean> checkPermission(@PathVariable Integer bookingId) {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("检查操作员：{} 对订单：{} 的权限", currentEmployeeId, bookingId);

        boolean hasPermission = operatorAssignmentService.hasPermission(currentEmployeeId, bookingId);
        return Result.success(hasPermission);
    }

    /**
     * 检查是否为排团主管
     */
    @GetMapping("/check-tour-master")
    @ApiOperation("检查是否为排团主管")
    public Result<Boolean> checkTourMaster() {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("检查操作员：{} 是否为排团主管", currentEmployeeId);

        boolean isTourMaster = operatorAssignmentService.isTourMaster(currentEmployeeId);
        return Result.success(isTourMaster);
    }

    /**
     * 获取工作量统计
     */
    @GetMapping("/workload/{operatorId}")
    @ApiOperation("获取操作员工作量统计")
    public Result<Object> getWorkloadStatistics(@PathVariable Long operatorId) {
        log.info("获取操作员工作量统计：{}", operatorId);

        // 只有排团主管可以查看其他操作员的统计，或者查看自己的统计
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId) && !operatorId.equals(currentEmployeeId)) {
            return Result.error("权限不足：只能查看自己的统计信息");
        }

        Object statistics = operatorAssignmentService.getWorkloadStatistics(operatorId);
        return Result.success(statistics);
    }

    /**
     * 批量分配订单
     */
    @PostMapping("/batch-assign")
    @ApiOperation("批量分配订单")
    public Result<String> batchAssignOrders(@RequestBody List<AssignOrderDTO> assignOrderDTOs) {
        log.info("批量分配订单：{}", assignOrderDTOs);

        Long currentEmployeeId = BaseContext.getCurrentId();
        
        // 验证是否为排团主管
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId)) {
            return Result.error("权限不足：只有排团主管可以分配订单");
        }

        // 逐个分配订单
        int successCount = 0;
        for (AssignOrderDTO dto : assignOrderDTOs) {
            try {
                operatorAssignmentService.assignOrder(dto, currentEmployeeId);
                successCount++;
            } catch (Exception e) {
                log.error("分配订单失败：{} - {}", dto.getBookingId(), e.getMessage());
            }
        }

        return Result.success(String.format("批量分配完成：成功 %d 个，总共 %d 个", successCount, assignOrderDTOs.size()));
    }

}
