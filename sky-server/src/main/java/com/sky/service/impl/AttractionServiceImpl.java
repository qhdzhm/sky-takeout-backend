package com.sky.service.impl;

import com.sky.entity.Attraction;
import com.sky.mapper.AttractionMapper;
import com.sky.service.AttractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 景点服务实现类 - 基于酒店服务实现类设计
 */
@Service
@Slf4j
public class AttractionServiceImpl implements AttractionService {

    @Autowired
    private AttractionMapper attractionMapper;

    @Override
    public List<Attraction> getAllAttractions() {
        log.info("获取所有景点列表");
        return attractionMapper.getAll();
    }

    @Override
    public List<Attraction> getAllActiveAttractions() {
        log.info("获取所有活跃景点列表");
        return attractionMapper.getAllActive();
    }

    @Override
    public Attraction getAttractionById(Long id) {
        log.info("根据ID获取景点详情：{}", id);
        return attractionMapper.getById(id);
    }

    @Override
    public void createAttraction(Attraction attraction) {
        log.info("创建景点：{}", attraction);
        attraction.setCreatedAt(LocalDateTime.now());
        attraction.setUpdatedAt(LocalDateTime.now());
        
        // 如果status为空，设置为active
        if (attraction.getStatus() == null || attraction.getStatus().trim().isEmpty()) {
            attraction.setStatus("active");
        }
        
        // 如果advanceDays为空，设置为默认值1
        if (attraction.getAdvanceDays() == null) {
            attraction.setAdvanceDays(1);
        }
        
        attractionMapper.insert(attraction);
    }

    @Override
    public void updateAttraction(Attraction attraction) {
        log.info("更新景点：{}", attraction);
        attraction.setUpdatedAt(LocalDateTime.now());
        
        // 如果status为空，设置为active
        if (attraction.getStatus() == null || attraction.getStatus().trim().isEmpty()) {
            attraction.setStatus("active");
        }
        
        attractionMapper.update(attraction);
    }

    @Override
    public void deleteAttraction(Long id) {
        log.info("删除景点：{}", id);
        attractionMapper.deleteById(id);
    }

    @Override
    public List<Attraction> getAttractionsByBookingType(String bookingType) {
        log.info("根据预订方式获取景点列表：{}", bookingType);
        return attractionMapper.getByBookingType(bookingType);
    }

    @Override
    public List<Attraction> getAttractionsByLocation(String location) {
        log.info("根据位置获取景点列表：{}", location);
        return attractionMapper.getByLocation(location);
    }

    @Override
    public List<Attraction> searchAttractionsByName(String name) {
        log.info("根据景点名称模糊查询：{}", name);
        return attractionMapper.getByNameLike(name);
    }

    @Override
    public void updateAttractionStatus(Long id, String status) {
        log.info("更新景点状态：id={}, status={}", id, status);
        attractionMapper.updateStatus(id, status);
    }

    @Override
    public List<Attraction> searchAttractions(String attractionName, String location, String bookingType, String status) {
        log.info("根据条件搜索景点：attractionName={}, location={}, bookingType={}, status={}", 
                attractionName, location, bookingType, status);
        return attractionMapper.searchAttractions(attractionName, location, bookingType, status);
    }
}

