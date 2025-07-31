package com.sky.service;

import com.sky.dto.TourGuideVehicleAssignmentDTO;
import com.sky.result.PageResult;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;
import com.sky.vo.TourGuideVehicleAssignmentVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游车辆游客分配Service接口
 */
public interface TourGuideVehicleAssignmentService {

    /**
     * 获取可用导游列表
     */
    List<GuideAvailabilityVO> getAvailableGuides(LocalDate date, LocalTime startTime, LocalTime endTime, String location);

    /**
     * 获取可用车辆列表
     */
    List<VehicleAvailabilityVO> getAvailableVehicles(LocalDate date, LocalTime startTime, LocalTime endTime, Integer peopleCount);

    /**
     * 创建分配记录
     * 包含业务逻辑：检查导游和车辆可用性，更新状态，保存分配记录
     */
    Long createAssignment(TourGuideVehicleAssignmentDTO assignmentDTO);

    /**
     * 批量创建分配记录
     */
    void batchCreateAssignment(List<TourGuideVehicleAssignmentDTO> assignmentDTOs);

    /**
     * 根据ID查询分配记录
     */
    TourGuideVehicleAssignmentVO getById(Long id);

    /**
     * 根据日期查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByDate(LocalDate assignmentDate);

    /**
     * 根据日期范围查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 根据目的地查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByDestination(String destination, LocalDate assignmentDate);

    /**
     * 根据导游ID查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByGuideId(Long guideId, LocalDate assignmentDate);

    /**
     * 根据车辆ID查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByVehicleId(Long vehicleId, LocalDate assignmentDate);

    /**
     * 更新分配记录
     */
    void updateAssignment(Long id, TourGuideVehicleAssignmentDTO assignmentDTO);

    /**
     * 取消分配记录
     * 包含业务逻辑：更新分配状态为取消，释放导游和车辆资源
     */
    void cancelAssignment(Long id);

    /**
     * 删除分配记录
     */
    void deleteAssignment(Long id);

    /**
     * 根据订单ID列表查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByBookingIds(List<Long> bookingIds);

    /**
     * 分页查询分配记录
     */
    PageResult pageQuery(int page, int pageSize, LocalDate startDate, LocalDate endDate,
                        String destination, String guideName, String licensePlate, String status);

    /**
     * 统计指定日期的分配数量
     */
    int countByDate(LocalDate assignmentDate);

    /**
     * 检查导游在指定日期是否已有分配
     */
    boolean checkGuideAssigned(Long guideId, LocalDate assignmentDate);

    /**
     * 检查车辆在指定日期是否已有分配
     */
    boolean checkVehicleAssigned(Long vehicleId, LocalDate assignmentDate);

    /**
     * 获取指定日期的分配统计信息
     */
    AssignmentStatistics getAssignmentStatistics(LocalDate assignmentDate);

    /**
     * 分配统计信息内部类
     */
    class AssignmentStatistics {
        private int totalAssignments;
        private int totalGuides;
        private int totalVehicles;
        private int totalPeople;
        private List<String> destinations;

        // getters and setters
        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
        public int getTotalGuides() { return totalGuides; }
        public void setTotalGuides(int totalGuides) { this.totalGuides = totalGuides; }
        public int getTotalVehicles() { return totalVehicles; }
        public void setTotalVehicles(int totalVehicles) { this.totalVehicles = totalVehicles; }
        public int getTotalPeople() { return totalPeople; }
        public void setTotalPeople(int totalPeople) { this.totalPeople = totalPeople; }
        public List<String> getDestinations() { return destinations; }
        public void setDestinations(List<String> destinations) { this.destinations = destinations; }
    }
} 