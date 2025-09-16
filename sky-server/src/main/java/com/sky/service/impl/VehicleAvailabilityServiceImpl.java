package com.sky.service.impl;

import com.sky.dto.VehicleAvailabilityDTO;
import com.sky.mapper.VehicleAvailabilityMapper;
import com.sky.service.VehicleAvailabilityService;
import com.sky.vo.VehicleAvailabilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VehicleAvailabilityServiceImpl implements VehicleAvailabilityService {

    @Autowired
    private VehicleAvailabilityMapper vehicleAvailabilityMapper;

    /**
     * 获取车辆可用性列表
     */
    @Override
    public List<VehicleAvailabilityVO> getVehicleAvailability(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        // 如果没有提供日期范围，默认查询当前月份
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // 当前月份第一天
        }
        if (endDate == null) {
            endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // 当前月份最后一天
        }
        
        return vehicleAvailabilityMapper.getVehicleAvailability(vehicleId, startDate, endDate);
    }

    /**
     * 设置车辆可用性
     */
    @Override
    public void setVehicleAvailability(VehicleAvailabilityDTO vehicleAvailabilityDTO) {
        // 检查是否已存在该日期的设置
        VehicleAvailabilityVO existing = vehicleAvailabilityMapper.getVehicleAvailabilityByDate(
                vehicleAvailabilityDTO.getVehicleId(), vehicleAvailabilityDTO.getAvailableDate());
        
        if (existing != null) {
            // 更新现有记录
            vehicleAvailabilityMapper.updateVehicleAvailability(vehicleAvailabilityDTO);
        } else {
            // 插入新记录
            vehicleAvailabilityMapper.insertVehicleAvailability(vehicleAvailabilityDTO);
        }
    }

    /**
     * 批量设置车辆可用性
     */
    @Override
    public void batchSetVehicleAvailability(VehicleAvailabilityDTO vehicleAvailabilityDTO) {
        LocalDate startDate = vehicleAvailabilityDTO.getStartDate();
        LocalDate endDate = vehicleAvailabilityDTO.getEndDate();
        Boolean excludeWeekends = vehicleAvailabilityDTO.getExcludeWeekends();
        
        List<VehicleAvailabilityDTO> batchList = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 如果排除周末，跳过周六和周日
            if (excludeWeekends != null && excludeWeekends) {
                if (currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7) {
                    currentDate = currentDate.plusDays(1);
                    continue;
                }
            }
            
            VehicleAvailabilityDTO dto = new VehicleAvailabilityDTO();
            BeanUtils.copyProperties(vehicleAvailabilityDTO, dto);
            dto.setAvailableDate(currentDate);
            batchList.add(dto);
            
            currentDate = currentDate.plusDays(1);
        }
        
        // 批量插入或更新
        for (VehicleAvailabilityDTO dto : batchList) {
            setVehicleAvailability(dto);
        }
    }

    /**
     * 删除车辆可用性设置
     */
    @Override
    public void deleteVehicleAvailability(Long vehicleId, LocalDate date) {
        vehicleAvailabilityMapper.deleteVehicleAvailability(vehicleId, date);
    }

    /**
     * 同步车辆过期状态到可用性表
     * 当车辆rego或检查过期时，自动将未来的可用性设置为out_of_service
     */
    @Override
    public void syncExpiredVehicleStatus() {
        log.info("开始同步过期车辆状态到可用性表");
        
        try {
            // 获取所有过期的车辆ID
            List<Long> expiredVehicleIds = vehicleAvailabilityMapper.getExpiredVehicleIds();
            log.info("发现 {} 辆过期车辆需要同步状态", expiredVehicleIds.size());
            
            if (!expiredVehicleIds.isEmpty()) {
                // 批量更新过期车辆的未来可用性
                vehicleAvailabilityMapper.updateExpiredVehiclesFutureAvailability();
                log.info("✅ 批量更新过期车辆可用性状态完成");
                
                // 记录每辆车的同步情况
                for (Long vehicleId : expiredVehicleIds) {
                    log.info("车辆 {} 的未来可用性已设置为 out_of_service", vehicleId);
                }
            } else {
                log.info("没有发现过期车辆需要同步");
            }
        } catch (Exception e) {
            log.error("同步过期车辆状态时出现异常", e);
            throw new RuntimeException("同步过期车辆状态失败", e);
        }
    }

    /**
     * 为单个车辆同步过期状态到可用性表
     * @param vehicleId 车辆ID
     */
    @Override
    public void syncSingleVehicleExpiredStatus(Long vehicleId) {
        log.info("开始同步车辆 {} 的过期状态到可用性表", vehicleId);
        
        try {
            vehicleAvailabilityMapper.updateSingleVehicleFutureAvailability(vehicleId);
            log.info("✅ 车辆 {} 的可用性状态同步完成", vehicleId);
        } catch (Exception e) {
            log.error("同步车辆 {} 过期状态时出现异常", vehicleId, e);
            throw new RuntimeException("同步车辆过期状态失败", e);
        }
    }
} 