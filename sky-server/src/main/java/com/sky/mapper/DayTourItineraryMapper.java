package com.sky.mapper;

import com.sky.entity.DayTourItinerary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游行程安排Mapper
 */
@Mapper
public interface DayTourItineraryMapper {

    /**
     * 根据一日游ID查询行程安排
     * @param dayTourId
     * @return
     */
    //@Select("select id, day_tour_id as dayTourId, time_slot as timeSlot, activity, location, description, " +
    //        "position, created_at as createdAt, updated_at as updatedAt " +
    //        "from day_tour_itinerary where day_tour_id = #{dayTourId} order by position")
    List<DayTourItinerary> getByDayTourId(Integer dayTourId);

    /**
     * 新增行程安排
     * @param dayTourItinerary
     */
    @Insert("insert into day_tour_itinerary(day_tour_id, time_slot, activity, location, description, position, created_at, updated_at) " +
            "values(#{dayTourId}, #{timeSlot}, #{activity}, #{location}, #{description}, #{position}, #{createdAt}, #{updatedAt})")
    void insert(DayTourItinerary dayTourItinerary);

    /**
     * 删除行程安排
     * @param id
     */
    @Delete("delete from day_tour_itinerary where id = #{id}")
    void deleteById(Integer id);

    /**
     * 删除一日游所有行程安排
     * @param dayTourId
     */
    @Delete("delete from day_tour_itinerary where day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);
} 