package com.sky.mapper;

import com.sky.dto.BookingDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预订数据访问层
 */
@Mapper
public interface BookingMapper {

    /**
     * 创建预订
     * @param bookingDTO 预订信息
     * @return 影响行数
     */
    @Insert("INSERT INTO bookings(user_id, tour_type, tour_id, booking_date, start_date, end_date, adults, children, total_price, status, payment_status, special_requests, contact_name, contact_phone, contact_email) " +
            "VALUES(#{userId}, #{tourType}, #{tourId}, NOW(), #{startDate}, #{endDate}, #{adults}, #{children}, #{totalPrice}, #{status}, #{paymentStatus}, #{specialRequests}, #{contactName}, #{contactPhone}, #{contactEmail})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "booking_id")
    int insert(BookingDTO bookingDTO);

    /**
     * 根据用户ID查询预订列表
     * @param userId 用户ID
     * @return 预订列表
     */
    @Select("SELECT * FROM bookings WHERE user_id = #{userId} ORDER BY booking_date DESC")
    List<BookingDTO> getByUserId(Integer userId);

    /**
     * 根据ID查询预订
     * @param id 预订ID
     * @return 预订信息
     */
    @Select("SELECT * FROM bookings WHERE booking_id = #{id}")
    BookingDTO getById(Integer id);

    /**
     * 取消预订
     * @param id 预订ID
     * @return 影响行数
     */
    @Update("UPDATE bookings SET status = 'cancelled' WHERE booking_id = #{id}")
    int cancel(Integer id);

    /**
     * 检查一日游可用性
     * @param tourId 一日游ID
     * @param date 日期
     * @return 可用性信息
     */
    @Select("SELECT available_slots FROM day_tour_schedules WHERE day_tour_id = #{tourId} AND date = #{date}")
    Integer checkDayTourAvailability(@Param("tourId") Integer tourId, @Param("date") LocalDate date);

    /**
     * 检查跟团游可用性
     * @param tourId 跟团游ID
     * @param startDate 开始日期
     * @return 可用性信息
     */
    @Select("SELECT available_slots FROM available_dates WHERE group_tour_id = #{tourId} AND start_date = #{startDate}")
    Integer checkGroupTourAvailability(@Param("tourId") Integer tourId, @Param("startDate") LocalDate startDate);

    /**
     * 更新一日游可用名额
     * @param tourId 一日游ID
     * @param date 日期
     * @param count 预订人数
     * @return 影响行数
     */
    @Update("UPDATE day_tour_schedules SET available_slots = available_slots - #{count} WHERE day_tour_id = #{tourId} AND date = #{date} AND available_slots >= #{count}")
    int updateDayTourAvailability(@Param("tourId") Integer tourId, @Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * 更新跟团游可用名额
     * @param tourId 跟团游ID
     * @param startDate 开始日期
     * @param count 预订人数
     * @return 影响行数
     */
    @Update("UPDATE available_dates SET available_slots = available_slots - #{count} WHERE group_tour_id = #{tourId} AND start_date = #{startDate} AND available_slots >= #{count}")
    int updateGroupTourAvailability(@Param("tourId") Integer tourId, @Param("startDate") LocalDate startDate, @Param("count") Integer count);
} 