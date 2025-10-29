package com.sky.service.impl;

import com.sky.entity.HotelDailyPrice;
import com.sky.mapper.HotelDailyPriceMapper;
import com.sky.service.HotelDailyPriceService;
import com.sky.service.HotelPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 酒店每日价格服务实现类
 */
@Service
@Slf4j
public class HotelDailyPriceServiceImpl implements HotelDailyPriceService {
    
    @Autowired
    private HotelDailyPriceMapper hotelDailyPriceMapper;
    
    @Autowired
    private HotelPriceService hotelPriceService;
    
    @Override
    public List<HotelDailyPrice> getByLevelAndDateRange(String hotelLevel, LocalDate startDate, LocalDate endDate) {
        log.info("查询酒店星级{}在{}到{}的每日价格", hotelLevel, startDate, endDate);
        return hotelDailyPriceMapper.selectByLevelAndDateRange(hotelLevel, startDate, endDate);
    }
    
    @Override
    public List<HotelDailyPrice> getByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("查询{}到{}所有星级的每日价格", startDate, endDate);
        return hotelDailyPriceMapper.selectByDateRange(startDate, endDate);
    }
    
    @Override
    public HotelDailyPrice getByLevelAndDate(String hotelLevel, LocalDate priceDate) {
        log.info("查询酒店星级{}在{}的价格", hotelLevel, priceDate);
        return hotelDailyPriceMapper.selectByLevelAndDate(hotelLevel, priceDate);
    }
    
    @Override
    @Transactional
    public boolean saveOrUpdate(HotelDailyPrice hotelDailyPrice) {
        log.info("保存或更新酒店每日价格：{}", hotelDailyPrice);
        
        // 检查是否已存在
        HotelDailyPrice existing = hotelDailyPriceMapper.selectByLevelAndDate(
                hotelDailyPrice.getHotelLevel(), 
                hotelDailyPrice.getPriceDate()
        );
        
        int result;
        if (existing != null) {
            // 更新
            hotelDailyPrice.setId(existing.getId());
            result = hotelDailyPriceMapper.update(hotelDailyPrice);
            log.info("更新酒店每日价格，结果：{}", result);
        } else {
            // 插入
            result = hotelDailyPriceMapper.insert(hotelDailyPrice);
            log.info("插入酒店每日价格，结果：{}", result);
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean batchSaveOrUpdate(List<HotelDailyPrice> list) {
        log.info("批量保存或更新酒店每日价格，数量：{}", list.size());
        int result = hotelDailyPriceMapper.batchInsert(list);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("删除酒店每日价格，ID：{}", id);
        int result = hotelDailyPriceMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteByLevelAndDate(String hotelLevel, LocalDate priceDate) {
        log.info("删除酒店星级{}在{}的价格", hotelLevel, priceDate);
        int result = hotelDailyPriceMapper.deleteByLevelAndDate(hotelLevel, priceDate);
        return result > 0;
    }
    
    @Override
    public BigDecimal getPriceDifferenceByLevelAndDate(String hotelLevel, LocalDate priceDate) {
        log.info("获取酒店星级{}在{}的价格差异", hotelLevel, priceDate);
        HotelDailyPrice dailyPrice = hotelDailyPriceMapper.selectByLevelAndDate(hotelLevel, priceDate);
        if (dailyPrice != null && dailyPrice.getPriceDifference() != null) {
            log.info("找到每日价格差异配置：{}元/人", dailyPrice.getPriceDifference());
            return dailyPrice.getPriceDifference();
        }
        // 如果没有设置每日价格差异，使用 hotel_price_differences 表中的固定价格差异
        log.info("未找到酒店星级{}在{}的每日价格差异配置，使用固定价格差异", hotelLevel, priceDate);
        BigDecimal fixedPriceDifference = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
        log.info("使用固定价格差异：{}元/人", fixedPriceDifference);
        return fixedPriceDifference;
    }
    
    @Override
    public BigDecimal getSingleRoomSupplementByLevelAndDate(String hotelLevel, LocalDate priceDate) {
        log.info("获取酒店星级{}在{}的单房差", hotelLevel, priceDate);
        HotelDailyPrice dailyPrice = hotelDailyPriceMapper.selectByLevelAndDate(hotelLevel, priceDate);
        if (dailyPrice != null && dailyPrice.getDailySingleRoomSupplement() != null) {
            log.info("找到每日单房差配置：{}元/晚", dailyPrice.getDailySingleRoomSupplement());
            return dailyPrice.getDailySingleRoomSupplement();
        }
        // 如果没有设置每日单房差，使用 hotel_price_differences 表中的固定单房差
        log.info("未找到酒店星级{}在{}的每日单房差配置，使用固定单房差", hotelLevel, priceDate);
        BigDecimal fixedSingleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
        log.info("使用固定单房差：{}元/晚", fixedSingleRoomSupplement);
        return fixedSingleRoomSupplement;
    }
}

