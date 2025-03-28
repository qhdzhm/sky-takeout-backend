package com.sky.service;

import com.sky.entity.DayTourTheme;

import java.util.List;

/**
 * 一日游主题服务接口
 */
public interface DayTourThemeService {

    /**
     * 查询所有主题
     * @return
     */
    List<DayTourTheme> list();

    /**
     * 根据ID查询主题
     * @param id
     * @return
     */
    DayTourTheme getById(Integer id);

    /**
     * 新增主题
     * @param dayTourTheme
     */
    void save(DayTourTheme dayTourTheme);

    /**
     * 修改主题
     * @param dayTourTheme
     */
    void update(DayTourTheme dayTourTheme);

    /**
     * 删除主题
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 根据一日游ID查询关联的主题
     * @param dayTourId
     * @return
     */
    List<DayTourTheme> getByDayTourId(Integer dayTourId);

    /**
     * 为一日游关联主题
     * @param dayTourId
     * @param themeIds
     */
    void associate(Integer dayTourId, List<Integer> themeIds);
} 