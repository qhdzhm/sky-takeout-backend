package com.sky.controller.admin;

import com.sky.dto.TourGuideVehicleAssignmentDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.TourGuideVehicleAssignmentService;
import com.sky.vo.TourGuideVehicleAssignmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 导游车辆游客分配管理接口
 */
@RestController
@RequestMapping("/admin/tour-assignments")
@Api(tags = "导游车辆游客分配管理接口")
@Slf4j
public class TourGuideVehicleAssignmentController {

    @Autowired
    private TourGuideVehicleAssignmentService assignmentService;

    /**
     * 创建分配记录
     */
    @PostMapping
    @ApiOperation("创建分配记录")
    public Result<String> createAssignment(@RequestBody TourGuideVehicleAssignmentDTO assignmentDTO) {
        log.info("创建分配记录：{}", assignmentDTO);
        assignmentService.createAssignment(assignmentDTO);
        return Result.success("分配成功");
    }

    /**
     * 批量创建分配记录
     */
    @PostMapping("/batch")
    @ApiOperation("批量创建分配记录")
    public Result<String> batchCreateAssignment(@RequestBody List<TourGuideVehicleAssignmentDTO> assignmentDTOs) {
        log.info("批量创建分配记录，数量：{}", assignmentDTOs.size());
        assignmentService.batchCreateAssignment(assignmentDTOs);
        return Result.success("批量分配成功");
    }

    /**
     * 根据ID查询分配记录
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询分配记录")
    public Result<TourGuideVehicleAssignmentVO> getById(@PathVariable Long id) {
        TourGuideVehicleAssignmentVO assignment = assignmentService.getById(id);
        return Result.success(assignment);
    }

    /**
     * 根据日期查询分配记录
     */
    @GetMapping("/date/{date}")
    @ApiOperation("根据日期查询分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> getByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDate(date);
        return Result.success(assignments);
    }

    /**
     * 根据日期范围查询分配记录
     */
    @GetMapping("/date-range")
    @ApiOperation("根据日期范围查询分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> getByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDateRange(startDate, endDate);
        return Result.success(assignments);
    }

    /**
     * 根据目的地查询分配记录
     */
    @GetMapping("/destination/{destination}")
    @ApiOperation("根据目的地查询分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> getByDestination(
            @PathVariable String destination,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDestination(destination, date);
        return Result.success(assignments);
    }

    /**
     * 根据导游ID查询分配记录
     */
    @GetMapping("/guide/{guideId}")
    @ApiOperation("根据导游ID查询分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> getByGuideId(
            @PathVariable Long guideId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByGuideId(guideId, date);
        return Result.success(assignments);
    }

    /**
     * 根据车辆ID查询分配记录
     */
    @GetMapping("/vehicle/{vehicleId}")
    @ApiOperation("根据车辆ID查询分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> getByVehicleId(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByVehicleId(vehicleId, date);
        return Result.success(assignments);
    }

