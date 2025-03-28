package com.sky.mapper;

import com.sky.entity.DayTourInclusion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游包含项Mapper
 */
@Mapper
public interface DayTourInclusionMapper {

    /**
     * 根据一日游ID查询包含项
     * @param dayTourId
     * @return
     */
    //@Select("select id, day_tour_id as dayTourId, description, position, created_at as createdAt, updated_at as updatedAt " +
    //        "from day_tour_inclusions where day_tour_id = #{dayTourId} order by position")
    List<DayTourInclusion> getByDayTourId(Integer dayTourId);

    /**
     * 新增包含项
     * @param dayTourInclusion
     */
    @Insert("insert into day_tour_inclusions(day_tour_id, description, position, created_at, updated_at) " +
            "values(#{dayTourId}, #{description}, #{position}, #{createdAt}, #{updatedAt})")
    void insert(DayTourInclusion dayTourInclusion);

    /**
     * 删除包含项
     * @param id
     */
    @Delete("delete from day_tour_inclusions where id = #{id}")
    void deleteById(Integer id);

    /**
     * 删除一日游所有包含项
     * @param dayTourId
     */
    @Delete("delete from day_tour_inclusions where day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);
} 