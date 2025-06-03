package com.sky.service;

import com.sky.dto.GuideAssignmentDTO;
import com.sky.dto.GuideAssignmentQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.GuideAssignmentVO;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游分配服务接口
 */
public interface GuideAssignmentService {

    /**
     * 分页查询导游分配记录
     */
    PageResult pageQuery(GuideAssignmentQueryDTO queryDTO);

    /**
     * 获取可用导游列表
     */
    List<GuideAvailabilityVO> getAvailableGuides(LocalDate date, LocalTime startTime, LocalTime endTime, String location);

    /**
     * 获取可用车辆列表
     */
    List<VehicleAvailabilityVO> getAvailableVehicles(LocalDate date, LocalTime startTime, LocalTime endTime, Integer peopleCount);

    /**
     * 自动分配导游和车辆
     */
    GuideAssignmentVO autoAssign(GuideAssignmentDTO assignmentDTO);

    /**
     * 手动分配导游和车辆
     */
    GuideAssignmentVO manualAssign(GuideAssignmentDTO assignmentDTO);

    /**
     * 更新分配信息
     */
    void update(GuideAssignmentDTO assignmentDTO);

    /**
     * 取消分配
     */
    void cancel(Long id, String reason);

    /**
     * 根据ID获取分配详情
     */
    GuideAssignmentVO getById(Long id);

    /**
     * 根据日期获取分配列表
     */
    List<GuideAssignmentVO> getByDate(LocalDate date);

    /**
     * 批量分配
     */
    List<GuideAssignmentVO> batchAssign(List<GuideAssignmentDTO> assignmentDTOs);
} 