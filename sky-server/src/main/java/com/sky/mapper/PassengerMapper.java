package com.sky.mapper;

import com.sky.entity.Passenger;
import com.sky.entity.BookingPassengerRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 乘客数据访问层接口
 */
@Mapper
public interface PassengerMapper {

    /**
     * 根据ID查询乘客
     * @param passengerId 乘客ID
     * @return 乘客信息
     */
    Passenger getById(Integer passengerId);

    /**
     * 根据护照号查询乘客
     * @param passportNumber 护照号
     * @return 乘客信息
     */
    Passenger getByPassportNumber(String passportNumber);

    /**
     * 根据姓名模糊查询乘客列表
     * @param fullName 乘客姓名
     * @return 乘客列表
     */
    List<Passenger> getByFullNameLike(@Param("fullName") String fullName);

    /**
     * 根据电话号码查询乘客
     * @param phone 电话号码
     * @return 乘客信息
     */
    Passenger getByPhone(String phone);

    /**
     * 根据订单ID查询乘客列表
     * @param bookingId 订单ID
     * @return 乘客列表
     */
    List<Passenger> getByBookingId(Integer bookingId);

    /**
     * 插入乘客信息
     * @param passenger 乘客信息
     * @return 影响行数
     */
    int insert(Passenger passenger);

    /**
     * 更新乘客信息
     * @param passenger 乘客信息
     * @return 影响行数
     */
    int update(Passenger passenger);

    /**
     * 删除乘客信息
     * @param passengerId 乘客ID
     * @return 影响行数
     */
    int deleteById(Integer passengerId);

    /**
     * 保存乘客和订单的关联
     * @param relation 关联信息
     * @return 影响行数
     */
    int saveBookingPassengerRelation(BookingPassengerRelation relation);

    /**
     * 更新乘客和订单的关联
     * @param relation 关联信息
     * @return 影响行数
     */
    int updateBookingPassengerRelation(BookingPassengerRelation relation);

    /**
     * 查询乘客和订单的关联信息
     * @param bookingId 订单ID
     * @param passengerId 乘客ID
     * @return 关联信息
     */
    BookingPassengerRelation getRelation(@Param("bookingId") Integer bookingId, @Param("passengerId") Integer passengerId);

    /**
     * 删除乘客和订单的关联
     * @param bookingId 订单ID
     * @param passengerId 乘客ID
     * @return 影响行数
     */
    int deleteRelation(@Param("bookingId") Integer bookingId, @Param("passengerId") Integer passengerId);
} 