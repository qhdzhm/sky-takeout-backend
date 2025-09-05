package com.sky.service.impl;

import com.sky.dto.PassengerDTO;
import com.sky.entity.Passenger;
import com.sky.entity.BookingPassengerRelation;
import com.sky.entity.TourBooking;
import com.sky.mapper.PassengerMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.service.PassengerService;
import com.sky.vo.PassengerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 乘客管理服务实现类
 */
@Service
@Slf4j
public class PassengerServiceImpl implements PassengerService {

    @Autowired
    private PassengerMapper passengerMapper;
    
    @Autowired
    private TourBookingMapper tourBookingMapper;

    /**
     * 根据ID查询乘客
     *
     * @param passengerId 乘客ID
     * @return 乘客信息
     */
    @Override
    public PassengerVO getById(Integer passengerId) {
        Passenger passenger = passengerMapper.getById(passengerId);
        if (passenger == null) {
            return null;
        }
        
        PassengerVO passengerVO = new PassengerVO();
        BeanUtils.copyProperties(passenger, passengerVO);
        
        return passengerVO;
    }

    /**
     * 根据护照号查询乘客
     *
     * @param passportNumber 护照号
     * @return 乘客信息
     */
    @Override
    public PassengerVO getByPassportNumber(String passportNumber) {
        Passenger passenger = passengerMapper.getByPassportNumber(passportNumber);
        if (passenger == null) {
            return null;
        }
        
        PassengerVO passengerVO = new PassengerVO();
        BeanUtils.copyProperties(passenger, passengerVO);
        
        return passengerVO;
    }

    /**
     * 根据订单ID查询乘客列表
     *
     * @param bookingId 订单ID
     * @return 乘客列表
     */
    @Override
    public List<PassengerVO> getByBookingId(Integer bookingId) {
        log.info("开始查询订单ID:{}的乘客信息", bookingId);
        
        List<Passenger> passengers = passengerMapper.getByBookingId(bookingId);
        log.info("从数据库查询到订单ID:{}的乘客数量:{}", bookingId, passengers != null ? passengers.size() : 0);
        
        if (passengers == null || passengers.isEmpty()) {
            log.warn("订单ID:{}没有关联的乘客信息", bookingId);
            return new ArrayList<>();
        }
        
        // 详细记录每个乘客的信息
        for (Passenger passenger : passengers) {
            log.info("乘客详细信息 - ID:{}, 姓名:{}, 电话:{}, 微信ID:{}, isChild:{}", 
                     passenger.getPassengerId(), 
                     passenger.getFullName(),
                     passenger.getPhone(),
                     passenger.getWechatId(),
                     passenger.getIsChild());
        }
        
        List<PassengerVO> passengerVOs = new ArrayList<>();
        for (Passenger passenger : passengers) {
            PassengerVO passengerVO = new PassengerVO();
            BeanUtils.copyProperties(passenger, passengerVO);
            
            // 获取关联信息并设置到VO中
            BookingPassengerRelation relation = passengerMapper.getRelation(bookingId, passenger.getPassengerId());
            if (relation != null) {
                passengerVO.setIsPrimary(relation.getIsPrimary());
                passengerVO.setTicketNumber(relation.getTicketNumber());
                passengerVO.setSeatNumber(relation.getSeatNumber());
                passengerVO.setLuggageTags(relation.getLuggageTags());
                passengerVO.setCheckInStatus(relation.getCheckInStatus());
            }
            
            // 记录最终生成的VO对象 (不使用可能不存在的getter方法)
            log.info("转换后的乘客VO - ID:{}, 姓名:{}, 原始电话:{}, 原始微信ID:{}", 
                     passengerVO.getPassengerId(), 
                     passengerVO.getFullName(),
                     passenger.getPhone(),
                     passenger.getWechatId());
            
            passengerVOs.add(passengerVO);
        }
        
        log.info("返回订单ID:{}的乘客VO列表，共{}条记录", bookingId, passengerVOs.size());
        return passengerVOs;
    }

