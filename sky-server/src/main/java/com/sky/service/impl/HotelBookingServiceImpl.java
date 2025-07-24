package com.sky.service.impl;

import com.sky.dto.HotelBookingDTO;
import com.sky.entity.HotelBooking;
import com.sky.entity.TourScheduleOrder;
import com.sky.entity.TourGuideVehicleAssignment;
import com.sky.entity.Hotel;
import com.sky.mapper.HotelBookingMapper;
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.mapper.TourGuideVehicleAssignmentMapper;
import com.sky.mapper.HotelMapper;
import com.sky.result.PageResult;
import com.sky.service.HotelBookingService;
import com.sky.vo.HotelBookingVO;
import com.sky.vo.TourGuideVehicleAssignmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 酒店预订服务实现类
 */
@Service
@Slf4j
public class HotelBookingServiceImpl implements HotelBookingService {

    @Autowired
    private HotelBookingMapper hotelBookingMapper;
    
    @Autowired
    private TourScheduleOrderMapper tourScheduleOrderMapper;
    
    @Autowired
    private TourGuideVehicleAssignmentMapper tourGuideVehicleAssignmentMapper;
    
    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    @Transactional
    public Integer createBooking(HotelBookingDTO hotelBookingDTO) {
        log.info("创建酒店预订：{}", hotelBookingDTO);
        
        HotelBooking hotelBooking = new HotelBooking();
        BeanUtils.copyProperties(hotelBookingDTO, hotelBooking);
        
        // 设置创建时间和更新时间
        hotelBooking.setCreatedAt(LocalDateTime.now());
        hotelBooking.setUpdatedAt(LocalDateTime.now());
        
        // 自动计算住宿天数
        if (hotelBooking.getCheckInDate() != null && hotelBooking.getCheckOutDate() != null) {
            long nights = ChronoUnit.DAYS.between(hotelBooking.getCheckInDate(), hotelBooking.getCheckOutDate());
            hotelBooking.setNights((int) nights);
        }
        
        // 自动计算总客人数
        int totalGuests = (hotelBooking.getAdultCount() != null ? hotelBooking.getAdultCount() : 0) +
                         (hotelBooking.getChildCount() != null ? hotelBooking.getChildCount() : 0);
        hotelBooking.setTotalGuests(totalGuests);
        
        // 设置默认状态
        if (hotelBooking.getBookingStatus() == null) {
            hotelBooking.setBookingStatus("pending");
        }
        if (hotelBooking.getPaymentStatus() == null) {
            hotelBooking.setPaymentStatus("unpaid");
        }
        if (hotelBooking.getBookingSource() == null) {
            hotelBooking.setBookingSource("system");
        }
        
        hotelBookingMapper.insert(hotelBooking);
        
        log.info("酒店预订创建成功，预订ID：{}", hotelBooking.getId());
        return hotelBooking.getId();
    }

    @Override
    @Transactional
    public Integer createBookingFromScheduleOrder(Integer scheduleOrderId, Integer hotelId, Integer roomTypeId) {
        log.info("基于排团记录创建酒店预订，排团记录ID：{}，酒店ID：{}，房型ID：{}", scheduleOrderId, hotelId, roomTypeId);
        
        // 查询排团记录
        TourScheduleOrder scheduleOrder = tourScheduleOrderMapper.getById(scheduleOrderId);
        if (scheduleOrder == null) {
            throw new RuntimeException("排团记录不存在");
        }
        
        // 检查是否已存在酒店预订
        HotelBooking existingBooking = hotelBookingMapper.getByScheduleOrderId(scheduleOrderId);
        if (existingBooking != null) {
            throw new RuntimeException("该排团记录已存在酒店预订");
        }
        
        // 创建酒店预订
        HotelBooking hotelBooking = HotelBooking.builder()
                .tourBookingId(scheduleOrder.getBookingId())
                .scheduleOrderId(scheduleOrderId)
                .hotelId(hotelId)
                .roomTypeId(roomTypeId)
                .guestName(scheduleOrder.getContactPerson())
                .guestPhone(scheduleOrder.getContactPhone())
                .checkInDate(scheduleOrder.getHotelCheckInDate())
                .checkOutDate(scheduleOrder.getHotelCheckOutDate())
                .roomCount(scheduleOrder.getHotelRoomCount())
                .adultCount(scheduleOrder.getAdultCount())
                .childCount(scheduleOrder.getChildCount())
                .bookingStatus("pending")
                .paymentStatus("unpaid")
                .bookingSource("agent")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 自动计算住宿天数和总客人数
        if (hotelBooking.getCheckInDate() != null && hotelBooking.getCheckOutDate() != null) {
            long nights = ChronoUnit.DAYS.between(hotelBooking.getCheckInDate(), hotelBooking.getCheckOutDate());
            hotelBooking.setNights((int) nights);
        }
        
        int totalGuests = (hotelBooking.getAdultCount() != null ? hotelBooking.getAdultCount() : 0) +
                         (hotelBooking.getChildCount() != null ? hotelBooking.getChildCount() : 0);
        hotelBooking.setTotalGuests(totalGuests);
        
        hotelBookingMapper.insert(hotelBooking);
        
        log.info("基于排团记录的酒店预订创建成功，预订ID：{}", hotelBooking.getId());
        return hotelBooking.getId();
    }

