package com.sky.mapper;

import com.sky.entity.HotelPriceDifference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 酒店价格差异Mapper接口
 */
@Mapper
public interface HotelPriceDifferenceMapper {
    
    /**
     * 获取所有酒店价格差异
     */
    List<HotelPriceDifference> selectAll();
    
    /**
     * 根据酒店星级获取价格差异
     */
    HotelPriceDifference selectByLevel(@Param("hotelLevel") String hotelLevel);
    
    /**
     * 获取基准酒店等级
     */
    HotelPriceDifference selectBaseLevel();
    
    /**
     * 根据ID获取酒店价格差异
     */
    HotelPriceDifference selectById(@Param("id") Integer id);
    
    /**
     * 插入酒店价格差异
     */
    int insert(HotelPriceDifference hotelPriceDifference);
    
    /**
     * 更新酒店价格差异
     */
    int update(HotelPriceDifference hotelPriceDifference);
    
    /**
     * 根据ID删除酒店价格差异
     */
    int deleteById(@Param("id") Integer id);
} 