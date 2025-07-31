package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.PaymentPageQueryDTO;
import com.sky.entity.CreditTransaction;
import com.sky.entity.AgentCredit;
import com.sky.entity.TourBooking;
import com.sky.exception.CustomException;
import com.sky.mapper.AgentCreditMapper;
import com.sky.mapper.BookingMapper;
import com.sky.mapper.CreditTransactionMapper;
import com.sky.mapper.PaymentMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.result.PageResult;
import com.sky.service.PaymentService;
import com.sky.service.AgentCreditService;
import com.sky.service.TourBookingService;
import com.sky.service.NotificationService;
import com.sky.vo.CreditPaymentResultVO;
import com.sky.vo.PriceDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现类
 */
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private BookingMapper bookingMapper;
    
    @Autowired
    private AgentCreditMapper agentCreditMapper;
    
    @Autowired
    private CreditTransactionMapper creditTransactionMapper;

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private AgentCreditService agentCreditService;

    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 创建支付
     * @param paymentDTO 支付信息
     * @return 支付ID
     */
    @Override
    @Transactional
    public Integer createPayment(PaymentDTO paymentDTO) {
        // 设置用户ID
        Long currentId = BaseContext.getCurrentId();
        paymentDTO.setUserId(currentId.intValue());
        
        // 设置支付订单号
        if (paymentDTO.getPaymentOrderNo() == null) {
            paymentDTO.setPaymentOrderNo(generatePaymentOrderNo());
        }
        
        // 设置初始状态
        paymentDTO.setStatus("pending");
        
        // 设置支付类型
        if (paymentDTO.getType() == null) {
            paymentDTO.setType("payment");
        }
        
        // 设置信用支付相关字段默认值
        if (paymentDTO.getIsCreditPayment() == null) {
            paymentDTO.setIsCreditPayment(false);
        }
        
        // 设置创建时间
        paymentDTO.setCreateTime(LocalDateTime.now());
        
        // 创建支付记录
        paymentMapper.insert(paymentDTO);
        
        return paymentDTO.getId();
    }

    /**
     * 根据ID获取支付详情
     * @param id 支付ID
     * @return 支付详情
     */
    @Override
    public PaymentDTO getPaymentById(Integer id) {
        return paymentMapper.getById(id);
    }

    /**
     * 根据预订ID获取支付列表
     * @param bookingId 预订ID
     * @return 支付列表
     */
    @Override
    public List<PaymentDTO> getPaymentsByBookingId(Integer bookingId) {
        return paymentMapper.getByBookingId(bookingId);
    }
    
    /**
     * 更新支付状态
     * @param id 支付ID
     * @param status 状态
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updatePaymentStatus(Integer id, String status) {
        log.info("更新支付状态，ID：{}，状态：{}", id, status);
        
        // 获取支付记录
        PaymentDTO paymentDTO = paymentMapper.getById(id);
        if (paymentDTO == null) {
            throw new CustomException("支付记录不存在");
        }
        
        // 更新支付状态
        int rows = paymentMapper.updateStatus(id, status);
        
        // 如果状态是completed，则更新预订状态为已支付
        if ("completed".equals(status)) {
            paymentMapper.updateBookingPaymentStatus(paymentDTO.getBookingId(), "paid");
            
            // 同时更新预订状态为已确认
            bookingMapper.updateStatus(paymentDTO.getBookingId(), "confirmed");
        }
        
        return rows > 0;
    }
    
    /**
     * 退款处理
     * @param id 原支付ID
     * @param refundDTO 退款信息
     * @return 退款ID
     */
    @Override
    @Transactional
    public Integer refundPayment(Integer id, PaymentDTO refundDTO) {
        log.info("退款处理，支付ID：{}", id);
        
        // 获取原支付记录
        PaymentDTO originalPayment = paymentMapper.getById(id);
        if (originalPayment == null) {
            throw new CustomException("原支付记录不存在");
        }
        
        // 检查原支付状态
        if (!"completed".equals(originalPayment.getStatus())) {
            throw new CustomException("只能对已完成的支付进行退款");
        }
        
        // 设置退款记录
        refundDTO.setBookingId(originalPayment.getBookingId());
        refundDTO.setRelatedPaymentId(id);
        refundDTO.setType("refund");
        refundDTO.setStatus("pending");
        refundDTO.setPaymentMethod(originalPayment.getPaymentMethod());
        refundDTO.setPaymentOrderNo(generatePaymentOrderNo());
        
        Long currentId = BaseContext.getCurrentId();
        refundDTO.setUserId(currentId.intValue());
        refundDTO.setCreateTime(LocalDateTime.now());
        
        // 创建退款记录
        paymentMapper.insert(refundDTO);
        
        // 更新原支付状态为已退款
        paymentMapper.updateStatus(id, "refunded");
        
        // 更新订单支付状态
        // 无论是全额退款还是部分退款，数据库状态都设为"refunded"
        // 但可以在note字段或其他方式记录是否为部分退款
        paymentMapper.updateBookingPaymentStatus(originalPayment.getBookingId(), "refunded");
        
        // 如果是部分退款，可以在日志或其他地方记录
        if (refundDTO.getAmount().compareTo(originalPayment.getAmount()) < 0) {
            log.info("部分退款：订单ID={}, 原金额={}, 退款金额={}", 
                    originalPayment.getBookingId(), 
                    originalPayment.getAmount(), 
                    refundDTO.getAmount());
        }
        
        return refundDTO.getId();
    }
    
    /**
     * 代理商信用额度支付
     * @param creditPaymentDTO 信用额度支付信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean processCreditPayment(CreditPaymentDTO creditPaymentDTO) {
        log.info("代理商信用额度支付：{}", creditPaymentDTO);
        
        // 获取订单信息
        Integer bookingId = creditPaymentDTO.getBookingId().intValue();
        
        // 🔒 添加分布式锁，防止同一订单的并发支付
        String lockKey = "payment_lock_" + bookingId;
        synchronized (lockKey.intern()) {
            // 首先检查订单是否已经支付
            TourBooking existingBooking = tourBookingMapper.getById(bookingId);
            if (existingBooking == null) {
                log.error("订单不存在，订单ID: {}", bookingId);
                throw new CustomException("订单不存在");
            }
            
            if ("paid".equals(existingBooking.getPaymentStatus())) {
                log.warn("⚠️ 订单已支付，拒绝重复支付请求，订单ID: {}", bookingId);
                return true; // 返回true表示支付成功（因为订单已经是支付状态）
            }
            
            log.info("🔒 获取支付锁成功，开始处理订单 {} 的支付", bookingId);
            
            // 获取当前用户信息
            Long currentId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            Long agentId = BaseContext.getCurrentAgentId();
            Long operatorId = BaseContext.getCurrentOperatorId();
            
            // 获取订单实际金额 - 需要根据用户类型决定使用哪个价格
            BigDecimal actualOrderAmount;
            
            if ("agent_operator".equals(userType)) {
                // 操作员支付：使用实际折扣价格（actualPaymentPrice）
                // 这里需要重新计算价格，获取actualPaymentPrice
                // 从订单中获取基本信息
                TourBooking booking = tourBookingMapper.getById(bookingId);
                if (booking == null) {
                    log.error("订单ID {} 不存在", bookingId);
                    throw new CustomException("订单不存在，请联系客服");
                }
                
                // 使用统一价格计算方法重新计算价格详情，获取实际支付价格
                Map<String, Object> priceResult = tourBookingService.calculateUnifiedPrice(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    null,  // userId参数
                    null,  // roomTypes
                    null,  // childrenAges
                    null   // selectedOptionalTours
                );
                
                actualOrderAmount = BigDecimal.ZERO;
                if (priceResult != null && priceResult.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
                    actualOrderAmount = (BigDecimal) data.get("totalPrice");
                }
                log.info("操作员支付，使用实际折扣价格: {}", actualOrderAmount);
            } else {
                // 代理商主账号或普通用户：使用订单中记录的价格
                actualOrderAmount = tourBookingMapper.getOrderAmount(bookingId);
                log.info("代理商主账号支付，使用订单价格: {}", actualOrderAmount);
            }
            
            if (actualOrderAmount == null) {
                log.error("订单ID {} 不存在或未找到金额信息", bookingId);
                throw new CustomException("订单不存在或金额异常，请联系客服");
            }
            
            // 确定使用哪个代理商的credit
            Integer targetAgentId;
            if ("agent_operator".equals(userType)) {
                // 操作员：使用所属代理商的credit
                if (agentId == null) {
                    log.error("操作员用户 {} 没有关联的代理商ID", currentId);
                    throw new CustomException("操作员账号配置异常，请联系管理员");
                }
                targetAgentId = agentId.intValue();
                log.info("操作员 {} 使用代理商 {} 的信用额度支付", operatorId, agentId);
            } else {
                // 代理商主账号：使用自己的credit
                targetAgentId = getAgentIdByUserId(currentId.intValue());
                if (targetAgentId == null) {
                    log.error("无法获取用户ID为 {} 的代理商ID", currentId);
                    throw new CustomException("无法获取代理商信息，请联系管理员");
                }
                log.info("代理商主账号 {} 使用自己的信用额度支付", targetAgentId);
            }
                
            // 使用AgentCreditService处理信用额度支付
            try {
                // 准备支付信息
                creditPaymentDTO.setAmount(actualOrderAmount);
                
                // 调用统一的信用额度支付方法
                CreditPaymentResultVO result = agentCreditService.payWithCredit(
                    Long.valueOf(targetAgentId), 
                    creditPaymentDTO
                );
                
                if (result == null || !"paid".equals(result.getPaymentStatus())) {
                    log.error("信用额度支付失败，订单ID: {}", bookingId);
                    return false;
                }
            
                // 创建支付记录
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setBookingId(bookingId);
                paymentDTO.setAmount(actualOrderAmount);
                paymentDTO.setPaymentMethod("agent_credit");
                paymentDTO.setStatus("completed");
                paymentDTO.setUserId(currentId.intValue());
                paymentDTO.setType("payment");
                paymentDTO.setPaymentTime(LocalDateTime.now());
                paymentDTO.setCreateTime(LocalDateTime.now());
                paymentDTO.setPaymentOrderNo(generatePaymentOrderNo());
                
                // 设置信用交易相关字段
                paymentDTO.setTransactionId(result.getTransactionId().toString());
                paymentDTO.setIsCreditPayment(true);
                paymentDTO.setCreditTransactionId(result.getTransactionId().intValue());
                
                // 插入支付记录
                paymentMapper.insert(paymentDTO);
                
                // 🔥 重要：调用统一的支付成功处理逻辑（包括同步到排团表）
                PaymentDTO paymentDTOForBooking = new PaymentDTO();
                paymentDTOForBooking.setAmount(actualOrderAmount);
                paymentDTOForBooking.setPaymentMethod("agent_credit");
                paymentDTOForBooking.setStatus("completed");
                paymentDTOForBooking.setPaymentTime(LocalDateTime.now());
                
                try {
                    // 调用TourBookingService的payBooking方法，这会触发支付后同步到排团表
                    tourBookingService.payBooking(bookingId, paymentDTOForBooking);
                    log.info("✅ 信用额度支付成功，已调用统一支付处理逻辑，订单ID: {}", bookingId);
                } catch (Exception e) {
                    log.error("❌ 调用统一支付处理逻辑失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
                    // 如果统一处理失败，则手动更新状态
                    paymentMapper.updateBookingPaymentStatus(bookingId, "paid");
                    bookingMapper.updateStatus(bookingId, "confirmed");
                }
                
                // 🔔 发送支付成功通知
                try {
                    // 获取订单信息
                    TourBooking booking = tourBookingMapper.getById(bookingId);
                    if (booking != null) {
                        // 获取操作者信息
                        String[] operatorInfo = getCurrentOperatorInfo();
                        String operatorName = operatorInfo[0];
                        String operatorType = operatorInfo[1];
                        
                        String contactPerson = booking.getContactPerson();
                        String orderNumber = booking.getOrderNumber();
                        String actionDetail = String.format("支付金额: $%.2f", actualOrderAmount);
                        
                        notificationService.createDetailedOrderNotification(
                            Long.valueOf(bookingId),
                            operatorName,
                            operatorType,
                            contactPerson,
                            orderNumber,
                            "payment",
                            actionDetail
                        );
                        
                        log.info("🔔 已发送支付成功通知: 订单ID={}, 操作者={} ({}), 金额={}", 
                                bookingId, operatorName, operatorType, actualOrderAmount);
                    }
                } catch (Exception e) {
                    log.error("❌ 发送支付成功通知失败: {}", e.getMessage(), e);
                }
                
                return true;
            } catch (Exception e) {
                log.error("信用额度支付处理过程中发生错误: {}", e.getMessage(), e);
                throw new CustomException("支付处理失败: " + e.getMessage());
            }
        } // synchronized块结束
    }
    
    /**
     * 分页查询支付流水
     * @param pageQueryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult getTransactionPage(PaymentPageQueryDTO pageQueryDTO) {
        log.info("分页查询支付流水：{}", pageQueryDTO);
        
        // 设置分页
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        
        // 查询支付流水
        Page<PaymentDTO> page = paymentMapper.getPaymentPage(pageQueryDTO);
        
        return new PageResult(page.getTotal(), page.getResult());
    }
    
    /**
     * 生成支付订单号
     * @return 支付订单号
     */
    private String generatePaymentOrderNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 根据用户ID获取代理商ID
     * @param userId 用户ID
     * @return 代理商ID
     */
    private Integer getAgentIdByUserId(int userId) {
        // 这里假设用户表中的agent_id字段就是代理商ID
        // 在实际应用中，可能需要通过用户ID查询用户表获取代理商ID
        // 简化处理：使用用户ID作为代理商ID
        return userId;
    }

    /**
     * 获取当前操作者信息
     * @return [操作者姓名, 操作者类型, 操作者ID]
     */
    private String[] getCurrentOperatorInfo() {
        try {
            String userType = BaseContext.getCurrentUserType();
            Long currentId = BaseContext.getCurrentId();
            Long agentId = BaseContext.getCurrentAgentId();
            Long operatorId = BaseContext.getCurrentOperatorId();

            if ("agent".equals(userType)) {
                // 代理商主账号 - 需要查询代理商信息
                if (agentId != null) {
                    // 这里应该查询代理商信息，但为了避免循环依赖，使用简化逻辑
                    return new String[]{"中介", "agent", String.valueOf(agentId)};
                }
            } else if ("agent_operator".equals(userType)) {
                // 操作员账号
                if (operatorId != null) {
                    return new String[]{"操作员", "operator", String.valueOf(operatorId)};
                }
            } else if ("user".equals(userType)) {
                // 普通用户
                if (currentId != null) {
                    return new String[]{"用户", "user", String.valueOf(currentId)};
                }
            }

            return new String[]{"未知操作者", "unknown", "0"};
        } catch (Exception e) {
            log.error("获取操作者信息失败: {}", e.getMessage(), e);
            return new String[]{"系统", "system", "0"};
        }
    }
} 