    /**
     * 保存乘客信息
     *
     * @param passengerDTO 乘客信息
     * @return 乘客ID
     */
    @Override
    @Transactional
    public Integer save(PassengerDTO passengerDTO) {
        // 打印传入的DTO详细信息
        log.info("开始保存乘客信息，传入的DTO: {}", passengerDTO);
        
        // 先查询是否已存在相同护照号的乘客
        if (passengerDTO.getPassportNumber() != null && !passengerDTO.getPassportNumber().isEmpty()) {
            Passenger existingPassenger = passengerMapper.getByPassportNumber(passengerDTO.getPassportNumber());
            if (existingPassenger != null) {
                // 已存在则更新
                BeanUtils.copyProperties(passengerDTO, existingPassenger);
                existingPassenger.setUpdatedAt(java.time.LocalDateTime.now());
                passengerMapper.update(existingPassenger);
                log.info("更新已存在乘客: passengerId={}, fullName={}, phone={}, wechatId={}",
                        existingPassenger.getPassengerId(), existingPassenger.getFullName(),
                        existingPassenger.getPhone(), existingPassenger.getWechatId());
                return existingPassenger.getPassengerId();
            }
        }
        
        // 不存在则新增
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerDTO, passenger);
        
        // 处理空字符串字段，将空字符串转换为null
        if (passenger.getWechatId() != null && passenger.getWechatId().trim().isEmpty()) {
            passenger.setWechatId(null);
        }
        if (passenger.getEmail() != null && passenger.getEmail().trim().isEmpty()) {
            passenger.setEmail(null);
        }
        if (passenger.getChildAge() != null && passenger.getChildAge().trim().isEmpty()) {
            passenger.setChildAge(null);
        }
        
        // 手动检查并设置关键字段，解决字段名不匹配问题
        // 注意：由于前端传入的可能是phoneNumber而不是phone
        try {
            // 尝试通过反射获取前端可能传入的不同名称的字段
            java.lang.reflect.Field phoneField = passengerDTO.getClass().getDeclaredField("phone");
            if (phoneField != null) {
                phoneField.setAccessible(true);
                Object phoneValue = phoneField.get(passengerDTO);
                if (phoneValue != null) {
                    passenger.setPhone(phoneValue.toString());
                    log.info("通过反射设置乘客phone: {}", phoneValue);
                }
            }
        } catch (Exception e) {
            log.debug("尝试通过反射获取phone字段失败: {}", e.getMessage());
        }
        
        try {
            java.lang.reflect.Field wechatIdField = passengerDTO.getClass().getDeclaredField("wechatId");
            if (wechatIdField != null) {
                wechatIdField.setAccessible(true);
                Object wechatIdValue = wechatIdField.get(passengerDTO);
                if (wechatIdValue != null) {
                    passenger.setWechatId(wechatIdValue.toString());
                    log.info("通过反射设置乘客wechatId: {}", wechatIdValue);
                }
            }
        } catch (Exception e) {
            log.debug("尝试通过反射获取wechatId字段失败: {}", e.getMessage());
        }
        
        try {
            java.lang.reflect.Field isChildField = passengerDTO.getClass().getDeclaredField("isChild");
            if (isChildField != null) {
                isChildField.setAccessible(true);
                Object isChildValue = isChildField.get(passengerDTO);
                if (isChildValue != null) {
                    passenger.setIsChild((Boolean)isChildValue);
                    log.info("通过反射设置乘客isChild: {}", isChildValue);
                }
            }
        } catch (Exception e) {
            log.debug("尝试通过反射获取isChild字段失败: {}", e.getMessage());
        }
        
        // 设置创建和更新时间
        passenger.setCreatedAt(java.time.LocalDateTime.now());
        passenger.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 打印整个对象进行调试
        log.info("新增乘客信息: {}", passenger);
        
