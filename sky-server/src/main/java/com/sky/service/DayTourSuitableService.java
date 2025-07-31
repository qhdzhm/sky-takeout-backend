package com.sky.service;

import com.sky.entity.SuitableFor;

import java.util.List;

/**
 * 一日游适合人群服务接口
 */
public interface DayTourSuitableService {

    /**
     * 查询所有适合人群
     * @return
     */
    List<SuitableFor> list();

    /**
     * 根据ID查询适合人群
     * @param id
     * @return
     */
    SuitableFor getById(Integer id);

    /**
     * 新增适合人群
     * @param suitableFor
     */
    void save(SuitableFor suitableFor);

    /**
     * 修改适合人群
     * @param suitableFor
     */
    void update(SuitableFor suitableFor);

    /**
     * 删除适合人群
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 根据一日游ID查询关联的适合人群
     * @param dayTourId
     * @return
     */
    List<SuitableFor> getByDayTourId(Integer dayTourId);

    /**
     * 为一日游关联适合人群
     * @param dayTourId
     * @param suitableIds
     */
    void associate(Integer dayTourId, List<Integer> suitableIds);
} 