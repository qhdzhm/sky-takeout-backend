package com.sky.mapper;

import com.sky.entity.HotelRoomType;
import com.sky.vo.HotelRoomTypeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 酒店房型Mapper接口
 */
@Mapper
public interface HotelRoomTypeMapper {

    /**
     * 插入房型信息
     * @param roomType 房型信息
     */
    void insert(HotelRoomType roomType);

    /**
     * 根据ID查询房型
     * @param id 房型ID
     * @return 房型信息
     */
    HotelRoomType getById(Integer id);

    /**
     * 更新房型信息
     * @param roomType 房型信息
     */
    void update(HotelRoomType roomType);

    /**
     * 删除房型
     * @param id 房型ID
     */
    void deleteById(Integer id);

    /**
     * 获取所有房型
     * @return 房型列表
     */
    List<HotelRoomType> getAll();

    /**
     * 根据酒店ID获取房型列表
     * @param hotelId 酒店ID
     * @return 房型列表
     */
    List<HotelRoomType> getByHotelId(Integer hotelId);

    /**
     * 根据酒店ID查询房型详细信息列表
     * @param hotelId 酒店ID
     * @return 房型详细信息列表
     */
    List<HotelRoomTypeVO> getDetailByHotelId(Integer hotelId);

    /**
     * 根据房型代码查询房型
     * @param hotelId 酒店ID
     * @param roomTypeCode 房型代码
     * @return 房型信息
     */
    HotelRoomType getByRoomTypeCode(@Param("hotelId") Integer hotelId, 
                                    @Param("roomTypeCode") String roomTypeCode);

    /**
     * 根据房型名称模糊查询
     * @param hotelId 酒店ID
     * @param roomType 房型名称
     * @return 房型列表
     */
    List<HotelRoomType> getByRoomTypeLike(@Param("hotelId") Integer hotelId, 
                                          @Param("roomType") String roomType);

    /**
     * 获取所有活跃的房型
     * @param hotelId 酒店ID
     * @return 房型列表
     */
    List<HotelRoomType> getAllActiveByHotelId(Integer hotelId);

    /**
     * 更新房型状态
     * @param id 房型ID
     * @param status 状态
     */
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 检查房型是否有关联的预订记录
     * @param id 房型ID
     * @return 关联的预订记录数量
     */
    int countBookingsByRoomTypeId(@Param("id") Integer id);
} 