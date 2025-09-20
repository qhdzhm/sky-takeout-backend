package com.sky.mapper;

import com.sky.entity.Attraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点Mapper接口 - 基于酒店Mapper架构设计
 */
@Mapper
public interface AttractionMapper {

    /**
     * 插入景点信息
     * @param attraction 景点信息
     */
    void insert(Attraction attraction);

    /**
     * 根据ID查询景点
     * @param id 景点ID
     * @return 景点信息
     */
    Attraction getById(Long id);

    /**
     * 更新景点信息
     * @param attraction 景点信息
     */
    void update(Attraction attraction);

    /**
     * 删除景点
     * @param id 景点ID
     */
    void deleteById(Long id);

    /**
     * 获取所有景点列表
     * @return 景点列表
     */
    List<Attraction> getAll();

    /**
     * 获取所有活跃的景点
     * @return 景点列表
     */
    List<Attraction> getAllActive();

    /**
     * 根据预订方式查询景点列表
     * @param bookingType 预订方式（email/website）
     * @return 景点列表
     */
    List<Attraction> getByBookingType(String bookingType);

    /**
     * 根据位置查询景点列表
     * @param location 位置
     * @return 景点列表
     */
    List<Attraction> getByLocation(String location);

    /**
     * 根据景点名称模糊查询
     * @param name 景点名称
     * @return 景点列表
     */
    List<Attraction> getByNameLike(@Param("name") String name);

    /**
     * 更新景点状态
     * @param id 景点ID
     * @param status 状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 根据条件搜索景点
     * @param attractionName 景点名称
     * @param location 位置
     * @param bookingType 预订方式
     * @param status 状态
     * @return 景点列表
     */
    List<Attraction> searchAttractions(@Param("attractionName") String attractionName,
                                      @Param("location") String location,
                                      @Param("bookingType") String bookingType,
                                      @Param("status") String status);
}

