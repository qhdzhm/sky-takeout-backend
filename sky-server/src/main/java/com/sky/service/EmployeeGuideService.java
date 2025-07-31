package com.sky.service;

import com.sky.dto.EmployeeGuideDTO;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeGuideVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工-导游管理服务接口
 */
public interface EmployeeGuideService {

    /**
     * 分页查询员工-导游信息
     */
    PageResult pageQuery(EmployeeGuideDTO queryDTO);

    /**
     * 将员工设置为导游
     */
    Long setEmployeeAsGuide(EmployeeGuideDTO employeeGuideDTO);

    /**
     * 取消员工的导游身份
     */
    void removeGuideRole(Long employeeId);

    /**
     * 更新导游信息
     */
    void updateGuideInfo(EmployeeGuideDTO employeeGuideDTO);

    /**
     * 根据员工ID获取导游信息
     */
    EmployeeGuideVO getGuideByEmployeeId(Long employeeId);

    /**
     * 获取所有导游员工列表
     */
    List<EmployeeGuideVO> getAllGuideEmployees();

    /**
     * 批量设置导游可用性
     */
    void batchSetGuideAvailability(Long guideId, LocalDate startDate, LocalDate endDate, 
                                   LocalTime startTime, LocalTime endTime, String status, String notes);

    /**
     * 获取导游可用性统计
     */
    List<EmployeeGuideVO> getGuideAvailabilityStats(LocalDate date);
} 