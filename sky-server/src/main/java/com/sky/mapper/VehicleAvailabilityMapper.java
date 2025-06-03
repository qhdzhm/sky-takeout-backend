package com.sky.mapper;

import com.sky.dto.VehicleAvailabilityDTO;
import com.sky.vo.VehicleAvailabilityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

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
} 