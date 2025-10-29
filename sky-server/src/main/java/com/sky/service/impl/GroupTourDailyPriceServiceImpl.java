package com.sky.service.impl;

import com.sky.dto.GroupTourDTO;
import com.sky.entity.GroupTourDailyPrice;
import com.sky.mapper.GroupTourDailyPriceMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.service.GroupTourDailyPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 团队游每日价格服务实现类
 */
@Service
@Slf4j
public class GroupTourDailyPriceServiceImpl implements GroupTourDailyPriceService {

    @Autowired
    private GroupTourDailyPriceMapper groupTourDailyPriceMapper;

    @Autowired
    private GroupTourMapper groupTourMapper;

    @Override
    public List<GroupTourDailyPrice> getByTourIdAndDateRange(Integer groupTourId, LocalDate startDate, LocalDate endDate) {
        log.info("查询团队游ID={}在{}到{}的每日价格", groupTourId, startDate, endDate);
        return groupTourDailyPriceMapper.selectByTourIdAndDateRange(groupTourId, startDate, endDate);
    }

    @Override
    public List<GroupTourDailyPrice> getByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("查询{}到{}所有团队游的每日价格", startDate, endDate);
        return groupTourDailyPriceMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    public GroupTourDailyPrice getByTourIdAndDate(Integer groupTourId, LocalDate priceDate) {
        log.info("查询团队游ID={}在{}的价格", groupTourId, priceDate);
        return groupTourDailyPriceMapper.selectByTourIdAndDate(groupTourId, priceDate);
    }

    @Override
    @Transactional
    public boolean saveOrUpdate(GroupTourDailyPrice groupTourDailyPrice) {
        log.info("保存或更新团队游每日价格：{}", groupTourDailyPrice);

        // 检查是否已存在
        GroupTourDailyPrice existing = groupTourDailyPriceMapper.selectByTourIdAndDate(
                groupTourDailyPrice.getGroupTourId(),
                groupTourDailyPrice.getPriceDate()
        );

        int result;
        if (existing != null) {
            // 更新
            groupTourDailyPrice.setId(existing.getId());
            result = groupTourDailyPriceMapper.update(groupTourDailyPrice);
            log.info("更新团队游每日价格，结果：{}", result);
        } else {
            // 插入
            result = groupTourDailyPriceMapper.insert(groupTourDailyPrice);
            log.info("插入团队游每日价格，结果：{}", result);
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean batchSaveOrUpdate(List<GroupTourDailyPrice> list) {
        log.info("批量保存或更新团队游每日价格，数量：{}", list.size());
        int result = groupTourDailyPriceMapper.batchInsert(list);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("删除团队游每日价格，ID：{}", id);
        int result = groupTourDailyPriceMapper.deleteById(id);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteByTourIdAndDate(Integer groupTourId, LocalDate priceDate) {
        log.info("删除团队游ID={}在{}的价格", groupTourId, priceDate);
        int result = groupTourDailyPriceMapper.deleteByTourIdAndDate(groupTourId, priceDate);
        return result > 0;
    }

    @Override
    public BigDecimal getDailyPriceByTourIdAndDate(Integer groupTourId, LocalDate priceDate) {
        log.info("获取团队游ID={}在{}的每日价格", groupTourId, priceDate);
        
        // 首先尝试从每日价格表中获取
        GroupTourDailyPrice dailyPrice = groupTourDailyPriceMapper.selectByTourIdAndDate(groupTourId, priceDate);
        if (dailyPrice != null && dailyPrice.getDailyPrice() != null) {
            log.info("找到每日价格配置：{}元/人", dailyPrice.getDailyPrice());
            return dailyPrice.getDailyPrice();
        }
        
        // 如果没有设置每日价格，返回团队游基础价格
        log.info("未找到团队游ID={}在{}的每日价格，使用基础价格", groupTourId, priceDate);
        GroupTourDTO groupTour = groupTourMapper.getById(groupTourId);
        if (groupTour != null) {
            BigDecimal basePrice = groupTour.getPrice();
            log.info("使用团队游基础价格：{}元/人", basePrice);
            return basePrice != null ? basePrice : BigDecimal.ZERO;
        }
        
        log.warn("未找到团队游ID={}的信息，返回默认价格0", groupTourId);
        return BigDecimal.ZERO;
    }
}







