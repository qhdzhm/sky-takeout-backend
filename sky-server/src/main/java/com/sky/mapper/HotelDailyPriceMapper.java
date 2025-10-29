package com.sky.mapper;

import com.sky.entity.HotelDailyPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店每日价格Mapper接口
 */
@Mapper
public interface HotelDailyPriceMapper {
    
    /**
     * 根据酒店星级和日期范围查询每日价格
     */
    List<HotelDailyPrice> selectByLevelAndDateRange(
            @Param("hotelLevel") String hotelLevel,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 根据日期范围查询所有星级的每日价格
     */
    List<HotelDailyPrice> selectByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 根据酒店星级和具体日期查询价格
     */
    HotelDailyPrice selectByLevelAndDate(
            @Param("hotelLevel") String hotelLevel,
            @Param("priceDate") LocalDate priceDate
    );
    
    /**
     * 插入每日价格
     */
    int insert(HotelDailyPrice hotelDailyPrice);
    
    /**
     * 更新每日价格
     */
    int update(HotelDailyPrice hotelDailyPrice);
    
    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据星级和日期删除
     */
    int deleteByLevelAndDate(
            @Param("hotelLevel") String hotelLevel,
            @Param("priceDate") LocalDate priceDate
    );
    
    /**
     * 批量插入每日价格
     */
    int batchInsert(@Param("list") List<HotelDailyPrice> list);
}







