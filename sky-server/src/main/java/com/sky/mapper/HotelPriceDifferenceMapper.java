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
} 