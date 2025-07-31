package com.sky.controller.admin;

import com.sky.dto.TourGuideVehicleAssignmentDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.TourGuideVehicleAssignmentService;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;
import com.sky.vo.TourGuideVehicleAssignmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 导游分配管理接口 - 兼容前端调用
 */
@RestController
@RequestMapping("/admin/guide-assignment")
@Api(tags = "导游分配管理接口")
@Slf4j
public class GuideAssignmentController {

    @Autowired
    private TourGuideVehicleAssignmentService assignmentService;

    /**
     * 获取可用导游列表
     */
    @GetMapping("/available-guides")
    @ApiOperation("获取可用导游列表")
    public Result<List<GuideAvailabilityVO>> getAvailableGuides(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime,
            @RequestParam(required = false) String location) {
        log.info("获取可用导游列表：日期={}, 开始时间={}, 结束时间={}, 地点={}", date, startTime, endTime, location);
        
        List<GuideAvailabilityVO> availableGuides = assignmentService.getAvailableGuides(date, startTime, endTime, location);
        return Result.success(availableGuides);
    }

    /**
     * 获取可用车辆列表
     */
    @GetMapping("/available-vehicles")
    @ApiOperation("获取可用车辆列表")
    public Result<List<VehicleAvailabilityVO>> getAvailableVehicles(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer peopleCount) {
        log.info("获取可用车辆列表：日期={}, 开始时间={}, 结束时间={}, 人数={}", date, startTime, endTime, peopleCount);
        
        List<VehicleAvailabilityVO> availableVehicles = assignmentService.getAvailableVehicles(date, startTime, endTime, peopleCount);
        return Result.success(availableVehicles);
    }

    /**
     * 自动分配导游和车辆
     */
    @PostMapping("/auto-assign")
    @ApiOperation("自动分配导游和车辆")
    public Result<String> autoAssign(@RequestBody Map<String, Object> requestData) {
        log.info("自动分配导游和车辆：{}", requestData);
        
        // 创建DTO并处理字段映射
        TourGuideVehicleAssignmentDTO assignmentDTO = new TourGuideVehicleAssignmentDTO();
        
        // 处理日期
        if (requestData.get("assignmentDate") != null) {
            assignmentDTO.setAssignmentDate(LocalDate.parse(requestData.get("assignmentDate").toString()));
        }
        
        // 处理location到destination的映射
        if (requestData.get("location") != null) {
            assignmentDTO.setDestination(requestData.get("location").toString());
        } else if (requestData.get("destination") != null) {
            assignmentDTO.setDestination(requestData.get("destination").toString());
        }
        
        // 处理其他基本字段
        if (requestData.get("totalPeople") != null) {
            assignmentDTO.setTotalPeople(Integer.valueOf(requestData.get("totalPeople").toString()));
        }
        if (requestData.get("adultCount") != null) {
            assignmentDTO.setAdultCount(Integer.valueOf(requestData.get("adultCount").toString()));
        }
        if (requestData.get("childCount") != null) {
            assignmentDTO.setChildCount(Integer.valueOf(requestData.get("childCount").toString()));
        }
        if (requestData.get("contactPerson") != null) {
            assignmentDTO.setContactPerson(requestData.get("contactPerson").toString());
        }
        if (requestData.get("contactPhone") != null) {
            assignmentDTO.setContactPhone(requestData.get("contactPhone").toString());
        }
        if (requestData.get("pickupMethod") != null) {
            assignmentDTO.setPickupMethod(requestData.get("pickupMethod").toString());
        }
        if (requestData.get("pickupLocation") != null) {
            assignmentDTO.setPickupLocation(requestData.get("pickupLocation").toString());
        }
        if (requestData.get("dropoffLocation") != null) {
            assignmentDTO.setDropoffLocation(requestData.get("dropoffLocation").toString());
        }
        if (requestData.get("remarks") != null) {
            assignmentDTO.setRemarks(requestData.get("remarks").toString());
        }
        
        // 处理数组字段
        if (requestData.get("tourScheduleOrderIds") != null) {
            @SuppressWarnings("unchecked")
            List<Object> orderIds = (List<Object>) requestData.get("tourScheduleOrderIds");
            List<Long> longOrderIds = orderIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
            assignmentDTO.setTourScheduleOrderIds(longOrderIds);
        }
        
        // TODO: 实现自动分配逻辑，目前先返回成功
        assignmentService.createAssignment(assignmentDTO);
        return Result.success("自动分配成功");
    }

