package com.sky.mapper;

import com.sky.dto.VehicleAvailabilityDTO;
import com.sky.vo.VehicleAvailabilityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 车辆可用性Mapper接口
 */
@Mapper
public interface VehicleAvailabilityMapper {

    /**
     * 获取车辆可用性列表
     */
    List<VehicleAvailabilityVO> getVehicleAvailability(@Param("vehicleId") Long vehicleId, 
                                                       @Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 根据日期获取车辆可用性
     */
    VehicleAvailabilityVO getVehicleAvailabilityByDate(@Param("vehicleId") Long vehicleId, 
                                                       @Param("date") LocalDate date);

    /**
     * 插入车辆可用性
     */
    void insertVehicleAvailability(VehicleAvailabilityDTO vehicleAvailabilityDTO);

    /**
     * 更新车辆可用性
     */
    void updateVehicleAvailability(VehicleAvailabilityDTO vehicleAvailabilityDTO);

    /**
     * 删除车辆可用性
     */
    void deleteVehicleAvailability(@Param("vehicleId") Long vehicleId, @Param("date") LocalDate date);

    /**
     * 更新车辆可用性状态
     */
    void updateAvailability(@Param("vehicleId") Long vehicleId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime,
                           @Param("isAvailable") Boolean isAvailable);

    /**
     * 检查车辆在指定时间段是否可用
     */
    Boolean checkAvailability(@Param("vehicleId") Long vehicleId,
                             @Param("date") LocalDate date,
                             @Param("startTime") LocalTime startTime,
                             @Param("endTime") LocalTime endTime);

    /**
     * 设置车辆为使用中状态
     */
    void setInUse(@Param("vehicleId") Long vehicleId,
                  @Param("date") LocalDate date);

    /**
     * 设置车辆为可用状态
     */
    void setAvailable(@Param("vehicleId") Long vehicleId,
                     @Param("date") LocalDate date);

    /**
     * 重置车辆可用性状态（取消分配时用）
     */
    void resetAvailability(@Param("vehicleId") Long vehicleId,
                          @Param("date") LocalDate date);

    /**
     * 根据日期、时间和人数获取可用车辆
     */
    List<VehicleAvailabilityVO> getAvailableVehiclesByDateTime(@Param("date") LocalDate date,
                                                            @Param("startTime") LocalTime startTime,
                                                            @Param("endTime") LocalTime endTime,
                                                            @Param("peopleCount") Integer peopleCount);
} 