        try {
            int result = passengerMapper.insert(passenger);
            log.info("插入操作影响行数: {}, 生成的passengerId: {}", result, passenger.getPassengerId());
            
            if (result > 0) {
                // 如果主键回填失败，尝试通过护照号查询获取ID
                if (passenger.getPassengerId() == null && passenger.getPassportNumber() != null) {
                    log.warn("主键回填失败，尝试通过护照号查询获取ID");
                    Passenger savedPassenger = passengerMapper.getByPassportNumber(passenger.getPassportNumber());
                    if (savedPassenger != null) {
                        passenger.setPassengerId(savedPassenger.getPassengerId());
                        log.info("通过护照号查询获取到passengerId: {}", passenger.getPassengerId());
                    }
                }
                
                // 如果还是没有ID，尝试通过姓名和电话查询
                if (passenger.getPassengerId() == null && passenger.getFullName() != null && passenger.getPhone() != null) {
                    log.warn("仍然没有获取到ID，尝试通过姓名和电话查询");
                    Passenger savedPassenger = passengerMapper.getByPhone(passenger.getPhone());
                    if (savedPassenger != null && savedPassenger.getFullName().equals(passenger.getFullName())) {
                        passenger.setPassengerId(savedPassenger.getPassengerId());
                        log.info("通过电话查询获取到passengerId: {}", passenger.getPassengerId());
                    }
                }
                
                if (passenger.getPassengerId() != null) {
                    log.info("保存乘客成功: passengerId={}", passenger.getPassengerId());
                    return passenger.getPassengerId();
                } else {
                    log.error("插入乘客后无法获取ID: 影响行数={}, passengerId={}", result, passenger.getPassengerId());
                    throw new RuntimeException("插入乘客后无法获取ID");
                }
            } else {
                log.error("插入乘客失败: 影响行数={}", result);
                throw new RuntimeException("插入乘客失败");
            }
        } catch (Exception e) {
            log.error("插入乘客时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("插入乘客失败: " + e.getMessage());
        }
    }

    /**
     * 更新乘客信息
     *
     * @param passengerDTO 乘客信息
     * @return 是否成功
     */
    @Override
    public Boolean update(PassengerDTO passengerDTO) {
        if (passengerDTO.getPassengerId() == null) {
            log.error("更新乘客信息时乘客ID不能为空");
            return false;
        }
        
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerDTO, passenger);
        
