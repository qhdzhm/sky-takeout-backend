package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.EmployeeGuideDTO;
import com.sky.exception.BaseException;
import com.sky.mapper.EmployeeGuideMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeGuideService;
import com.sky.vo.EmployeeGuideVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工-导游管理服务实现类
 */
@Service
@Slf4j
public class EmployeeGuideServiceImpl implements EmployeeGuideService {

    @Autowired
    private EmployeeGuideMapper employeeGuideMapper;

    @Override
    public PageResult pageQuery(EmployeeGuideDTO queryDTO) {
        log.info("分页查询员工-导游信息：{}", queryDTO);
        
        // 开始分页
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        
        // 执行查询
        Page<EmployeeGuideVO> page = (Page<EmployeeGuideVO>) employeeGuideMapper.pageQuery(queryDTO);
        
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public Long setEmployeeAsGuide(EmployeeGuideDTO employeeGuideDTO) {
        log.info("将员工设置为导游：{}", employeeGuideDTO);
        
        try {
            // 调用存储过程设置员工为导游
            Long guideId = employeeGuideMapper.setEmployeeAsGuide(
                employeeGuideDTO.getEmployeeId(),
                employeeGuideDTO.getLanguages(),
                employeeGuideDTO.getExperienceYears(),
                employeeGuideDTO.getHourlyRate(),
                employeeGuideDTO.getDailyRate(),
                employeeGuideDTO.getMaxGroups()
            );
            
            if (guideId == null) {
                throw new BaseException("设置导游失败");
            }
            
            // 如果有额外的导游信息，更新导游记录
            if (employeeGuideDTO.getLicenseNumber() != null || 
                employeeGuideDTO.getSpecialties() != null ||
                employeeGuideDTO.getEmergencyContact() != null) {
                
                employeeGuideDTO.setGuideId(guideId);
                employeeGuideMapper.updateGuideExtraInfo(employeeGuideDTO);
            }
            
            return guideId;
            
        } catch (Exception e) {
            log.error("设置员工为导游失败", e);
            throw new BaseException("设置导游失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeGuideRole(Long employeeId) {
        log.info("取消员工的导游身份：employeeId={}", employeeId);
        
        try {
            // 检查是否有未完成的分配
            int activeAssignments = employeeGuideMapper.countActiveAssignmentsByEmployeeId(employeeId);
            if (activeAssignments > 0) {
                throw new BaseException("该导游还有未完成的分配，无法取消导游身份");
            }
            
            // 更新员工表，取消导游标记
            employeeGuideMapper.removeGuideRole(employeeId);
            
            // 停用导游记录（不删除，保留历史）
            employeeGuideMapper.deactivateGuide(employeeId);
            
        } catch (Exception e) {
            log.error("取消导游身份失败", e);
            throw new BaseException("取消导游身份失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateGuideInfo(EmployeeGuideDTO employeeGuideDTO) {
        log.info("更新导游信息：{}", employeeGuideDTO);
        
        try {
            employeeGuideMapper.updateGuideInfo(employeeGuideDTO);
        } catch (Exception e) {
            log.error("更新导游信息失败", e);
            throw new BaseException("更新导游信息失败：" + e.getMessage());
        }
    }

    @Override
    public EmployeeGuideVO getGuideByEmployeeId(Long employeeId) {
        log.info("根据员工ID获取导游信息：employeeId={}", employeeId);
        
        try {
            return employeeGuideMapper.getGuideByEmployeeId(employeeId);
        } catch (Exception e) {
            log.error("获取导游信息失败", e);
            throw new BaseException("获取导游信息失败");
        }
    }

    @Override
    public List<EmployeeGuideVO> getAllGuideEmployees() {
        log.info("获取所有导游员工列表");
        
        try {
            return employeeGuideMapper.getAllGuideEmployees();
        } catch (Exception e) {
            log.error("获取导游员工列表失败", e);
            throw new BaseException("获取导游员工列表失败");
        }
    }

    @Override
    @Transactional
    public void batchSetGuideAvailability(Long guideId, LocalDate startDate, LocalDate endDate, 
                                          LocalTime startTime, LocalTime endTime, String status, String notes) {
        log.info("批量设置导游可用性：guideId={}, startDate={}, endDate={}, status={}", 
                 guideId, startDate, endDate, status);
        
        try {
            String result = employeeGuideMapper.batchSetGuideAvailability(
                guideId, startDate, endDate, startTime, endTime, status, notes);
            
            if (!result.contains("成功")) {
                throw new BaseException("批量设置可用性失败：" + result);
            }
            
        } catch (Exception e) {
            log.error("批量设置导游可用性失败", e);
            throw new BaseException("批量设置可用性失败：" + e.getMessage());
        }
    }

    @Override
    public List<EmployeeGuideVO> getGuideAvailabilityStats(LocalDate date) {
        log.info("获取导游可用性统计：date={}", date);
        
        try {
            return employeeGuideMapper.getGuideAvailabilityStats(date);
        } catch (Exception e) {
            log.error("获取导游可用性统计失败", e);
            throw new BaseException("获取可用性统计失败");
        }
    }
} 