package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.AgentPriceResponseDTO;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.PriceModificationRequestDTO;
import com.sky.entity.PaymentAuditLog;
import com.sky.entity.PriceModificationRequest;
import com.sky.entity.TourBooking;
import com.sky.exception.BusinessException;
import com.sky.mapper.PaymentAuditLogMapper;
import com.sky.mapper.PriceModificationRequestMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.result.PageResult;
import com.sky.service.AgentCreditService;
import com.sky.service.NotificationService;
import com.sky.service.PriceModificationService;
import com.sky.vo.CreditPaymentResultVO;
import com.sky.vo.PriceModificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * 价格修改服务实现类
 */
@Service
@Slf4j
public class PriceModificationServiceImpl implements PriceModificationService {

    @Autowired
    private PriceModificationRequestMapper priceModificationRequestMapper;

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private PaymentAuditLogMapper paymentAuditLogMapper;

    @Autowired
    private AgentCreditService agentCreditService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 创建价格修改请求
     */
    @Override
    @Transactional
    public String createPriceModificationRequest(PriceModificationRequestDTO requestDTO) {
        log.info("创建价格修改请求: {}", requestDTO);

        // 1. 获取订单信息
        TourBooking booking = tourBookingMapper.getById(requestDTO.getBookingId());
        if (booking == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 检查订单状态
        if (!"paid".equals(booking.getPaymentStatus())) {
            throw new BusinessException("只能修改已支付订单的价格");
        }

        // 3. 计算价格差异
        BigDecimal originalPrice = booking.getTotalPrice();
        BigDecimal newPrice = requestDTO.getNewPrice();
        BigDecimal priceDifference = newPrice.subtract(originalPrice);

        if (priceDifference.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("新价格与原价格相同，无需修改");
        }

        // 4. 确定修改类型
        String modificationType = priceDifference.compareTo(BigDecimal.ZERO) > 0 ? "increase" : "decrease";
        
        // 5. 创建价格修改请求
        PriceModificationRequest request = PriceModificationRequest.builder()
                .bookingId(requestDTO.getBookingId())
                .originalPrice(originalPrice)
                .newPrice(newPrice)
                .priceDifference(priceDifference)
                .modificationType(modificationType)
                .reason(requestDTO.getReason())
                .createdByAdmin(BaseContext.getCurrentId().intValue())
                .createdAt(LocalDateTime.now())
                .build();

        if ("decrease".equals(modificationType)) {
            // 降价：自动处理
            return handlePriceDecrease(request, booking);
        } else {
            // 涨价：需要代理商确认
            return handlePriceIncrease(request, booking);
        }
    }

    /**
     * 处理降价请求
     */
    private String handlePriceDecrease(PriceModificationRequest request, TourBooking booking) {
        log.info("处理降价请求: 订单ID={}, 降价金额={}", request.getBookingId(), request.getPriceDifference().abs());

        try {
            // 1. 设置为自动处理状态
            request.setStatus("auto_processed");
            request.setProcessedAt(LocalDateTime.now());
            priceModificationRequestMapper.insert(request);

            // 2. 记录降价audit日志
            PaymentAuditLog decreaseLog = PaymentAuditLog.builder()
                    .requestId(UUID.randomUUID().toString())
                    .action("price_decrease")
                    .bookingId(request.getBookingId())
                    .orderNumber(booking.getOrderNumber())
                    .agentId(Long.valueOf(booking.getAgentId()))
                    .amount(request.getPriceDifference())
                    .note("订单降价" + request.getPriceDifference().abs() + "元")
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentAuditLogMapper.insert(decreaseLog);

            // 3. 退款到代理商信用账户
            BigDecimal refundAmount = request.getPriceDifference().abs();
            agentCreditService.addCredit(Long.valueOf(booking.getAgentId()), refundAmount, 
                    "订单降价退款，订单号：" + booking.getOrderNumber());

            // 4. 记录退款audit日志
            PaymentAuditLog refundLog = PaymentAuditLog.builder()
                    .requestId(UUID.randomUUID().toString())
                    .action("refund")
                    .bookingId(request.getBookingId())
                    .orderNumber(booking.getOrderNumber())
                    .agentId(Long.valueOf(booking.getAgentId()))
                    .amount(refundAmount)
                    .note("价格降价退款")
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentAuditLogMapper.insert(refundLog);

            // 5. 更新订单价格
            booking.setTotalPrice(request.getNewPrice());
            tourBookingMapper.update(booking);

            // 6. 发送通知给代理商
            String notificationMessage = String.format(
                    "您的订单 %s 价格已调整，降价 ¥%.2f，已自动退款到信用账户",
                    booking.getOrderNumber(), refundAmount
            );
            notificationService.createAgentOrderChangeNotification(
                    Long.valueOf(booking.getAgentId()),
                    null, // operatorId，这里暂时为空
                    Long.valueOf(request.getBookingId()),
                    booking.getOrderNumber(),
                    "订单降价通知",
                    notificationMessage
            );

            // 7. 更新状态为完成
            priceModificationRequestMapper.updateStatus(request.getId(), "completed", LocalDateTime.now());

            return "降价请求已自动处理完成，退款金额：" + refundAmount + "元";

        } catch (Exception e) {
            log.error("处理降价请求失败: {}", e.getMessage(), e);
            throw new BusinessException("降价处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理涨价请求
     */
    private String handlePriceIncrease(PriceModificationRequest request, TourBooking booking) {
        log.info("处理涨价请求: 订单ID={}, 涨价金额={}", request.getBookingId(), request.getPriceDifference());

        try {
            // 1. 设置为待处理状态
            request.setStatus("pending");
            priceModificationRequestMapper.insert(request);

            // 2. 涨价不立即记录audit日志，等代理商确认后再记录
            log.info("涨价请求已创建，audit记录将在代理商确认后生成");

            // 3. 发送通知给代理商
            String notificationMessage = String.format(
                    "您的订单 %s 价格需要调整，涨价 ¥%.2f，请确认是否同意补款。原因：%s",
                    booking.getOrderNumber(), request.getPriceDifference(), request.getReason()
            );
            notificationService.createAgentOrderChangeNotification(
                    Long.valueOf(booking.getAgentId()),
                    null, // operatorId，这里暂时为空
                    Long.valueOf(request.getBookingId()),
                    booking.getOrderNumber(),
                    "订单涨价确认",
                    notificationMessage
            );

            return "涨价请求已提交，等待代理商确认，涨价金额：" + request.getPriceDifference() + "元";

        } catch (Exception e) {
            log.error("处理涨价请求失败: {}", e.getMessage(), e);
            throw new BusinessException("涨价请求创建失败: " + e.getMessage());
        }
    }

    /**
     * 代理商响应价格修改请求
     */
    @Override
    @Transactional
    public String agentResponseToRequest(AgentPriceResponseDTO responseDTO) {
        log.info("代理商响应价格修改请求: {}", responseDTO);

        // 1. 获取价格修改请求
        PriceModificationRequest request = priceModificationRequestMapper.getById(responseDTO.getRequestId());
        if (request == null) {
            throw new BusinessException("价格修改请求不存在");
        }

        // 2. 检查请求状态
        if (!"pending".equals(request.getStatus())) {
            throw new BusinessException("该请求已经处理过，无法重复操作");
        }

        // 3. 验证权限：确保只有相关代理商能够响应
        TourBooking booking = tourBookingMapper.getById(request.getBookingId());
        Long currentAgentId = BaseContext.getCurrentAgentId();
        if (!currentAgentId.equals(Long.valueOf(booking.getAgentId()))) {
            throw new BusinessException("无权限处理该价格修改请求");
        }

        // 4. 更新代理商响应
        priceModificationRequestMapper.updateAgentResponse(
                request.getId(),
                responseDTO.getResponse(),
                responseDTO.getNote(),
                LocalDateTime.now()
        );

        if ("approved".equals(responseDTO.getResponse())) {
            return handleAgentApproval(request, booking);
        } else {
            return handleAgentRejection(request, booking);
        }
    }

    /**
     * 处理代理商同意涨价
     */
    private String handleAgentApproval(PriceModificationRequest request, TourBooking booking) {
        log.info("处理代理商同意涨价: 请求ID={}", request.getId());

        try {
            // 1. 扣除补款
            CreditPaymentDTO creditPayment = new CreditPaymentDTO();
            creditPayment.setBookingId(Long.valueOf(request.getBookingId()));
            creditPayment.setAmount(request.getPriceDifference());
            creditPayment.setNote("订单涨价补款，订单号：" + booking.getOrderNumber());

            CreditPaymentResultVO paymentResult = agentCreditService.payWithCredit(
                    Long.valueOf(booking.getAgentId()), creditPayment);

            if (!"paid".equals(paymentResult.getPaymentStatus())) {
                throw new BusinessException("信用额度不足或支付失败");
            }

            // 2. 更新订单价格
            booking.setTotalPrice(request.getNewPrice());
            tourBookingMapper.update(booking);

            // 3. 记录涨价成功audit日志
            String currentUsername = BaseContext.getCurrentUsername();
            PaymentAuditLog approvedLog = PaymentAuditLog.builder()
                    .requestId(UUID.randomUUID().toString())
                    .action("price_increase_approved")
                    .bookingId(request.getBookingId())
                    .orderNumber(booking.getOrderNumber())
                    .agentId(Long.valueOf(booking.getAgentId()))
                    .operatorId(Long.valueOf(booking.getAgentId()))
                    .operatorType("agent")
                    .operatorName(currentUsername != null ? currentUsername : "代理商")
                    .amount(request.getPriceDifference())
                    .balanceBefore(paymentResult.getBalanceBefore())
                    .balanceAfter(paymentResult.getBalanceAfter())
                    .note(String.format("代理商同意涨价，补款 ¥%.2f 已扣除 [代理商: %s]", 
                            request.getPriceDifference(), currentUsername != null ? currentUsername : "未知"))
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentAuditLogMapper.insert(approvedLog);

            // 4. 更新请求状态为已完成
            priceModificationRequestMapper.updateStatus(request.getId(), "completed", LocalDateTime.now());

            // 5. 发送确认通知
            String notificationMessage = String.format(
                    "您的订单 %s 涨价确认已处理完成，补款 ¥%.2f 已从信用账户扣除",
                    booking.getOrderNumber(), request.getPriceDifference()
            );
            notificationService.createAgentOrderChangeNotification(
                    Long.valueOf(booking.getAgentId()),
                    null, // operatorId，这里暂时为空
                    Long.valueOf(request.getBookingId()),
                    booking.getOrderNumber(),
                    "涨价处理完成",
                    notificationMessage
            );

            return "涨价确认处理完成，补款金额：" + request.getPriceDifference() + "元";

        } catch (Exception e) {
            log.error("处理代理商同意涨价失败: {}", e.getMessage(), e);
            // 回滚状态
            priceModificationRequestMapper.updateStatus(request.getId(), "pending", null);
            throw new BusinessException("涨价处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理代理商拒绝涨价
     */
    private String handleAgentRejection(PriceModificationRequest request, TourBooking booking) {
        log.info("处理代理商拒绝涨价: 请求ID={}", request.getId());

        // 1. 更新请求状态为已拒绝
        priceModificationRequestMapper.updateStatus(request.getId(), "rejected", LocalDateTime.now());

        // 2. 记录拒绝audit日志
        String currentUsername = BaseContext.getCurrentUsername();
        PaymentAuditLog rejectionLog = PaymentAuditLog.builder()
                .requestId(UUID.randomUUID().toString())
                .action("price_increase_rejected")
                .bookingId(request.getBookingId())
                .orderNumber(booking.getOrderNumber())
                .agentId(Long.valueOf(booking.getAgentId()))
                .operatorId(Long.valueOf(booking.getAgentId()))
                .operatorType("agent")
                .operatorName(currentUsername != null ? currentUsername : "代理商")
                .amount(request.getPriceDifference())
                .balanceBefore(null) // 拒绝时无余额变化
                .balanceAfter(null)  // 拒绝时无余额变化
                .note(String.format("代理商拒绝涨价，订单保持原价格 [代理商: %s]", 
                        currentUsername != null ? currentUsername : "未知"))
                .createdAt(LocalDateTime.now())
                .build();
        paymentAuditLogMapper.insert(rejectionLog);

        // 3. 发送通知给管理员
        // TODO: 实现管理员通知功能

        return "涨价请求已被拒绝，订单保持原价格";
    }

    /**
     * 管理后台分页查询
     */
    @Override
    public PageResult pageQuery(String status, String modificationType,
                               LocalDate startDate, LocalDate endDate,
                               Integer page, Integer pageSize) {
        LocalDateTime startTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        int offset = (page - 1) * pageSize;

        List<PriceModificationVO> list = priceModificationRequestMapper.selectPage(
                status, modificationType, startTime, endTime, pageSize, offset);
        int total = priceModificationRequestMapper.countPage(
                status, modificationType, startTime, endTime);

        return new PageResult(total, list);
    }

    /**
     * 代理商分页查询
     */
    @Override
    public PageResult pageQueryByAgent(Long agentId, String status,
                                      LocalDate startDate, LocalDate endDate,
                                      Integer page, Integer pageSize) {
        LocalDateTime startTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        int offset = (page - 1) * pageSize;

        List<PriceModificationVO> list = priceModificationRequestMapper.selectByAgent(
                agentId, status, startTime, endTime, pageSize, offset);
        int total = priceModificationRequestMapper.countByAgent(
                agentId, status, startTime, endTime);

        return new PageResult(total, list);
    }

    /**
     * 代理商根据订单ID分页查询价格修改请求
     */
    @Override
    public PageResult pageQueryByBookingId(Long agentId, Long bookingId, String status,
                                          Integer page, Integer pageSize) {
        log.info("代理商根据订单ID查询价格修改请求: agentId={}, bookingId={}, status={}, page={}, pageSize={}",
                agentId, bookingId, status, page, pageSize);
        
        int offset = (page - 1) * pageSize;

        List<PriceModificationVO> list = priceModificationRequestMapper.selectByBookingId(
                agentId, bookingId, status, pageSize, offset);
        int total = priceModificationRequestMapper.countByBookingId(
                agentId, bookingId, status);

        log.info("代理商订单价格修改请求查询结果: 总数={}, 当前页数据={}", total, list.size());
        return new PageResult(total, list);
    }

    /**
     * 根据ID获取详情
     */
    @Override
    public PriceModificationVO getById(Long id) {
        // 这里需要实现VO的查询，简化处理直接返回基础信息
        PriceModificationRequest request = priceModificationRequestMapper.getById(id);
        if (request == null) {
            return null;
        }

        // 简化转换，实际应该查询关联信息
        PriceModificationVO vo = new PriceModificationVO();
        // TODO: 完善VO转换逻辑
        return vo;
    }


}
