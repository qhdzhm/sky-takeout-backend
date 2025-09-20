package com.sky.service;

import com.sky.dto.HotelBookingDTO;
import com.sky.entity.HotelBooking;
import com.sky.result.PageResult;
import com.sky.vo.HotelBookingVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店预订服务接口
 */
public interface HotelBookingService {

    /**
     * 创建酒店预订
     * @param hotelBookingDTO 酒店预订信息
     * @return 预订ID
     */
    Integer createBooking(HotelBookingDTO hotelBookingDTO);

    /**
     * 基于排团记录创建酒店预订
     * @param scheduleOrderId 排团记录ID
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @return 预订ID
     */
    Integer createBookingFromScheduleOrder(Integer scheduleOrderId, Integer hotelId, Integer roomTypeId);

    /**
     * 根据ID查询酒店预订
     * @param id 预订ID
     * @return 酒店预订信息
     */
    HotelBooking getById(Integer id);

    /**
     * 根据ID查询酒店预订详细信息
     * @param id 预订ID
     * @return 酒店预订详细信息
     */
    HotelBookingVO getDetailById(Integer id);

    /**
     * 根据预订参考号查询酒店预订
     * @param bookingReference 预订参考号
     * @return 酒店预订信息
     */
    HotelBooking getByBookingReference(String bookingReference);

    /**
     * 根据排团记录ID查询酒店预订
     * @param scheduleOrderId 排团记录ID
     * @return 酒店预订信息
     */
    HotelBooking getByScheduleOrderId(Integer scheduleOrderId);

    /**
     * 根据旅游订单ID查询酒店预订列表
     * @param tourBookingId 旅游订单ID
     * @return 酒店预订列表
     */
    List<HotelBooking> getByTourBookingId(Integer tourBookingId);

    /**
     * 根据旅游订单ID查询酒店预订列表（含酒店详细信息）
     * @param tourBookingId 旅游订单ID
     * @return 酒店预订详细信息列表
     */
    List<HotelBookingVO> getByTourBookingIdWithDetails(Integer tourBookingId);

    /**
     * 更新酒店预订信息
     * @param hotelBookingDTO 酒店预订信息
     * @return 是否成功
     */
    Boolean updateBooking(HotelBookingDTO hotelBookingDTO);

    /**
     * 取消酒店预订
     * @param id 预订ID
     * @return 是否成功
     */
    Boolean cancelBooking(Integer id);

    /**
     * 确认酒店预订
     * @param id 预订ID
     * @return 是否成功
     */
    Boolean confirmBooking(Integer id);

    /**
     * 确认酒店预订并设置预订号
     * @param id 预订ID
     * @param hotelBookingNumber 酒店预订号
     * @return 是否成功
     */
    Boolean confirmBookingWithNumber(Integer id, String hotelBookingNumber);

    /**
     * 办理入住
     * @param id 预订ID
     * @return 是否成功
     */
    Boolean checkIn(Integer id);

    /**
     * 办理退房
     * @param id 预订ID
     * @return 是否成功
     */
    Boolean checkOut(Integer id);

    /**
     * 更新预订状态
     * @param id 预订ID
     * @param status 新状态
     * @return 是否成功
     */
    Boolean updateBookingStatus(Integer id, String status);

    /**
     * 分页查询酒店预订列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param status 预订状态
     * @param guestName 客人姓名
     * @param guestPhone 客人电话
     * @param hotelId 酒店ID
     * @param hotelSpecialist 酒店专员
     * @param checkInDate 入住日期开始
     * @param checkOutDate 入住日期结束
     * @return 分页结果
     */
    PageResult pageQuery(Integer page, Integer pageSize, String status, String guestName, String guestPhone,
                         Integer hotelId, String hotelSpecialist, LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * 根据代理商ID查询酒店预订列表
     * @param agentId 代理商ID
     * @return 酒店预订列表
     */
    List<HotelBookingVO> getByAgentId(Integer agentId);

    /**
     * 检查酒店房间可用性
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param checkInDate 入住日期
     * @param checkOutDate 退房日期
     * @param roomCount 需要房间数
     * @return 是否可用
     */
    Boolean checkRoomAvailability(Integer hotelId, Integer roomTypeId, 
                                  LocalDate checkInDate, LocalDate checkOutDate, 
                                  Integer roomCount);

    /**
     * 更新支付状态
     * @param id 预订ID
     * @param paymentStatus 支付状态
     * @return 是否成功
     */
    Boolean updatePaymentStatus(Integer id, String paymentStatus);

    /**
     * 删除酒店预订
     * @param id 预订ID
     * @return 是否成功
     */
    Boolean deleteBooking(Integer id);

    /**
     * 根据导游车辆分配ID查询酒店预订列表
     * @param assignmentId 导游车辆分配ID
     * @return 酒店预订列表
     */
    List<HotelBooking> getByAssignmentId(Integer assignmentId);

    /**
     * 批量创建酒店预订（基于导游车辆分配）
     * @param assignmentId 导游车辆分配ID
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @return 创建的预订数量
     */
    Integer batchCreateBookingsFromAssignment(Integer assignmentId, Integer hotelId, Integer roomTypeId);

    /**
     * 发送酒店预订邮件
     * @param emailDTO 邮件信息
     * @return 是否成功
     */
    Boolean sendBookingEmail(com.sky.dto.HotelBookingEmailDTO emailDTO);

    /**
     * 根据日期范围批量查询酒店预订
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 酒店预订列表
     */
    List<HotelBooking> getByDateRange(LocalDate startDate, LocalDate endDate);
} 