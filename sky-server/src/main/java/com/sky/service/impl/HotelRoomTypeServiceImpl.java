package com.sky.service.impl;

import com.sky.entity.HotelRoomType;
import com.sky.exception.BusinessException;
import com.sky.mapper.HotelRoomTypeMapper;
import com.sky.service.HotelRoomTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 酒店房型服务实现类
 */
@Service
@Slf4j
public class HotelRoomTypeServiceImpl implements HotelRoomTypeService {

    @Autowired
    private HotelRoomTypeMapper hotelRoomTypeMapper;

    @Override
    public List<HotelRoomType> getAllRoomTypes() {
        log.info("获取所有房型列表");
        return hotelRoomTypeMapper.getAll();
    }

    @Override
    public List<HotelRoomType> getRoomTypesByHotelId(Long hotelId) {
        log.info("根据酒店ID获取房型列表：{}", hotelId);
        return hotelRoomTypeMapper.getByHotelId(hotelId.intValue());
    }

    @Override
    public HotelRoomType getRoomTypeById(Long id) {
        log.info("根据ID获取房型详情：{}", id);
        return hotelRoomTypeMapper.getById(id.intValue());
    }

    @Override
    public void createRoomType(HotelRoomType roomType) {
        log.info("创建房型：{}", roomType);
        roomType.setCreatedAt(LocalDateTime.now());
        roomType.setUpdatedAt(LocalDateTime.now());
        // 如果status为空，设置为active
        if (roomType.getStatus() == null || roomType.getStatus().trim().isEmpty()) {
            roomType.setStatus("active");
        }
        hotelRoomTypeMapper.insert(roomType);
    }

    @Override
    public void updateRoomType(HotelRoomType roomType) {
        log.info("更新房型：{}", roomType);
        roomType.setUpdatedAt(LocalDateTime.now());
        // 如果status为空，设置为active
        if (roomType.getStatus() == null || roomType.getStatus().trim().isEmpty()) {
            roomType.setStatus("active");
        }
        hotelRoomTypeMapper.update(roomType);
    }

    @Override
    public void deleteRoomType(Long id) {
        log.info("删除房型：{}", id);
        
        // 检查是否有关联的预订记录
        int bookingCount = hotelRoomTypeMapper.countBookingsByRoomTypeId(id.intValue());
        if (bookingCount > 0) {
            log.warn("无法删除房型 ID: {}，存在 {} 条关联的预订记录", id, bookingCount);
            throw new BusinessException("无法删除该房型，因为存在 " + bookingCount + " 条关联的预订记录。请先取消或删除所有相关预订，或将房型状态设置为'不活跃'。");
        }
        
        hotelRoomTypeMapper.deleteById(id.intValue());
        log.info("成功删除房型：{}", id);
    }
} 