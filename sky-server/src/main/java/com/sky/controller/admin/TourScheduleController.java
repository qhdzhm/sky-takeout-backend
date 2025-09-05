package com.sky.controller.admin;

import com.sky.annotation.RequireOperatorPermission;
import com.sky.context.BaseContext;
import com.sky.dto.AssignOrderDTO;
import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.Result;
import com.sky.service.OperatorAssignmentService;
import com.sky.service.TourScheduleOrderService;
import com.sky.vo.OperatorAssignmentVO;
import com.sky.vo.TourScheduleVO;
import com.sky.vo.HotelCustomerStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行程排序接口
 */
@RestController
@RequestMapping("/admin/tour/schedule")
@Api(tags = "行程排序相关接口")
@Slf4j
public class TourScheduleController {

    @Autowired
    private TourScheduleOrderService tourScheduleOrderService;

    @Autowired
    private OperatorAssignmentService operatorAssignmentService;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 通过订单ID获取行程排序
     */
    @GetMapping("/booking/{bookingId}")
    @ApiOperation("通过订单ID获取行程排序")
    public Result<List<TourScheduleVO>> getSchedulesByBookingId(@PathVariable Integer bookingId) {
        log.info("通过订单ID获取行程排序: {}", bookingId);
        List<TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByBookingId(bookingId);
        return Result.success(schedules);
    }

