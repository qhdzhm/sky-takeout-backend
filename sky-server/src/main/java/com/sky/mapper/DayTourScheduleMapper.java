package com.sky.mapper;

import com.sky.entity.DayTourSchedule;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 一日游日程安排表的数据库操作接口
 */
@Mapper
public interface DayTourScheduleMapper {

    /**
     * 根据一日游ID获取所有日程安排
     * 
     * @param dayTourId 一日游ID
     * @return 日程安排列表
     */
    @Select("SELECT * FROM day_tour_schedule WHERE day_tour_id = #{dayTourId}")
    List<DayTourSchedule> getByDayTourId(Integer dayTourId);

    /**
     * 新增日程安排
     * 
     * @param dayTourSchedule 日程安排实体
     */
    @Insert("INSERT INTO day_tour_schedule (day_tour_id, schedule_date, available_seats, status, remarks) " +
            "VALUES (#{dayTourId}, #{scheduleDate}, #{availableSeats}, #{status}, #{remarks})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DayTourSchedule dayTourSchedule);

    /**
     * 根据ID删除日程安排
     * 
     * @param id 日程安排ID
     */
    @Delete("DELETE FROM day_tour_schedule WHERE id = #{id}")
    void deleteById(Integer id);

    /**
     * 根据一日游ID删除所有日程安排
     * 
     * @param dayTourId 一日游ID
     */
    @Delete("DELETE FROM day_tour_schedule WHERE day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);

    /**
     * 获取指定日期范围内的日程安排
     * 
     * @param dayTourId 一日游ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日程安排列表
     */
    @Select("SELECT * FROM day_tour_schedule WHERE day_tour_id = #{dayTourId} " +
            "AND schedule_date BETWEEN #{startDate} AND #{endDate}")
    List<DayTourSchedule> getSchedulesInDateRange(Integer dayTourId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据ID获取日程安排
     * 
     * @param id 日程安排ID
     * @return 日程安排实体
     */
    @Select("SELECT * FROM day_tour_schedule WHERE id = #{id}")
    DayTourSchedule getById(Integer id);
}