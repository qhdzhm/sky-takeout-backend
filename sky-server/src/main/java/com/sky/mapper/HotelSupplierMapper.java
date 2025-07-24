package com.sky.mapper;

import com.sky.entity.HotelSupplier;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 酒店供应商Mapper接口
 */
@Mapper
public interface HotelSupplierMapper {

    /**
     * 插入供应商信息
     * @param supplier 供应商信息
     */
    void insert(HotelSupplier supplier);

    /**
     * 根据ID查询供应商
     * @param id 供应商ID
     * @return 供应商信息
     */
    HotelSupplier getById(Integer id);

    /**
     * 更新供应商信息
     * @param supplier 供应商信息
     */
    void update(HotelSupplier supplier);

    /**
     * 删除供应商
     * @param id 供应商ID
     */
    void deleteById(Integer id);

    /**
     * 获取所有活跃的供应商
     * @return 供应商列表
     */
    List<HotelSupplier> getAllActive();
} 