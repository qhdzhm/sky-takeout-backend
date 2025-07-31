package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.EmployeeGuideDTO;
import com.sky.vo.EmployeeGuideVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工-导游管理Mapper接口
 */
@Mapper
public interface EmployeeGuideMapper {
    
    /**
     * 分页查询员工-导游信息
     */
    List<EmployeeGuideVO> pageQuery(EmployeeGuideDTO queryDTO);
    
    /**
     * 调用存储过程设置员工为导游
     */
    Long setEmployeeAsGuide(@Param("employeeId") Long employeeId,
                           @Param("languages") String languages,
                           @Param("experienceYears") Integer experienceYears,
                           @Param("hourlyRate") Double hourlyRate,
                           @Param("dailyRate") Double dailyRate,
                           @Param("maxGroups") Integer maxGroups);
    
    /**
     * 更新导游额外信息
     */
    void updateGuideExtraInfo(EmployeeGuideDTO employeeGuideDTO);
    
    /**
     * 统计员工的活跃分配数量
     */
    int countActiveAssignmentsByEmployeeId(@Param("employeeId") Long employeeId);
    
    /**
     * 取消员工的导游角色
     */
    void removeGuideRole(@Param("employeeId") Long employeeId);
    
    /**
     * 停用导游记录
     */
    void deactivateGuide(@Param("employeeId") Long employeeId);
    
    /**
     * 更新导游信息
     */
    void updateGuideInfo(EmployeeGuideDTO employeeGuideDTO);
    
    /**
     * 根据员工ID获取导游信息
     */
    EmployeeGuideVO getGuideByEmployeeId(@Param("employeeId") Long employeeId);
    
    /**
     * 获取所有导游员工列表
     */
    List<EmployeeGuideVO> getAllGuideEmployees();
    
    /**
     * 批量设置导游可用性
     */
    String batchSetGuideAvailability(@Param("guideId") Long guideId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime,
                                    @Param("status") String status,
                                    @Param("notes") String notes);
    
    /**
     * 获取导游可用性统计
     */
    List<EmployeeGuideVO> getGuideAvailabilityStats(@Param("date") LocalDate date);
} 