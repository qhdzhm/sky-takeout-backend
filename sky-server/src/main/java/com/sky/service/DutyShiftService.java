package com.sky.service;

import com.sky.dto.TransferDutyDTO;
import com.sky.entity.DutyShift;
import com.sky.vo.CurrentDutyStatusVO;
import com.sky.vo.DutyShiftVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 值班管理服务接口
 */
public interface DutyShiftService {

    /**
     * 开始值班
     * @param operatorId 操作员ID
     * @param dutyType 值班类型
     */
    void startDuty(Long operatorId, String dutyType);

    /**
     * 转移值班
     * @param fromOperatorId 当前值班人ID
     * @param transferDutyDTO 转移数据
     */
    void transferDuty(Long fromOperatorId, TransferDutyDTO transferDutyDTO);

    /**
     * 结束值班
     * @param operatorId 操作员ID
     * @param dutyType 值班类型
     */
    void endDuty(Long operatorId, String dutyType);

    /**
     * 获取当前排团主管信息
     * @return 当前排团主管信息
     */
    CurrentDutyStatusVO getCurrentTourMaster();

    /**
     * 获取当前值班状态（按类型）
     * @param dutyType 值班类型
     * @return 值班信息
     */
    DutyShiftVO getCurrentDutyByType(String dutyType);

    /**
     * 获取操作员的值班历史
     * @param operatorId 操作员ID
     * @return 值班历史列表
     */
    List<DutyShiftVO> getShiftHistoryByOperatorId(Long operatorId);

    /**
     * 获取所有值班记录
     * @return 值班记录列表
     */
    List<DutyShiftVO> getAllShifts();

    /**
     * 检查值班冲突
     * @param operatorId 操作员ID
     * @param dutyType 值班类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否有冲突
     */
    boolean hasConflict(Long operatorId, String dutyType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取值班统计信息
     * @param operatorId 操作员ID
     * @return 统计信息
     */
    Object getShiftStatistics(Long operatorId);

    /**
     * 检查操作员是否可以开始排团主管值班
     * @param operatorId 操作员ID
     * @return 是否可以开始值班
     */
    boolean canStartTourMasterDuty(Long operatorId);

    /**
     * 获取可用的排团主管候选人
     * @return 候选人列表
     */
    List<Object> getAvailableTourMasterCandidates();
}
