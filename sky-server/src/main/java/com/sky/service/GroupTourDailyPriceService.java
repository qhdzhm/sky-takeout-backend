package com.sky.service;

import com.sky.entity.GroupTourDailyPrice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 团队游每日价格服务接口
 */
public interface GroupTourDailyPriceService {

    /**
     * 根据团队游ID和日期范围查询每日价格
     */
    List<GroupTourDailyPrice> getByTourIdAndDateRange(Integer groupTourId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期范围查询所有团队游的每日价格
     */
    List<GroupTourDailyPrice> getByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 根据团队游ID和具体日期查询价格
     */
    GroupTourDailyPrice getByTourIdAndDate(Integer groupTourId, LocalDate priceDate);

    /**
     * 添加或更新每日价格
     */
    boolean saveOrUpdate(GroupTourDailyPrice groupTourDailyPrice);

    /**
     * 批量添加或更新每日价格
     */
    boolean batchSaveOrUpdate(List<GroupTourDailyPrice> list);

    /**
     * 删除每日价格
     */
    boolean delete(Long id);

    /**
     * 根据团队游ID和日期删除
     */
    boolean deleteByTourIdAndDate(Integer groupTourId, LocalDate priceDate);

    /**
     * 根据团队游ID和日期获取每日价格
     * 如果没有设置，返回团队游基础价格
     */
    BigDecimal getDailyPriceByTourIdAndDate(Integer groupTourId, LocalDate priceDate);
}







