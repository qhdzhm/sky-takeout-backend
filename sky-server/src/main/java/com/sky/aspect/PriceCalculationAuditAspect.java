package com.sky.aspect;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.entity.PriceCalculationAuditLog;
import com.sky.mapper.PriceCalculationAuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 价格计算审计切面
 * 自动记录所有价格计算请求
 */
@Aspect
@Component
@Slf4j
public class PriceCalculationAuditAspect {
    
    @Autowired
    private PriceCalculationAuditLogMapper auditLogMapper;
    
    /**
     * 环绕通知：记录价格计算审计日志
     */
    @Around("execution(* com.sky.service.*.calculateUnifiedPrice(..))")
    public Object auditPriceCalculation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取输入参数
        Object[] args = joinPoint.getArgs();
        Integer tourId = args.length > 0 ? (Integer) args[0] : null;
        String tourType = args.length > 1 ? (String) args[1] : null;
        
        // 执行价格计算
        Object result = null;
        String status = "success";
        String errorMessage = null;
        BigDecimal calculatedPrice = BigDecimal.ZERO;
        Map<String, Object> calculationDetails = null;
        
        try {
            result = joinPoint.proceed();
            
            // 提取计算结果
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultMap = (Map<String, Object>) result;
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resultMap.get("data");
                if (data != null) {
                    calculatedPrice = (BigDecimal) data.get("totalPrice");
                    calculationDetails = data;
                }
            }
            
        } catch (Exception e) {
            status = "error";
            errorMessage = e.getMessage();
            log.error("价格计算失败", e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 异步记录审计日志（不影响主流程性能）
            saveAuditLogAsync(request, tourId, tourType, args, calculatedPrice, 
                            calculationDetails, duration, status, errorMessage);
        }
        
        return result;
    }
    
    /**
     * 异步保存审计日志
     */
    private void saveAuditLogAsync(HttpServletRequest request, Integer tourId, String tourType,
                                    Object[] args, BigDecimal calculatedPrice,
                                    Map<String, Object> calculationDetails, long duration,
                                    String status, String errorMessage) {
        CompletableFuture.runAsync(() -> {
            try {
                PriceCalculationAuditLog auditLog = PriceCalculationAuditLog.builder()
                        // 用户信息
                        .userId(BaseContext.getCurrentId() != null ? BaseContext.getCurrentId().intValue() : null)
                        .agentId(BaseContext.getCurrentAgentId() != null ? BaseContext.getCurrentAgentId().intValue() : null)
                        .userType(BaseContext.getCurrentUserType())
                        
                        // 产品信息
                        .tourId(tourId)
                        .tourType(tourType)
                        
                        // 输入参数
                        .inputParams(JSON.toJSONString(args))
                        
                        // 计算结果
                        .calculatedPrice(calculatedPrice)
                        .calculationDetails(calculationDetails != null ? JSON.toJSONString(calculationDetails) : null)
                        .calculationDurationMs((int) duration)
                        
                        // 状态
                        .status(status)
                        .errorMessage(errorMessage)
                        
                        // 可疑标记（后续可以添加规则判断）
                        .isSuspicious(false)
                        
                        .createdAt(LocalDateTime.now())
                        .build();
                
                // 请求信息
                if (request != null) {
                    auditLog.setIpAddress(getClientIp(request));
                    
                    // User-Agent 截断到500字符
                    String userAgent = request.getHeader("User-Agent");
                    if (userAgent != null && userAgent.length() > 500) {
                        userAgent = userAgent.substring(0, 500);
                    }
                    auditLog.setUserAgent(userAgent);
                    
                    // Request URL 截断到500字符
                    String requestUrl = request.getRequestURI();
                    if (requestUrl != null && requestUrl.length() > 500) {
                        requestUrl = requestUrl.substring(0, 500);
                    }
                    auditLog.setRequestUrl(requestUrl);
                    
                    // Referer 截断到500字符
                    String referer = request.getHeader("Referer");
                    if (referer != null && referer.length() > 500) {
                        referer = referer.substring(0, 500);
                    }
                    auditLog.setReferer(referer);
                }
                
                // 保存到数据库
                auditLogMapper.insert(auditLog);
                log.info("价格计算审计日志已保存: tourId={}, tourType={}, price={}, duration={}ms", 
                        tourId, tourType, calculatedPrice, duration);
                
            } catch (Exception e) {
                // 审计日志保存失败不影响主流程
                log.error("保存价格计算审计日志失败", e);
            }
        });
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

