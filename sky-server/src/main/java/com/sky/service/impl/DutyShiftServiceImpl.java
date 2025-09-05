package com.sky.service.impl;

import com.sky.dto.TransferDutyDTO;
import com.sky.entity.DutyShift;
import com.sky.entity.Employee;
import com.sky.exception.BaseException;
import com.sky.mapper.DutyShiftMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.DutyShiftService;
import com.sky.vo.CurrentDutyStatusVO;
import com.sky.vo.DutyShiftVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班管理服务实现类
 */
@Service
@Slf4j
public class DutyShiftServiceImpl implements DutyShiftService {

    @Autowired
    private DutyShiftMapper dutyShiftMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 开始值班
     */
    @Override
    @Transactional
    public void startDuty(Long operatorId, String dutyType) {
        log.info("操作员：{} 开始值班，类型：{}", operatorId, dutyType);

        // 1. 验证操作员是否存在
        Employee employee = employeeMapper.getById(operatorId.intValue());
        if (employee == null) {
            throw new BaseException("操作员不存在");
        }

        // 2. 如果是排团主管值班，需要额外验证
        if ("tour_master".equals(dutyType)) {
            if (!"tour_master".equals(employee.getOperatorType()) && !Boolean.TRUE.equals(employee.getCanAssignOrders())) {
                throw new BaseException("该操作员无法担任排团主管");
            }

            // 检查是否已有其他人在担任排团主管
            DutyShift currentTourMaster = dutyShiftMapper.getCurrentTourMaster();
            if (currentTourMaster != null) {
                throw new BaseException("当前已有排团主管在值班，需要先转移值班");
            }
        }

        // 3. 检查是否有冲突的值班记录
        if (hasConflict(operatorId, dutyType, LocalDateTime.now(), null)) {
            throw new BaseException("存在冲突的值班时间");
        }

        // 4. 创建值班记录
        DutyShift dutyShift = DutyShift.builder()
                .operatorId(operatorId)
                .dutyType(dutyType)
                .shiftStart(LocalDateTime.now())
                .status("active")
                .build();

        dutyShiftMapper.insert(dutyShift);

        // 5. 如果是排团主管，更新员工表状态
        if ("tour_master".equals(dutyType)) {
            // 先清除所有排团主管标识
            updateAllTourMasterStatus(false);
            
            // 设置当前操作员为排团主管
            Employee updateEmployee = new Employee();
            updateEmployee.setId(operatorId);
            updateEmployee.setIsTourMaster(true);
            updateEmployee.setOperatorType("tour_master");
            updateEmployee.setCanAssignOrders(true);
            employeeMapper.update(updateEmployee);
        }

        log.info("值班开始成功：操作员：{} 类型：{}", operatorId, dutyType);
    }

    /**
     * 转移值班
     */
    @Override
    @Transactional
    public void transferDuty(Long fromOperatorId, TransferDutyDTO transferDutyDTO) {
        log.info("转移值班：从操作员：{} 到操作员：{}", fromOperatorId, transferDutyDTO.getToOperatorId());

        String dutyType = transferDutyDTO.getDutyType();

        // 1. 获取当前值班记录
        DutyShift currentDuty = dutyShiftMapper.getCurrentDutyByType(dutyType);
        if (currentDuty == null || !currentDuty.getOperatorId().equals(fromOperatorId)) {
            throw new BaseException("当前无有效的值班记录或无权限转移");
        }

        // 2. 验证目标操作员
        Employee toOperator = employeeMapper.getById(transferDutyDTO.getToOperatorId().intValue());
        if (toOperator == null) {
            throw new BaseException("目标操作员不存在");
        }

        if ("tour_master".equals(dutyType)) {
            if (!"tour_master".equals(toOperator.getOperatorType()) && !Boolean.TRUE.equals(toOperator.getCanAssignOrders())) {
                throw new BaseException("目标操作员无法担任排团主管");
            }
        }

        // 3. 转移当前值班记录
        dutyShiftMapper.transfer(currentDuty.getId(), transferDutyDTO.getToOperatorId(),
                transferDutyDTO.getTransferReason(), transferDutyDTO.getNotes());

        // 4. 创建新的值班记录
        DutyShift newDuty = DutyShift.builder()
                .operatorId(transferDutyDTO.getToOperatorId())
                .dutyType(dutyType)
                .shiftStart(LocalDateTime.now())
                .status("active")
                .build();

        dutyShiftMapper.insert(newDuty);

        // 5. 如果是排团主管转移，更新员工表状态
        if ("tour_master".equals(dutyType)) {
            // 清除原排团主管标识
            Employee fromEmployee = new Employee();
            fromEmployee.setId(fromOperatorId);
            fromEmployee.setIsTourMaster(false);
            employeeMapper.update(fromEmployee);

            // 设置新排团主管
            Employee toEmployee = new Employee();
            toEmployee.setId(transferDutyDTO.getToOperatorId());
            toEmployee.setIsTourMaster(true);
            toEmployee.setOperatorType("tour_master");
            toEmployee.setCanAssignOrders(true);
            employeeMapper.update(toEmployee);
        }

        log.info("值班转移成功：{} -> {}", fromOperatorId, transferDutyDTO.getToOperatorId());
    }

