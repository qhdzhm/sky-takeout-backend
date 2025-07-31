package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.TourGuideVehicleAssignmentDTO;
import com.sky.entity.Guide;
import com.sky.entity.TourGuideVehicleAssignment;
import com.sky.entity.Vehicle;
import com.sky.exception.BaseException;
import com.sky.mapper.GuideMapper;
import com.sky.mapper.GuideAvailabilityMapper;
import com.sky.mapper.TourGuideVehicleAssignmentMapper;
import com.sky.mapper.VehicleMapper;
import com.sky.mapper.VehicleAvailabilityMapper;
import com.sky.result.PageResult;
import com.sky.service.TourGuideVehicleAssignmentService;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;
import com.sky.vo.TourGuideVehicleAssignmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 导游车辆游客分配Service实现类
 */
@Service
@Slf4j
public class TourGuideVehicleAssignmentServiceImpl implements TourGuideVehicleAssignmentService {

    @Autowired
    private TourGuideVehicleAssignmentMapper assignmentMapper;

    @Autowired
    private GuideMapper guideMapper;
    
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private GuideAvailabilityMapper guideAvailabilityMapper;
    
    @Autowired
    private VehicleAvailabilityMapper vehicleAvailabilityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取可用导游列表 - 基于 guide_availability 表
     */
    @Override
    public List<GuideAvailabilityVO> getAvailableGuides(LocalDate date, LocalTime startTime, LocalTime endTime, String location) {
        log.info("获取可用导游列表：日期={}, 开始时间={}, 结束时间={}, 地点={}", date, startTime, endTime, location);
        
        // 直接从 guide_availability 表查询可用导游
        List<GuideAvailabilityVO> availableGuides = guideAvailabilityMapper.getAvailableGuidesByDateTime(date, startTime, endTime);
        
        log.info("从 guide_availability 表找到可用导游数量：{}", availableGuides.size());
        return availableGuides;
    }

    /**
     * 获取可用车辆列表 - 基于 vehicle_availability 表
     */
    @Override
    public List<VehicleAvailabilityVO> getAvailableVehicles(LocalDate date, LocalTime startTime, LocalTime endTime, Integer peopleCount) {
        log.info("获取可用车辆列表：日期={}, 开始时间={}, 结束时间={}, 人数={}", date, startTime, endTime, peopleCount);
        
        // 直接从 vehicle_availability 表查询可用车辆
        List<VehicleAvailabilityVO> availableVehicles = vehicleAvailabilityMapper.getAvailableVehiclesByDateTime(date, startTime, endTime, peopleCount);
        
        log.info("从 vehicle_availability 表找到可用车辆数量：{}", availableVehicles.size());
        return availableVehicles;
    }

    /**
     * 创建分配记录
     * 包含业务逻辑：检查导游和车辆可用性，更新状态，保存分配记录
     */
    @Override
    @Transactional
    public Long createAssignment(TourGuideVehicleAssignmentDTO assignmentDTO) {
        log.info("创建导游车辆分配记录：{}", assignmentDTO);

        // 1. 数据验证
        validateAssignmentData(assignmentDTO);

        // 获取导游信息以获取正确的guide_id
        log.info("开始获取导游信息，导游ID：{}", assignmentDTO.getGuideId());
        Guide guide = guideMapper.getGuideById(assignmentDTO.getGuideId());
        if (guide == null) {
            log.error("导游不存在，导游ID：{}", assignmentDTO.getGuideId());
            throw new BaseException("导游不存在");
        }
        log.info("导游信息获取成功：{}", guide.getName());

        // 2. 检查导游可用性（使用guide_id）
        if (checkGuideAssigned(guide.getGuideId().longValue(), assignmentDTO.getAssignmentDate())) {
            throw new BaseException("导游在指定日期已有分配，无法重复分配");
        }

        // 3. 检查车辆可用性
        if (checkVehicleAssigned(assignmentDTO.getVehicleId(), assignmentDTO.getAssignmentDate())) {
            throw new BaseException("车辆在指定日期已有分配，无法重复分配");
        }

        // 4. 获取车辆详细信息
        log.info("开始获取车辆信息，车辆ID：{}", assignmentDTO.getVehicleId());
        Vehicle vehicle = vehicleMapper.getById(assignmentDTO.getVehicleId());
        if (vehicle == null) {
            log.error("车辆不存在，车辆ID：{}", assignmentDTO.getVehicleId());
            throw new BaseException("车辆不存在");
        }
        log.info("车辆信息获取成功：{}", vehicle.getLicensePlate());

        // 5. 检查车辆座位数是否足够
        log.info("检查车辆座位数，座位数：{}，需要人数：{}", vehicle.getSeatCount(), assignmentDTO.getTotalPeople());
        if (vehicle.getSeatCount() < assignmentDTO.getTotalPeople()) {
            log.error("车辆座位数不足，座位数：{}，需要人数：{}", vehicle.getSeatCount(), assignmentDTO.getTotalPeople());
            throw new BaseException("车辆座位数不足，无法分配");
        }

        // 6. 构建分配实体
        log.info("开始构建分配实体");
        TourGuideVehicleAssignment assignment = buildAssignmentEntity(assignmentDTO, guide, vehicle);
        log.info("分配实体构建完成");

        // 7. 保存分配记录
        log.info("开始保存分配记录到数据库");
        try {
            assignmentMapper.insert(assignment);
            log.info("分配记录保存成功，分配ID：{}", assignment.getId());
        } catch (Exception e) {
            log.error("保存分配记录失败，错误信息：{}", e.getMessage(), e);
            throw new BaseException("保存分配记录失败：" + e.getMessage());
        }

        // 8. 更新导游状态（设置为忙碌）
        log.info("开始更新导游状态");
        updateGuideStatus(guide.getGuideId().longValue(), assignmentDTO.getAssignmentDate(), true);

        // 9. 更新车辆状态（设置为已分配）
        log.info("开始更新车辆状态");
        updateVehicleStatus(assignmentDTO.getVehicleId(), assignmentDTO.getAssignmentDate(), true);

        return assignment.getId();
    }

