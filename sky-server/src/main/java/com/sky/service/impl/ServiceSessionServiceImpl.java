package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.TransferToServiceDTO;
import com.sky.entity.CustomerService;
import com.sky.entity.ServiceSession;
import com.sky.mapper.ServiceSessionMapper;
import com.sky.result.PageResult;
import com.sky.service.CustomerServiceService;
import com.sky.service.ServiceSessionService;
import com.sky.service.NotificationService;
import com.sky.vo.ServiceSessionVO;
import com.sky.vo.ServiceStatisticsVO;
import com.sky.vo.WorkbenchDataVO;
import com.sky.webSocket.AdminWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

/**
 * 服务会话Service实现类
 */
@Service
@Slf4j
public class ServiceSessionServiceImpl implements ServiceSessionService {

    @Autowired
    private ServiceSessionMapper serviceSessionMapper;

    @Autowired
    private CustomerServiceService customerServiceService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 转人工服务
     */
    @Override
    public ServiceSession transferToService(TransferToServiceDTO transferToServiceDTO) {
        Long userId = BaseContext.getCurrentId();
        
        // 如果无法获取用户ID，使用默认ID进行测试
        if (userId == null) {
            log.warn("无法获取当前用户ID，使用默认用户ID 1 进行测试");
            userId = 1L;
        }
        
        log.info("用户 {} 申请转人工服务，会话类型: {}", userId, transferToServiceDTO.getSessionType());
        
        // 检查用户是否已有活跃会话
        ServiceSession existingSession = serviceSessionMapper.getActiveSessionByUserId(userId);
        if (existingSession != null) {
            log.info("用户 {} 已有活跃会话，将其转为等待分配状态: {}", userId, existingSession.getId());
            
            // 更新现有会话为等待分配状态
            existingSession.setSessionStatus(0); // 等待分配
            existingSession.setSubject(transferToServiceDTO.getSubject());
            existingSession.setSessionType(transferToServiceDTO.getSessionType());
            existingSession.setUpdateTime(LocalDateTime.now());
            existingSession.setEmployeeId(null); // 清空之前的客服分配
            
            // 更新数据库
            serviceSessionMapper.updateSession(existingSession);
            
            // 尝试重新分配客服
            try {
                assignService(existingSession.getId());
            } catch (Exception e) {
                log.error("重新分配客服失败，会话将进入等待队列: {}", e.getMessage(), e);
            }
            
            return existingSession;
        }

        // 创建新会话
        ServiceSession serviceSession = ServiceSession.builder()
                .sessionNo("SS" + System.currentTimeMillis())
                .userId(userId)
                .sessionStatus(0) // 等待分配
                .sessionType(transferToServiceDTO.getSessionType())
                .subject(transferToServiceDTO.getSubject())
                .startTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        serviceSessionMapper.insert(serviceSession);
        log.info("创建新会话成功，会话ID: {}, 会话号: {}", serviceSession.getId(), serviceSession.getSessionNo());

        // 🔔 发送聊天请求通知
        try {
            String customerName = "用户" + userId; // 可以后续从用户表获取真实姓名
            String subject = transferToServiceDTO.getSubject() != null ? 
                           transferToServiceDTO.getSubject() : "客服咨询";
            
            notificationService.createChatRequestNotification(
                serviceSession.getId(),
                customerName,
                subject
            );
            
            log.info("🔔 已发送聊天请求通知: 会话ID={}, 客户={}, 主题={}", 
                    serviceSession.getId(), customerName, subject);
        } catch (Exception e) {
            log.error("❌ 发送聊天请求通知失败: {}", e.getMessage(), e);
        }

        // 尝试自动分配客服
        try {
            assignService(serviceSession.getId());
        } catch (Exception e) {
            log.error("自动分配客服失败，会话将进入等待队列: {}", e.getMessage(), e);
        }

        return serviceSession;
    }

