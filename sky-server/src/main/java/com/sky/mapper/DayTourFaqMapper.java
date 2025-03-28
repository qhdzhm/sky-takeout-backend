package com.sky.mapper;

import com.sky.entity.DayTourFaq;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游常见问题Mapper
 */
@Mapper
public interface DayTourFaqMapper {

    /**
     * 根据一日游ID查询常见问题
     * @param dayTourId
     * @return
     */
    //@Select("select id, day_tour_id as dayTourId, question, answer, position, created_at as createdAt, updated_at as updatedAt " +
    //        "from day_tour_faqs where day_tour_id = #{dayTourId} order by position")
    List<DayTourFaq> getByDayTourId(Integer dayTourId);

    /**
     * 新增常见问题
     * @param dayTourFaq
     */
    @Insert("insert into day_tour_faqs(day_tour_id, question, answer, position, created_at, updated_at) " +
            "values(#{dayTourId}, #{question}, #{answer}, #{position}, #{createdAt}, #{updatedAt})")
    void insert(DayTourFaq dayTourFaq);

    /**
     * 删除常见问题
     * @param id
     */
    @Delete("delete from day_tour_faqs where id = #{id}")
    void deleteById(Integer id);

    /**
     * 删除一日游所有常见问题
     * @param dayTourId
     */
    @Delete("delete from day_tour_faqs where day_tour_id = #{dayTourId}")
    void deleteByDayTourId(Integer dayTourId);
} 