    @Override
    public HotelBooking getById(Integer id) {
        log.info("根据ID查询酒店预订：{}", id);
        return hotelBookingMapper.getById(id);
    }

    @Override
    public HotelBookingVO getDetailById(Integer id) {
        log.info("根据ID查询酒店预订详细信息：{}", id);
        return hotelBookingMapper.getDetailById(id);
    }

    @Override
    public HotelBooking getByBookingReference(String bookingReference) {
        log.info("根据预订参考号查询酒店预订：{}", bookingReference);
        return hotelBookingMapper.getByBookingReference(bookingReference);
    }

    @Override
    public HotelBooking getByScheduleOrderId(Integer scheduleOrderId) {
        log.info("根据排团记录ID查询酒店预订：{}", scheduleOrderId);
        
        try {
            // 策略1：直接通过tour_booking_id查询酒店预订（前端传入的可能就是订单ID）
            List<HotelBooking> hotelBookings = hotelBookingMapper.getByTourBookingId(scheduleOrderId);
            if (hotelBookings != null && !hotelBookings.isEmpty()) {
                HotelBooking result = hotelBookings.get(0);
                log.info("通过tour_booking_id直接查询到酒店预订，booking_reference：{}, status：{}", 
                    result.getBookingReference(), result.getBookingStatus());
                return result;
            }
            
            // 策略2：如果策略1失败，尝试通过schedule_order_id查找对应的tour_booking_id
            TourScheduleOrder scheduleOrder = tourScheduleOrderMapper.getById(scheduleOrderId);
            if (scheduleOrder == null) {
                log.warn("未找到排团记录，且无法通过tour_booking_id直接查询，scheduleOrderId：{}", scheduleOrderId);
                return null;
            }
            
            Integer tourBookingId = scheduleOrder.getBookingId();
            log.info("找到排团记录，tour_booking_id：{}", tourBookingId);
            
            // 第二步：根据tour_booking_id查询酒店预订
            hotelBookings = hotelBookingMapper.getByTourBookingId(tourBookingId);
            if (hotelBookings == null || hotelBookings.isEmpty()) {
                log.info("该订单暂无酒店预订，tour_booking_id：{}", tourBookingId);
                return null;
            }
            
            // 返回第一个酒店预订记录（一般一个订单只有一个酒店预订）
            HotelBooking result = hotelBookings.get(0);
            log.info("通过schedule_order查询到酒店预订，booking_reference：{}, status：{}", 
                result.getBookingReference(), result.getBookingStatus());
            
            return result;
            
        } catch (Exception e) {
            log.error("根据排团记录ID查询酒店预订失败，scheduleOrderId：{}", scheduleOrderId, e);
            return null;
        }
    }

    @Override
    public List<HotelBooking> getByTourBookingId(Integer tourBookingId) {
        log.info("根据旅游订单ID查询酒店预订列表：{}", tourBookingId);
        return hotelBookingMapper.getByTourBookingId(tourBookingId);
    }