    /**
     * 分配客服
     */
    @Override
    public void assignService(Long sessionId) {
        ServiceSession session = serviceSessionMapper.getById(sessionId);
        if (session == null || session.getSessionStatus() != 0) {
            log.warn("会话 {} 不存在或状态不是等待分配状态", sessionId);
            return;
        }

        // 根据会话类型确定技能标签
        String skillTag = getSkillTagBySessionType(session.getSessionType());
        log.info("会话 {} 需要技能标签: {}", sessionId, skillTag);
        
        // 获取可用客服
        CustomerService availableService = customerServiceService.getAvailableService(skillTag);
        
        if (availableService != null) {
            // 分配客服
            serviceSessionMapper.assignService(sessionId, availableService.getId(), LocalDateTime.now());
            
            // 更新客服当前服务客户数
            int newCount = availableService.getCurrentCustomerCount() + 1;
            customerServiceService.updateCurrentCustomerCount(availableService.getId(), newCount);
            
            log.info("会话 {} 已分配给客服 {} (ID: {})", sessionId, availableService.getName(), availableService.getId());
        } else {
            log.warn("暂无可用客服，会话 {} 进入等待队列。技能标签: {}", sessionId, skillTag);
            
            // 查看所有在线客服状态
            List<CustomerService> onlineServices = customerServiceService.getOnlineServices();
            if (onlineServices != null) {
                log.info("当前在线客服数量: {}", onlineServices.size());
                for (CustomerService service : onlineServices) {
                    log.info("客服 {} (ID: {}) - 当前服务数: {}/{}, 技能: {}", 
                        service.getName(), service.getId(), 
                        service.getCurrentCustomerCount(), service.getMaxConcurrentCustomers(),
                        service.getSkillTags());
                }
            } else {
                log.warn("无法获取在线客服列表，onlineServices为null");
            }
        }
    }

    /**
     * 获取客服的活跃会话列表
     */
    @Override
    public List<ServiceSessionVO> getActiveSessionsByServiceId(Long serviceId) {
        return serviceSessionMapper.getActiveSessionsByServiceId(serviceId);
    }

    /**
     * 获取用户的活跃会话
     */
    @Override
    public ServiceSession getActiveSessionByUserId(Long userId) {
        return serviceSessionMapper.getActiveSessionByUserId(userId);
    }

    /**
     * 结束会话
     */
    @Override
    public void endSession(Long sessionId, Integer endType) {
        endSession(sessionId, endType, null);
    }

    /**
     * 结束会话（带原因）
     */
    @Override
    public void endSession(Long sessionId, Integer endType, String reason) {
        ServiceSession session = serviceSessionMapper.getById(sessionId);
        if (session == null) {
            return;
        }

        LocalDateTime endTime = LocalDateTime.now();
        Integer serviceDuration = null;
        
        if (session.getStartTime() != null) {
            serviceDuration = (int) java.time.Duration.between(session.getStartTime(), endTime).getSeconds();
        }

        // 获取结束类型的描述
        String endTypeDescription = getEndTypeDescription(endType);

        serviceSessionMapper.updateSessionStatus(sessionId, endType, endTime, serviceDuration, LocalDateTime.now());

        // 如果有分配的客服，减少其当前服务客户数
        if (session.getEmployeeId() != null) {
            List<CustomerService> onlineServices = customerServiceService.getOnlineServices();
            CustomerService customerService = null;
            
            if (onlineServices != null) {
                customerService = onlineServices.stream()
                        .filter(cs -> cs.getId().equals(session.getEmployeeId()))
                        .findFirst()
                        .orElse(null);
            } else {
                log.warn("无法获取在线客服列表进行客户数更新");
            }
            
            if (customerService != null) {
                int newCount = Math.max(0, customerService.getCurrentCustomerCount() - 1);
                customerServiceService.updateCurrentCustomerCount(session.getEmployeeId(), newCount);
            }
            
            // 通知客服端会话已结束
            try {
                AdminWebSocketServer.notifySessionEnded(sessionId, session.getEmployeeId());
                log.info("✅ 已通知客服 {} 会话 {} 结束，结束类型: {}", session.getEmployeeId(), sessionId, endTypeDescription);
            } catch (Exception e) {
                log.error("❌ 通知客服会话结束失败：{}", e.getMessage());
            }
        }

        log.info("会话 {} 已结束，结束类型: {} ({}), 原因: {}", sessionId, endType, endTypeDescription, reason);
    }

