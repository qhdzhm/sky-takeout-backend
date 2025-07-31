package com.sky.service.impl;

import com.sky.entity.HotelSupplier;
import com.sky.mapper.HotelSupplierMapper;
import com.sky.service.HotelSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 酒店供应商服务实现类
 */
@Service
@Slf4j
public class HotelSupplierServiceImpl implements HotelSupplierService {

    @Autowired
    private HotelSupplierMapper hotelSupplierMapper;

    @Override
    public List<HotelSupplier> getAllSuppliers() {
        log.info("获取所有供应商列表");
        return hotelSupplierMapper.getAllActive();
    }

    @Override
    public HotelSupplier getSupplierById(Long id) {
        log.info("根据ID获取供应商详情：{}", id);
        return hotelSupplierMapper.getById(id.intValue());
    }

    @Override
    public void createSupplier(HotelSupplier supplier) {
        log.info("创建供应商：{}", supplier);
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setUpdatedAt(LocalDateTime.now());
        // 如果status为空，设置为active
        if (supplier.getStatus() == null || supplier.getStatus().trim().isEmpty()) {
            supplier.setStatus("active");
        }
        hotelSupplierMapper.insert(supplier);
    }

    @Override
    public void updateSupplier(HotelSupplier supplier) {
        log.info("更新供应商：{}", supplier);
        supplier.setUpdatedAt(LocalDateTime.now());
        // 如果status为空，设置为active
        if (supplier.getStatus() == null || supplier.getStatus().trim().isEmpty()) {
            supplier.setStatus("active");
        }
        hotelSupplierMapper.update(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        log.info("删除供应商：{}", id);
        hotelSupplierMapper.deleteById(id.intValue());
    }
} 