    /**
     * 通过日期范围获取行程排序
     */
    @GetMapping("/date")
    @ApiOperation("通过日期范围获取行程排序")
    public Result<List<TourScheduleVO>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("通过日期范围获取行程排序: {} - {}", startDate, endDate);
        List<TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByDateRange(startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 保存单个行程排序
     */
    @PostMapping
    @ApiOperation("保存单个行程排序")
    public Result<Boolean> saveSchedule(@RequestBody TourScheduleOrderDTO tourScheduleOrderDTO) {
        log.info("保存单个行程排序: {}", tourScheduleOrderDTO);
        boolean result = tourScheduleOrderService.saveSchedule(tourScheduleOrderDTO);
        return Result.success(result);
    }

    /**
     * 批量保存行程排序
     */
    @PostMapping("/batch")
    @ApiOperation("批量保存行程排序")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以调整行程顺序")
    public Result<Boolean> saveBatchSchedules(@RequestBody TourScheduleBatchSaveDTO batchSaveDTO) {
        log.info("批量保存行程排序: {}", batchSaveDTO);
        boolean result = tourScheduleOrderService.saveBatchSchedules(batchSaveDTO);
        return Result.success(result);
    }

    /**
     * 根据日期和地点获取导游车辆分配信息
     */
    @GetMapping("/assignment")
    @ApiOperation("根据日期和地点获取导游车辆分配信息")
    public Result<List<Object>> getAssignmentByDateAndLocation(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String location) {
        log.info("获取导游车辆分配信息: 日期={}, 地点={}", date, location);
        List<Object> assignments = tourScheduleOrderService.getAssignmentByDateAndLocation(date, location);
        return Result.success(assignments);
    }

    /**
     * 根据订单号搜索行程排序
     */
    @GetMapping("/search")
    @ApiOperation("根据订单号搜索行程排序")
    public Result<List<TourScheduleVO>> getSchedulesByOrderNumber(@RequestParam String orderNumber) {
        log.info("根据订单号搜索行程排序: {}", orderNumber);
        List<TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByOrderNumber(orderNumber);
        return Result.success(schedules);
    }

    /**
     * 根据联系人姓名搜索行程排序
     */
    @GetMapping("/search/contact")
    @ApiOperation("根据联系人姓名搜索行程排序")
    public Result<List<TourScheduleVO>> getSchedulesByContactPerson(@RequestParam String contactPerson) {
        log.info("根据联系人姓名搜索行程排序: {}", contactPerson);
        List<TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByContactPerson(contactPerson);
        return Result.success(schedules);
    }

    /**
     * 获取可选的一日游产品列表（用于额外行程）
     */
    @GetMapping("/day-tours")
    @ApiOperation("获取可选的一日游产品列表")
    public Result<List<Map<String, Object>>> getAvailableDayTours() {
        log.info("获取可选的一日游产品列表");
        
        try {
            // 获取所有激活状态的一日游
            Map<String, Object> params = new HashMap<>();
            params.put("isActive", 1); // 只获取激活的产品
            params.put("pageSize", 1000); // 获取所有产品
            
            List<Map<String, Object>> dayTours = tourScheduleOrderService.getAvailableDayTours(params);
            
            return Result.success(dayTours);
        } catch (Exception e) {
            log.error("获取一日游产品列表失败: {}", e.getMessage(), e);
            return Result.error("获取一日游产品列表失败");
        }
    }

    /**
     * 删除行程排序
     */
    @DeleteMapping("/{scheduleId}")
    @ApiOperation("删除行程排序")
    public Result<String> deleteSchedule(
            @ApiParam(name = "scheduleId", value = "行程排序ID", required = true)
            @PathVariable Integer scheduleId) {
        log.info("删除行程排序，ID：{}", scheduleId);
        
        try {
            boolean success = tourScheduleOrderService.deleteSchedule(scheduleId);
            if (success) {
                log.info("行程排序删除成功，ID：{}", scheduleId);
                return Result.success("行程删除成功");
            } else {
                log.warn("行程排序删除失败，ID：{}", scheduleId);
                return Result.error("行程删除失败，请检查行程是否存在");
            }
        } catch (Exception e) {
            log.error("删除行程排序失败，ID：{}，错误：{}", scheduleId, e.getMessage(), e);
            return Result.error("删除行程失败：" + e.getMessage());
        }
    }

    /**
     * 更新导游备注
     */
    @PutMapping("/{scheduleId}/guide-remarks")
    @ApiOperation("更新导游备注")
    public Result<String> updateGuideRemarks(
            @ApiParam(name = "scheduleId", value = "行程排序ID", required = true) @PathVariable Integer scheduleId,
            @ApiParam(name = "guideRemarks", value = "导游备注", required = true) @RequestBody String guideRemarks) {
        
        log.info("更新导游备注，行程ID：{}，备注：{}", scheduleId, guideRemarks);
        
        try {
            boolean success = tourScheduleOrderService.updateGuideRemarks(scheduleId, guideRemarks);
            if (success) {
                log.info("导游备注更新成功，行程ID：{}", scheduleId);
                return Result.success("导游备注更新成功");
            } else {
                log.warn("导游备注更新失败，行程ID：{}", scheduleId);
                return Result.error("导游备注更新失败，请检查行程是否存在");
            }
        } catch (Exception e) {
            log.error("更新导游备注失败，行程ID：{}，错误：{}", scheduleId, e.getMessage(), e);
            return Result.error("更新导游备注失败：" + e.getMessage());
        }
    }

    /**
     * 根据酒店名称和日期统计住在该酒店的所有客人
     */
    @GetMapping("/hotel-statistics")
    @ApiOperation("根据酒店名称和日期统计酒店客人信息")
    public Result<HotelCustomerStatisticsVO> getHotelCustomerStatistics(
            @ApiParam(name = "hotelName", value = "酒店名称", required = true) 
            @RequestParam String hotelName,
            @ApiParam(name = "tourDate", value = "旅游日期", required = true) 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tourDate) {
        
        log.info("统计酒店客人信息，酒店：{}，日期：{}", hotelName, tourDate);
        
        try {
            HotelCustomerStatisticsVO statistics = tourScheduleOrderService.getHotelCustomerStatistics(hotelName, tourDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("统计酒店客人信息失败，酒店：{}，日期：{}，错误：{}", hotelName, tourDate, e.getMessage(), e);
            return Result.error("统计酒店客人信息失败：" + e.getMessage());
        }
    }

    // ====== 操作员分配相关接口 ======

    /**
     * 获取订单的分配状态（用于前端展示）
     */
    @GetMapping("/assignment-status/{bookingId}")
    @ApiOperation("获取订单的分配状态")
    public Result<Map<String, Object>> getOrderAssignmentStatus(@PathVariable Integer bookingId) {
        log.info("获取订单分配状态：{}", bookingId);

        try {
            OperatorAssignmentVO assignment = operatorAssignmentService.getAssignmentByBookingId(bookingId);
            Long currentUserId = BaseContext.getCurrentId();
            boolean isTourMaster = operatorAssignmentService.isTourMaster(currentUserId);
            boolean hasPermission = operatorAssignmentService.hasPermission(currentUserId, bookingId);

            Map<String, Object> result = new HashMap<>();
            result.put("assignment", assignment);
            result.put("isTourMaster", isTourMaster);
            result.put("hasPermission", hasPermission);
            result.put("canAssign", isTourMaster);
            result.put("canOperate", hasPermission || isTourMaster);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单分配状态失败：{} - {}", bookingId, e.getMessage());
            return Result.error("获取分配状态失败");
        }
    }

    /**
     * 快速分配订单（在排团表中直接分配）
     */
    @PostMapping("/quick-assign")
    @ApiOperation("快速分配订单")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以分配订单")
    public Result<String> quickAssignOrder(@RequestBody AssignOrderDTO assignOrderDTO) {
        log.info("快速分配订单：{}", assignOrderDTO);

        try {
            Long currentEmployeeId = BaseContext.getCurrentId();
            operatorAssignmentService.assignOrder(assignOrderDTO, currentEmployeeId);
            return Result.success("订单分配成功");
        } catch (Exception e) {
            log.error("快速分配订单失败：{} - {}", assignOrderDTO.getBookingId(), e.getMessage());
            return Result.error("分配失败：" + e.getMessage());
        }
    }

    /**
     * 获取可分配的酒店专员列表
     */
    @GetMapping("/available-operators")
    @ApiOperation("获取可分配的酒店专员列表")
    @RequireOperatorPermission(requireTourMaster = true, description = "只有排团主管可以查看操作员列表")
    public Result<List<Map<String, Object>>> getAvailableOperators() {
        log.info("获取可分配的酒店专员列表");

        try {
            // 查询所有酒店专员
            List<Employee> hotelOperators = employeeMapper.findByOperatorType("hotel_operator");
            log.info("查询到 {} 个酒店专员", hotelOperators.size());

            List<Map<String, Object>> operators = new ArrayList<>();
            for (Employee operator : hotelOperators) {
                Map<String, Object> operatorInfo = new HashMap<>();
                operatorInfo.put("id", operator.getId());
                operatorInfo.put("name", operator.getName());
                operatorInfo.put("username", operator.getUsername());
                operators.add(operatorInfo);
            }

            log.info("返回可分配的酒店专员列表: {}", operators);
            return Result.success(operators);
        } catch (Exception e) {
            log.error("获取酒店专员列表失败：{}", e.getMessage());
            return Result.error("获取操作员列表失败");
        }
    }

    /**
     * 批量获取订单的分配状态
     */
    @PostMapping("/batch-assignment-status")
    @ApiOperation("批量获取订单的分配状态")
    public Result<Map<Integer, OperatorAssignmentVO>> getBatchAssignmentStatus(@RequestBody List<Integer> bookingIds) {
        log.info("批量获取订单分配状态：{}", bookingIds);

        try {
            Map<Integer, OperatorAssignmentVO> result = new HashMap<>();
            
            for (Integer bookingId : bookingIds) {
                OperatorAssignmentVO assignment = operatorAssignmentService.getAssignmentByBookingId(bookingId);
                if (assignment != null) {
                    result.put(bookingId, assignment);
                }
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量获取订单分配状态失败：{}", e.getMessage());
            return Result.error("获取分配状态失败");
        }
    }

    /**
     * 检查当前用户对订单的权限
     */
    @GetMapping("/check-order-permission/{bookingId}")
    @ApiOperation("检查当前用户对订单的操作权限")
    public Result<Map<String, Object>> checkOrderPermission(@PathVariable Integer bookingId) {
        log.info("检查订单操作权限：{}", bookingId);

        Long currentUserId = BaseContext.getCurrentId();
        boolean isTourMaster = operatorAssignmentService.isTourMaster(currentUserId);
        boolean hasPermission = operatorAssignmentService.hasPermission(currentUserId, bookingId);

        Map<String, Object> result = new HashMap<>();
        result.put("isTourMaster", isTourMaster);
        result.put("hasOrderPermission", hasPermission);
        result.put("canDrag", isTourMaster); // 只有排团主管可以拖拽
        result.put("canAssign", isTourMaster); // 只有排团主管可以分配
        result.put("canViewHotel", hasPermission || isTourMaster); // 有权限或是排团主管可以查看酒店

        return Result.success(result);
    }
} 