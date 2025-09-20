package com.sky.service;

import com.sky.entity.TicketType;
import java.util.List;

/**
 * 票务类型服务接口 - 对标酒店房型服务接口
 */
public interface TicketTypeService {

    /**
     * 根据景点ID获取票务类型列表
     * @param attractionId 景点ID
     * @return 票务类型列表
     */
    List<TicketType> getTicketTypesByAttractionId(Long attractionId);

    /**
     * 根据景点ID获取活跃票务类型列表
     * @param attractionId 景点ID
     * @return 票务类型列表
     */
    List<TicketType> getActiveTicketTypesByAttractionId(Long attractionId);

    /**
     * 获取所有活跃票务类型
     * @return 票务类型列表
     */
    List<TicketType> getAllActiveTicketTypes();

    /**
     * 根据ID获取票务类型详情
     * @param id 票务类型ID
     * @return 票务类型信息
     */
    TicketType getTicketTypeById(Long id);

    /**
     * 根据票务代码获取票务类型
     * @param ticketCode 票务代码
     * @return 票务类型信息
     */
    TicketType getTicketTypeByCode(String ticketCode);

    /**
     * 创建票务类型
     * @param ticketType 票务类型信息
     */
    void createTicketType(TicketType ticketType);

    /**
     * 更新票务类型
     * @param ticketType 票务类型信息
     */
    void updateTicketType(TicketType ticketType);

    /**
     * 删除票务类型
     * @param id 票务类型ID
     */
    void deleteTicketType(Long id);

    /**
     * 更新票务类型状态
     * @param id 票务类型ID
     * @param status 状态
     */
    void updateTicketTypeStatus(Long id, String status);

    /**
     * 根据景点ID删除所有票务类型
     * @param attractionId 景点ID
     */
    void deleteTicketTypesByAttractionId(Long attractionId);

    /**
     * 批量创建票务类型
     * @param ticketTypes 票务类型列表
     */
    void batchCreateTicketTypes(List<TicketType> ticketTypes);

    /**
     * 根据条件搜索票务类型
     * @param attractionId 景点ID
     * @param ticketType 票务类型名称
     * @param status 状态
     * @return 票务类型列表
     */
    List<TicketType> searchTicketTypes(Long attractionId, String ticketType, String status);
}