    @Override
    @Transactional
    public Boolean updateBooking(HotelBookingDTO hotelBookingDTO) {
        log.info("更新酒店预订信息：{}", hotelBookingDTO);
        
        HotelBooking hotelBooking = new HotelBooking();
        BeanUtils.copyProperties(hotelBookingDTO, hotelBooking);
        hotelBooking.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算住宿天数和总客人数
        if (hotelBooking.getCheckInDate() != null && hotelBooking.getCheckOutDate() != null) {
            long nights = ChronoUnit.DAYS.between(hotelBooking.getCheckInDate(), hotelBooking.getCheckOutDate());
            hotelBooking.setNights((int) nights);
        }
        
        int totalGuests = (hotelBooking.getAdultCount() != null ? hotelBooking.getAdultCount() : 0) +
                         (hotelBooking.getChildCount() != null ? hotelBooking.getChildCount() : 0);
        hotelBooking.setTotalGuests(totalGuests);
        
        hotelBookingMapper.update(hotelBooking);
        return true;
    }

    @Override
    @Transactional
    public Boolean cancelBooking(Integer id) {
        log.info("取消酒店预订：{}", id);
        
        // 1. 获取酒店预订详情（取消前）
        HotelBooking hotelBooking = hotelBookingMapper.getById(id);
        if (hotelBooking == null) {
            throw new RuntimeException("酒店预订不存在");
        }
        
        // 2. 更新酒店预订状态
        hotelBookingMapper.updateBookingStatus(id, "cancelled");
        
        // 3. 清除排团表中的接送信息
        clearPickupDropoffFromScheduleOrders(hotelBooking);
        
        return true;
    }
    
    /**
     * 清除排团表中的酒店接送信息
     */
    private void clearPickupDropoffFromScheduleOrders(HotelBooking hotelBooking) {
        try {
            log.info("开始清除排团表中的酒店接送信息，酒店预订ID：{}", hotelBooking.getId());
            
            // 获取该订单的所有排团记录
            List<TourScheduleOrder> scheduleOrders = tourScheduleOrderMapper.getByBookingId(hotelBooking.getTourBookingId());
            if (scheduleOrders == null || scheduleOrders.isEmpty()) {
                log.warn("未找到相关排团记录，订单ID：{}", hotelBooking.getTourBookingId());
                return;
            }
            
            LocalDate checkInDate = hotelBooking.getCheckInDate();
            LocalDate checkOutDate = hotelBooking.getCheckOutDate();
            
            // 清除相关日期的接送信息
            for (TourScheduleOrder schedule : scheduleOrders) {
                boolean needUpdate = false;
                LocalDate tourDate = schedule.getTourDate();
                
                // 清除入住日、退房日和中间日期的接送信息
                if ((tourDate.equals(checkInDate) || tourDate.equals(checkOutDate) || 
                    (tourDate.isAfter(checkInDate) && tourDate.isBefore(checkOutDate)))) {
                    
                    // 清空接送地点
                    schedule.setPickupLocation(null);
                    schedule.setDropoffLocation(null);
                    
                    // 清理特殊要求中的酒店相关信息
                    String specialRequests = schedule.getSpecialRequests();
                    if (specialRequests != null) {
                        // 移除酒店相关的备注信息
                        specialRequests = specialRequests.replaceAll("\\s*\\|\\s*送至酒店：[^|]*", "")
                                                       .replaceAll("\\s*\\|\\s*从酒店接客：[^|]*", "")
                                                       .replaceAll("\\s*\\|\\s*酒店接送：[^|]*", "")
                                                       .trim();
                        // 如果只剩下分隔符，则清空
                        if (specialRequests.matches("^\\s*\\|\\s*$")) {
                            specialRequests = null;
                        }
                        schedule.setSpecialRequests(specialRequests);
                    }
                    
                    needUpdate = true;
                }
                
                // 更新排团记录
                if (needUpdate) {
                    tourScheduleOrderMapper.update(schedule);
                    log.info("已清除排团记录接送信息，记录ID：{}，日期：{}", schedule.getId(), tourDate);
                }
            }
            
            log.info("酒店接送信息清除完成，酒店预订ID：{}", hotelBooking.getId());
            
        } catch (Exception e) {
            log.error("清除排团表中的酒店接送信息失败，酒店预订ID：{}", hotelBooking.getId(), e);
            // 不抛出异常，避免影响酒店预订取消流程
        }
    }