    /**
     * 批量创建分配记录
     */
    @Override
    @Transactional
    public void batchCreateAssignment(List<TourGuideVehicleAssignmentDTO> assignmentDTOs) {
        log.info("批量创建导游车辆分配记录，数量：{}", assignmentDTOs.size());

        if (assignmentDTOs == null || assignmentDTOs.isEmpty()) {
            throw new BaseException("分配记录列表不能为空");
        }

        // 检查重复分配
        validateBatchAssignments(assignmentDTOs);

        List<TourGuideVehicleAssignment> assignments = new ArrayList<>();
        Set<Long> assignedGuides = new HashSet<>();
        Set<Long> assignedVehicles = new HashSet<>();

        for (TourGuideVehicleAssignmentDTO dto : assignmentDTOs) {
            // 验证数据
            validateAssignmentData(dto);

            // 获取导游和车辆信息
            Guide guide = guideMapper.getGuideById(dto.getGuideId());
            Vehicle vehicle = vehicleMapper.getById(dto.getVehicleId());

            if (guide == null || vehicle == null) {
                throw new BaseException("导游或车辆不存在");
            }

            // 构建实体
            TourGuideVehicleAssignment assignment = buildAssignmentEntity(dto, guide, vehicle);
            assignments.add(assignment);

            assignedGuides.add(dto.getGuideId());
            assignedVehicles.add(dto.getVehicleId());
        }

        // 批量保存
        assignmentMapper.batchInsert(assignments);

        // 批量更新导游和车辆状态
        for (TourGuideVehicleAssignmentDTO dto : assignmentDTOs) {
            // 获取对应的Guide对象以获取正确的guide_id
            Guide guide = guideMapper.getGuideById(dto.getGuideId());
            updateGuideStatus(guide.getGuideId().longValue(), dto.getAssignmentDate(), true);
            updateVehicleStatus(dto.getVehicleId(), dto.getAssignmentDate(), true);
        }

        log.info("批量分配完成，共处理 {} 条记录", assignments.size());
    }

    /**
     * 根据ID查询分配记录
     */
    @Override
    public TourGuideVehicleAssignmentVO getById(Long id) {
        return assignmentMapper.getById(id);
    }

    /**
     * 根据日期查询分配记录
     */
    @Override
    public List<TourGuideVehicleAssignmentVO> getByDate(LocalDate assignmentDate) {
        return assignmentMapper.getByDate(assignmentDate);
    }

