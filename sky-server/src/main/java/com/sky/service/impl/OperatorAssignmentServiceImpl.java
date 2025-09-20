package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.AssignOrderDTO;
import com.sky.dto.OperatorAssignmentPageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.OperatorAssignment;
import com.sky.entity.TourBooking;
import com.sky.exception.BaseException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.HotelBookingMapper;
import com.sky.mapper.OperatorAssignmentMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.result.PageResult;
import com.sky.service.OperatorAssignmentService;
import com.sky.vo.OperatorAssignmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作员分配服务实现类
 */
@Service
@Slf4j
public class OperatorAssignmentServiceImpl implements OperatorAssignmentService {

    @Autowired
    private OperatorAssignmentMapper operatorAssignmentMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private HotelBookingMapper hotelBookingMapper;

    /**
     * 分配订单给操作员
     */
    @Override
    @Transactional
    public void assignOrder(AssignOrderDTO assignOrderDTO, Long assignedBy) {
        log.info("开始分配订单：{} 给操作员：{}", assignOrderDTO.getBookingId(), assignOrderDTO.getOperatorId());

        // 1. 验证排团主管权限
        if (!isTourMaster(assignedBy)) {
            throw new BaseException("只有排团主管才能分配订单");
        }

        // 2. 验证订单是否存在
        TourBooking booking = tourBookingMapper.getById(assignOrderDTO.getBookingId());
        if (booking == null) {
            throw new BaseException("订单不存在");
        }

        // 3. 验证操作员是否存在且为酒店专员
        Employee operator = employeeMapper.getById(assignOrderDTO.getOperatorId().intValue());
        if (operator == null) {
            throw new BaseException("操作员不存在");
        }
        if (!"hotel_operator".equals(operator.getOperatorType())) {
            throw new BaseException("只能分配给酒店专员");
        }

        // 4. 检查订单是否已经分配
        OperatorAssignment existingAssignment = operatorAssignmentMapper.getActiveByBookingId(assignOrderDTO.getBookingId());
        if (existingAssignment != null) {
            throw new BaseException("订单已经分配给其他操作员，请先取消现有分配");
        }

        // 5. 创建分配记录
        OperatorAssignment assignment = OperatorAssignment.builder()
                .bookingId(assignOrderDTO.getBookingId())
                .operatorId(assignOrderDTO.getOperatorId())
                .assignedBy(assignedBy)
                .assignmentType(assignOrderDTO.getAssignmentType())
                .notes(assignOrderDTO.getNotes())
                .status("active")
                .build();

        operatorAssignmentMapper.insert(assignment);

        // 6. 更新订单的分配状态
        TourBooking updateBooking = new TourBooking();
        updateBooking.setBookingId(assignOrderDTO.getBookingId());
        updateBooking.setAssignedOperatorId(assignOrderDTO.getOperatorId());
        updateBooking.setAssignedBy(assignedBy);
        updateBooking.setAssignedAt(LocalDateTime.now());
        updateBooking.setAssignmentStatus("assigned");
        tourBookingMapper.update(updateBooking);

        // 7. 同步更新相关酒店预订的酒店专员
        try {
            hotelBookingMapper.updateHotelSpecialistByTourBookingId(
                assignOrderDTO.getBookingId(), 
                operator.getUsername()
            );
            log.info("已同步更新酒店专员：订单 {} -> 专员 {}", assignOrderDTO.getBookingId(), operator.getUsername());
        } catch (Exception e) {
            log.warn("同步更新酒店专员失败，订单：{}，错误：{}", assignOrderDTO.getBookingId(), e.getMessage());
            // 不影响主流程，继续执行
        }

        log.info("订单分配成功：{} -> 操作员：{}", assignOrderDTO.getBookingId(), assignOrderDTO.getOperatorId());
    }

