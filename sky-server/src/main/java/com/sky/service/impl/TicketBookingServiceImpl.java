package com.sky.service.impl;

import com.sky.entity.TicketBooking;
import com.sky.mapper.TicketBookingMapper;
import com.sky.mapper.AttractionMapper;
import com.sky.mapper.TicketTypeMapper;
import com.sky.result.PageResult;
import com.sky.service.TicketBookingService;
import com.sky.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 票务预订服务实现类 - 基于酒店预订服务实现类设计
 */
@Service
@Slf4j
public class TicketBookingServiceImpl implements TicketBookingService {

    @Autowired
    private TicketBookingMapper ticketBookingMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private TicketTypeMapper ticketTypeMapper;
    
    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public String createTicketBooking(TicketBooking ticketBooking) {
        log.info("创建票务预订：{}", ticketBooking);
        
        // 设置创建时间和更新时间
        ticketBooking.setCreatedAt(LocalDateTime.now());
        ticketBooking.setUpdatedAt(LocalDateTime.now());
        ticketBooking.setBookingDate(LocalDate.now());
        
        // 生成预订参考号
        String bookingReference = generateBookingReference();
        ticketBooking.setBookingReference(bookingReference);
        
        // 计算总游客数
        int adultCount = ticketBooking.getAdultCount() != null ? ticketBooking.getAdultCount() : 0;
        int childCount = ticketBooking.getChildCount() != null ? ticketBooking.getChildCount() : 0;
        ticketBooking.setTotalGuests(adultCount + childCount);
        
        // 设置默认值
        if (ticketBooking.getBookingStatus() == null) {
            ticketBooking.setBookingStatus("pending");
        }
        if (ticketBooking.getPaymentStatus() == null) {
            ticketBooking.setPaymentStatus("unpaid");
        }
        if (ticketBooking.getCurrency() == null) {
            ticketBooking.setCurrency("AUD");
        }
        if (ticketBooking.getBookingSource() == null) {
            ticketBooking.setBookingSource("system");
        }
        
        ticketBookingMapper.insert(ticketBooking);
        return bookingReference;
    }

    @Override
    public TicketBooking getTicketBookingById(Long id) {
        log.info("根据ID获取票务预订详情：{}", id);
        return ticketBookingMapper.getById(id);
    }

    @Override
    public TicketBooking getTicketBookingByReference(String bookingReference) {
        log.info("根据预订参考号获取票务预订：{}", bookingReference);
        return ticketBookingMapper.getByBookingReference(bookingReference);
    }

    @Override
    public TicketBooking getTicketBookingByConfirmationNumber(String confirmationNumber) {
        log.info("根据确认号获取票务预订：{}", confirmationNumber);
        return ticketBookingMapper.getByConfirmationNumber(confirmationNumber);
    }

    @Override
    public TicketBooking getTicketBookingByScheduleOrderId(Long scheduleOrderId) {
        log.info("根据排团记录ID获取票务预订：{}", scheduleOrderId);
        return ticketBookingMapper.getByScheduleOrderId(scheduleOrderId);
    }

    @Override
    public List<TicketBooking> getTicketBookingsByTourBookingId(Long tourBookingId) {
        log.info("根据旅游订单ID获取票务预订列表：{}", tourBookingId);
        return ticketBookingMapper.getByTourBookingId(tourBookingId);
    }

    @Override
    public void updateTicketBooking(TicketBooking ticketBooking) {
        log.info("更新票务预订：{}", ticketBooking);
        ticketBooking.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算总游客数
        int adultCount = ticketBooking.getAdultCount() != null ? ticketBooking.getAdultCount() : 0;
        int childCount = ticketBooking.getChildCount() != null ? ticketBooking.getChildCount() : 0;
        ticketBooking.setTotalGuests(adultCount + childCount);
        
        ticketBookingMapper.update(ticketBooking);
    }

    @Override
    public void deleteTicketBooking(Long id) {
        log.info("删除票务预订：{}", id);
        ticketBookingMapper.deleteById(id);
    }

    @Override
    public void batchDeleteTicketBookings(List<Long> ids) {
        log.info("批量删除票务预订，数量：{}", ids.size());
        ticketBookingMapper.batchDelete(ids);
    }

    @Override
    public void updateBookingStatus(Long id, String status) {
        log.info("更新预订状态：id={}, status={}", id, status);
        ticketBookingMapper.updateStatus(id, status);
    }

    @Override
    public void batchUpdateBookingStatus(List<Long> ids, String status) {
        log.info("批量更新预订状态：ids={}, status={}", ids, status);
        ticketBookingMapper.batchUpdateStatus(ids, status);
    }