    /**
     * 根据日期范围查询分配记录
     */
    @Override
    public List<TourGuideVehicleAssignmentVO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return assignmentMapper.getByDateRange(startDate, endDate);
    }

    /**
     * 根据目的地查询分配记录
     */
    @Override
    public List<TourGuideVehicleAssignmentVO> getByDestination(String destination, LocalDate assignmentDate) {
        return assignmentMapper.getByDestination(destination, assignmentDate);
    }

    /**
     * 根据导游ID查询分配记录
     */
    @Override
    public List<TourGuideVehicleAssignmentVO> getByGuideId(Long guideId, LocalDate assignmentDate) {
        return assignmentMapper.getByGuideId(guideId, assignmentDate);
    }

    /**
     * 根据车辆ID查询分配记录
     */
    @Override
    public List<TourGuideVehicleAssignmentVO> getByVehicleId(Long vehicleId, LocalDate assignmentDate) {
        return assignmentMapper.getByVehicleId(vehicleId, assignmentDate);
    }

    /**
     * 更新分配记录
     */
    @Override
    @Transactional
    public void updateAssignment(Long id, TourGuideVehicleAssignmentDTO assignmentDTO) {
        log.info("更新分配记录，ID：{}，数据：{}", id, assignmentDTO);

        // 获取现有记录
        TourGuideVehicleAssignmentVO existingAssignment = assignmentMapper.getById(id);
        if (existingAssignment == null) {
            throw new BaseException("分配记录不存在");
        }

        // 数据验证
        validateAssignmentData(assignmentDTO);

        // 获取导游和车辆信息
        Guide guide = guideMapper.getGuideById(assignmentDTO.getGuideId());
        Vehicle vehicle = vehicleMapper.getById(assignmentDTO.getVehicleId());

        if (guide == null || vehicle == null) {
            throw new BaseException("导游或车辆不存在");
        }

        // 检查导游和车辆是否发生变化
        boolean guideChanged = !existingAssignment.getGuide().getGuideId().equals(guide.getGuideId().longValue());
        boolean vehicleChanged = !existingAssignment.getVehicle().getVehicleId().equals(assignmentDTO.getVehicleId());

        // 如果导游或车辆发生变化，需要检查新资源的可用性
        if (guideChanged && checkGuideAssigned(guide.getGuideId().longValue(), assignmentDTO.getAssignmentDate())) {
            throw new BaseException("导游在指定日期已有分配，无法重复分配");
            }
        if (vehicleChanged && checkVehicleAssigned(assignmentDTO.getVehicleId(), assignmentDTO.getAssignmentDate())) {
            throw new BaseException("车辆在指定日期已有分配，无法重复分配");
        }

        // 构建更新实体
        TourGuideVehicleAssignment assignment = buildAssignmentEntity(assignmentDTO, guide, vehicle);
        assignment.setId(id);
        assignment.setUpdatedTime(LocalDateTime.now());
        assignment.setUpdatedBy(BaseContext.getCurrentId());

        // 更新记录
        assignmentMapper.update(assignment);

        // 更新资源状态
        if (guideChanged) {
            // 释放原导游
            updateGuideStatus(existingAssignment.getGuide().getGuideId(),
                existingAssignment.getAssignmentDate(), false);
            // 分配新导游
            updateGuideStatus(guide.getGuideId().longValue(), 
                assignmentDTO.getAssignmentDate(), true);
        }

        if (vehicleChanged) {
            // 释放原车辆
            updateVehicleStatus(existingAssignment.getVehicle().getVehicleId(),
                existingAssignment.getAssignmentDate(), false);
            // 分配新车辆
            updateVehicleStatus(vehicle.getVehicleId(), 
                assignmentDTO.getAssignmentDate(), true);
        }

        log.info("分配记录更新成功，ID: {}", id);
    }

    /**
     * 取消分配
     */
    @Override
    @Transactional
    public void cancelAssignment(Long id) {
        log.info("取消分配，ID: {}", id);

        TourGuideVehicleAssignmentVO assignment = assignmentMapper.getById(id);
        if (assignment == null) {
            throw new BaseException("分配记录不存在");
        }

        // 更新状态为已取消
        TourGuideVehicleAssignment updateEntity = new TourGuideVehicleAssignment();
        updateEntity.setId(id);
        updateEntity.setStatus("cancelled");
        updateEntity.setUpdatedTime(LocalDateTime.now());
        updateEntity.setUpdatedBy(BaseContext.getCurrentId());

        assignmentMapper.update(updateEntity);

        // 释放导游和车辆资源
        if (!"cancelled".equals(assignment.getAssignmentStatus())) {
            updateGuideStatus(assignment.getGuide().getGuideId(), assignment.getAssignmentDate(), false);
            updateVehicleStatus(assignment.getVehicle().getVehicleId(), assignment.getAssignmentDate(), false);
        }

        log.info("分配已取消，ID: {}", id);
    }

    /**
     * 删除分配
     */
    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        log.info("删除分配，ID: {}", id);

        TourGuideVehicleAssignmentVO assignment = assignmentMapper.getById(id);
        if (assignment == null) {
            throw new BaseException("分配记录不存在");
        }

        // 先释放资源
        if (!"cancelled".equals(assignment.getAssignmentStatus())) {
            updateGuideStatus(assignment.getGuide().getGuideId(), assignment.getAssignmentDate(), false);
            updateVehicleStatus(assignment.getVehicle().getVehicleId(), assignment.getAssignmentDate(), false);
        }

        // 删除记录
        assignmentMapper.deleteById(id);
        log.info("分配已删除，ID: {}", id);
    }

    /**
     * 根据订单ID列表查询分配记录
     */
    @Override
    public List<TourGuideVehicleAssignmentVO> getByBookingIds(List<Long> bookingIds) {
        return assignmentMapper.getByBookingIds(bookingIds);
    }

    /**
     * 分页查询分配记录
     */
    @Override
    public PageResult pageQuery(int page, int pageSize, LocalDate startDate, LocalDate endDate,
                               String destination, String guideName, String licensePlate, String status) {
        PageHelper.startPage(page, pageSize);

        Page<TourGuideVehicleAssignmentVO> pageResult = (Page<TourGuideVehicleAssignmentVO>) 
            assignmentMapper.pageQuery(startDate, endDate, destination, guideName, licensePlate, status);

        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    /**
     * 统计指定日期的分配数量
     */
    @Override
    public int countByDate(LocalDate assignmentDate) {
        return assignmentMapper.countByDate(assignmentDate);
    }

    /**
     * 检查导游在指定日期是否已有分配
     */
    @Override
    public boolean checkGuideAssigned(Long guideId, LocalDate assignmentDate) {
        return assignmentMapper.checkGuideAssigned(guideId, assignmentDate);
    }

    /**
     * 检查车辆在指定日期是否已有分配
     */
    @Override
    public boolean checkVehicleAssigned(Long vehicleId, LocalDate assignmentDate) {
        return assignmentMapper.checkVehicleAssigned(vehicleId, assignmentDate);
    }

    /**
     * 获取指定日期的分配统计信息
     */
    @Override
    public AssignmentStatistics getAssignmentStatistics(LocalDate assignmentDate) {
        List<TourGuideVehicleAssignmentVO> assignments = assignmentMapper.getByDate(assignmentDate);

        AssignmentStatistics statistics = new AssignmentStatistics();
        statistics.setTotalAssignments(assignments.size());
        statistics.setTotalGuides((int) assignments.stream()
            .map(a -> a.getGuide().getGuideId()).distinct().count());
        statistics.setTotalVehicles((int) assignments.stream()
            .map(a -> a.getVehicle().getVehicleId()).distinct().count());
        statistics.setTotalPeople(assignments.stream()
            .mapToInt(TourGuideVehicleAssignmentVO::getTotalPeople).sum());
        statistics.setDestinations(assignments.stream()
            .map(TourGuideVehicleAssignmentVO::getDestination).distinct().collect(Collectors.toList()));

        return statistics;
    }

    // ============ 私有辅助方法 ============

    /**
     * 验证分配数据
     */
    private void validateAssignmentData(TourGuideVehicleAssignmentDTO assignmentDTO) {
        if (assignmentDTO.getAssignmentDate() == null) {
            throw new BaseException("分配日期不能为空");
        }
        if (assignmentDTO.getGuideId() == null) {
            throw new BaseException("导游ID不能为空");
        }
        if (assignmentDTO.getVehicleId() == null) {
            throw new BaseException("车辆ID不能为空");
        }
        if (assignmentDTO.getTotalPeople() == null || assignmentDTO.getTotalPeople() <= 0) {
            throw new BaseException("总人数必须大于0");
        }
        // 验证目的地不能为空
        if (assignmentDTO.getDestination() == null || assignmentDTO.getDestination().trim().isEmpty()) {
            throw new BaseException("目的地不能为空");
        }
    }

    /**
     * 验证批量分配数据
     */
    private void validateBatchAssignments(List<TourGuideVehicleAssignmentDTO> assignmentDTOs) {
        Set<String> guideVehicleDateCombinations = new HashSet<>();
        
        for (TourGuideVehicleAssignmentDTO dto : assignmentDTOs) {
            // 获取导游信息以获取正确的guide_id
            Guide guide = guideMapper.getGuideById(dto.getGuideId());
            if (guide == null) {
                throw new BaseException("导游不存在，guide_id: " + dto.getGuideId());
            }
            
            String guideKey = guide.getGuideId() + "-" + dto.getAssignmentDate();
            String vehicleKey = dto.getVehicleId() + "-" + dto.getAssignmentDate();
            
            if (guideVehicleDateCombinations.contains(guideKey)) {
                throw new BaseException("批量分配中存在重复的导游日期组合");
            }
            if (guideVehicleDateCombinations.contains(vehicleKey)) {
                throw new BaseException("批量分配中存在重复的车辆日期组合");
            }
            
            guideVehicleDateCombinations.add(guideKey);
            guideVehicleDateCombinations.add(vehicleKey);

            // 检查现有分配（使用guide_id）
            if (checkGuideAssigned(guide.getGuideId().longValue(), dto.getAssignmentDate())) {
                throw new BaseException("导游在 " + dto.getAssignmentDate() + " 已有分配");
            }
            if (checkVehicleAssigned(dto.getVehicleId(), dto.getAssignmentDate())) {
                throw new BaseException("车辆在 " + dto.getAssignmentDate() + " 已有分配");
            }
        }
    }

    /**
     * 构建分配实体
     */
    private TourGuideVehicleAssignment buildAssignmentEntity(TourGuideVehicleAssignmentDTO dto, 
                                                            Guide guide, Vehicle vehicle) {
        TourGuideVehicleAssignment assignment = new TourGuideVehicleAssignment();
        BeanUtils.copyProperties(dto, assignment);

        // 设置导游信息
        assignment.setGuideId(guide.getGuideId().longValue());
        assignment.setGuideName(guide.getName());

        // 设置车辆信息
        assignment.setVehicleId(vehicle.getVehicleId());
        assignment.setLicensePlate(vehicle.getLicensePlate());
        assignment.setVehicleType(vehicle.getVehicleType());
        assignment.setSeatCount(vehicle.getSeatCount());

        // 转换JSON字段
        try {
            if (dto.getBookingIds() != null) {
                assignment.setBookingIds(objectMapper.writeValueAsString(dto.getBookingIds()));
            }
            if (dto.getTourScheduleOrderIds() != null) {
                assignment.setTourScheduleOrderIds(objectMapper.writeValueAsString(dto.getTourScheduleOrderIds()));
            }
            if (dto.getPassengerDetails() != null) {
                assignment.setPassengerDetails(objectMapper.writeValueAsString(dto.getPassengerDetails()));
            }
        } catch (JsonProcessingException e) {
            throw new BaseException("JSON数据转换失败");
        }

        // 设置状态和时间
        assignment.setStatus("confirmed");
        assignment.setCreatedTime(LocalDateTime.now());
        assignment.setUpdatedTime(LocalDateTime.now());
        assignment.setCreatedBy(BaseContext.getCurrentId());
        assignment.setUpdatedBy(BaseContext.getCurrentId());

        return assignment;
    }

    /**
     * 更新导游状态
     */
    private void updateGuideStatus(Long guideId, LocalDate assignmentDate, boolean assigned) {
        log.info("更新导游状态：导游ID={}，日期={}，已分配={}", guideId, assignmentDate, assigned);
        
        try {
            // 更新 guide_availability 表的状态
            if (assigned) {
                // 设置为忙碌状态
                guideAvailabilityMapper.updateAvailability(guideId, assignmentDate, 
                    LocalTime.of(8, 0), LocalTime.of(18, 0), false, 1);
            } else {
                // 先确保导游在该日期有可用性记录
                guideAvailabilityMapper.ensureAvailabilityRecord(guideId, assignmentDate);
                // 重置为可用状态
                guideAvailabilityMapper.resetAvailability(guideId, assignmentDate);
            }
            log.info("导游状态更新成功");
        } catch (Exception e) {
            log.error("更新导游状态失败：{}", e.getMessage());
        }
    }

    /**
     * 更新车辆状态
     */
    private void updateVehicleStatus(Long vehicleId, LocalDate assignmentDate, boolean assigned) {
        log.info("更新车辆状态：车辆ID={}，日期={}，已分配={}", vehicleId, assignmentDate, assigned);
        
        try {
            // 更新 vehicle_availability 表的状态
            if (assigned) {
                // 设置为使用中状态
                vehicleAvailabilityMapper.setInUse(vehicleId, assignmentDate);
            } else {
                // 重置为可用状态
                vehicleAvailabilityMapper.resetAvailability(vehicleId, assignmentDate);
            }
            log.info("车辆状态更新成功");
        } catch (Exception e) {
            log.error("更新车辆状态失败：{}", e.getMessage());
        }
    }
} 