    /**
     * 重新分配订单
     */
    @Override
    @Transactional
    public void reassignOrder(Integer bookingId, Long newOperatorId, Long assignedBy, String notes) {
        log.info("开始重新分配订单：{} 给操作员：{}", bookingId, newOperatorId);

        // 1. 验证排团主管权限
        if (!isTourMaster(assignedBy)) {
            throw new BaseException("只有排团主管才能重新分配订单");
        }

        // 2. 获取当前分配记录
        OperatorAssignment currentAssignment = operatorAssignmentMapper.getActiveByBookingId(bookingId);
        if (currentAssignment == null) {
            throw new BaseException("订单未分配，无法重新分配");
        }

        // 3. 验证新操作员
        Employee newOperator = employeeMapper.getById(newOperatorId.intValue());
        if (newOperator == null) {
            throw new BaseException("新操作员不存在");
        }
        if (!"hotel_operator".equals(newOperator.getOperatorType())) {
            throw new BaseException("只能分配给酒店专员");
        }

        // 4. 标记当前分配为已转移
        operatorAssignmentMapper.transfer(currentAssignment.getId(), newOperatorId);

        // 5. 创建新的分配记录
        OperatorAssignment newAssignment = OperatorAssignment.builder()
                .bookingId(bookingId)
                .operatorId(newOperatorId)
                .assignedBy(assignedBy)
                .assignmentType("hotel_management")
                .notes(notes != null ? notes : "重新分配")
                .status("active")
                .build();

        operatorAssignmentMapper.insert(newAssignment);

        // 6. 更新订单信息
        TourBooking updateBooking = new TourBooking();
        updateBooking.setBookingId(bookingId);
        updateBooking.setAssignedOperatorId(newOperatorId);
        updateBooking.setAssignedBy(assignedBy);
        updateBooking.setAssignedAt(LocalDateTime.now());
        updateBooking.setAssignmentStatus("assigned");
        tourBookingMapper.update(updateBooking);

        // 7. 同步更新相关酒店预订的酒店专员
        try {
            hotelBookingMapper.updateHotelSpecialistByTourBookingId(
                bookingId, 
                newOperator.getUsername()
            );
            log.info("已同步更新酒店专员：订单 {} -> 专员 {}", bookingId, newOperator.getUsername());
        } catch (Exception e) {
            log.warn("同步更新酒店专员失败，订单：{}，错误：{}", bookingId, e.getMessage());
            // 不影响主流程，继续执行
        }

        log.info("订单重新分配成功：{} -> 操作员：{}", bookingId, newOperatorId);
    }

    /**
     * 取消分配
     */
    @Override
    @Transactional
    public void cancelAssignment(Integer bookingId, Long operatorId) {
        log.info("开始取消订单分配：{}", bookingId);

        // 1. 验证排团主管权限
        if (!isTourMaster(operatorId)) {
            throw new BaseException("只有排团主管才能取消分配");
        }

        // 2. 获取当前分配记录
        OperatorAssignment assignment = operatorAssignmentMapper.getActiveByBookingId(bookingId);
        if (assignment == null) {
            throw new BaseException("订单未分配，无法取消");
        }

        // 3. 更新分配状态
        operatorAssignmentMapper.updateStatus(assignment.getId(), "cancelled");

        // 4. 更新订单状态
        TourBooking updateBooking = new TourBooking();
        updateBooking.setBookingId(bookingId);
        updateBooking.setAssignedOperatorId(null);
        updateBooking.setAssignedBy(null);
        updateBooking.setAssignedAt(null);
        updateBooking.setAssignmentStatus("unassigned");
        tourBookingMapper.update(updateBooking);

        // 5. 同步清除相关酒店预订的酒店专员
        try {
            hotelBookingMapper.updateHotelSpecialistByTourBookingId(bookingId, null);
            log.info("已同步清除酒店专员：订单 {}", bookingId);
        } catch (Exception e) {
            log.warn("同步清除酒店专员失败，订单：{}，错误：{}", bookingId, e.getMessage());
            // 不影响主流程，继续执行
        }

        log.info("订单分配取消成功：{}", bookingId);
    }

