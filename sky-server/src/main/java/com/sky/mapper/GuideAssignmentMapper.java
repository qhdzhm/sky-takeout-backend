package com.sky.mapper;

import com.sky.dto.GuideAssignmentQueryDTO;
import com.sky.entity.GuideDailyAssignment;
import com.sky.vo.GuideAssignmentVO;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游分配Mapper接口
 */
@Mapper
public interface GuideAssignmentMapper {

    /**
     * 分页查询导游分配记录
     */
    List<GuideAssignmentVO> pageQuery(GuideAssignmentQueryDTO queryDTO);

    /**
     * 查询总记录数
     */
    Long countQuery(GuideAssignmentQueryDTO queryDTO);

    /**
     * 获取可用导游列表
     */
    List<GuideAvailabilityVO> getAvailableGuides(@Param("date") LocalDate date,
                                                  @Param("startTime") LocalTime startTime,
                                                  @Param("endTime") LocalTime endTime,
                                                  @Param("location") String location);

    /**
     * 获取可用车辆列表
     */
    List<VehicleAvailabilityVO> getAvailableVehicles(@Param("date") LocalDate date,
                                                      @Param("startTime") LocalTime startTime,
                                                      @Param("endTime") LocalTime endTime,
                                                      @Param("peopleCount") Integer peopleCount);

    /**
     * 插入分配记录
     */
    void insert(GuideDailyAssignment assignment);

    /**
     * 更新分配记录
     */
    void update(GuideDailyAssignment assignment);

    /**
     * 根据ID查询分配记录
     */
    GuideAssignmentVO getById(Long id);

    /**
     * 根据日期查询分配记录
     */
    List<GuideAssignmentVO> getByDate(LocalDate date);

    /**
     * 调用自动分配存储过程
     */
    void callAutoAssignProcedure(@Param("assignmentDate") LocalDate assignmentDate,
                                 @Param("location") String location,
                                 @Param("totalPeople") Integer totalPeople,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime,
                                 @Param("assignmentId") Long assignmentId,
                                 @Param("resultMessage") String resultMessage);

    /**
     * 检查导游可用性
     */
    Boolean checkGuideAvailability(@Param("guideId") Long guideId,
                                   @Param("date") LocalDate date,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime);

    /**
     * 检查车辆可用性
     */
    Boolean checkVehicleAvailability(@Param("vehicleId") Long vehicleId,
                                     @Param("date") LocalDate date,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime);
} 