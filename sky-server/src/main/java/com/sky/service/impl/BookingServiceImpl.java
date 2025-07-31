package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.BookingDTO;
import com.sky.entity.TourBooking;
import com.sky.exception.CustomException;
import com.sky.mapper.BookingMapper;
import com.sky.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预订服务实现类
 */
@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingMapper bookingMapper;

    /**
     * 创建预订
     * @param bookingDTO 预订信息
     * @return 预订ID
     */
    @Override
    @Transactional
    public Integer createBooking(BookingDTO bookingDTO) {
        // 设置用户ID
        Long currentId = BaseContext.getCurrentId();
        bookingDTO.setUserId(currentId.intValue());
        
        // 设置初始状态
        bookingDTO.setStatus("pending");
        bookingDTO.setPaymentStatus("unpaid");
        
        // 检查可用性
        boolean isAvailable = checkAvailabilityInternal(bookingDTO);
        if (!isAvailable) {
            throw new CustomException("所选日期已满，请选择其他日期");
        }
        
        // 更新可用名额
        updateAvailability(bookingDTO);
        
        // 创建预订
        bookingMapper.insert(bookingDTO);
        
        return bookingDTO.getId();
    }

    /**
     * 获取用户预订列表
     * @return 预订列表
     */
    @Override
    public List<BookingDTO> getUserBookings() {
        Long currentId = BaseContext.getCurrentId();
        return bookingMapper.getByUserId(currentId.intValue());
    }

    /**
     * 根据ID获取预订详情
     * @param id 预订ID
     * @return 预订详情
     */
    @Override
    public TourBooking getBookingById(Integer id) {
        log.info("开始获取预订详情, 预订ID: {}", id);
        TourBooking booking = bookingMapper.getById(id);
        log.info("获取到预订详情: {}", booking);
        return booking;
    }

    /**
     * 取消预订
     * @param id 预订ID
     */
    @Override
    @Transactional
    public void cancelBooking(Integer id) {
        // 检查预订是否存在
        TourBooking booking = bookingMapper.getById(id);
        if (booking == null) {
            throw new CustomException("预订不存在");
        }
        
        // 检查预订是否属于当前用户
        Long currentId = BaseContext.getCurrentId();
        if (!booking.getUserId().equals(currentId)) {
            throw new CustomException("无权操作此预订");
        }
        
        // 检查预订状态
        if ("cancelled".equals(booking.getStatus())) {
            throw new CustomException("预订已取消");
        }
        
        // 取消预订
        bookingMapper.cancel(id);
        
        // 恢复可用名额
        // 这里简化处理，实际应该根据预订信息恢复可用名额
    }

    /**
     * 检查可用性
     * @param params 查询参数
     * @return 可用性信息
     */
    @Override
    public Map<String, Object> checkAvailability(Map<String, Object> params) {
        String tourType = (String) params.get("tourType");
        Integer tourId = Integer.parseInt(params.get("tourId").toString());
        LocalDate date = LocalDate.parse(params.get("date").toString());
        Integer adults = Integer.parseInt(params.get("adults").toString());
        Integer children = params.getOrDefault("children", 0) != null ? Integer.parseInt(params.get("children").toString()) : 0;
        
        Integer totalCount = adults + children;
        Integer availableSlots;
        
        if ("day_tour".equals(tourType)) {
            availableSlots = bookingMapper.checkDayTourAvailability(tourId, date);
        } else {
            availableSlots = bookingMapper.checkGroupTourAvailability(tourId, date);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("available", availableSlots != null && availableSlots >= totalCount);
        result.put("availableSlots", availableSlots);
        result.put("requiredSlots", totalCount);
        
        return result;
    }
    
    /**
     * 内部检查可用性
     * @param bookingDTO 预订信息
     * @return 是否可用
     */
    private boolean checkAvailabilityInternal(BookingDTO bookingDTO) {
        String tourType = bookingDTO.getTourType();
        Integer tourId = bookingDTO.getTourId();
        LocalDate date = bookingDTO.getStartDate();
        Integer adults = bookingDTO.getAdults();
        Integer children = bookingDTO.getChildren() != null ? bookingDTO.getChildren() : 0;
        
        Integer totalCount = adults + children;
        Integer availableSlots;
        
        if ("day_tour".equals(tourType)) {
            availableSlots = bookingMapper.checkDayTourAvailability(tourId, date);
        } else {
            availableSlots = bookingMapper.checkGroupTourAvailability(tourId, date);
        }
        
        return availableSlots != null && availableSlots >= totalCount;
    }
    
    /**
     * 更新可用名额
     * @param bookingDTO 预订信息
     */
    private void updateAvailability(BookingDTO bookingDTO) {
        String tourType = bookingDTO.getTourType();
        Integer tourId = bookingDTO.getTourId();
        LocalDate date = bookingDTO.getStartDate();
        Integer adults = bookingDTO.getAdults();
        Integer children = bookingDTO.getChildren() != null ? bookingDTO.getChildren() : 0;
        
        Integer totalCount = adults + children;
        
        if ("day_tour".equals(tourType)) {
            bookingMapper.updateDayTourAvailability(tourId, date, totalCount);
        } else {
            bookingMapper.updateGroupTourAvailability(tourId, date, totalCount);
        }
    }
} 