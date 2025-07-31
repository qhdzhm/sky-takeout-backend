package com.sky.service;

import com.sky.entity.HotelSupplier;
import java.util.List;

/**
 * 酒店供应商服务接口
 */
public interface HotelSupplierService {

    /**
     * 获取所有供应商列表
     */
    List<HotelSupplier> getAllSuppliers();

    /**
     * 根据ID获取供应商详情
     */
    HotelSupplier getSupplierById(Long id);

    /**
     * 创建供应商
     */
    void createSupplier(HotelSupplier supplier);

    /**
     * 更新供应商
     */
    void updateSupplier(HotelSupplier supplier);

    /**
     * 删除供应商
     */
    void deleteSupplier(Long id);
} 