    /**
     * 完成分配任务
     */
    @Override
    @Transactional
    public void completeAssignment(Integer bookingId, Long operatorId) {
        log.info("操作员：{} 完成订单：{} 的任务", operatorId, bookingId);

        // 1. 获取分配记录
        OperatorAssignment assignment = operatorAssignmentMapper.getActiveByBookingId(bookingId);
        if (assignment == null) {
            throw new BaseException("订单未分配");
        }

        // 2. 验证权限（只有分配给自己的订单或排团主管可以标记完成）
        if (!assignment.getOperatorId().equals(operatorId) && !isTourMaster(operatorId)) {
            throw new BaseException("无权限操作该订单");
        }

        // 3. 更新状态为已完成
        operatorAssignmentMapper.updateStatus(assignment.getId(), "completed");

        log.info("订单任务完成：{}", bookingId);
    }

    /**
     * 根据订单ID获取当前分配信息
     */
    @Override
    public OperatorAssignmentVO getAssignmentByBookingId(Integer bookingId) {
        OperatorAssignment assignment = operatorAssignmentMapper.getActiveByBookingId(bookingId);
        if (assignment == null) {
            return null;
        }

        OperatorAssignmentVO vo = new OperatorAssignmentVO();
        BeanUtils.copyProperties(assignment, vo);
        return vo;
    }

    /**
     * 获取操作员的所有分配任务
     */
    @Override
    public List<OperatorAssignmentVO> getAssignmentsByOperatorId(Long operatorId) {
        List<OperatorAssignment> assignments = operatorAssignmentMapper.getActiveByOperatorId(operatorId);
        
        return assignments.stream().map(assignment -> {
            OperatorAssignmentVO vo = new OperatorAssignmentVO();
            BeanUtils.copyProperties(assignment, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询分配记录
     */
    @Override
    public PageResult pageQuery(OperatorAssignmentPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        
        // 这里需要在Mapper中实现复杂查询，暂时返回所有有效分配
        List<OperatorAssignment> assignments = operatorAssignmentMapper.getAllActive();
        Page<OperatorAssignment> page = (Page<OperatorAssignment>) assignments;

        List<OperatorAssignmentVO> voList = assignments.stream().map(assignment -> {
            OperatorAssignmentVO vo = new OperatorAssignmentVO();
            BeanUtils.copyProperties(assignment, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), voList);
    }

    /**
     * 获取所有有效的分配记录
     */
    @Override
    public List<OperatorAssignmentVO> getAllActiveAssignments() {
        List<OperatorAssignment> assignments = operatorAssignmentMapper.getAllActive();
        
        return assignments.stream().map(assignment -> {
            OperatorAssignmentVO vo = new OperatorAssignmentVO();
            BeanUtils.copyProperties(assignment, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 检查操作员是否有权限操作指定订单
     */
    @Override
    public boolean hasPermission(Long operatorId, Integer bookingId) {
        // 1. 如果是排团主管，有所有权限
        if (isTourMaster(operatorId)) {
            return true;
        }

        // 2. 检查是否分配给该操作员
        OperatorAssignment assignment = operatorAssignmentMapper.getActiveByBookingId(bookingId);
        return assignment != null && assignment.getOperatorId().equals(operatorId);
    }

    /**
     * 检查操作员是否为排团主管
     */
    @Override
    public boolean isTourMaster(Long operatorId) {
        Employee employee = employeeMapper.getById(operatorId.intValue());
        return employee != null && 
               "tour_master".equals(employee.getOperatorType()) && 
               Boolean.TRUE.equals(employee.getIsTourMaster());
    }

    /**
     * 获取操作员工作量统计
     */
    @Override
    public Object getWorkloadStatistics(Long operatorId) {
        return operatorAssignmentMapper.getWorkloadStatistics(operatorId);
    }
}