    /**
     * 结束值班
     */
    @Override
    @Transactional
    public void endDuty(Long operatorId, String dutyType) {
        log.info("结束值班：操作员：{} 类型：{}", operatorId, dutyType);

        // 1. 获取当前值班记录
        DutyShift currentDuty = dutyShiftMapper.getCurrentDutyByType(dutyType);
        if (currentDuty == null || !currentDuty.getOperatorId().equals(operatorId)) {
            throw new BaseException("当前无有效的值班记录或无权限结束");
        }

        // 2. 完成值班记录
        dutyShiftMapper.complete(currentDuty.getId());

        // 3. 如果是排团主管，清除员工表状态（但要确保有接班人）
        if ("tour_master".equals(dutyType)) {
            // 注意：实际业务中排团主管不应该直接结束值班，应该转移给其他人
            log.warn("排团主管直接结束值班，请确保有接班安排");
        }

        log.info("值班结束成功：操作员：{}", operatorId);
    }

    /**
     * 获取当前排团主管信息
     */
    @Override
    public CurrentDutyStatusVO getCurrentTourMaster() {
        DutyShift currentDuty = dutyShiftMapper.getCurrentTourMaster();
        if (currentDuty == null) {
            return CurrentDutyStatusVO.builder()
                    .canTransfer(false)
                    .notes("当前无排团主管在值班")
                    .build();
        }

        long durationMinutes = ChronoUnit.MINUTES.between(currentDuty.getShiftStart(), LocalDateTime.now());

        return CurrentDutyStatusVO.builder()
                .currentTourMasterId(currentDuty.getOperatorId())
                .currentTourMasterName(currentDuty.getOperatorName())
                .shiftStart(currentDuty.getShiftStart())
                .durationMinutes(durationMinutes)
                .canTransfer(true)
                .notes(currentDuty.getNotes())
                .build();
    }

    /**
     * 获取当前值班状态（按类型）
     */
    @Override
    public DutyShiftVO getCurrentDutyByType(String dutyType) {
        DutyShift dutyShift = dutyShiftMapper.getCurrentDutyByType(dutyType);
        if (dutyShift == null) {
            return null;
        }

        DutyShiftVO vo = new DutyShiftVO();
        BeanUtils.copyProperties(dutyShift, vo);
        return vo;
    }

    /**
     * 获取操作员的值班历史
     */
    @Override
    public List<DutyShiftVO> getShiftHistoryByOperatorId(Long operatorId) {
        List<DutyShift> shifts = dutyShiftMapper.getByOperatorId(operatorId);
        
        return shifts.stream().map(shift -> {
            DutyShiftVO vo = new DutyShiftVO();
            BeanUtils.copyProperties(shift, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有值班记录
     */
    @Override
    public List<DutyShiftVO> getAllShifts() {
        List<DutyShift> shifts = dutyShiftMapper.getAll();
        
        return shifts.stream().map(shift -> {
            DutyShiftVO vo = new DutyShiftVO();
            BeanUtils.copyProperties(shift, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 检查值班冲突
     */
    @Override
    public boolean hasConflict(Long operatorId, String dutyType, LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime == null) {
            endTime = LocalDateTime.now().plusDays(1); // 假设最长值班24小时
        }
        
        Integer conflictCount = dutyShiftMapper.checkConflict(operatorId, dutyType, startTime, endTime);
        return conflictCount != null && conflictCount > 0;
    }

    /**
     * 获取值班统计信息
     */
    @Override
    public Object getShiftStatistics(Long operatorId) {
        return dutyShiftMapper.getShiftStatistics(operatorId);
    }

    /**
     * 检查操作员是否可以开始排团主管值班
     */
    @Override
    public boolean canStartTourMasterDuty(Long operatorId) {
        Employee employee = employeeMapper.getById(operatorId.intValue());
        if (employee == null) {
            return false;
        }

        // 检查是否有权限
        boolean hasPermission = "tour_master".equals(employee.getOperatorType()) || 
                               Boolean.TRUE.equals(employee.getCanAssignOrders());

        if (!hasPermission) {
            return false;
        }

        // 检查是否已有其他排团主管在值班
        DutyShift currentTourMaster = dutyShiftMapper.getCurrentTourMaster();
        return currentTourMaster == null;
    }

    /**
     * 获取可用的排团主管候选人
     */
    @Override
    public List<Object> getAvailableTourMasterCandidates() {
        // 这里需要在EmployeeMapper中实现查询有排团主管权限的员工
        // 暂时返回空列表
        return new ArrayList<>();
    }

    /**
     * 更新所有员工的排团主管状态
     */
    private void updateAllTourMasterStatus(boolean isTourMaster) {
        // 这里需要在EmployeeMapper中实现批量更新
        // 暂时不实现
    }
}
