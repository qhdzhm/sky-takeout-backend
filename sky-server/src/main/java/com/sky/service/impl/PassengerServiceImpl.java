package com.sky.service.impl;

import com.sky.dto.PassengerDTO;
import com.sky.entity.Passenger;
import com.sky.entity.BookingPassengerRelation;
import com.sky.mapper.PassengerMapper;
import com.sky.service.PassengerService;
import com.sky.vo.PassengerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 乘客管理服务实现类
 */
@Service
@Slf4j
public class PassengerServiceImpl implements PassengerService {

    @Autowired
    private PassengerMapper passengerMapper;

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
        
        passengerMapper.insert(passenger);
        log.info("保存乘客成功: passengerId={}", passenger.getPassengerId());
        return passenger.getPassengerId();
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
        
        // 检查乘客是否有效 - 修改判断逻辑，允许儿童只有isChild和childAge
        boolean isValidPassenger = false;
        
        // 如果有姓名，则视为有效
        if (passengerDTO.getFullName() != null && !passengerDTO.getFullName().trim().isEmpty()) {
            isValidPassenger = true;
        }
        
        // 如果是儿童并且有年龄，则视为有效
        if (Boolean.TRUE.equals(passengerDTO.getIsChild()) && passengerDTO.getChildAge() != null && !passengerDTO.getChildAge().trim().isEmpty()) {
            isValidPassenger = true;
            // 如果儿童没有姓名，设置一个默认姓名，避免数据库约束问题
            if (passengerDTO.getFullName() == null || passengerDTO.getFullName().trim().isEmpty()) {
                passengerDTO.setFullName("儿童-" + passengerDTO.getChildAge());
                log.info("为儿童设置默认姓名: {}", passengerDTO.getFullName());
            }
        }
        
        // 如果乘客信息无效，则跳过
        if (!isValidPassenger) {
            log.info("乘客信息无效，跳过录入");
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
        
        return true; // 只要乘客基本信息更新成功，就认为更新成功
    }
} 