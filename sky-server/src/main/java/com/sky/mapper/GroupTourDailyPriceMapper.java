package com.sky.mapper;

import com.sky.entity.GroupTourDailyPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 团队游每日价格 Mapper 接口
 */
@Mapper
public interface GroupTourDailyPriceMapper {

    /**
     * 根据团队游ID和日期范围查询每日价格
     */
    List<GroupTourDailyPrice> selectByTourIdAndDateRange(
            @Param("groupTourId") Integer groupTourId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据日期范围查询所有团队游的每日价格
     */
    List<GroupTourDailyPrice> selectByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据团队游ID和具体日期查询价格
     */
    GroupTourDailyPrice selectByTourIdAndDate(
            @Param("groupTourId") Integer groupTourId,
            @Param("priceDate") LocalDate priceDate
    );

    /**
     * 插入每日价格
     */
    int insert(GroupTourDailyPrice groupTourDailyPrice);

    /**
     * 更新每日价格
     */
    int update(GroupTourDailyPrice groupTourDailyPrice);

    /**
     * 批量插入（使用 ON DUPLICATE KEY UPDATE）
     */
    int batchInsert(@Param("list") List<GroupTourDailyPrice> list);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据团队游ID和日期删除
     */
    int deleteByTourIdAndDate(
            @Param("groupTourId") Integer groupTourId,
            @Param("priceDate") LocalDate priceDate
    );
}







