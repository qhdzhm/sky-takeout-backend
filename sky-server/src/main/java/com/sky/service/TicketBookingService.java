package com.sky.service;

import com.sky.entity.TicketBooking;
import com.sky.result.PageResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 票务预订服务接口 - 基于酒店预订服务接口设计
 */
public interface TicketBookingService {

    /**
     * 创建票务预订
     * @param ticketBooking 票务预订信息
     * @return 预订参考号
     */
    String createTicketBooking(TicketBooking ticketBooking);

    /**
     * 根据ID获取票务预订详情
     * @param id 预订ID
     * @return 票务预订信息
     */
    TicketBooking getTicketBookingById(Long id);

    /**
     * 根据预订参考号获取票务预订
     * @param bookingReference 预订参考号
     * @return 票务预订信息
     */
    TicketBooking getTicketBookingByReference(String bookingReference);

    /**
     * 根据确认号获取票务预订
     * @param confirmationNumber 确认号
     * @return 票务预订信息
     */
    TicketBooking getTicketBookingByConfirmationNumber(String confirmationNumber);

    /**
     * 根据排团记录ID获取票务预订
     * @param scheduleOrderId 排团记录ID
     * @return 票务预订信息
     */
    TicketBooking getTicketBookingByScheduleOrderId(Long scheduleOrderId);

    /**
     * 根据旅游订单ID获取票务预订列表
     * @param tourBookingId 旅游订单ID
     * @return 票务预订列表
     */
    List<TicketBooking> getTicketBookingsByTourBookingId(Long tourBookingId);

    /**
     * 更新票务预订
     * @param ticketBooking 票务预订信息
     */
    void updateTicketBooking(TicketBooking ticketBooking);

    /**
     * 删除票务预订
     * @param id 预订ID
     */
    void deleteTicketBooking(Long id);

    /**
     * 批量删除票务预订
     * @param ids 预订ID列表
     */
    void batchDeleteTicketBookings(List<Long> ids);

    /**
     * 更新预订状态
     * @param id 预订ID
     * @param status 预订状态
     */
    void updateBookingStatus(Long id, String status);

    /**
     * 批量更新预订状态
     * @param ids 预订ID列表
     * @param status 预订状态
     */
    void batchUpdateBookingStatus(List<Long> ids, String status);

    /**
     * 确认预订并设置确认号
     * @param id 预订ID
     * @param confirmationNumber 确认号
     */
    void confirmBookingWithNumber(Long id, String confirmationNumber);

    /**
     * 更新邮件发送时间
     * @param id 预订ID
     */
    void updateEmailSentTime(Long id);

    /**
     * 分页查询票务预订列表
     * @param page 页码
     * @param pageSize 页面大小
     * @param guestName 游客姓名
     * @param guestPhone 游客电话
     * @param attractionId 景点ID
     * @param bookingStatus 预订状态
     * @param bookingMethod 预订方式
     * @param visitDateStart 游览日期开始
     * @param visitDateEnd 游览日期结束
     * @param ticketSpecialist 票务专员
     * @return 分页结果
     */
    PageResult pageQuery(int page, int pageSize, String guestName, String guestPhone,
                        Long attractionId, String bookingStatus, String bookingMethod,
                        LocalDate visitDateStart, LocalDate visitDateEnd, String ticketSpecialist);

    /**
     * 根据景点ID和游览日期查询预订列表
     * @param attractionId 景点ID
     * @param visitDate 游览日期
     * @return 预订列表
     */
    List<TicketBooking> getBookingsByAttractionAndDate(Long attractionId, LocalDate visitDate);

    /**
     * 根据游览日期范围查询预订列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预订列表
     */
    List<TicketBooking> getBookingsByVisitDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 统计预订状态数量
     * @param attractionId 景点ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 状态统计结果
     */
    Map<String, Object> getBookingStatusStatistics(Long attractionId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成预订参考号
     * @return 预订参考号
     */
    String generateBookingReference();

    /**
     * 根据票务专员查询预订列表
     * @param ticketSpecialist 票务专员
     * @param bookingStatus 预订状态（可选）
     * @return 预订列表
     */
    List<TicketBooking> getBookingsByTicketSpecialist(String ticketSpecialist, String bookingStatus);

    /**
     * 发送预订邮件
     * @param bookingId 预订ID
     * @param emailContent 邮件内容
     * @param recipientEmail 收件人邮箱
     * @param subject 邮件主题
     * @return 是否发送成功
     */
    boolean sendBookingEmail(Long bookingId, String emailContent, String recipientEmail, String subject);

    /**
     * 从排团订单创建票务预订
     * @param scheduleOrderId 排团订单ID
     * @param attractionId 景点ID
     * @param ticketTypeId 票务类型ID
     * @return 预订参考号
     */
    String createFromScheduleOrder(Long scheduleOrderId, Long attractionId, Long ticketTypeId);

    /**
     * 从导游车辆分配批量创建票务预订
     * @param assignmentIds 分配ID列表
     * @param ticketBooking 预订基础信息
     * @return 创建的预订数量
     */
    int createFromAssignments(List<Long> assignmentIds, TicketBooking ticketBooking);

}

