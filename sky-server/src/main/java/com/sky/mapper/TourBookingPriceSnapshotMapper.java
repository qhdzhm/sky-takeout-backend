package com.sky.mapper;

import com.sky.entity.TourBookingPriceSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单价格快照Mapper
 */
@Mapper
public interface TourBookingPriceSnapshotMapper {

    /**
     * 插入价格快照
     * @param snapshot 价格快照
     */
    void insert(TourBookingPriceSnapshot snapshot);

    /**
     * 根据订单ID查询价格快照
     * @param bookingId 订单ID
     * @return 价格快照
     */
    TourBookingPriceSnapshot selectByBookingId(@Param("bookingId") Integer bookingId);

    /**
     * 根据ID查询价格快照
     * @param id 快照ID
     * @return 价格快照
     */
    TourBookingPriceSnapshot selectById(@Param("id") Long id);
}







