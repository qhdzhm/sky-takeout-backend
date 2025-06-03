package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.GuideAssignmentDTO;
import com.sky.dto.GuideAssignmentQueryDTO;
import com.sky.entity.GuideDailyAssignment;
import com.sky.entity.GuideAssignmentOrder;
import com.sky.exception.BaseException;
import com.sky.mapper.GuideAssignmentMapper;
import com.sky.mapper.GuideAssignmentOrderMapper;
import com.sky.result.PageResult;
import com.sky.service.GuideAssignmentService;
import com.sky.vo.GuideAssignmentVO;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导游分配服务实现类
 */
@Service
@Slf4j
public class GuideAssignmentServiceImpl implements GuideAssignmentService {

    @Autowired
    private GuideAssignmentMapper guideAssignmentMapper;
    
    @Autowired
    private GuideAssignmentOrderMapper guideAssignmentOrderMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult pageQuery(GuideAssignmentQueryDTO queryDTO) {
        log.info("分页查询导游分配记录：{}", queryDTO);
        
        // 开始分页
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        
        // 执行查询
        Page<GuideAssignmentVO> page = (Page<GuideAssignmentVO>) guideAssignmentMapper.pageQuery(queryDTO);
        
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<GuideAvailabilityVO> getAvailableGuides(LocalDate date, LocalTime startTime, LocalTime endTime, String location) {
        log.info("获取可用导游列表：日期={}, 开始时间={}, 结束时间={}, 地点={}", date, startTime, endTime, location);
        
        try {
            // 查询可用导游
            List<GuideAvailabilityVO> availableGuides = guideAssignmentMapper.getAvailableGuides(date, startTime, endTime, location);
            
            // 为每个导游设置推荐状态和推荐原因
            availableGuides.forEach(guide -> {
                // 根据语言能力、经验等因素设置推荐状态
                boolean isRecommended = calculateGuideRecommendation(guide, location);
                guide.setRecommended(isRecommended);
                
                if (isRecommended) {
                    guide.setRecommendReason(generateGuideRecommendReason(guide, location));
                }
            });
            
            // 按推荐状态和经验年数排序
            return availableGuides.stream()
                    .sorted((g1, g2) -> {
                        // 推荐的导游排在前面
                        if (g1.getRecommended() && !g2.getRecommended()) return -1;
                        if (!g1.getRecommended() && g2.getRecommended()) return 1;
                        // 经验丰富的排在前面
                        return Integer.compare(g2.getExperienceYears() != null ? g2.getExperienceYears() : 0,
                                             g1.getExperienceYears() != null ? g1.getExperienceYears() : 0);
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取可用导游列表失败", e);
            throw new BaseException("获取可用导游列表失败");
        }
    }

    @Override
    public List<VehicleAvailabilityVO> getAvailableVehicles(LocalDate date, LocalTime startTime, LocalTime endTime, Integer peopleCount) {
        log.info("获取可用车辆列表：日期={}, 开始时间={}, 结束时间={}, 人数={}", date, startTime, endTime, peopleCount);
        
        try {
            // 查询可用车辆
            List<VehicleAvailabilityVO> availableVehicles = guideAssignmentMapper.getAvailableVehicles(date, startTime, endTime, peopleCount);
            
            // 为每个车辆设置推荐状态
            availableVehicles.forEach(vehicle -> {
                boolean isRecommended = calculateVehicleRecommendation(vehicle, peopleCount);
                vehicle.setRecommended(isRecommended);
                
                if (isRecommended) {
                    vehicle.setRecommendReason(generateVehicleRecommendReason(vehicle, peopleCount));
                }
            });
            
            // 按推荐状态和座位数排序
            return availableVehicles.stream()
                    .sorted((v1, v2) -> {
                        // 推荐的车辆排在前面
                        if (v1.getRecommended() && !v2.getRecommended()) return -1;
                        if (!v1.getRecommended() && v2.getRecommended()) return 1;
                        // 座位数接近需求的排在前面
                        int diff1 = Math.abs((v1.getSeatCount() != null ? v1.getSeatCount() : 0) - peopleCount);
                        int diff2 = Math.abs((v2.getSeatCount() != null ? v2.getSeatCount() : 0) - peopleCount);
                        return Integer.compare(diff1, diff2);
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取可用车辆列表失败", e);
            throw new BaseException("获取可用车辆列表失败");
        }
    }

    @Override
    @Transactional
    public GuideAssignmentVO autoAssign(GuideAssignmentDTO assignmentDTO) {
        log.info("自动分配导游和车辆：{}", assignmentDTO);
        
        try {
            // 1. 获取可用导游和车辆
            List<GuideAvailabilityVO> availableGuides = getAvailableGuides(
                assignmentDTO.getAssignmentDate(),
                assignmentDTO.getStartTime(),
                assignmentDTO.getEndTime(),
                assignmentDTO.getLocation()
            );
            
            List<VehicleAvailabilityVO> availableVehicles = getAvailableVehicles(
                assignmentDTO.getAssignmentDate(),
                assignmentDTO.getStartTime(),
                assignmentDTO.getEndTime(),
                assignmentDTO.getTotalPeople()
            );
            
            if (CollectionUtils.isEmpty(availableGuides)) {
                throw new BaseException("当前时间段没有可用导游");
            }
            
            if (CollectionUtils.isEmpty(availableVehicles)) {
                throw new BaseException("当前时间段没有可用车辆");
            }
            
            // 2. 选择最佳导游和车辆（选择推荐的或第一个）
            GuideAvailabilityVO selectedGuide = availableGuides.stream()
                    .filter(GuideAvailabilityVO::getRecommended)
                    .findFirst()
                    .orElse(availableGuides.get(0));
                    
            VehicleAvailabilityVO selectedVehicle = availableVehicles.stream()
                    .filter(VehicleAvailabilityVO::getRecommended)
                    .findFirst()
                    .orElse(availableVehicles.get(0));
            
            // 3. 设置分配信息
            assignmentDTO.setGuideId(selectedGuide.getGuideId());
            assignmentDTO.setVehicleId(selectedVehicle.getVehicleId());
            
            // 4. 执行手动分配逻辑
            return manualAssign(assignmentDTO);
            
        } catch (Exception e) {
            log.error("自动分配失败", e);
            throw new BaseException("自动分配失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public GuideAssignmentVO manualAssign(GuideAssignmentDTO assignmentDTO) {
        log.info("手动分配导游和车辆：{}", assignmentDTO);
        
        try {
            // 1. 验证导游和车辆的可用性
            if (assignmentDTO.getGuideId() != null) {
                Boolean guideAvailable = guideAssignmentMapper.checkGuideAvailability(
                    assignmentDTO.getGuideId(),
                    assignmentDTO.getAssignmentDate(),
                    assignmentDTO.getStartTime(),
                    assignmentDTO.getEndTime()
                );
                if (!Boolean.TRUE.equals(guideAvailable)) {
                    throw new BaseException("选择的导游在该时间段不可用");
                }
            }
            
            if (assignmentDTO.getVehicleId() != null) {
                Boolean vehicleAvailable = guideAssignmentMapper.checkVehicleAvailability(
                    assignmentDTO.getVehicleId(),
                    assignmentDTO.getAssignmentDate(),
                    assignmentDTO.getStartTime(),
                    assignmentDTO.getEndTime()
                );
                if (!Boolean.TRUE.equals(vehicleAvailable)) {
                    throw new BaseException("选择的车辆在该时间段不可用");
                }
            }
            
            // 2. 创建分配记录
            GuideDailyAssignment assignment = new GuideDailyAssignment();
            BeanUtils.copyProperties(assignmentDTO, assignment);
            assignment.setCreatedTime(LocalDateTime.now());
            assignment.setUpdatedTime(LocalDateTime.now());
            
            // 转换订单ID列表为JSON字符串
            if (!CollectionUtils.isEmpty(assignmentDTO.getTourScheduleOrderIds())) {
                try {
                    assignment.setTourScheduleOrderIds(objectMapper.writeValueAsString(assignmentDTO.getTourScheduleOrderIds()));
                } catch (JsonProcessingException e) {
                    log.error("转换订单ID列表失败", e);
                    throw new BaseException("转换订单ID列表失败");
                }
            }
            
            // 插入分配记录
            guideAssignmentMapper.insert(assignment);
            
            // 3. 创建订单关联记录
            if (!CollectionUtils.isEmpty(assignmentDTO.getOrders())) {
                List<GuideAssignmentOrder> orderList = assignmentDTO.getOrders().stream()
                        .map(orderDTO -> {
                            GuideAssignmentOrder order = new GuideAssignmentOrder();
                            BeanUtils.copyProperties(orderDTO, order);
                            order.setAssignmentId(assignment.getId());
                            order.setCreatedTime(LocalDateTime.now());
                            order.setUpdatedTime(LocalDateTime.now());
                            return order;
                        })
                        .collect(Collectors.toList());
                
                guideAssignmentOrderMapper.batchInsert(orderList);
            }
            
            // 4. 返回分配结果
            return getById(assignment.getId());
            
        } catch (Exception e) {
            log.error("手动分配失败", e);
            throw new BaseException("手动分配失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void update(GuideAssignmentDTO assignmentDTO) {
        log.info("更新分配信息：{}", assignmentDTO);
        
        try {
            // 验证分配记录是否存在
            GuideAssignmentVO existing = getById(assignmentDTO.getId());
            if (existing == null) {
                throw new BaseException("分配记录不存在");
            }
            
            // 如果更新了导游或车辆，需要验证可用性
            if (assignmentDTO.getGuideId() != null && !assignmentDTO.getGuideId().equals(existing.getGuide().getGuideId())) {
                Boolean guideAvailable = guideAssignmentMapper.checkGuideAvailability(
                    assignmentDTO.getGuideId(),
                    assignmentDTO.getAssignmentDate(),
                    assignmentDTO.getStartTime(),
                    assignmentDTO.getEndTime()
                );
                if (!Boolean.TRUE.equals(guideAvailable)) {
                    throw new BaseException("选择的导游在该时间段不可用");
                }
            }
            
            if (assignmentDTO.getVehicleId() != null && !assignmentDTO.getVehicleId().equals(existing.getVehicle().getVehicleId())) {
                Boolean vehicleAvailable = guideAssignmentMapper.checkVehicleAvailability(
                    assignmentDTO.getVehicleId(),
                    assignmentDTO.getAssignmentDate(),
                    assignmentDTO.getStartTime(),
                    assignmentDTO.getEndTime()
                );
                if (!Boolean.TRUE.equals(vehicleAvailable)) {
                    throw new BaseException("选择的车辆在该时间段不可用");
                }
            }
            
            // 更新分配记录
            GuideDailyAssignment assignment = new GuideDailyAssignment();
            BeanUtils.copyProperties(assignmentDTO, assignment);
            assignment.setUpdatedTime(LocalDateTime.now());
            
            // 转换订单ID列表为JSON字符串
            if (!CollectionUtils.isEmpty(assignmentDTO.getTourScheduleOrderIds())) {
                try {
                    assignment.setTourScheduleOrderIds(objectMapper.writeValueAsString(assignmentDTO.getTourScheduleOrderIds()));
                } catch (JsonProcessingException e) {
                    log.error("转换订单ID列表失败", e);
                    throw new BaseException("转换订单ID列表失败");
                }
            }
            
            guideAssignmentMapper.update(assignment);
            
            // 更新订单关联记录
            if (!CollectionUtils.isEmpty(assignmentDTO.getOrders())) {
                // 先删除原有关联
                guideAssignmentOrderMapper.deleteByAssignmentId(assignmentDTO.getId());
                
                // 重新插入
                List<GuideAssignmentOrder> orderList = assignmentDTO.getOrders().stream()
                        .map(orderDTO -> {
                            GuideAssignmentOrder order = new GuideAssignmentOrder();
                            BeanUtils.copyProperties(orderDTO, order);
                            order.setAssignmentId(assignmentDTO.getId());
                            order.setCreatedTime(LocalDateTime.now());
                            order.setUpdatedTime(LocalDateTime.now());
                            return order;
                        })
                        .collect(Collectors.toList());
                
                guideAssignmentOrderMapper.batchInsert(orderList);
            }
            
        } catch (Exception e) {
            log.error("更新分配信息失败", e);
            throw new BaseException("更新分配信息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancel(Long id, String reason) {
        log.info("取消分配：id={}, reason={}", id, reason);
        
        try {
            // 验证分配记录是否存在
            GuideAssignmentVO existing = getById(id);
            if (existing == null) {
                throw new BaseException("分配记录不存在");
            }
            
            // 更新分配状态为取消
            GuideDailyAssignment assignment = new GuideDailyAssignment();
            assignment.setId(id);
            assignment.setAssignmentStatus("cancelled");
            assignment.setRemarks(StringUtils.hasText(reason) ? reason : "手动取消");
            assignment.setUpdatedTime(LocalDateTime.now());
            
            guideAssignmentMapper.update(assignment);
            
        } catch (Exception e) {
            log.error("取消分配失败", e);
            throw new BaseException("取消分配失败：" + e.getMessage());
        }
    }

    @Override
    public GuideAssignmentVO getById(Long id) {
        log.info("根据ID获取分配详情：id={}", id);
        
        try {
            return guideAssignmentMapper.getById(id);
        } catch (Exception e) {
            log.error("根据ID获取分配详情失败", e);
            throw new BaseException("获取分配详情失败");
        }
    }

    @Override
    public List<GuideAssignmentVO> getByDate(LocalDate date) {
        log.info("根据日期获取分配列表：date={}", date);
        
        try {
            return guideAssignmentMapper.getByDate(date);
        } catch (Exception e) {
            log.error("根据日期获取分配列表失败", e);
            throw new BaseException("获取分配列表失败");
        }
    }

    @Override
    @Transactional
    public List<GuideAssignmentVO> batchAssign(List<GuideAssignmentDTO> assignmentDTOs) {
        log.info("批量分配：count={}", assignmentDTOs.size());
        
        List<GuideAssignmentVO> results = new ArrayList<>();
        
        try {
            for (GuideAssignmentDTO assignmentDTO : assignmentDTOs) {
                try {
                    // 如果没有指定导游和车辆，使用自动分配
                    GuideAssignmentVO result;
                    if (assignmentDTO.getGuideId() == null || assignmentDTO.getVehicleId() == null) {
                        result = autoAssign(assignmentDTO);
                    } else {
                        result = manualAssign(assignmentDTO);
                    }
                    results.add(result);
                } catch (Exception e) {
                    log.error("批量分配中单个分配失败：{}", e.getMessage());
                    // 继续处理下一个，不中断整个批量操作
                }
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("批量分配失败", e);
            throw new BaseException("批量分配失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================
    
    /**
     * 计算导游推荐状态
     */
    private boolean calculateGuideRecommendation(GuideAvailabilityVO guide, String location) {
        if (guide == null) return false;
        
        // 推荐条件：
        // 1. 经验年数 >= 3年
        // 2. 语言能力匹配（包含中文或英文）
        // 3. 当前分配团数 < 最大接团数
        
        boolean hasExperience = guide.getExperienceYears() != null && guide.getExperienceYears() >= 3;
        boolean hasLanguageSkill = StringUtils.hasText(guide.getLanguages()) && 
                                  (guide.getLanguages().contains("中文") || guide.getLanguages().contains("英文"));
        boolean hasCapacity = guide.getCurrentGroups() != null && guide.getMaxGroups() != null &&
                             guide.getCurrentGroups() < guide.getMaxGroups();
        
        return hasExperience && hasLanguageSkill && hasCapacity;
    }
    
    /**
     * 生成导游推荐原因
     */
    private String generateGuideRecommendReason(GuideAvailabilityVO guide, String location) {
        StringBuilder reason = new StringBuilder();
        
        if (guide.getExperienceYears() != null && guide.getExperienceYears() >= 5) {
            reason.append("经验丰富(").append(guide.getExperienceYears()).append("年)；");
        }
        
        if (StringUtils.hasText(guide.getLanguages())) {
            reason.append("语言能力：").append(guide.getLanguages()).append("；");
        }
        
        if (guide.getCurrentGroups() != null && guide.getMaxGroups() != null) {
            reason.append("当前负荷：").append(guide.getCurrentGroups()).append("/").append(guide.getMaxGroups()).append("；");
        }
        
        return reason.length() > 0 ? reason.toString() : "综合评估推荐";
    }
    
    /**
     * 计算车辆推荐状态
     */
    private boolean calculateVehicleRecommendation(VehicleAvailabilityVO vehicle, Integer peopleCount) {
        if (vehicle == null || peopleCount == null) return false;
        
        // 推荐条件：
        // 1. 座位数合适（人数 <= 座位数 <= 人数+2，避免浪费）
        // 2. 油量充足（>= 50%）
        // 3. 状态可用
        
        boolean seatCountSuitable = vehicle.getSeatCount() != null && 
                                   vehicle.getSeatCount() >= peopleCount && 
                                   vehicle.getSeatCount() <= peopleCount + 2;
        boolean fuelSufficient = vehicle.getFuelLevel() != null && vehicle.getFuelLevel() >= 50.0;
        boolean statusAvailable = "available".equals(vehicle.getStatus());
        
        return seatCountSuitable && fuelSufficient && statusAvailable;
    }
    
    /**
     * 生成车辆推荐原因
     */
    private String generateVehicleRecommendReason(VehicleAvailabilityVO vehicle, Integer peopleCount) {
        StringBuilder reason = new StringBuilder();
        
        if (vehicle.getSeatCount() != null) {
            reason.append("座位数匹配(").append(vehicle.getSeatCount()).append("座)；");
        }
        
        if (vehicle.getFuelLevel() != null) {
            reason.append("油量充足(").append(vehicle.getFuelLevel()).append("%)；");
        }
        
        if (StringUtils.hasText(vehicle.getCurrentLocation())) {
            reason.append("位置：").append(vehicle.getCurrentLocation()).append("；");
        }
        
        return reason.length() > 0 ? reason.toString() : "综合评估推荐";
    }
} 