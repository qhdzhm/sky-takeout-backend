package com.sky.mapper;

import com.sky.dto.RegionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区数据访问层
 */
@Mapper
public interface RegionMapper {

    /**
     * 查询所有地区
     * @return 地区列表
     */
    @Select("SELECT * FROM regions")
    List<RegionDTO> getAll();

    /**
     * 根据ID查询地区
     * @param id 地区ID
     * @return 地区信息
     */
    @Select("SELECT * FROM regions WHERE region_id = #{id}")
    RegionDTO getById(Integer id);

    /**
     * 查询地区旅游产品数量
     * @param regionId 地区ID
     * @return 旅游产品数量
     */
    @Select("SELECT COUNT(*) FROM day_tours WHERE region_id = #{regionId} AND is_active = 1")
    Integer countDayTours(Integer regionId);

    /**
     * 查询地区跟团游数量
     * @param regionId 地区ID
     * @return 跟团游数量
     */
    @Select("SELECT COUNT(*) FROM group_tours WHERE location LIKE CONCAT('%', (SELECT name FROM regions WHERE region_id = #{regionId}), '%') AND is_active = 1")
    Integer countGroupTours(Integer regionId);

    /**
     * 查询地区一日游
     * @param regionId 地区ID
     * @param name 名称
     * @param category 类别
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 一日游列表
     */
    List<Object> getDayTours(@Param("regionId") Integer regionId,
                            @Param("name") String name,
                            @Param("category") String category,
                            @Param("minPrice") Double minPrice,
                            @Param("maxPrice") Double maxPrice);

    /**
     * 查询地区跟团游
     * @param regionId 地区ID
     * @param title 标题
     * @param category 类别
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 跟团游列表
     */
    List<Object> getGroupTours(@Param("regionId") Integer regionId,
                              @Param("title") String title,
                              @Param("category") String category,
                              @Param("minPrice") Double minPrice,
                              @Param("maxPrice") Double maxPrice);
} 