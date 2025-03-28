package com.sky.service.impl;

import com.sky.entity.DayTourSuitable;
import com.sky.entity.SuitableFor;
import com.sky.mapper.DayTourSuitableMapper;
import com.sky.service.DayTourSuitableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 一日游适合人群服务实现类
 */
@Service
@Slf4j
public class DayTourSuitableServiceImpl implements DayTourSuitableService {

    @Autowired
    private DayTourSuitableMapper dayTourSuitableMapper;

    /**
     * 查询所有适合人群
     */
    @Override
    public List<SuitableFor> list() {
        return dayTourSuitableMapper.list();
    }

    /**
     * 根据ID查询适合人群
     */
    @Override
    public SuitableFor getById(Integer id) {
        return dayTourSuitableMapper.getById(id);
    }

    /**
     * 新增适合人群
     */
    @Override
    public void save(SuitableFor suitableFor) {
        dayTourSuitableMapper.insert(suitableFor);
    }

    /**
     * 修改适合人群
     */
    @Override
    public void update(SuitableFor suitableFor) {
        dayTourSuitableMapper.update(suitableFor);
    }

    /**
     * 删除适合人群
     */
    @Override
    public void deleteById(Integer id) {
        dayTourSuitableMapper.deleteById(id);
    }

    /**
     * 根据一日游ID查询关联的适合人群
     */
    @Override
    public List<SuitableFor> getByDayTourId(Integer dayTourId) {
        return dayTourSuitableMapper.getByDayTourId(dayTourId);
    }

    /**
     * 为一日游关联适合人群
     */
    @Override
    @Transactional
    public void associate(Integer dayTourId, List<Integer> suitableIds) {
        // 先删除原有关联
        dayTourSuitableMapper.deleteRelationByDayTourId(dayTourId);
        
        // 添加新关联
        if (suitableIds != null && !suitableIds.isEmpty()) {
            for (Integer suitableId : suitableIds) {
                DayTourSuitable dayTourSuitable = new DayTourSuitable();
                dayTourSuitable.setDayTourId(dayTourId);
                dayTourSuitable.setSuitableId(suitableId);
                dayTourSuitableMapper.insertRelation(dayTourSuitable);
            }
        }
    }
} 