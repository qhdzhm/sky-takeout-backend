package com.sky.service;

import com.sky.dto.PassengerDTO;
import com.sky.entity.Passenger;
import com.sky.vo.PassengerVO;

import java.util.List;

/**
 * 乘客信息服务接口
 */
public interface PassengerService {

    /**
     * 根据ID查询乘客
     * @param passengerId 乘客ID
     * @return 乘客信息
     */
    PassengerVO getById(Integer passengerId);

    /**
     * 根据护照号查询乘客
     * @param passportNumber 护照号
     * @return 乘客信息
     */
    PassengerVO getByPassportNumber(String passportNumber);

    /**
     * 根据订单ID查询乘客列表
     * @param bookingId 订单ID
     * @return 乘客列表
     */
    List<PassengerVO> getByBookingId(Integer bookingId);

    /**
     * 保存乘客信息
     * @param passengerDTO 乘客信息
     * @return 乘客ID
     */
    Integer save(PassengerDTO passengerDTO);

    /**
     * 更新乘客信息
     * @param passengerDTO 乘客信息
     * @return 是否成功
     */
    Boolean update(PassengerDTO passengerDTO);

    /**
     * 删除乘客信息
     * @param passengerId 乘客ID
     * @return 是否成功
     */
    Boolean delete(Integer passengerId);

    /**
     * 删除乘客信息
     * @param passengerId 乘客ID
     * @return 是否成功
     */
    Boolean deleteById(Integer passengerId);

    /**
     * 添加乘客到订单
     * @param bookingId 订单ID
     * @param passengerDTO 乘客信息
     * @return 是否成功
     */
    Boolean addPassengerToBooking(Integer bookingId, PassengerDTO passengerDTO);

    /**
     * 从订单中移除乘客
     * @param bookingId 订单ID
     * @param passengerId 乘客ID
     * @return 是否成功
     */
    Boolean removePassengerFromBooking(Integer bookingId, Integer passengerId);

    /**
     * 更新乘客在订单中的信息（如座位号、登记状态等）
     * @param bookingId 订单ID
     * @param passengerDTO 乘客信息
     * @return 是否成功
     */
    Boolean updatePassengerBookingInfo(Integer bookingId, PassengerDTO passengerDTO);
} 