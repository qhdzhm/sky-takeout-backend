package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GuidePageQueryDTO;
import com.sky.entity.Guide;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GuideMapper {

    /**
     * 导游分页查询
     */
    Page<Guide> pageQuery(GuidePageQueryDTO guidePageQueryDTO);

    /**
     * 通过员工ID获取导游信息
     */
    @Select("SELECT * FROM guides WHERE employee_id = #{employeeId}")
    Guide getGuideByEmployeeId(Long employeeId);

    /**
     * 通过导游ID获取导游信息
     */
    @Select("SELECT * FROM guides WHERE guide_id = #{guideId}")
    Guide getGuideById(Long guideId);

    /**
     * 更新导游的员工关联
     */
    @Update("UPDATE guides SET employee_id = #{employeeId} WHERE guide_id = #{guideId}")
    void updateGuideEmployeeId(Integer guideId, Long employeeId);

    /**
     * 插入新的导游记录
     */
    @Insert("INSERT INTO guides (name, phone, email, employee_id, is_active, status, max_groups, experience_years, languages) " +
            "VALUES (#{name}, #{phone}, #{email}, #{employeeId}, #{isActive}, #{status}, #{maxGroups}, #{experienceYears}, #{languages})")
    void insertGuide(Guide guide);

    /**
     * 获取所有没有关联员工ID的导游
     */
    @Select("SELECT * FROM guides WHERE employee_id IS NULL")
    List<Guide> getGuidesWithoutEmployee();

    /**
     * 获取所有活跃的导游
     */
    @Select("SELECT * FROM guides WHERE status = 1 ORDER BY guide_id")
    List<Guide> getAllActiveGuides();
} 