    @Override
    @Transactional
    public Boolean confirmBooking(Integer id) {
        log.info("确认酒店预订：{}", id);
        
        // 1. 更新酒店预订状态
        hotelBookingMapper.updateBookingStatus(id, "confirmed");
        
        // 2. 获取酒店预订详情
        HotelBooking hotelBooking = hotelBookingMapper.getById(id);
        if (hotelBooking == null) {
            throw new RuntimeException("酒店预订不存在");
        }
        
        // 3. 获取酒店信息
        Hotel hotel = hotelMapper.getById(hotelBooking.getHotelId());
        if (hotel == null) {
            log.warn("酒店信息不存在，无法同步接送地点");
            return true;
        }
        
        // 4. 同步接送信息到排团表
        syncPickupDropoffToScheduleOrders(hotelBooking, hotel);
        
        return true;
    }
    
    /**
     * 同步酒店接送信息到排团表
     */
    private void syncPickupDropoffToScheduleOrders(HotelBooking hotelBooking, Hotel hotel) {
        try {
            log.info("开始同步酒店接送信息到排团表，酒店预订ID：{}", hotelBooking.getId());
            
            // 获取该订单的所有排团记录
            List<TourScheduleOrder> scheduleOrders = tourScheduleOrderMapper.getByBookingId(hotelBooking.getTourBookingId());
            if (scheduleOrders == null || scheduleOrders.isEmpty()) {
                log.warn("未找到相关排团记录，订单ID：{}", hotelBooking.getTourBookingId());
                return;
            }
            
            String hotelName = hotel.getHotelName();
            String hotelAddress = hotel.getAddress() != null ? hotel.getAddress() : hotelName;
            LocalDate checkInDate = hotelBooking.getCheckInDate();
            LocalDate checkOutDate = hotelBooking.getCheckOutDate();
            
            // 根据入住日期逻辑更新接送信息
            for (TourScheduleOrder schedule : scheduleOrders) {
                boolean needUpdate = false;
                LocalDate tourDate = schedule.getTourDate();
                
                // 入住日：送客人到酒店（dropoff_location）
                if (tourDate.equals(checkInDate)) {
                    schedule.setDropoffLocation(hotelAddress);
                    String specialRequests = schedule.getSpecialRequests();
                    schedule.setSpecialRequests(specialRequests != null ? 
                        specialRequests + " | 送至酒店：" + hotelName : 
                        "送至酒店：" + hotelName);
                    needUpdate = true;
                }
                // 退房日：从酒店接客人（pickup_location）
                else if (tourDate.equals(checkOutDate)) {
                    schedule.setPickupLocation(hotelAddress);
                    String specialRequests = schedule.getSpecialRequests();
                    schedule.setSpecialRequests(specialRequests != null ? 
                        specialRequests + " | 从酒店接客：" + hotelName : 
                        "从酒店接客：" + hotelName);
                    needUpdate = true;
                }
                // 中间日期：从酒店接客，送回酒店
                else if (tourDate.isAfter(checkInDate) && tourDate.isBefore(checkOutDate)) {
                    schedule.setPickupLocation(hotelAddress);
                    schedule.setDropoffLocation(hotelAddress);
                    String specialRequests = schedule.getSpecialRequests();
                    schedule.setSpecialRequests(specialRequests != null ? 
                        specialRequests + " | 酒店接送：" + hotelName : 
                        "酒店接送：" + hotelName);
                    needUpdate = true;
                }
                
                // 更新排团记录
                if (needUpdate) {
                    tourScheduleOrderMapper.update(schedule);
                    log.info("已更新排团记录接送信息，记录ID：{}，日期：{}", schedule.getId(), tourDate);
                }
            }
            
            log.info("酒店接送信息同步完成，酒店预订ID：{}", hotelBooking.getId());
            
        } catch (Exception e) {
            log.error("同步酒店接送信息到排团表失败，酒店预订ID：{}", hotelBooking.getId(), e);
            // 不抛出异常，避免影响酒店预订确认流程
        }
    }

    @Override
    @Transactional
    public Boolean checkIn(Integer id) {
        log.info("办理入住：{}", id);
        hotelBookingMapper.updateBookingStatus(id, "checked_in");
        return true;
    }

    @Override
    @Transactional
    public Boolean checkOut(Integer id) {
        log.info("办理退房：{}", id);
        hotelBookingMapper.updateBookingStatus(id, "checked_out");
        return true;
    }

