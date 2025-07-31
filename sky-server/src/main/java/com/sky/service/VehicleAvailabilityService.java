package com.sky.service;

import com.sky.dto.VehicleAvailabilityDTO;
import com.sky.vo.VehicleAvailabilityVO;

import java.time.LocalDate;
import java.util.List;

public interface VehicleAvailabilityService {

    /**
     * 获取车辆可用性列表
     */
    List<VehicleAvailabilityVO> getVehicleAvailability(Long vehicleId, LocalDate startDate, LocalDate endDate);

    /**
     * 设置车辆可用性
     */
    void setVehicleAvailability(VehicleAvailabilityDTO vehicleAvailabilityDTO);

    /**
     * 批量设置车辆可用性
     */
    void batchSetVehicleAvailability(VehicleAvailabilityDTO vehicleAvailabilityDTO);

    /**
     * 删除车辆可用性设置
     */
    void deleteVehicleAvailability(Long vehicleId, LocalDate date);
} 