    /**
     * 获取结束类型描述
     */
    private String getEndTypeDescription(Integer endType) {
        if (endType == null) return "未知";
        switch (endType) {
            case 2: return "用户结束";
            case 3: return "客服结束";
            case 4: return "超时结束";
            case 5: return "管理员强制结束";
            default: return "其他原因";
        }
    }

    /**
     * 评价会话
     */
    @Override
    public void rateSession(Long sessionId, Integer rating, String comment) {
        serviceSessionMapper.updateRating(sessionId, rating, comment, LocalDateTime.now());
    }

    /**
     * 更新客服备注
     */
    @Override
    public void updateServiceRemark(Long sessionId, String remark) {
        serviceSessionMapper.updateServiceRemark(sessionId, remark, LocalDateTime.now());
    }

    // ========== 管理端新增方法实现 ==========

    /**
     * 分页查询会话列表
     */
    @Override
    public PageResult getSessionList(Integer page, Integer pageSize, Integer status, 
                                   String startDate, String endDate, String keyword) {
        try {
            // 由于没有完整的分页查询SQL，先返回所有会话的简单版本
            List<ServiceSession> allSessions;
            
            if (status != null && status == 0) {
                // 如果查询等待中的会话
                allSessions = serviceSessionMapper.getWaitingAssignSessions();
            } else {
                // 这里应该有完整的分页查询，暂时用简单查询代替
                log.warn("分页查询会话列表功能需要完整实现，当前返回等待队列");
                allSessions = serviceSessionMapper.getWaitingAssignSessions();
            }
            
            log.info("查询到会话数量: {}", allSessions.size());
            
            // 简单分页处理
            int total = allSessions.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<ServiceSession> pageData = allSessions.subList(
                Math.max(0, startIndex), 
                Math.max(0, endIndex)
            );
            
            return new PageResult(total, pageData);
        } catch (Exception e) {
            log.error("查询会话列表失败", e);
            return new PageResult(0, Collections.emptyList());
        }
    }

