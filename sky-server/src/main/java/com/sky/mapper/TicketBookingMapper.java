package com.sky.mapper;

import com.sky.entity.TicketBooking;
import com.sky.vo.TicketBookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 票务预订Mapper接口 - 基于酒店预订Mapper架构设计
 */
@Mapper
public interface TicketBookingMapper {

    /**
     * 插入票务预订记录
     * @param ticketBooking 票务预订信息
     */
    void insert(TicketBooking ticketBooking);

    /**
     * 根据ID查询票务预订
     * @param id 预订ID
     * @return 票务预订信息
     */
    TicketBooking getById(Long id);

    /**
     * 根据预订参考号查询票务预订
     * @param bookingReference 预订参考号
     * @return 票务预订信息
     */
    TicketBooking getByBookingReference(String bookingReference);

    /**
     * 根据确认号查询票务预订
     * @param confirmationNumber 确认号
     * @return 票务预订信息
     */
    TicketBooking getByConfirmationNumber(String confirmationNumber);

    /**
     * 根据排团记录ID查询票务预订
     * @param scheduleOrderId 排团记录ID
     * @return 票务预订信息
     */
    TicketBooking getByScheduleOrderId(Long scheduleOrderId);

    /**
     * 根据旅游订单ID查询票务预订列表
     * @param tourBookingId 旅游订单ID
     * @return 票务预订列表
     */
    List<TicketBooking> getByTourBookingId(Long tourBookingId);

    /**
     * 更新票务预订信息
     * @param ticketBooking 票务预订信息
     */
    void update(TicketBooking ticketBooking);

    /**
     * 删除票务预订
     * @param id 预订ID
     */
    void deleteById(Long id);

    /**
     * 批量删除票务预订
     * @param ids 预订ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 更新预订状态
     * @param id 预订ID
     * @param status 预订状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 批量更新预订状态
     * @param ids 预订ID列表
     * @param status 预订状态
     */
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 更新确认号
     * @param id 预订ID
     * @param confirmationNumber 确认号
     * @param confirmedTime 确认时间
     */
    void updateConfirmationNumber(@Param("id") Long id, 
                                 @Param("confirmationNumber") String confirmationNumber,
                                 @Param("confirmedTime") LocalDateTime confirmedTime);

    /**
     * 更新邮件发送时间
     * @param id 预订ID
     * @param emailSentTime 邮件发送时间
     */
    void updateEmailSentTime(@Param("id") Long id, @Param("emailSentTime") LocalDateTime emailSentTime);

    /**
     * 分页查询票务预订列表
     * @param offset 偏移量
     * @param pageSize 页面大小
     * @param guestName 游客姓名
     * @param guestPhone 游客电话
     * @param attractionId 景点ID
     * @param bookingStatus 预订状态
     * @param bookingMethod 预订方式
     * @param visitDateStart 游览日期开始
     * @param visitDateEnd 游览日期结束
     * @param ticketSpecialist 票务专员
     * @return 票务预订列表
     */
    List<TicketBooking> pageQuery(@Param("offset") int offset,
                                 @Param("pageSize") int pageSize,
                                 @Param("guestName") String guestName,
                                 @Param("guestPhone") String guestPhone,
                                 @Param("attractionId") Long attractionId,
                                 @Param("bookingStatus") String bookingStatus,
                                 @Param("bookingMethod") String bookingMethod,
                                 @Param("visitDateStart") LocalDate visitDateStart,
                                 @Param("visitDateEnd") LocalDate visitDateEnd,
                                 @Param("ticketSpecialist") String ticketSpecialist);

    /**
     * 统计票务预订总数
     * @param guestName 游客姓名
     * @param guestPhone 游客电话
     * @param attractionId 景点ID
     * @param bookingStatus 预订状态
     * @param bookingMethod 预订方式
     * @param visitDateStart 游览日期开始
     * @param visitDateEnd 游览日期结束
     * @param ticketSpecialist 票务专员
     * @return 总数
     */
    long countQuery(@Param("guestName") String guestName,
                   @Param("guestPhone") String guestPhone,
                   @Param("attractionId") Long attractionId,
                   @Param("bookingStatus") String bookingStatus,
                   @Param("bookingMethod") String bookingMethod,
                   @Param("visitDateStart") LocalDate visitDateStart,
                   @Param("visitDateEnd") LocalDate visitDateEnd,
                   @Param("ticketSpecialist") String ticketSpecialist);

    /**
     * 根据景点ID和游览日期查询预订列表
     * @param attractionId 景点ID
     * @param visitDate 游览日期
     * @return 预订列表
     */
    List<TicketBooking> getByAttractionAndDate(@Param("attractionId") Long attractionId, 
                                              @Param("visitDate") LocalDate visitDate);

    /**
     * 根据游览日期范围查询预订列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预订列表
     */
    List<TicketBooking> getByVisitDateRange(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    /**
     * 统计预订状态数量
     * @param attractionId 景点ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 状态统计结果
     */
    List<java.util.Map<String, Object>> getStatusStatistics(@Param("attractionId") Long attractionId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    /**
     * 获取今日预订参考号的最大序号
     * @param datePrefix 日期前缀（TB+yyyyMMdd）
     * @return 最大序号
     */
    Integer getMaxSequenceByDatePrefix(@Param("datePrefix") String datePrefix);

    /**
     * 根据票务专员查询预订列表
     * @param ticketSpecialist 票务专员
     * @param bookingStatus 预订状态（可选）
     * @return 预订列表
     */
    List<TicketBooking> getByTicketSpecialist(@Param("ticketSpecialist") String ticketSpecialist,
                                             @Param("bookingStatus") String bookingStatus);
    
    /**
     * 根据旅游订单ID查询票务预订详细信息（包含景点信息）
     * @param tourBookingId 旅游订单ID
     * @return 票务预订详细信息列表
     */
    List<TicketBookingVO> getByTourBookingIdWithDetails(Long tourBookingId);
}

