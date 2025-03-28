package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DayTourPageQueryDTO;
import com.sky.entity.DayTour;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 一日游Mapper
 */
@Mapper
public interface DayTourMapper {

    /**
     * 分页查询
     * @param dto
     * @return
     */
    Page<DayTour> pageQuery(DayTourPageQueryDTO dto);

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    DayTour getById(Integer id);

    /**
     * 新增
     * @param dayTour
     */
    @Insert("insert into day_tours(name, location, description, price, child_price, duration, image_url, " +
            "category, is_active, created_at, updated_at, region_id, departure_address, guide_fee, guide_id, " +
            "pickup_info, cancellation_policy) " +
            "values(#{name}, #{location}, #{description}, #{price}, #{childPrice}, #{duration}, #{imageUrl}, " +
            "#{category}, #{isActive}, #{createdAt}, #{updatedAt}, #{regionId}, #{departureAddress}, #{guideFee}, " +
            "#{guideId}, #{pickupInfo}, #{cancellationPolicy})")
    void insert(DayTour dayTour);

    /**
     * 修改
     * @param dayTour
     */
    void update(DayTour dayTour);

    /**
     * 删除
     * @param id
     */
    @Delete("delete from day_tours where day_tour_id = #{id}")
    void deleteById(Integer id);

    /**
     * 修改状态
     * @param id
     * @param status
     */
    void updateStatus(Integer id, Integer status);
} 