    /**
     * 获取等待队列
     */
    @Override
    public List<ServiceSession> getWaitingQueue() {
        try {
            List<ServiceSession> waitingQueue = serviceSessionMapper.getWaitingAssignSessions();
            log.info("等待队列中有 {} 个会话", waitingQueue.size());
            return waitingQueue;
        } catch (Exception e) {
            log.error("获取等待队列失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取客服工作台数据
     */
    @Override
    public WorkbenchDataVO getWorkbenchData(Long serviceId) {
        try {
            log.info("获取客服 {} 的工作台数据", serviceId);
            
            // 获取客服的活跃会话
            List<ServiceSessionVO> activeSessionVOs = serviceSessionMapper.getActiveSessionsByServiceId(serviceId);
            log.info("客服 {} 的活跃会话数量: {}", serviceId, activeSessionVOs != null ? activeSessionVOs.size() : 0);
            
            // 获取等待队列（所有等待分配的会话）
            List<ServiceSession> waitingQueue = getWaitingQueue();
            log.info("等待队列会话数量: {}", waitingQueue.size());
            
            // 构建统计数据
            WorkbenchDataVO.WorkbenchStatistics statistics = WorkbenchDataVO.WorkbenchStatistics.builder()
                .todayServiceCount(0) // 今日服务数量，需要实现具体查询
                .avgRating(0.0) // 平均评分，需要实现具体查询
                .activeSessionCount(activeSessionVOs != null ? activeSessionVOs.size() : 0)
                .waitingQueueCount(waitingQueue.size())
                .build();
            
            log.info("客服 {} 工作台数据：活跃会话 {} 个，等待队列 {} 个", 
                serviceId, statistics.getActiveSessionCount(), statistics.getWaitingQueueCount());
            
            // 构建返回数据 - 分别返回活跃会话和等待队列
            WorkbenchDataVO workbenchData = WorkbenchDataVO.builder()
                .activeSessions(activeSessionVOs != null ? 
                    activeSessionVOs.stream()
                        .map(vo -> {
                            ServiceSession session = new ServiceSession();
                            session.setId(vo.getId());
                            session.setUserId(vo.getUserId());
                            session.setEmployeeId(vo.getServiceId()); // ServiceSessionVO中是serviceId字段
                            session.setSessionStatus(vo.getSessionStatus());
                            session.setSessionType(vo.getSessionType());
                            session.setSubject(vo.getSubject());
                            session.setCreateTime(vo.getCreateTime());
                            session.setUpdateTime(LocalDateTime.now()); // VO中没有updateTime，使用当前时间
                            return session;
                        })
                        .collect(java.util.stream.Collectors.toList()) : 
                    Collections.emptyList())
                .waitingQueue(waitingQueue) // 单独的等待队列字段
                .statistics(statistics)
                .build();
                
            log.info("工作台数据构建完成，返回 {} 个活跃会话和 {} 个等待会话", 
                activeSessionVOs != null ? activeSessionVOs.size() : 0, waitingQueue.size());
            return workbenchData;
        } catch (Exception e) {
            log.error("获取工作台数据失败", e);
            return WorkbenchDataVO.builder()
                .activeSessions(Collections.emptyList())
                .waitingQueue(Collections.emptyList())
                .statistics(WorkbenchDataVO.WorkbenchStatistics.builder()
                    .todayServiceCount(0)
                    .avgRating(0.0)
                    .activeSessionCount(0)
                    .waitingQueueCount(0)
                    .build())
                .build();
        }
    }

    /**
     * 接受会话
     */
    @Override
    public void acceptSession(Long sessionId, Long serviceId) {
        try {
            ServiceSession session = serviceSessionMapper.getById(sessionId);
            if (session == null) {
                log.warn("会话 {} 不存在", sessionId);
                return;
            }

            if (session.getSessionStatus() != 0) {
                log.warn("会话 {} 状态不是等待分配状态，当前状态: {}", sessionId, session.getSessionStatus());
                return;
            }

            // 分配客服（assignService方法会同时更新状态为进行中）
            serviceSessionMapper.assignService(sessionId, serviceId, LocalDateTime.now());
            
            // 建立WebSocket会话映射关系
            AdminWebSocketServer.bindSessionToService(sessionId, serviceId);
            
            log.info("客服 {} 成功接受了会话 {}，会话状态已更新为进行中", serviceId, sessionId);
        } catch (Exception e) {
            log.error("接受会话失败", e);
            throw new RuntimeException("接受会话失败: " + e.getMessage());
        }
    }

    /**
     * 分配会话给客服
     */
    @Override
    public void assignSession(Long sessionId, Long serviceId) {
        acceptSession(sessionId, serviceId);
    }

    /**
     * 获取会话详情
     */
    @Override
    public ServiceSessionVO getSessionDetail(Long sessionId) {
        // 临时实现：返回空数据
        return new ServiceSessionVO();
    }

    /**
     * 获取客服的活跃会话
     */
    @Override
    public List<ServiceSession> getActiveSessionsByService(Long serviceId) {
        // 临时实现：返回空列表，避免编译错误
        log.info("获取客服 {} 的活跃会话（临时实现）", serviceId);
        return Collections.emptyList();
    }

    /**
     * 获取会话统计信息
     */
    @Override
    public ServiceStatisticsVO getStatistics() {
        // 临时实现：返回默认统计数据
        return ServiceStatisticsVO.builder()
            .totalSessions(0)
            .activeSessions(0)
            .avgServiceDuration(0)
            .avgUserRating(0.0)
            .todayServiceCount(0)
            .waitingQueueCount(0)
            .build();
    }

    /**
     * 根据会话类型获取技能标签
     */
    private String getSkillTagBySessionType(Integer sessionType) {
        switch (sessionType) {
            case 1:
                return "旅游咨询";
            case 2:
                return "AI转人工";
            case 3:
                return "投诉处理";
            default:
                return "通用服务";
        }
    }
} 