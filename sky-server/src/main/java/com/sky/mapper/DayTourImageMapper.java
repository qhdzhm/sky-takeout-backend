package com.sky.mapper;

import com.sky.entity.DayTourImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 一日游图片数据访问层
 */
@Mapper
public interface DayTourImageMapper {

    /**
     * 插入一日游图片
     *
     * @param dayTourImage 一日游图片实体
     * @return 影响行数
     */
    int insert(DayTourImage dayTourImage);

    /**
     * 根据ID查询一日游图片
     *
     * @param id 图片ID
     * @return 一日游图片实体
     */
    DayTourImage selectById(Integer id);

    /**
     * 根据一日游ID查询图片列表
     *
     * @param dayTourId 一日游ID
     * @return 图片列表
     */
    List<DayTourImage> selectByDayTourId(Integer dayTourId);

    /**
     * 查询一日游的第一张图片
     *
     * @param dayTourId 一日游ID
     * @return 一日游图片实体
     */
    DayTourImage selectFirstByDayTourId(Integer dayTourId);

    /**
     * 删除图片
     *
     * @param id 图片ID
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 重置一日游的所有图片为非主图
     *
     * @param dayTourId 一日游ID
     * @return 影响行数
     */
    int resetPrimaryImage(Integer dayTourId);

    /**
     * 设置图片为主图
     *
     * @param id 图片ID
     * @return 影响行数
     */
    int setPrimaryImage(Integer id);

    /**
     * 更新一日游的主图URL
     *
     * @param dayTourId 一日游ID
     * @param imageUrl  图片URL
     * @return 影响行数
     */
    int updateDayTourImageUrl(@Param("dayTourId") Integer dayTourId, @Param("imageUrl") String imageUrl);

    /**
     * 更新图片位置
     *
     * @param id       图片ID
     * @param position 位置
     * @return 影响行数
     */
    int updatePosition(@Param("id") Integer id, @Param("position") Integer position);

    /**
     * 更新图片描述
     *
     * @param id          图片ID
     * @param description 描述
     * @return 影响行数
     */
    int updateDescription(@Param("id") Integer id, @Param("description") String description);
} 