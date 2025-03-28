package com.sky.mapper;

import com.sky.entity.DayTourSuitable;
import com.sky.entity.SuitableFor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 一日游适合人群Mapper
 */
@Mapper
public interface DayTourSuitableMapper {

    /**
     * 查询所有适合人群
     * @return
     */
    @Select("select * from suitable_for")
    List<SuitableFor> list();

    /**
     * 根据ID查询适合人群
     * @param id
     * @return
     */
    @Select("select * from suitable_for where suitable_id = #{id}")
    SuitableFor getById(Integer id);

    /**
     * 新增适合人群
     * @param suitableFor
     */
    @Insert("insert into suitable_for(name, description) values(#{name}, #{description})")
    void insert(SuitableFor suitableFor);

    /**
     * 修改适合人群
     * @param suitableFor
     */
    void update(SuitableFor suitableFor);

    /**
     * 删除适合人群
     * @param id
     */
    @Delete("delete from suitable_for where suitable_id = #{id}")
    void deleteById(Integer id);

    /**
     * 根据一日游ID查询适合人群
     * @param dayTourId
     * @return
     */
    @Select("select s.suitable_id as suitableId, s.name " +
            "from suitable_for s join day_tour_suitable_relation ds on s.suitable_id = ds.suitable_id " +
            "where ds.day_tour_id = #{dayTourId}")
    List<SuitableFor> getByDayTourId(Integer dayTourId);

    /**
     * 添加一日游与适合人群关联
     * @param dayTourSuitable
     */
    @Insert("insert into day_tour_suitable_relation(day_tour_id, suitable_id) values(#{dayTourId}, #{suitableId})")
    void insertRelation(DayTourSuitable dayTourSuitable);

    /**
     * 删除一日游与适合人群关联
     * @param dayTourId
     * @param suitableId
     */
    @Delete("delete from day_tour_suitable_relation where day_tour_id = #{dayTourId} and suitable_id = #{suitableId}")
    void deleteRelation(Integer dayTourId, Integer suitableId);

    /**
     * 删除一日游所有适合人群关联
     * @param dayTourId
     */
    @Delete("delete from day_tour_suitable_relation where day_tour_id = #{dayTourId}")
    void deleteRelationByDayTourId(Integer dayTourId);

    /**
     * 添加一日游与适合人群关联 (直接使用参数)
     * @param dayTourId
     * @param suitableId
     */
    @Insert("insert into day_tour_suitable_relation(day_tour_id, suitable_id) values(#{dayTourId}, #{suitableId})")
    void insertAssociation(Integer dayTourId, Integer suitableId);
} 