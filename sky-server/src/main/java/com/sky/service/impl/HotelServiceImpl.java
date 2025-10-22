package com.sky.service.impl;

import com.sky.entity.Hotel;
import com.sky.exception.BusinessException;
import com.sky.mapper.HotelMapper;
import com.sky.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 酒店服务实现类
 */
@Service
@Slf4j
public class HotelServiceImpl implements HotelService {

    @Autowired
    private HotelMapper hotelMapper;

    @Override
    public List<Hotel> getAllHotels() {
        log.info("获取所有酒店列表");
        return hotelMapper.getAllActive();
    }

    @Override
    public Hotel getHotelById(Long id) {
        log.info("根据ID获取酒店详情：{}", id);
        return hotelMapper.getById(id.intValue());
    }

    @Override
    public void createHotel(Hotel hotel) {
        log.info("创建酒店：{}", hotel);
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        // 如果status为空，设置为active
        if (hotel.getStatus() == null || hotel.getStatus().trim().isEmpty()) {
            hotel.setStatus("active");
        }
        hotelMapper.insert(hotel);
    }

    @Override
    public void updateHotel(Hotel hotel) {
        log.info("更新酒店：{}", hotel);
        hotel.setUpdatedAt(LocalDateTime.now());
        // 如果status为空，设置为active
        if (hotel.getStatus() == null || hotel.getStatus().trim().isEmpty()) {
            hotel.setStatus("active");
        }
        hotelMapper.update(hotel);
    }

    @Override
    public void deleteHotel(Long id) {
        log.info("删除酒店：{}", id);
        
        // 检查是否有关联的房型
        int roomTypeCount = hotelMapper.countRoomTypesByHotelId(id.intValue());
        if (roomTypeCount > 0) {
            log.warn("无法删除酒店 ID: {}，存在 {} 个关联的房型", id, roomTypeCount);
            throw new BusinessException("无法删除该酒店，因为存在 " + roomTypeCount + " 个关联的房型。请先删除所有房型，或将酒店状态设置为'不活跃'。");
        }
        
        // 检查是否有关联的预订记录
        int bookingCount = hotelMapper.countBookingsByHotelId(id.intValue());
        if (bookingCount > 0) {
            log.warn("无法删除酒店 ID: {}，存在 {} 条关联的预订记录", id, bookingCount);
            throw new BusinessException("无法删除该酒店，因为存在 " + bookingCount + " 条关联的预订记录。请先取消或删除所有相关预订，或将酒店状态设置为'不活跃'。");
        }
        
        hotelMapper.deleteById(id.intValue());
        log.info("成功删除酒店：{}", id);
    }


} 