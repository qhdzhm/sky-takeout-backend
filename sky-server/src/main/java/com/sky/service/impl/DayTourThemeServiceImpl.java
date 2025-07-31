package com.sky.service.impl;

import com.sky.entity.DayTourTheme;
import com.sky.mapper.DayTourThemeMapper;
import com.sky.service.DayTourThemeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 一日游主题服务实现类
 */
@Service
@Slf4j
public class DayTourThemeServiceImpl implements DayTourThemeService {

    @Autowired
    private DayTourThemeMapper dayTourThemeMapper;

    /**
     * 查询所有主题
     */
    @Override
    public List<DayTourTheme> list() {
        return dayTourThemeMapper.list();
    }

    /**
     * 根据ID查询主题
     */
    @Override
    public DayTourTheme getById(Integer id) {
        return dayTourThemeMapper.getById(id);
    }

    /**
     * 新增主题
     */
    @Override
    public void save(DayTourTheme dayTourTheme) {
        dayTourThemeMapper.insert(dayTourTheme);
    }

    /**
     * 修改主题
     */
    @Override
    public void update(DayTourTheme dayTourTheme) {
        dayTourThemeMapper.update(dayTourTheme);
    }

    /**
     * 删除主题
     */
    @Override
    public void deleteById(Integer id) {
        dayTourThemeMapper.deleteById(id);
    }

    /**
     * 根据一日游ID查询关联的主题
     */
    @Override
    public List<DayTourTheme> getByDayTourId(Integer dayTourId) {
        return dayTourThemeMapper.getByDayTourId(dayTourId);
    }

    /**
     * 为一日游关联主题
     */
    @Override
    @Transactional
    public void associate(Integer dayTourId, List<Integer> themeIds) {
        // 先删除原有关联
        dayTourThemeMapper.deleteAssociation(dayTourId);
        
        // 添加新关联
        if (themeIds != null && !themeIds.isEmpty()) {
            for (Integer themeId : themeIds) {
                dayTourThemeMapper.insertAssociation(dayTourId, themeId);
            }
        }
    }
} 