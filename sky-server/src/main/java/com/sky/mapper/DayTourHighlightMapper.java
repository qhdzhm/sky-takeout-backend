package com.sky.mapper;

import com.sky.entity.DayTourHighlight;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游亮点Mapper
 */
@Mapper
public interface DayTourHighlightMapper {

    /**
     * 根据一日游ID查询亮点
     * @param dayTourId
     * @return
     */
    //@Select("select id, day_tour_id as dayTourId, description, position, created_at as createdAt, updated_at as updatedAt " +
    //        "from day_tour_highlights where day_tour_id = #{dayTourId} order by position")
    List<DayTourHighlight> getByDayTourId(Integer dayTourId);

    /**
     * 新增亮点
     * @param dayTourHighlight
     */
    @Insert("insert into day_tour_highlights(day_tour_id, description, position, created_at, updated_at) " +
            "values(#{dayTourId}, #{description}, #{position}, #{createdAt}, #{updatedAt})")
    void insert(DayTourHighlight dayTourHighlight);

    /**
     * 删除亮点
     * @param id
     */
    @Delete("delete from day_tour_highlights where id = #{id}")
    void deleteById(Integer id);

    /**
     * 删除一日游所有亮点
     * @param dayTourId
     */
    @Delete("delete from day_tour_highlights where day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);
} 