    /**
     * 分页查询分配记录
     */
    @GetMapping("/page")
    @ApiOperation("分页查询分配记录")
    public Result<PageResult> pageQuery(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String guideName,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) String status) {
        
        PageResult pageResult = assignmentService.pageQuery(page, pageSize, startDate, endDate, 
                                                           destination, guideName, licensePlate, status);
        return Result.success(pageResult);
    }

    /**
     * 更新分配记录
     */
    @PutMapping("/{id}")
    @ApiOperation("更新分配记录")
    public Result<String> updateAssignment(@PathVariable Long id, 
                                          @RequestBody TourGuideVehicleAssignmentDTO assignmentDTO) {
        log.info("更新分配记录，ID：{}，数据：{}", id, assignmentDTO);
        assignmentService.updateAssignment(id, assignmentDTO);
        return Result.success("更新成功");
    }

    /**
     * 取消分配记录
     */
    @PutMapping("/{id}/cancel")
    @ApiOperation("取消分配记录")
    public Result<String> cancelAssignment(@PathVariable Long id) {
        log.info("取消分配记录，ID：{}", id);
        assignmentService.cancelAssignment(id);
        return Result.success("取消成功");
    }

    /**
     * 删除分配记录
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除分配记录")
    public Result<String> deleteAssignment(@PathVariable Long id) {
        log.info("删除分配记录，ID：{}", id);
        assignmentService.deleteAssignment(id);
        return Result.success("删除成功");
    }

    /**
     * 根据订单ID列表查询分配记录
     */
    @PostMapping("/booking-ids")
    @ApiOperation("根据订单ID列表查询分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> getByBookingIds(@RequestBody List<Long> bookingIds) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByBookingIds(bookingIds);
        return Result.success(assignments);
    }

    /**
     * 统计指定日期的分配数量
     */
    @GetMapping("/count/{date}")
    @ApiOperation("统计指定日期的分配数量")
    public Result<Integer> countByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        int count = assignmentService.countByDate(date);
        return Result.success(count);
    }

    /**
     * 检查导游在指定日期是否已有分配
     */
    @GetMapping("/check/guide/{guideId}/{date}")
    @ApiOperation("检查导游在指定日期是否已有分配")
    public Result<Boolean> checkGuideAssigned(
            @PathVariable Long guideId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        boolean assigned = assignmentService.checkGuideAssigned(guideId, date);
        return Result.success(assigned);
    }

    /**
     * 检查车辆在指定日期是否已有分配
     */
    @GetMapping("/check/vehicle/{vehicleId}/{date}")
    @ApiOperation("检查车辆在指定日期是否已有分配")
    public Result<Boolean> checkVehicleAssigned(
            @PathVariable Long vehicleId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        boolean assigned = assignmentService.checkVehicleAssigned(vehicleId, date);
        return Result.success(assigned);
    }

    /**
     * 获取指定日期的分配统计信息
     */
    @GetMapping("/statistics/{date}")
    @ApiOperation("获取指定日期的分配统计信息")
    public Result<TourGuideVehicleAssignmentService.AssignmentStatistics> getAssignmentStatistics(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        TourGuideVehicleAssignmentService.AssignmentStatistics statistics = 
            assignmentService.getAssignmentStatistics(date);
        return Result.success(statistics);
    }

    /**
     * 获取今日分配概览
     */
    @GetMapping("/today")
    @ApiOperation("获取今日分配概览")
    public Result<List<TourGuideVehicleAssignmentVO>> getTodayAssignments() {
        LocalDate today = LocalDate.now();
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDate(today);
        return Result.success(assignments);
    }

    /**
     * 获取本周分配概览
     */
    @GetMapping("/this-week")
    @ApiOperation("获取本周分配概览")
    public Result<List<TourGuideVehicleAssignmentVO>> getThisWeekAssignments() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        List<TourGuideVehicleAssignmentVO> assignments = 
            assignmentService.getByDateRange(startOfWeek, endOfWeek);
        return Result.success(assignments);
    }

    /**
     * 导出分配记录（返回Excel文件数据）
     */
    @GetMapping("/export")
    @ApiOperation("导出分配记录")
    public Result<List<TourGuideVehicleAssignmentVO>> exportAssignments(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String status) {
        
        // 这里可以根据需要返回Excel文件或者JSON数据
        // 目前返回JSON数据，前端可以处理导出
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDateRange(startDate, endDate);
        return Result.success(assignments);
    }

    /**
     * 获取每日行程安排
     */
    @GetMapping("/daily-schedule")
    @ApiOperation("获取每日行程安排")
    public Result<List<TourGuideVehicleAssignmentVO>> getDailySchedule(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String destination) {
        
        List<TourGuideVehicleAssignmentVO> assignments;
        if (StringUtils.hasText(destination)) {
            assignments = assignmentService.getByDestination(destination, date);
        } else {
            assignments = assignmentService.getByDate(date);
        }
        
        return Result.success(assignments);
    }

    /**
     * 检查指定日期和地点的分配状态
     */
    @GetMapping("/status")
    @ApiOperation("检查指定日期和地点的分配状态")
    public Result<AssignmentStatusVO> checkAssignmentStatus(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String location) {
        log.info("检查分配状态：日期={}，地点={}", date, location);
        
        try {
            // 查询该日期该地点的分配记录
            List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDestination(location, date);
            log.info("查询结果：日期={}，地点={}，找到{}条记录", date, location, assignments != null ? assignments.size() : 0);
            
            if (assignments != null && !assignments.isEmpty()) {
                log.info("第一条记录详情：ID={}, 目的地={}, 导游ID={}, 导游姓名={}, 车辆ID={}, 车牌={}, 状态={}", 
                    assignments.get(0).getId(), 
                    assignments.get(0).getDestination(),
                    assignments.get(0).getGuide() != null ? assignments.get(0).getGuide().getGuideId() : "null",
                    assignments.get(0).getGuide() != null ? assignments.get(0).getGuide().getGuideName() : "null",
                    assignments.get(0).getVehicle() != null ? assignments.get(0).getVehicle().getVehicleId() : "null",
                    assignments.get(0).getVehicle() != null ? assignments.get(0).getVehicle().getLicensePlate() : "null",
                    assignments.get(0).getAssignmentStatus());
            }
            
            AssignmentStatusVO statusVO = new AssignmentStatusVO();
            if (assignments != null && !assignments.isEmpty()) {
                // 已分配
                statusVO.setAssigned(true);
                
                // 获取第一个分配记录的信息（如果有多个分配，选择第一个）
                TourGuideVehicleAssignmentVO firstAssignment = assignments.get(0);
                
                // 安全获取导游信息
                if (firstAssignment.getGuide() != null) {
                    statusVO.setGuideName(firstAssignment.getGuide().getGuideName() != null ? 
                        firstAssignment.getGuide().getGuideName() : "");
                } else {
                    statusVO.setGuideName("");
                }
                
                // 安全获取车辆信息
                if (firstAssignment.getVehicle() != null) {
                    String licensePlate = firstAssignment.getVehicle().getLicensePlate() != null ? 
                        firstAssignment.getVehicle().getLicensePlate() : "";
                    String vehicleType = firstAssignment.getVehicle().getVehicleType() != null ? 
                        firstAssignment.getVehicle().getVehicleType() : "";
                    statusVO.setVehicleInfo(licensePlate + " (" + vehicleType + ")");
                } else {
                    statusVO.setVehicleInfo("");
                }
                
                statusVO.setAssignmentId(firstAssignment.getId());
                statusVO.setStatus(firstAssignment.getAssignmentStatus() != null ? 
                    firstAssignment.getAssignmentStatus() : "");
            } else {
                // 未分配
                statusVO.setAssigned(false);
                statusVO.setGuideName("");
                statusVO.setVehicleInfo("");
                statusVO.setAssignmentId(null);
                statusVO.setStatus("");
            }
            
            return Result.success(statusVO);
        } catch (Exception e) {
            log.error("检查分配状态失败：{}", e.getMessage(), e);
            // 返回未分配状态作为默认值
            AssignmentStatusVO statusVO = new AssignmentStatusVO();
            statusVO.setAssigned(false);
            statusVO.setGuideName("");
            statusVO.setVehicleInfo("");
            statusVO.setAssignmentId(null);
            statusVO.setStatus("");
            return Result.success(statusVO);
        }
    }

    /**
     * 分配状态响应VO
     */
    public static class AssignmentStatusVO {
        private Boolean isAssigned = false;
        private String guideName = "";
        private String vehicleInfo = "";
        private Long assignmentId;
        private String status = "";

        // Getters and Setters
        public Boolean getIsAssigned() { return isAssigned; }
        public void setAssigned(Boolean assigned) { isAssigned = assigned; }
        
        public String getGuideName() { return guideName; }
        public void setGuideName(String guideName) { this.guideName = guideName; }
        
        public String getVehicleInfo() { return vehicleInfo; }
        public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
        
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
} 