        int result = passengerMapper.update(passenger);
        return result > 0;
    }

    /**
     * 删除乘客信息
     *
     * @param passengerId 乘客ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean deleteById(Integer passengerId) {
        int result = passengerMapper.deleteById(passengerId);
        return result > 0;
    }

    /**
     * 删除乘客信息（兼容旧方法）
     *
     * @param passengerId 乘客ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean delete(Integer passengerId) {
        return deleteById(passengerId);
    }

    /**
     * 添加乘客到订单
     *
     * @param bookingId    订单ID
     * @param passengerDTO 乘客信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean addPassengerToBooking(Integer bookingId, PassengerDTO passengerDTO) {
        // 打印原始DTO数据以便调试
        log.info("添加乘客到订单的原始DTO数据: {}", passengerDTO);
        log.info("🔍 详细DTO字段检查:");
        log.info("  - fullName: '{}' (是否为null: {})", passengerDTO.getFullName(), passengerDTO.getFullName() == null);
        log.info("  - phone: '{}' (是否为null: {})", passengerDTO.getPhone(), passengerDTO.getPhone() == null);
        log.info("  - isPrimary: '{}' (是否为null: {})", passengerDTO.getIsPrimary(), passengerDTO.getIsPrimary() == null);
        log.info("  - isChild: '{}' (是否为null: {})", passengerDTO.getIsChild(), passengerDTO.getIsChild() == null);
        log.info("  - wechatId: '{}' (是否为null: {})", passengerDTO.getWechatId(), passengerDTO.getWechatId() == null);
        
        // 检查乘客是否有效 - 修改判断逻辑，允许只有联系方式的乘客
        boolean isValidPassenger = false;
        
        // 如果有姓名，则视为有效
        if (passengerDTO.getFullName() != null && !passengerDTO.getFullName().trim().isEmpty()) {
            isValidPassenger = true;
        }
        
        // 如果有电话号码但没有姓名，记录警告但不自动设置姓名
        if (passengerDTO.getPhone() != null && !passengerDTO.getPhone().trim().isEmpty()) {
            if (passengerDTO.getFullName() != null && !passengerDTO.getFullName().trim().isEmpty()) {
                // 有姓名和电话，正常情况
                isValidPassenger = true;
                log.info("乘客信息完整: 姓名='{}', 电话='{}'", passengerDTO.getFullName(), passengerDTO.getPhone());
            } else {
                // 只有电话没有姓名，这是异常情况，应该修复前端传递问题
                log.warn("⚠️ 乘客只有电话号码没有姓名，这通常表示前端数据传递有问题: phone='{}', fullName=null", passengerDTO.getPhone());
                // 不自动设置姓名，而是跳过此乘客
                log.warn("⚠️ 跳过此乘客，等待前端修复数据传递问题");
                return false;
            }
        }
        
        // 如果有微信号，则视为有效
        if (passengerDTO.getWechatId() != null && !passengerDTO.getWechatId().trim().isEmpty()) {
            isValidPassenger = true;
            // 如果没有姓名，设置一个默认姓名
            if (passengerDTO.getFullName() == null || passengerDTO.getFullName().trim().isEmpty()) {
                passengerDTO.setFullName(passengerDTO.getWechatId());
                log.info("为只有微信号的乘客设置默认姓名: {}", passengerDTO.getFullName());
            }
        }
        
        // 如果是儿童并且有年龄，则视为有效
        if (Boolean.TRUE.equals(passengerDTO.getIsChild()) && passengerDTO.getChildAge() != null && !passengerDTO.getChildAge().trim().isEmpty()) {
            isValidPassenger = true;
            // 如果儿童没有姓名，设置一个默认姓名，避免数据库约束问题
            if (passengerDTO.getFullName() == null || passengerDTO.getFullName().trim().isEmpty()) {
                passengerDTO.setFullName("儿童" + passengerDTO.getChildAge() + "岁");
                log.info("为儿童设置默认姓名: {}", passengerDTO.getFullName());
            }
        }
        
        // 如果乘客信息无效，则跳过
        if (!isValidPassenger) {
            log.info("乘客信息无效，跳过录入。需要至少提供：姓名、电话号码、微信号或儿童年龄中的任意一项");
            return false;
        }
        
        // 先保存乘客信息
        Integer passengerId = save(passengerDTO);
        
        // 查询保存后的乘客信息以验证
        Passenger savedPassenger = passengerMapper.getById(passengerId);
        log.info("保存后的乘客信息: {}", savedPassenger);
        
        // 查询是否已建立关联
        BookingPassengerRelation existingRelation = passengerMapper.getRelation(bookingId, passengerId);
        if (existingRelation != null) {
            // 已存在关联，更新关联信息
            existingRelation.setIsPrimary(passengerDTO.getIsPrimary());
            existingRelation.setTicketNumber(passengerDTO.getTicketNumber());
            existingRelation.setSeatNumber(passengerDTO.getSeatNumber());
            // 不设置行李标签，DTO中没有此字段
            int result = passengerMapper.updateBookingPassengerRelation(existingRelation);
            log.info("更新乘客与订单关联: bookingId={}, passengerId={}, result={}", 
                    bookingId, passengerId, result);
            return result > 0;
        }
        
        // 创建乘客和订单的关联
        BookingPassengerRelation relation = new BookingPassengerRelation();
        relation.setBookingId(bookingId);
        relation.setPassengerId(passengerId);
        relation.setIsPrimary(passengerDTO.getIsPrimary());
        relation.setTicketNumber(passengerDTO.getTicketNumber());
        relation.setSeatNumber(passengerDTO.getSeatNumber());
        // 设置默认行李标签
        relation.setLuggageTags(null);
        relation.setCheckInStatus("not_checked");
        
        log.info("添加乘客到订单: bookingId={}, passengerId={}, isPrimary={}", 
                bookingId, passengerId, relation.getIsPrimary());
                
        int result = passengerMapper.saveBookingPassengerRelation(relation);
        
        // 🆕 自动更新订单人数统计
        if (result > 0) {
            updateBookingPassengerCount(bookingId);
        }
        
        return result > 0;
    }

    /**
     * 从订单中移除乘客
     *
     * @param bookingId   订单ID
     * @param passengerId 乘客ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean removePassengerFromBooking(Integer bookingId, Integer passengerId) {
        int result = passengerMapper.deleteRelation(bookingId, passengerId);
        
        // 🆕 自动更新订单人数统计
        if (result > 0) {
            updateBookingPassengerCount(bookingId);
        }
        
        return result > 0;
    }

    /**
     * 更新乘客在订单中的信息（如座位号、登记状态等）
     *
     * @param bookingId    订单ID
     * @param passengerDTO 乘客信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean updatePassengerBookingInfo(Integer bookingId, PassengerDTO passengerDTO) {
        if (passengerDTO.getPassengerId() == null) {
            log.error("更新乘客订单信息时乘客ID不能为空");
            return false;
        }
        
        log.info("更新乘客ID:{}的信息，传入的DTO: {}", passengerDTO.getPassengerId(), passengerDTO);
        
        // 1. 首先更新乘客基本信息
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerDTO, passenger);
        
        // 设置更新时间
        passenger.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 确保我们只在更新时设置ID
        passenger.setPassengerId(passengerDTO.getPassengerId());
        
        // 记录即将更新的乘客信息
        log.info("即将更新乘客基本信息: passengerId={}, fullName={}, phone={}, wechatId={}, isChild={}",
                passenger.getPassengerId(), passenger.getFullName(),
                passenger.getPhone(), passenger.getWechatId(), passenger.getIsChild());
                
        int passengerResult = passengerMapper.update(passenger);
        if (passengerResult <= 0) {
            log.error("更新乘客基本信息失败: {}", passenger.getPassengerId());
            return false;
        }
        
        // 2. 然后更新关联信息
        // 查询关联信息
        BookingPassengerRelation relation = passengerMapper.getRelation(bookingId, passengerDTO.getPassengerId());
        if (relation == null) {
            log.error("乘客 {} 不在订单 {} 中", passengerDTO.getPassengerId(), bookingId);
            return false;
        }
        
        // 更新关联信息
        if (passengerDTO.getIsPrimary() != null) {
            relation.setIsPrimary(passengerDTO.getIsPrimary());
        }
        if (passengerDTO.getTicketNumber() != null) {
            relation.setTicketNumber(passengerDTO.getTicketNumber());
        }
        if (passengerDTO.getSeatNumber() != null) {
            relation.setSeatNumber(passengerDTO.getSeatNumber());
        }
        if (passengerDTO.getLuggageTags() != null) {
            relation.setLuggageTags(passengerDTO.getLuggageTags());
        }
        if (passengerDTO.getCheckInStatus() != null) {
            relation.setCheckInStatus(passengerDTO.getCheckInStatus());
        }
        
        int relationResult = passengerMapper.updateBookingPassengerRelation(relation);
        log.info("更新乘客关联信息结果: {}", relationResult > 0 ? "成功" : "失败");
        
        // 🆕 自动更新订单人数统计（因为乘客的isChild属性可能改变）
        updateBookingPassengerCount(bookingId);
        
        return true; // 只要乘客基本信息更新成功，就认为更新成功
    }
    
    /**
     * 🆕 自动更新订单的乘客人数统计
     * 根据passengers表和booking_passenger_relation表的实际数据重新计算并更新订单的adultCount和childCount
     * 
     * @param bookingId 订单ID
     */
    @Transactional
    private void updateBookingPassengerCount(Integer bookingId) {
        try {
            log.info("🔄 开始更新订单{}的乘客人数统计", bookingId);
            
            // 1. 获取该订单的所有乘客
            List<Passenger> passengers = passengerMapper.getByBookingId(bookingId);
            
            // 2. 统计成人和儿童数量
            int adultCount = 0;
            int childCount = 0;
            
            if (passengers != null) {
                for (Passenger passenger : passengers) {
                    if (passenger != null && passenger.getFullName() != null && !passenger.getFullName().trim().isEmpty()) {
                        if (Boolean.TRUE.equals(passenger.getIsChild())) {
                            childCount++;
                        } else {
                            adultCount++;
                        }
                    }
                }
            }
            
            log.info("📊 订单{}重新计算人数 - 成人: {}, 儿童: {}", bookingId, adultCount, childCount);
            
            // 3. 更新订单表的人数字段
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking != null) {
                // 记录更新前的数据
                Integer oldAdultCount = tourBooking.getAdultCount();
                Integer oldChildCount = tourBooking.getChildCount();
                
                // 更新人数
                tourBooking.setAdultCount(adultCount);
                tourBooking.setChildCount(childCount);
                tourBooking.setGroupSize(adultCount + childCount); // 同时更新团队规模
                tourBooking.setUpdatedAt(java.time.LocalDateTime.now());
                
                // 保存到数据库
                tourBookingMapper.update(tourBooking);
                
                log.info("✅ 订单{}人数统计更新完成 - 成人: {} -> {}, 儿童: {} -> {}, 总人数: {}", 
                        bookingId, oldAdultCount, adultCount, oldChildCount, childCount, adultCount + childCount);
            } else {
                log.warn("⚠️ 未找到订单ID为{}的订单记录", bookingId);
            }
            
        } catch (Exception e) {
            log.error("❌ 更新订单{}的乘客人数统计失败: {}", bookingId, e.getMessage(), e);
            // 不抛出异常，避免影响主要业务流程
        }
    }
    
    /**
     * 🆕 修复所有订单的乘客人数统计
     * 批量处理所有订单，根据实际乘客数据重新计算人数
     */
    @Override
    @Transactional
    public Integer fixAllBookingPassengerCounts() {
        log.info("🚀 开始批量修复所有订单的乘客人数统计");
        
        int fixedCount = 0;
        
        try {
            // 1. 获取所有订单ID
            List<Integer> allBookingIds = tourBookingMapper.getAllBookingIds();
            
            if (allBookingIds == null || allBookingIds.isEmpty()) {
                log.info("📝 没有找到需要修复的订单");
                return 0;
            }
            
            log.info("📊 找到{}个订单需要检查和修复", allBookingIds.size());
            
            // 2. 逐个修复每个订单的人数统计
            for (Integer bookingId : allBookingIds) {
                try {
                    // 获取修复前的数据
                    TourBooking beforeBooking = tourBookingMapper.getById(bookingId);
                    Integer oldAdultCount = beforeBooking != null ? beforeBooking.getAdultCount() : null;
                    Integer oldChildCount = beforeBooking != null ? beforeBooking.getChildCount() : null;
                    
                    // 修复人数统计
                    updateBookingPassengerCount(bookingId);
                    
                    // 获取修复后的数据进行对比
                    TourBooking afterBooking = tourBookingMapper.getById(bookingId);
                    if (afterBooking != null) {
                        Integer newAdultCount = afterBooking.getAdultCount();
                        Integer newChildCount = afterBooking.getChildCount();
                        
                        // 检查是否有变化
                        boolean hasChanged = false;
                        if (!java.util.Objects.equals(oldAdultCount, newAdultCount) || 
                            !java.util.Objects.equals(oldChildCount, newChildCount)) {
                            hasChanged = true;
                            fixedCount++;
                            log.info("🔧 订单{}修复完成 - 成人: {} -> {}, 儿童: {} -> {}", 
                                    bookingId, oldAdultCount, newAdultCount, oldChildCount, newChildCount);
                        }
                        
                        if (!hasChanged) {
                            log.debug("✅ 订单{}数据正确，无需修复", bookingId);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("❌ 修复订单{}失败: {}", bookingId, e.getMessage(), e);
                    // 继续处理下一个订单
                }
            }
            
            log.info("🎉 批量修复完成！共检查{}个订单，实际修复{}个订单", allBookingIds.size(), fixedCount);
            
        } catch (Exception e) {
            log.error("❌ 批量修复过程发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("批量修复失败: " + e.getMessage());
        }
        
        return fixedCount;
    }
    
    /**
     * 🆕 清理重复乘客数据
     * 识别并清理因接口重复调用导致的重复乘客记录
     */
    @Override
    @Transactional
    public Integer cleanDuplicatePassengers() {
        log.info("🚀 开始清理重复乘客数据");
        
        int cleanedCount = 0;
        
        try {
            // 1. 获取所有订单ID
            List<Integer> allBookingIds = tourBookingMapper.getAllBookingIds();
            
            if (allBookingIds == null || allBookingIds.isEmpty()) {
                log.info("📝 没有找到需要检查的订单");
                return 0;
            }
            
            log.info("📊 开始检查{}个订单的重复乘客数据", allBookingIds.size());
            
            // 2. 逐个检查每个订单的重复乘客
            for (Integer bookingId : allBookingIds) {
                try {
                    // 获取该订单的所有乘客
                    List<Passenger> passengers = passengerMapper.getByBookingId(bookingId);
                    
                    if (passengers == null || passengers.size() <= 1) {
                        continue; // 没有乘客或只有一个乘客，无需检查
                    }
                    
                    // 3. 识别重复乘客（相同姓名和电话号码的）
                    Map<String, List<Passenger>> duplicateGroups = new HashMap<>();
                    
                    for (Passenger passenger : passengers) {
                        String key = (passenger.getFullName() != null ? passenger.getFullName() : "unknown") + 
                                   "_" + (passenger.getPhone() != null ? passenger.getPhone() : "unknown");
                        
                        duplicateGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(passenger);
                    }
                    
                    // 4. 处理重复组，保留最新的记录，删除旧的
                    for (Map.Entry<String, List<Passenger>> entry : duplicateGroups.entrySet()) {
                        List<Passenger> duplicates = entry.getValue();
                        
                        if (duplicates.size() > 1) {
                            log.info("🔍 发现订单{}的重复乘客: {} ({}条记录)", 
                                    bookingId, entry.getKey(), duplicates.size());
                            
                            // 按创建时间排序，保留最新的
                            duplicates.sort((a, b) -> {
                                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                                if (a.getCreatedAt() == null) return -1;
                                if (b.getCreatedAt() == null) return 1;
                                return b.getCreatedAt().compareTo(a.getCreatedAt());
                            });
                            
                            // 保留第一个（最新的），删除其他
                            for (int i = 1; i < duplicates.size(); i++) {
                                Passenger duplicatePassenger = duplicates.get(i);
                                
                                try {
                                    // 删除关联关系
                                    int relationResult = passengerMapper.deleteRelation(bookingId, duplicatePassenger.getPassengerId());
                                    
                                    // 删除乘客记录（如果没有其他订单关联）
                                    List<BookingPassengerRelation> otherRelations = passengerMapper.getPassengerRelations(duplicatePassenger.getPassengerId());
                                    if (otherRelations == null || otherRelations.isEmpty()) {
                                        int deleteResult = passengerMapper.deleteById(duplicatePassenger.getPassengerId());
                                        log.info("🗑️  删除重复乘客记录: ID={}, 姓名={}", 
                                                duplicatePassenger.getPassengerId(), duplicatePassenger.getFullName());
                                    }
                                    
                                    cleanedCount++;
                                    
                                } catch (Exception e) {
                                    log.error("❌ 删除重复乘客{}失败: {}", duplicatePassenger.getPassengerId(), e.getMessage());
                                }
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("❌ 处理订单{}的重复乘客时发生异常: {}", bookingId, e.getMessage(), e);
                }
            }
            
            log.info("✅ 重复乘客数据清理完成，共清理了{}条重复记录", cleanedCount);
            
            // 5. 重新修复人数统计
            if (cleanedCount > 0) {
                log.info("🔄 重新修复订单人数统计...");
                fixAllBookingPassengerCounts();
            }
            
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("❌ 清理重复乘客数据时发生异常: {}", e.getMessage(), e);
            return 0;
        }
    }
} 