    @Override
    public void confirmBookingWithNumber(Long id, String confirmationNumber) {
        log.info("确认预订并设置确认号：id={}, confirmationNumber={}", id, confirmationNumber);
        ticketBookingMapper.updateConfirmationNumber(id, confirmationNumber, LocalDateTime.now());
    }

    @Override
    public void updateEmailSentTime(Long id) {
        log.info("更新邮件发送时间：id={}", id);
        ticketBookingMapper.updateEmailSentTime(id, LocalDateTime.now());
    }

    @Override
    public PageResult pageQuery(int page, int pageSize, String guestName, String guestPhone,
                               Long attractionId, String bookingStatus, String bookingMethod,
                               LocalDate visitDateStart, LocalDate visitDateEnd, String ticketSpecialist) {
        log.info("分页查询票务预订：page={}, pageSize={}", page, pageSize);
        
        PageHelper.startPage(page, pageSize);
        Page<TicketBooking> ticketBookingPage = (Page<TicketBooking>) ticketBookingMapper.pageQuery(
                (page - 1) * pageSize, pageSize, guestName, guestPhone, attractionId, 
                bookingStatus, bookingMethod, visitDateStart, visitDateEnd, ticketSpecialist);
        
        return new PageResult(ticketBookingPage.getTotal(), ticketBookingPage.getResult());
    }

    @Override
    public List<TicketBooking> getBookingsByAttractionAndDate(Long attractionId, LocalDate visitDate) {
        log.info("根据景点ID和游览日期查询预订列表：attractionId={}, visitDate={}", attractionId, visitDate);
        return ticketBookingMapper.getByAttractionAndDate(attractionId, visitDate);
    }

    @Override
    public List<TicketBooking> getBookingsByVisitDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("根据游览日期范围查询预订列表：startDate={}, endDate={}", startDate, endDate);
        return ticketBookingMapper.getByVisitDateRange(startDate, endDate);
    }

    @Override
    public Map<String, Object> getBookingStatusStatistics(Long attractionId, LocalDate startDate, LocalDate endDate) {
        log.info("统计预订状态数量：attractionId={}, startDate={}, endDate={}", attractionId, startDate, endDate);
        
        List<Map<String, Object>> rawStats = ticketBookingMapper.getStatusStatistics(attractionId, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        
        for (Map<String, Object> stat : rawStats) {
            String status = (String) stat.get("status");
            result.put(status, stat);
        }
        
        return result;
    }

    @Override
    public String generateBookingReference() {
        log.info("生成预订参考号");
        
        // 格式：TB + yyyyMMdd + 3位序号
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = "TB" + dateStr;
        
        // 获取当日最大序号
        Integer maxSeq = ticketBookingMapper.getMaxSequenceByDatePrefix(datePrefix);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        
        return datePrefix + String.format("%03d", nextSeq);
    }

    @Override
    public List<TicketBooking> getBookingsByTicketSpecialist(String ticketSpecialist, String bookingStatus) {
        log.info("根据票务专员查询预订列表：ticketSpecialist={}, bookingStatus={}", ticketSpecialist, bookingStatus);
        return ticketBookingMapper.getByTicketSpecialist(ticketSpecialist, bookingStatus);
    }

    @Override
    public boolean sendBookingEmail(Long bookingId, String emailContent, String recipientEmail) {
        log.info("发送预订邮件：bookingId={}, recipientEmail={}", bookingId, recipientEmail);
        
        try {
            // 调用邮件服务发送邮件
            // emailService.sendBookingEmail(recipientEmail, "票务预订确认", emailContent);
            
            // 更新邮件发送时间
            updateEmailSentTime(bookingId);
            
            return true;
        } catch (Exception e) {
            log.error("发送预订邮件失败：bookingId={}, error={}", bookingId, e.getMessage());
            return false;
        }
    }

    @Override
    public String createFromScheduleOrder(Long scheduleOrderId, Long attractionId, Long ticketTypeId) {
        log.info("从排团订单创建票务预订：scheduleOrderId={}, attractionId={}, ticketTypeId={}", 
                scheduleOrderId, attractionId, ticketTypeId);
        
        // TODO: 实现从排团订单创建预订的逻辑
        // 1. 获取排团订单信息
        // 2. 创建票务预订
        // 3. 关联排团订单ID
        
        return generateBookingReference();
    }

    @Override
    public int createFromAssignments(List<Long> assignmentIds, TicketBooking ticketBooking) {
        log.info("从导游车辆分配批量创建票务预订：assignmentIds={}", assignmentIds);
        
        // TODO: 实现从导游车辆分配批量创建预订的逻辑
        // 1. 获取分配信息
        // 2. 批量创建票务预订
        // 3. 关联分配ID
        
        return assignmentIds.size();
    }
}
