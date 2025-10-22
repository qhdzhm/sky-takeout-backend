package com.sky.mapper;

import com.sky.entity.Hotel;
import com.sky.vo.HotelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店Mapper接口
 */
@Mapper
public interface HotelMapper {

    /**
     * 插入酒店信息
     * @param hotel 酒店信息
     */
    void insert(Hotel hotel);

    /**
     * 根据ID查询酒店
     * @param id 酒店ID
     * @return 酒店信息
     */
    Hotel getById(Integer id);

    /**
     * 更新酒店信息
     * @param hotel 酒店信息
     */
    void update(Hotel hotel);

    /**
     * 删除酒店
     * @param id 酒店ID
     */
    void deleteById(Integer id);

    /**
     * 根据条件搜索酒店
     * @param locationArea 区域位置
     * @param hotelLevel 酒店等级
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param sortBy 排序方式
     * @return 酒店列表
     */
    List<HotelVO> searchHotels(@Param("locationArea") String locationArea,
                               @Param("hotelLevel") String hotelLevel,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               @Param("sortBy") String sortBy);

    /**
     * 根据ID查询酒店详细信息（包含房型）
     * @param id 酒店ID
     * @return 酒店详细信息
     */
    HotelVO getDetailById(Integer id);

    /**
     * 根据酒店等级查询酒店列表
     * @param hotelLevel 酒店等级
     * @return 酒店列表
     */
    List<Hotel> getByHotelLevel(String hotelLevel);

    /**
     * 根据区域位置查询酒店列表
     * @param locationArea 区域位置
     * @return 酒店列表
     */
    List<Hotel> getByLocationArea(String locationArea);

    /**
     * 获取所有活跃的酒店
     * @return 酒店列表
     */
    List<Hotel> getAllActive();

    /**
     * 更新酒店状态
     * @param id 酒店ID
     * @param status 状态
     */
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 检查酒店是否有关联的预订记录
     * @param id 酒店ID
     * @return 关联的预订记录数量
     */
    int countBookingsByHotelId(@Param("id") Integer id);

    /**
     * 检查酒店是否有关联的房型
     * @param id 酒店ID
     * @return 关联的房型数量
     */
    int countRoomTypesByHotelId(@Param("id") Integer id);
} 