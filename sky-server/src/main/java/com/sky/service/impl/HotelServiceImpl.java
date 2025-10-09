package com.sky.service.impl;

import com.sky.entity.Hotel;
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
        hotelMapper.deleteById(id.intValue());
    }


} 