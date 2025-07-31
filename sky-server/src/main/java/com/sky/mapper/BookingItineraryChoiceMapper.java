package com.sky.mapper;

import com.sky.entity.BookingItineraryChoice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预订行程选择Mapper
 */
@Mapper
public interface BookingItineraryChoiceMapper {

    /**
     * 保存行程选择
     */
    int insert(BookingItineraryChoice choice);

    /**
     * 批量保存行程选择
     */
    int batchInsert(List<BookingItineraryChoice> choices);

    /**
     * 根据预订ID获取行程选择
     */
    List<BookingItineraryChoice> getByBookingId(Integer bookingId);

    /**
     * 根据预订ID和天数获取选择
     */
    BookingItineraryChoice getByBookingIdAndDay(Integer bookingId, Integer dayNumber);

    /**
     * 更新行程选择
     */
    int update(BookingItineraryChoice choice);

    /**
     * 删除预订的所有行程选择
     */
    int deleteByBookingId(Integer bookingId);

    /**
     * 删除特定天数的行程选择
     */
    int deleteByBookingIdAndDay(Integer bookingId, Integer dayNumber);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);
} 