package com.sky.mapper;

import com.sky.entity.TicketType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 票务类型Mapper接口 - 对标酒店房型Mapper
 */
@Mapper
public interface TicketTypeMapper {

    /**
     * 插入票务类型信息
     * @param ticketType 票务类型信息
     */
    void insert(TicketType ticketType);

    /**
     * 根据ID查询票务类型
     * @param id 票务类型ID
     * @return 票务类型信息
     */
    TicketType getById(Long id);

    /**
     * 更新票务类型信息
     * @param ticketType 票务类型信息
     */
    void update(TicketType ticketType);

    /**
     * 删除票务类型
     * @param id 票务类型ID
     */
    void deleteById(Long id);

    /**
     * 根据景点ID查询票务类型列表
     * @param attractionId 景点ID
     * @return 票务类型列表
     */
    List<TicketType> getByAttractionId(Long attractionId);

    /**
     * 根据景点ID查询活跃的票务类型列表
     * @param attractionId 景点ID
     * @return 票务类型列表
     */
    List<TicketType> getActiveByAttractionId(Long attractionId);

    /**
     * 获取所有活跃的票务类型
     * @return 票务类型列表
     */
    List<TicketType> getAllActive();

    /**
     * 根据票务代码查询票务类型
     * @param ticketCode 票务代码
     * @return 票务类型信息
     */
    TicketType getByTicketCode(String ticketCode);

    /**
     * 更新票务类型状态
     * @param id 票务类型ID
     * @param status 状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 根据景点ID删除所有票务类型
     * @param attractionId 景点ID
     */
    void deleteByAttractionId(Long attractionId);

    /**
     * 批量插入票务类型
     * @param ticketTypes 票务类型列表
     */
    void batchInsert(List<TicketType> ticketTypes);

    /**
     * 根据条件搜索票务类型
     * @param attractionId 景点ID
     * @param ticketType 票务类型名称
     * @param status 状态
     * @return 票务类型列表
     */
    List<TicketType> searchTicketTypes(@Param("attractionId") Long attractionId,
                                      @Param("ticketType") String ticketType,
                                      @Param("status") String status);
}

