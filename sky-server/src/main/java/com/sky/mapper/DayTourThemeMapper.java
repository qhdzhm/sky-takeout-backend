package com.sky.mapper;

import com.sky.entity.DayTourTheme;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 一日游主题Mapper接口
 */
@Mapper
public interface DayTourThemeMapper {

    /**
     * 查询所有主题
     * @return
     */
    @Select("select * from day_tour_themes")
    List<DayTourTheme> list();

    /**
     * 根据ID查询主题
     * @param id
     * @return
     */
    @Select("select * from day_tour_themes where theme_id = #{id}")
    DayTourTheme getById(Integer id);

    /**
     * 新增主题
     * @param dayTourTheme
     */
    @Insert("insert into day_tour_themes(name, description) values(#{name}, #{description})")
    void insert(DayTourTheme dayTourTheme);

    /**
     * 修改主题
     * @param dayTourTheme
     */
    void update(DayTourTheme dayTourTheme);

    /**
     * 删除主题
     * @param id
     */
    @Delete("delete from day_tour_themes where theme_id = #{id}")
    void deleteById(Integer id);

    /**
     * 根据一日游ID查询关联的主题
     * @param dayTourId
     * @return
     */
    @Select("select t.theme_id as id, t.theme_id as themeId, t.name from day_tour_themes t join day_tour_theme_relation dt on t.theme_id = dt.theme_id where dt.day_tour_id = #{dayTourId}")
    List<DayTourTheme> getByDayTourId(Integer dayTourId);

    /**
     * 删除一日游与主题的关联关系
     * @param dayTourId
     */
    @Delete("delete from day_tour_theme_relation where day_tour_id = #{dayTourId}")
    void deleteAssociation(Integer dayTourId);

    /**
     * 添加一日游与主题的关联关系
     * @param dayTourId
     * @param themeId
     */
    @Insert("insert into day_tour_theme_relation(day_tour_id, theme_id) values(#{dayTourId}, #{themeId})")
    void insertAssociation(Integer dayTourId, Integer themeId);
} 