    @Override
    @Transactional
    public Boolean updateBookingStatus(Integer id, String status) {
        log.info("更新预订状态：{}, {}", id, status);
        hotelBookingMapper.updateBookingStatus(id, status);
        return true;
    }

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, String status, String guestName, String guestPhone,
                               Integer hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        log.info("分页查询酒店预订列表");
        
        // 设置分页参数
        PageHelper.startPage(page, pageSize);
        
        // 执行查询
        Page<HotelBookingVO> pageResult = (Page<HotelBookingVO>) hotelBookingMapper.pageQuery(status, guestName, guestPhone, hotelId, checkInDate, checkOutDate);
        
        // 返回分页结果
        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    public List<HotelBookingVO> getByAgentId(Integer agentId) {
        log.info("根据代理商ID查询酒店预订列表：{}", agentId);
        return hotelBookingMapper.getByAgentId(agentId);
    }

    @Override
    public Boolean checkRoomAvailability(Integer hotelId, Integer roomTypeId, 
                                        LocalDate checkInDate, LocalDate checkOutDate, 
                                        Integer roomCount) {
        log.info("检查酒店房间可用性");
        return hotelBookingMapper.checkRoomAvailability(hotelId, roomTypeId, checkInDate, checkOutDate, roomCount);
    }

    @Override
    @Transactional
    public Boolean updatePaymentStatus(Integer id, String paymentStatus) {
        log.info("更新支付状态：{}, {}", id, paymentStatus);
        hotelBookingMapper.updatePaymentStatus(id, paymentStatus);
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteBooking(Integer id) {
        log.info("删除酒店预订：{}", id);
        hotelBookingMapper.deleteById(id);
        return true;
    }

    @Override
    public List<HotelBooking> getByAssignmentId(Integer assignmentId) {
        log.info("根据导游车辆分配ID查询酒店预订列表：{}", assignmentId);
        return hotelBookingMapper.getByAssignmentId(assignmentId);
    }

    @Override
    @Transactional
    public Integer batchCreateBookingsFromAssignment(Integer assignmentId, Integer hotelId, Integer roomTypeId) {
        log.info("批量创建酒店预订，分配ID：{}，酒店ID：{}，房型ID：{}", assignmentId, hotelId, roomTypeId);
        
        // 查询导游车辆分配记录
        TourGuideVehicleAssignmentVO assignmentVO = tourGuideVehicleAssignmentMapper.getById(assignmentId.longValue());
        if (assignmentVO == null) {
            throw new RuntimeException("导游车辆分配记录不存在");
        }
        
        // 解析关联的排团记录ID列表
        List<Long> scheduleOrderIdList = assignmentVO.getTourScheduleOrderIds();
        if (scheduleOrderIdList == null || scheduleOrderIdList.isEmpty()) {
            throw new RuntimeException("导游车辆分配记录中没有关联的排团记录");
        }
        
        int createdCount = 0;
        
        for (Long orderIdLong : scheduleOrderIdList) {
            try {
                Integer scheduleOrderId = orderIdLong.intValue();
                
                // 检查是否已存在酒店预订
                HotelBooking existingBooking = hotelBookingMapper.getByScheduleOrderId(scheduleOrderId);
                if (existingBooking == null) {
                    // 创建酒店预订
                    Integer bookingId = createBookingFromScheduleOrder(scheduleOrderId, hotelId, roomTypeId);
                    if (bookingId != null) {
                        // 更新酒店预订的分配ID
                        HotelBooking hotelBooking = hotelBookingMapper.getById(bookingId);
                        hotelBooking.setAssignmentId(assignmentId);
                        hotelBookingMapper.update(hotelBooking);
                        createdCount++;
                    }
                }
            } catch (Exception e) {
                log.warn("处理排团记录ID失败：{}", orderIdLong, e);
            }
        }
        
        log.info("批量创建酒店预订完成，共创建{}个预订", createdCount);
        return createdCount;
    }

    @Override
    @Transactional
    public Boolean sendBookingEmail(com.sky.dto.HotelBookingEmailDTO emailDTO) {
        log.info("发送酒店预订邮件：{}", emailDTO);
        
        try {
            // 获取当前登录员工信息
            Long currentEmployeeId = null;
            String currentEmployeeName = "System";
            
            try {
                currentEmployeeId = BaseContext.getCurrentId();
                currentEmployeeName = BaseContext.getCurrentUsername();
                log.info("当前操作员：ID={}, 姓名={}", currentEmployeeId, currentEmployeeName);
            } catch (Exception e) {
                log.warn("无法获取当前登录员工信息，使用默认值", e);
                currentEmployeeId = 1L; // 默认员工ID
            }
            
            // 1. 立即更新预订状态为邮件发送中
            hotelBookingMapper.updateBookingStatus(emailDTO.getBookingId(), "email_sending");
            
            // 2. 记录邮件发送信息
            hotelBookingMapper.updateEmailSentInfo(
                emailDTO.getBookingId(),
                emailDTO.getTo(),
                emailDTO.getContent(),
                currentEmployeeId
            );
            
            // 3. 通过WebSocket通知前端邮件开始发送
            notifyEmailStatus(currentEmployeeId, emailDTO.getBookingId(), "sending", "邮件正在发送中...", null);
            
            // 4. 异步发送邮件
            sendEmailAsync(emailDTO, currentEmployeeName, currentEmployeeId);
            
            log.info("酒店预订邮件提交发送成功，预订ID：{}, 操作员：{}", emailDTO.getBookingId(), currentEmployeeName);
            return true;
            
        } catch (Exception e) {
            log.error("发送酒店预订邮件失败，预订ID：{}", emailDTO.getBookingId(), e);
            
            // 通过WebSocket通知前端邮件发送失败
            try {
                Long currentEmployeeId = BaseContext.getCurrentId();
                notifyEmailStatus(currentEmployeeId, emailDTO.getBookingId(), "failed", "邮件发送失败", e.getMessage());
            } catch (Exception wsException) {
                log.warn("无法发送WebSocket通知", wsException);
            }
            
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 异步发送邮件
     */
    @Async("emailTaskExecutor")
    public void sendEmailAsync(com.sky.dto.HotelBookingEmailDTO emailDTO, String operatorName, Long operatorId) {
        try {
            log.info("开始异步发送邮件到：{}", emailDTO.getTo());
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("Tom.zhang@htas.com.au");
            helper.setTo(emailDTO.getTo());
            helper.setSubject(emailDTO.getSubject());
            helper.setText(emailDTO.getContent(), false); // 纯文本邮件
            
            javaMailSender.send(message);
            log.info("✅ 邮件发送成功 - 收件人：{}, 操作员：{}", emailDTO.getTo(), operatorName);
            
            // 更新预订状态为已发送
            hotelBookingMapper.updateBookingStatus(emailDTO.getBookingId(), "email_sent");
            
            // 通过WebSocket通知前端邮件发送成功
            notifyEmailStatus(operatorId, emailDTO.getBookingId(), "success", "邮件发送成功", null);
            
        } catch (Exception mailException) {
            log.error("❌ 异步邮件发送失败 - 收件人：{}, 错误：{}", emailDTO.getTo(), mailException.getMessage(), mailException);
            
            // 更新预订状态为发送失败
            try {
                hotelBookingMapper.updateBookingStatus(emailDTO.getBookingId(), "email_failed");
            } catch (Exception dbException) {
                log.error("更新邮件失败状态到数据库失败", dbException);
            }
            
            // 通过WebSocket通知前端邮件发送失败
            notifyEmailStatus(operatorId, emailDTO.getBookingId(), "failed", "邮件发送失败", mailException.getMessage());
        }
    }
    
    /**
     * 通过WebSocket通知邮件发送状态
     */
    private void notifyEmailStatus(Long operatorId, Integer bookingId, String status, String message, String error) {
        try {
            // 创建数据Map（Java 8兼容写法）
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("bookingId", bookingId);
            data.put("status", status);
            data.put("message", message);
            data.put("error", error != null ? error : "");
            
            // 导入AdminWebSocketServer类
            com.sky.webSocket.AdminWebSocketServer.sendMessage(operatorId, 
                com.sky.webSocket.AdminWebSocketServer.createNotificationMessage(
                    "email_status", 
                    message, 
                    data
                )
            );
            log.info("✅ 通过WebSocket发送邮件状态通知 - 操作员：{}, 预订：{}, 状态：{}", operatorId, bookingId, status);
        } catch (Exception e) {
            log.warn("⚠️ WebSocket通知发送失败：{}", e.getMessage());
        }
    }
} 