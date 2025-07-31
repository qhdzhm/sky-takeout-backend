package com.sky.mapper;

import com.sky.entity.HotelBooking;
import com.sky.vo.HotelBookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店预订Mapper接口
 */
@Mapper
public interface HotelBookingMapper {

    /**
     * 插入酒店预订记录
     * @param hotelBooking 酒店预订信息
     */
    void insert(HotelBooking hotelBooking);

    /**
     * 根据ID查询酒店预订
     * @param id 预订ID
     * @return 酒店预订信息
     */
    HotelBooking getById(Integer id);

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
     * @param hotelBooking 酒店预订信息
     */
    void update(HotelBooking hotelBooking);

    /**
     * 删除酒店预订
     * @param id 预订ID
     */
    void deleteById(Integer id);

    /**
     * 分页查询酒店预订列表（带详细信息）
     * @param status 预订状态
     * @param guestName 客人姓名
     * @param guestPhone 客人电话
     * @param hotelId 酒店ID
     * @param checkInDate 入住日期开始
     * @param checkOutDate 入住日期结束
     * @return 酒店预订详细信息列表
     */
    List<HotelBookingVO> pageQuery(@Param("status") String status,
                                   @Param("guestName") String guestName,
                                   @Param("guestPhone") String guestPhone,
                                   @Param("hotelId") Integer hotelId,
                                   @Param("checkInDate") LocalDate checkInDate,
                                   @Param("checkOutDate") LocalDate checkOutDate);

    /**
     * 根据ID查询酒店预订详细信息（包含关联信息）
     * @param id 预订ID
     * @return 酒店预订详细信息
     */
    HotelBookingVO getDetailById(Integer id);

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
     * @return 是否可用（true：可用，false：不可用）
     */
    boolean checkRoomAvailability(@Param("hotelId") Integer hotelId,
                                  @Param("roomTypeId") Integer roomTypeId,
                                  @Param("checkInDate") LocalDate checkInDate,
                                  @Param("checkOutDate") LocalDate checkOutDate,
                                  @Param("roomCount") Integer roomCount);

    /**
     * 更新预订状态
     * @param id 预订ID
     * @param bookingStatus 新状态
     */
    void updateBookingStatus(@Param("id") Integer id, @Param("bookingStatus") String bookingStatus);

    /**
     * 更新支付状态
     * @param id 预订ID
     * @param paymentStatus 支付状态
     */
    void updatePaymentStatus(@Param("id") Integer id, @Param("paymentStatus") String paymentStatus);

    /**
     * 根据导游车辆分配ID查询酒店预订列表
     * @param assignmentId 导游车辆分配ID
     * @return 酒店预订列表
     */
    List<HotelBooking> getByAssignmentId(Integer assignmentId);

    /**
     * 更新邮件发送信息
     * @param bookingId 预订ID
     * @param emailTo 邮件接收地址
     * @param emailContent 邮件内容
     * @param sentBy 发送人ID
     */
    void updateEmailSentInfo(@Param("bookingId") Integer bookingId,
                            @Param("emailTo") String emailTo,
                            @Param("emailContent") String emailContent,
                            @Param("sentBy") Long sentBy);
} 