package com.sky.mapper;

import com.sky.entity.TourScheduleOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 行程排序Mapper接口
 */
@Mapper
public interface TourScheduleOrderMapper {

    /**
     * 通过订单ID查询行程排序
     * @param bookingId 订单ID
     * @return 行程排序列表
     */
    @Select("SELECT * FROM tour_schedule_order WHERE booking_id = #{bookingId} ORDER BY day_number ASC")
    List<TourScheduleOrder> getByBookingId(Integer bookingId);

    /**
     * 通过日期范围查询行程排序
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 行程排序列表
     */
    @Select("SELECT * FROM tour_schedule_order WHERE tour_date BETWEEN #{startDate} AND #{endDate} ORDER BY booking_id, day_number ASC")
    List<TourScheduleOrder> getByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 插入行程排序
     * @param tourScheduleOrder 行程排序对象
     */
    @Insert("INSERT INTO tour_schedule_order (booking_id, day_number, tour_id, tour_type, tour_date, title, description, display_order) " +
            "VALUES (#{bookingId}, #{dayNumber}, #{tourId}, #{tourType}, #{tourDate}, #{title}, #{description}, #{displayOrder})")
    void insert(TourScheduleOrder tourScheduleOrder);

    /**
     * 批量插入行程排序（使用XML配置）
     * @param tourScheduleOrders 行程排序对象列表
     */
    void insertBatch(List<TourScheduleOrder> tourScheduleOrders);

    /**
     * 通过订单ID删除行程排序
     * @param bookingId 订单ID
     */
    @Delete("DELETE FROM tour_schedule_order WHERE booking_id = #{bookingId}")
    void deleteByBookingId(Integer bookingId);
} 