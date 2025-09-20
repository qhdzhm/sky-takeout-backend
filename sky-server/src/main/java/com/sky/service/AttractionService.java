package com.sky.service;

import com.sky.entity.Attraction;
import java.util.List;

/**
 * 景点服务接口 - 基于酒店服务接口设计
 */
public interface AttractionService {

    /**
     * 获取所有景点列表
     * @return 景点列表
     */
    List<Attraction> getAllAttractions();

    /**
     * 获取所有活跃景点列表
     * @return 景点列表
     */
    List<Attraction> getAllActiveAttractions();

    /**
     * 根据ID获取景点详情
     * @param id 景点ID
     * @return 景点信息
     */
    Attraction getAttractionById(Long id);

    /**
     * 创建景点
     * @param attraction 景点信息
     */
    void createAttraction(Attraction attraction);

    /**
     * 更新景点
     * @param attraction 景点信息
     */
    void updateAttraction(Attraction attraction);

    /**
     * 删除景点
     * @param id 景点ID
     */
    void deleteAttraction(Long id);

    /**
     * 根据预订方式获取景点列表
     * @param bookingType 预订方式
     * @return 景点列表
     */
    List<Attraction> getAttractionsByBookingType(String bookingType);

    /**
     * 根据位置获取景点列表
     * @param location 位置
     * @return 景点列表
     */
    List<Attraction> getAttractionsByLocation(String location);

    /**
     * 根据景点名称模糊查询
     * @param name 景点名称
     * @return 景点列表
     */
    List<Attraction> searchAttractionsByName(String name);

    /**
     * 更新景点状态
     * @param id 景点ID
     * @param status 状态
     */
    void updateAttractionStatus(Long id, String status);

    /**
     * 根据条件搜索景点
     * @param attractionName 景点名称
     * @param location 位置
     * @param bookingType 预订方式
     * @param status 状态
     * @return 景点列表
     */
    List<Attraction> searchAttractions(String attractionName, String location, String bookingType, String status);
}

