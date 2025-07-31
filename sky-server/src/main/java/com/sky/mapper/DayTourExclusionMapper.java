package com.sky.mapper;

import com.sky.entity.DayTourExclusion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游不包含项Mapper
 */
@Mapper
public interface DayTourExclusionMapper {

    /**
     * 根据一日游ID查询不包含项
     * @param dayTourId
     * @return
     */
    //@Select("select id, day_tour_id as dayTourId, description, position, created_at as createdAt, updated_at as updatedAt " +
    //        "from day_tour_exclusions where day_tour_id = #{dayTourId} order by position")
    List<DayTourExclusion> getByDayTourId(Integer dayTourId);

    /**
     * 新增不包含项
     * @param dayTourExclusion
     */
    @Insert("insert into day_tour_exclusions(day_tour_id, description, position, created_at, updated_at) " +
            "values(#{dayTourId}, #{description}, #{position}, #{createdAt}, #{updatedAt})")
    void insert(DayTourExclusion dayTourExclusion);

    /**
     * 删除不包含项
     * @param id
     */
    @Delete("delete from day_tour_exclusions where id = #{id}")
    void deleteById(Integer id);

    /**
     * 删除一日游所有不包含项
     * @param dayTourId
     */
    @Delete("delete from day_tour_exclusions where day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);
} 