    /**
     * 手动分配导游和车辆
     */
    @PostMapping("/manual-assign")
    @ApiOperation("手动分配导游和车辆")
    public Result<TourGuideVehicleAssignmentVO> manualAssign(@RequestBody Map<String, Object> requestData) {
        log.info("手动分配导游和车辆：{}", requestData);
        
        // 创建DTO并处理字段映射
        TourGuideVehicleAssignmentDTO assignmentDTO = new TourGuideVehicleAssignmentDTO();
        
        // 处理日期
        if (requestData.get("assignmentDate") != null) {
            assignmentDTO.setAssignmentDate(LocalDate.parse(requestData.get("assignmentDate").toString()));
        }
        
        // 处理location到destination的映射
        if (requestData.get("location") != null) {
            assignmentDTO.setDestination(requestData.get("location").toString());
        } else if (requestData.get("destination") != null) {
            assignmentDTO.setDestination(requestData.get("destination").toString());
        }
        
        // 处理其他字段
        if (requestData.get("guideId") != null) {
            assignmentDTO.setGuideId(Long.valueOf(requestData.get("guideId").toString()));
        }
        if (requestData.get("vehicleId") != null) {
            assignmentDTO.setVehicleId(Long.valueOf(requestData.get("vehicleId").toString()));
        }
        if (requestData.get("totalPeople") != null) {
            assignmentDTO.setTotalPeople(Integer.valueOf(requestData.get("totalPeople").toString()));
        }
        if (requestData.get("adultCount") != null) {
            assignmentDTO.setAdultCount(Integer.valueOf(requestData.get("adultCount").toString()));
        }
        if (requestData.get("childCount") != null) {
            assignmentDTO.setChildCount(Integer.valueOf(requestData.get("childCount").toString()));
        }
        if (requestData.get("contactPerson") != null) {
            assignmentDTO.setContactPerson(requestData.get("contactPerson").toString());
        }
        if (requestData.get("contactPhone") != null) {
            assignmentDTO.setContactPhone(requestData.get("contactPhone").toString());
        }
        if (requestData.get("pickupMethod") != null) {
            assignmentDTO.setPickupMethod(requestData.get("pickupMethod").toString());
        }
        if (requestData.get("pickupLocation") != null) {
            assignmentDTO.setPickupLocation(requestData.get("pickupLocation").toString());
        }
        if (requestData.get("dropoffLocation") != null) {
            assignmentDTO.setDropoffLocation(requestData.get("dropoffLocation").toString());
        }
        if (requestData.get("remarks") != null) {
            assignmentDTO.setRemarks(requestData.get("remarks").toString());
        }
        if (requestData.get("specialRequirements") != null) {
            assignmentDTO.setSpecialRequirements(requestData.get("specialRequirements").toString());
        }
        if (requestData.get("dietaryRestrictions") != null) {
            assignmentDTO.setDietaryRestrictions(requestData.get("dietaryRestrictions").toString());
        }
        if (requestData.get("emergencyContact") != null) {
            assignmentDTO.setEmergencyContact(requestData.get("emergencyContact").toString());
        }
        if (requestData.get("languagePreference") != null) {
            assignmentDTO.setLanguagePreference(requestData.get("languagePreference").toString());
        }
        
        // 处理数组字段
        if (requestData.get("tourScheduleOrderIds") != null) {
            @SuppressWarnings("unchecked")
            List<Object> orderIds = (List<Object>) requestData.get("tourScheduleOrderIds");
            List<Long> longOrderIds = orderIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
            assignmentDTO.setTourScheduleOrderIds(longOrderIds);
        }
        
        if (requestData.get("bookingIds") != null) {
            @SuppressWarnings("unchecked")
            List<Object> bookingIds = (List<Object>) requestData.get("bookingIds");
            List<Long> longBookingIds = bookingIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
            assignmentDTO.setBookingIds(longBookingIds);
        }
        
        Long assignmentId = assignmentService.createAssignment(assignmentDTO);
        TourGuideVehicleAssignmentVO result = assignmentService.getById(assignmentId);
        return Result.success(result);
    }

    /**
     * 分页查询导游分配记录
     */
    @GetMapping("/page")
    @ApiOperation("分页查询导游分配记录")
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
     * 根据ID获取分配详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取分配详情")
    public Result<TourGuideVehicleAssignmentVO> getById(@PathVariable Long id) {
        TourGuideVehicleAssignmentVO assignment = assignmentService.getById(id);
        return Result.success(assignment);
    }

    /**
     * 根据日期获取分配列表
     */
    @GetMapping("/by-date")
    @ApiOperation("根据日期获取分配列表")
    public Result<List<TourGuideVehicleAssignmentVO>> getByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDate(date);
        return Result.success(assignments);
    }

    /**
     * 更新分配信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新分配信息")
    public Result<String> updateAssignment(@PathVariable Long id, 
                                          @RequestBody TourGuideVehicleAssignmentDTO assignmentDTO) {
        log.info("更新分配记录，ID：{}，数据：{}", id, assignmentDTO);
        assignmentService.updateAssignment(id, assignmentDTO);
        return Result.success("更新成功");
    }

    /**
     * 取消分配
     */
    @DeleteMapping("/{id}")
    @ApiOperation("取消分配")
    public Result<String> cancelAssignment(@PathVariable Long id, @RequestParam(required = false) String reason) {
        log.info("取消分配记录，ID：{}，原因：{}", id, reason);
        assignmentService.deleteAssignment(id);
        return Result.success("取消成功");
    }

    /**
     * 批量分配
     */
    @PostMapping("/batch-assign")
    @ApiOperation("批量分配")
    public Result<String> batchAssign(@RequestBody List<TourGuideVehicleAssignmentDTO> assignmentDTOs) {
        log.info("批量分配导游和车辆，数量：{}", assignmentDTOs.size());
        assignmentService.batchCreateAssignment(assignmentDTOs);
        return Result.success("批量分配成功");
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
            return Result.success(statusVO);
        }
    }

    /**
     * 根据日期和地点获取分配详情（包含分配ID）
     */
    @GetMapping("/by-date-location")
    @ApiOperation("根据日期和地点获取分配详情")
    public Result<List<TourGuideVehicleAssignmentVO>> getAssignmentByDateAndLocation(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String location) {
        log.info("根据日期和地点获取分配详情：日期={}，地点={}", date, location);
        
        try {
            List<TourGuideVehicleAssignmentVO> assignments = assignmentService.getByDestination(location, date);
            return Result.success(assignments);
        } catch (Exception e) {
            log.error("获取分配详情失败：{}", e.getMessage(), e);
            return Result.error("获取分配详情失败");
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