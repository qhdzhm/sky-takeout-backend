package com.sky.mapper;

import com.sky.entity.DayTourTip;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游旅行提示Mapper
 */
@Mapper
public interface DayTourTipMapper {

    /**
     * 根据一日游ID查询旅行提示
     * @param dayTourId
     * @return
     */
    //@Select("select id, day_tour_id as dayTourId, description, position, created_at as createdAt, updated_at as updatedAt " +
    //        "from day_tour_tips where day_tour_id = #{dayTourId} order by position")
    List<DayTourTip> getByDayTourId(Integer dayTourId);

    /**
     * 新增旅行提示
     * @param dayTourTip
     */
    @Insert("insert into day_tour_tips(day_tour_id, description, position, created_at, updated_at) " +
            "values(#{dayTourId}, #{description}, #{position}, #{createdAt}, #{updatedAt})")
    void insert(DayTourTip dayTourTip);

    /**
     * 删除旅行提示
     * @param id
     */
    @Delete("delete from day_tour_tips where id = #{id}")
    void deleteById(Integer id);

    /**
     * 删除一日游所有旅行提示
     * @param dayTourId
     */
    @Delete("delete from day_tour_tips where day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);
}