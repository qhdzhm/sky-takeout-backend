package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DayTourDTO;
import com.sky.dto.DayTourPageQueryDTO;
import com.sky.entity.DayTour;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    Page<DayTourDTO> pageQuery(DayTourPageQueryDTO dto);

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
    @Insert("insert into day_tours(name, location, description, price, duration, image_url, banner_image, " +
            "category, is_active, created_at, updated_at, region_id, departure_address, guide_fee, guide_id, cost_price) " +
            "values(#{name}, #{location}, #{description}, #{price}, #{duration}, #{imageUrl}, #{bannerImage}, " +
            "#{category}, #{isActive}, #{createdAt}, #{updatedAt}, #{regionId}, #{departureAddress}, #{guideFee}, " +
            "#{guideId}, #{costPrice})")
    @Options(useGeneratedKeys = true, keyProperty = "dayTourId", keyColumn = "day_tour_id")
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
    @Update("update day_tours set is_active = #{status} where day_tour_id = #{id}")
    void updateStatus(Integer id, Integer status);

    // ===== Dashboard统计相关方法 =====
    
    /**
     * 获取一日游产品总数
     * @return 产品总数
     */
    @Select("SELECT COUNT(*) FROM day_tours")
    Integer count();

    /**
     * 根据状态获取一日游产品数量
     * @param status 状态
     * @return 产品数量
     */
    @Select("SELECT COUNT(*) FROM day_tours WHERE is_active = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 获取一日游产品平均价格
     * @return 平均价格
     */
    @Select("SELECT AVG(price) FROM day_tours WHERE is_active = 1")
    java.math.BigDecimal getAveragePrice();
} 