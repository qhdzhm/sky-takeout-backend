package com.sky.service;

import com.sky.dto.TransferToServiceDTO;
import com.sky.entity.ServiceSession;
import com.sky.result.PageResult;
import com.sky.vo.ServiceSessionVO;
import com.sky.vo.ServiceStatisticsVO;
import com.sky.vo.WorkbenchDataVO;

import java.util.List;

/**
 * 服务会话Service接口
 */
public interface ServiceSessionService {

    /**
     * 转人工服务
     */
    ServiceSession transferToService(TransferToServiceDTO transferToServiceDTO);

    /**
     * 分配客服
     */
    void assignService(Long sessionId);

    /**
     * 获取客服的活跃会话列表
     */
    List<ServiceSessionVO> getActiveSessionsByServiceId(Long serviceId);

    /**
     * 获取用户的活跃会话
     */
    ServiceSession getActiveSessionByUserId(Long userId);

    /**
     * 结束会话
     */
    void endSession(Long sessionId, Integer endType);

    /**
     * 结束会话（带原因）
     */
    void endSession(Long sessionId, Integer endType, String reason);

    /**
     * 评价会话
     */
    void rateSession(Long sessionId, Integer rating, String comment);

    /**
     * 更新客服备注
     */
    void updateServiceRemark(Long sessionId, String remark);

    // ========== 管理端新增方法 ==========

    /**
     * 分页查询会话列表
     */
    PageResult getSessionList(Integer page, Integer pageSize, Integer status, 
                             String startDate, String endDate, String keyword, Long serviceId);

    /**
     * 获取等待队列（带用户信息）
     */
    List<ServiceSessionVO> getWaitingQueue();

    /**
     * 获取客服工作台数据
     */
    WorkbenchDataVO getWorkbenchData(Long serviceId);

    /**
     * 接受会话
     */
    void acceptSession(Long sessionId, Long serviceId);

    /**
     * 分配会话给客服
     */
    void assignSession(Long sessionId, Long serviceId);

    /**
     * 获取会话详情
     */
    ServiceSessionVO getSessionDetail(Long sessionId);

    /**
     * 获取客服的活跃会话
     */
    List<ServiceSession> getActiveSessionsByService(Long serviceId);

    /**
     * 获取会话统计信息
     */
    ServiceStatisticsVO getStatistics();
} 