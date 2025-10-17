package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.mapper.PassengerMapper;
import com.sky.entity.Passenger;
import com.sky.entity.TourScheduleOrder;
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
import java.util.Map;
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
    
    @Autowired
    private TourScheduleOrderMapper tourScheduleOrderMapper;
    
    @Autowired
    private PassengerMapper passengerMapper;

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
        // 🚌 酒店摆渡任务允许与正常行程共存，跳过唯一性检查
        boolean isShuttleTask = assignmentDTO.getDestination() != null && assignmentDTO.getDestination().contains("酒店摆渡");
        
        if (!isShuttleTask && checkGuideAssigned(guide.getGuideId().longValue(), assignmentDTO.getAssignmentDate())) {
            throw new BaseException("导游在指定日期已有分配，无法重复分配");
        }
        
        if (isShuttleTask) {
            log.info("🚌 酒店摆渡任务，允许导游在同一天多次分配");
        }

        // 3. 获取车辆详细信息
        log.info("开始获取车辆信息，车辆ID：{}", assignmentDTO.getVehicleId());
        Vehicle vehicle = vehicleMapper.getById(assignmentDTO.getVehicleId());
        if (vehicle == null) {
            log.error("车辆不存在，车辆ID：{}", assignmentDTO.getVehicleId());
            throw new BaseException("车辆不存在");
        }
        log.info("车辆信息获取成功：{}", vehicle.getLicensePlate());

        // 4. 完整的车辆可用性检查（按优先级检查）
        // 🚌 酒店摆渡任务跳过重复分配检查
        if (!isShuttleTask) {
            checkVehicleAvailabilityForAssignment(vehicle, assignmentDTO.getAssignmentDate(), assignmentDTO.getTotalPeople());
        } else {
            // 摆渡任务只检查基础状态和座位数
            checkVehicleBasicStatus(vehicle, assignmentDTO.getAssignmentDate());
            
            // 检查座位数
            if (vehicle.getSeatCount() != null && vehicle.getSeatCount() < assignmentDTO.getTotalPeople()) {
                throw new BaseException(String.format("车辆座位数不足，需要%d人，车辆只有%d座", 
                        assignmentDTO.getTotalPeople(), vehicle.getSeatCount()));
            }
            
            log.info("🚌 酒店摆渡任务，跳过车辆分配唯一性检查");
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
     * 根据分配记录ID获取包含订单详情的完整分配信息
     */
    @Override
    public TourGuideVehicleAssignmentVO getAssignmentWithOrderDetails(Long assignmentId) {
        log.info("获取包含订单详情的分配信息，assignmentId: {}", assignmentId);
        
        // 1. 获取分配基本信息
        TourGuideVehicleAssignmentVO assignment = assignmentMapper.getById(assignmentId);
        if (assignment == null) {
            throw new BaseException("分配记录不存在");
        }
        
        // 2. 解析并获取关联的订单详细信息
        if (assignment.getTourScheduleOrderIds() != null && !assignment.getTourScheduleOrderIds().isEmpty()) {
            try {
                // 获取booking_ids列表
                List<Long> bookingIds = assignment.getTourScheduleOrderIds();
                
                log.info("解析的booking IDs: {}", bookingIds);
                
                // 3. 根据booking_id查询订单详细信息
                List<TourScheduleOrder> orderDetails = new ArrayList<>();
                for (Long bookingId : bookingIds) {
                    List<TourScheduleOrder> orders = tourScheduleOrderMapper.getByBookingId(bookingId.intValue());
                    if (orders != null && !orders.isEmpty()) {
                        orderDetails.addAll(orders);
                        log.info("查询到booking_id {} 对应的 {} 条订单记录", bookingId, orders.size());
                        // 🏨 调试：检查每个订单的酒店预订号
                        for (TourScheduleOrder order : orders) {
                            log.info("🏨 [调试] 订单详情 - booking_id: {}, order_number: {}, hotel_booking_number: '{}'", 
                                order.getBookingId(), order.getOrderNumber(), order.getHotelBookingNumber());
                        }
                    }
                }
                
                // 4. 将订单信息转换为JSON并设置到assignment中
                if (!orderDetails.isEmpty()) {
                    // 将订单详情转换为乘客信息格式
                    List<TourGuideVehicleAssignmentVO.PassengerInfo> passengerDetails = new ArrayList<>();
                    
                    // 按booking_id分组处理订单
                    Map<Integer, List<TourScheduleOrder>> groupedOrders = orderDetails.stream()
                        .collect(Collectors.groupingBy(TourScheduleOrder::getBookingId));
                    
                    for (Map.Entry<Integer, List<TourScheduleOrder>> entry : groupedOrders.entrySet()) {
                        Integer bookingId = entry.getKey();
                        List<TourScheduleOrder> orders = entry.getValue();
                        TourScheduleOrder firstOrder = orders.get(0); // 取第一个订单作为代表
                        
                        // 🆕 查询该订单关联的所有乘客信息
                        try {
                            List<Passenger> passengers = passengerMapper.getByBookingId(bookingId);
                            
                            if (passengers != null && !passengers.isEmpty()) {
                                log.info("为booking_id {} 查询到 {} 个乘客信息", bookingId, passengers.size());
                                
                                // 为每个乘客创建一个PassengerInfo记录
                                for (Passenger passenger : passengers) {
                                    TourGuideVehicleAssignmentVO.PassengerInfo passengerDetail = new TourGuideVehicleAssignmentVO.PassengerInfo();
                                    
                                    // 优先使用乘客表中的信息，如果为空则使用订单中的联系人信息
                                    passengerDetail.setName(passenger.getFullName() != null && !passenger.getFullName().trim().isEmpty() 
                                        ? passenger.getFullName() : firstOrder.getContactPerson());
                                    passengerDetail.setPhoneNumber(passenger.getPhone() != null && !passenger.getPhone().trim().isEmpty() 
                                        ? passenger.getPhone() : firstOrder.getContactPhone());
                                    passengerDetail.setWechat(passenger.getWechatId()); // 🆕 添加微信信息
                                    passengerDetail.setRequirements(passenger.getSpecialRequests() != null 
                                        ? passenger.getSpecialRequests() : firstOrder.getSpecialRequests());
                                    
                                    // 组装详细信息，包含航班和酒店信息
                                    StringBuilder infoBuilder = new StringBuilder();
                                    infoBuilder.append(String.format("订单号: %s, 成人: %d, 儿童: %d, 接送: %s -> %s",
                                        firstOrder.getOrderNumber(),
                                        firstOrder.getAdultCount() != null ? firstOrder.getAdultCount() : 0,
                                        firstOrder.getChildCount() != null ? firstOrder.getChildCount() : 0,
                                        firstOrder.getPickupLocation() != null ? firstOrder.getPickupLocation() : "待确认",
                                        firstOrder.getDropoffLocation() != null ? firstOrder.getDropoffLocation() : "待确认"
                                    ));
                                    
                                    // 添加航班信息
                                    if (firstOrder.getFlightNumber() != null && !firstOrder.getFlightNumber().trim().isEmpty()) {
                                        infoBuilder.append(", 航班: ").append(firstOrder.getFlightNumber());
                                    }
                                    if (firstOrder.getReturnFlightNumber() != null && !firstOrder.getReturnFlightNumber().trim().isEmpty()) {
                                        infoBuilder.append(", 返程航班: ").append(firstOrder.getReturnFlightNumber());
                                    }
                                    
                                    // 🆕 添加航班时间信息
                                    if (firstOrder.getArrivalLandingTime() != null) {
                                        infoBuilder.append(", 到达降落时间: ").append(firstOrder.getArrivalLandingTime());
                                    }
                                    if (firstOrder.getDepartureDepartureTime() != null) {
                                        infoBuilder.append(", 返程起飞时间: ").append(firstOrder.getDepartureDepartureTime());
                                    }
                                    
                                    // 添加酒店预订号信息
                                    log.info("🏨 [调试] booking_id: {}, hotelBookingNumber: '{}'", bookingId, firstOrder.getHotelBookingNumber());
                                    if (firstOrder.getHotelBookingNumber() != null && !firstOrder.getHotelBookingNumber().trim().isEmpty()) {
                                        infoBuilder.append(", 酒店预订号: ").append(firstOrder.getHotelBookingNumber());
                                        log.info("🏨 [调试] 已添加酒店预订号到specialNeeds: {}", firstOrder.getHotelBookingNumber());
                                    } else {
                                        log.warn("🏨 [调试] booking_id {} 的酒店预订号为空或null", bookingId);
                                    }
                                    
                                    passengerDetail.setSpecialNeeds(infoBuilder.toString());
                                    passengerDetails.add(passengerDetail);
                                }
                            } else {
                                // 没有关联乘客信息时，使用订单中的联系人作为默认乘客
                                log.warn("booking_id {} 没有找到关联的乘客信息，使用订单联系人作为默认乘客", bookingId);
                                
                                TourGuideVehicleAssignmentVO.PassengerInfo passengerDetail = new TourGuideVehicleAssignmentVO.PassengerInfo();
                                passengerDetail.setName(firstOrder.getContactPerson());
                                passengerDetail.setPhoneNumber(firstOrder.getContactPhone());
                                passengerDetail.setRequirements(firstOrder.getSpecialRequests());
                                
                                // 组装详细信息
                                StringBuilder infoBuilder = new StringBuilder();
                                infoBuilder.append(String.format("订单号: %s, 成人: %d, 儿童: %d, 接送: %s -> %s",
                                    firstOrder.getOrderNumber(),
                                    firstOrder.getAdultCount() != null ? firstOrder.getAdultCount() : 0,
                                    firstOrder.getChildCount() != null ? firstOrder.getChildCount() : 0,
                                    firstOrder.getPickupLocation() != null ? firstOrder.getPickupLocation() : "待确认",
                                    firstOrder.getDropoffLocation() != null ? firstOrder.getDropoffLocation() : "待确认"
                                ));
                                
                                // 添加航班信息
                                if (firstOrder.getFlightNumber() != null && !firstOrder.getFlightNumber().trim().isEmpty()) {
                                    infoBuilder.append(", 航班: ").append(firstOrder.getFlightNumber());
                                }
                                if (firstOrder.getReturnFlightNumber() != null && !firstOrder.getReturnFlightNumber().trim().isEmpty()) {
                                    infoBuilder.append(", 返程航班: ").append(firstOrder.getReturnFlightNumber());
                                }
                                
                                // 🆕 添加航班时间信息
                                if (firstOrder.getArrivalLandingTime() != null) {
                                    infoBuilder.append(", 到达降落时间: ").append(firstOrder.getArrivalLandingTime());
                                }
                                if (firstOrder.getDepartureDepartureTime() != null) {
                                    infoBuilder.append(", 返程起飞时间: ").append(firstOrder.getDepartureDepartureTime());
                                }
                                
                                // 添加酒店预订号信息
                                log.info("🏨 [调试-fallback] booking_id: {}, hotelBookingNumber: '{}'", bookingId, firstOrder.getHotelBookingNumber());
                                if (firstOrder.getHotelBookingNumber() != null && !firstOrder.getHotelBookingNumber().trim().isEmpty()) {
                                    infoBuilder.append(", 酒店预订号: ").append(firstOrder.getHotelBookingNumber());
                                    log.info("🏨 [调试-fallback] 已添加酒店预订号到specialNeeds: {}", firstOrder.getHotelBookingNumber());
                                } else {
                                    log.warn("🏨 [调试-fallback] booking_id {} 的酒店预订号为空或null", bookingId);
                                }
                                
                                passengerDetail.setSpecialNeeds(infoBuilder.toString());
                                passengerDetails.add(passengerDetail);
                            }
                        } catch (Exception e) {
                            log.error("查询booking_id {} 的乘客信息时出现异常: {}", bookingId, e.getMessage(), e);
                            
                            // 异常时使用订单联系人信息作为fallback
                            TourGuideVehicleAssignmentVO.PassengerInfo passengerDetail = new TourGuideVehicleAssignmentVO.PassengerInfo();
                            passengerDetail.setName(firstOrder.getContactPerson());
                            passengerDetail.setPhoneNumber(firstOrder.getContactPhone());
                            passengerDetail.setRequirements(firstOrder.getSpecialRequests());
                            
                            String orderInfo = String.format("订单号: %s, 成人: %d, 儿童: %d, 接送: %s -> %s",
                                firstOrder.getOrderNumber(),
                                firstOrder.getAdultCount() != null ? firstOrder.getAdultCount() : 0,
                                firstOrder.getChildCount() != null ? firstOrder.getChildCount() : 0,
                                firstOrder.getPickupLocation() != null ? firstOrder.getPickupLocation() : "待确认",
                                firstOrder.getDropoffLocation() != null ? firstOrder.getDropoffLocation() : "待确认"
                            );
                            passengerDetail.setSpecialNeeds(orderInfo);
                            passengerDetails.add(passengerDetail);
                        }
                    }
                    
                    // 将乘客详情设置回assignment
                    assignment.setPassengerDetails(passengerDetails);
                    
                    // 保存更新的passenger_details到数据库
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String passengerDetailsJson = objectMapper.writeValueAsString(passengerDetails);
                        assignmentMapper.updatePassengerDetails(assignment.getId(), passengerDetailsJson);
                        log.info("已将重新生成的乘客详情保存到数据库，分配ID：{}", assignment.getId());
                    } catch (JsonProcessingException e) {
                        log.error("保存乘客详情到数据库失败：{}", e.getMessage(), e);
                    }
                    
                    log.info("成功获取到 {} 个订单的详细信息", passengerDetails.size());
                }
                
            } catch (Exception e) {
                log.error("解析订单ID或查询订单详情失败: {}", e.getMessage(), e);
                throw new BaseException("获取订单详情失败");
            }
        }
        
        return assignment;
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

        // 🚌 酒店摆渡任务允许与正常行程共存
        boolean isShuttleTask = assignmentDTO.getDestination() != null && assignmentDTO.getDestination().contains("酒店摆渡");

        // 如果导游或车辆发生变化，需要检查新资源的可用性（摆渡任务除外）
        if (!isShuttleTask) {
            if (guideChanged && checkGuideAssigned(guide.getGuideId().longValue(), assignmentDTO.getAssignmentDate())) {
                throw new BaseException("导游在指定日期已有分配，无法重复分配");
            }
            if (vehicleChanged && checkVehicleAssigned(assignmentDTO.getVehicleId(), assignmentDTO.getAssignmentDate())) {
                throw new BaseException("车辆在指定日期已有分配，无法重复分配");
            }
        } else {
            log.info("🚌 酒店摆渡任务更新，允许导游和车辆在同一天多次分配");
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

            // 🚌 酒店摆渡任务允许与正常行程共存，跳过唯一性检查
            boolean isShuttleTask = dto.getDestination() != null && dto.getDestination().contains("酒店摆渡");
            
            if (!isShuttleTask) {
                // 只对非摆渡任务进行唯一性检查
                if (checkGuideAssigned(guide.getGuideId().longValue(), dto.getAssignmentDate())) {
                    throw new BaseException("导游在 " + dto.getAssignmentDate() + " 已有分配");
                }
                if (checkVehicleAssigned(dto.getVehicleId(), dto.getAssignmentDate())) {
                    throw new BaseException("车辆在 " + dto.getAssignmentDate() + " 已有分配");
                }
            } else {
                log.info("🚌 酒店摆渡任务，允许导游和车辆在同一天多次分配");
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

    /**
     * 完整的车辆可用性检查（用于排团分配）
     * 检查优先级：
     * 1. 车辆基础状态（rego过期、路检过期、送修状态等）
     * 2. 车辆是否已被分配
     * 3. 车辆动态可用性（vehicle_availability表）
     * 4. 座位数是否足够
     */
    private void checkVehicleAvailabilityForAssignment(Vehicle vehicle, LocalDate assignmentDate, Integer peopleCount) {
        log.info("开始完整的车辆可用性检查：车辆ID={}，日期={}，人数={}", 
                vehicle.getVehicleId(), assignmentDate, peopleCount);

        // 1. 检查车辆基础状态（最重要的检查）
        checkVehicleBasicStatus(vehicle, assignmentDate);

        // 2. 检查车辆是否已被分配到其他团
        if (checkVehicleAssigned(vehicle.getVehicleId(), assignmentDate)) {
            throw new BaseException("车辆在指定日期已有分配，无法重复分配");
        }

        // 3. 检查动态可用性（vehicle_availability表）
        checkVehicleDynamicAvailability(vehicle.getVehicleId(), assignmentDate);

        // 4. 检查座位数是否足够
        if (vehicle.getSeatCount() != null && vehicle.getSeatCount() < peopleCount) {
            log.error("车辆座位数不足，座位数：{}，需要人数：{}", vehicle.getSeatCount(), peopleCount);
            throw new BaseException(String.format("车辆座位数不足，需要%d人，车辆只有%d座", 
                    peopleCount, vehicle.getSeatCount()));
        }

        log.info("车辆可用性检查通过");
    }

    /**
     * 检查车辆基础状态（车辆管理表中的状态）
     */
    private void checkVehicleBasicStatus(Vehicle vehicle, LocalDate assignmentDate) {
        LocalDate today = LocalDate.now();
        
        // 计算动态状态（与VehicleMapper.xml中的逻辑一致）
        Integer calculatedStatus;
        
        if (vehicle.getStatus() != null && vehicle.getStatus() == 0) {
            calculatedStatus = 0; // 送修中
        } else if (vehicle.getStatus() != null && vehicle.getStatus() == 2) {
            calculatedStatus = 2; // 维修中
        } else if (vehicle.getStatus() != null && vehicle.getStatus() == 3) {
            calculatedStatus = 3; // 停用
        } else if (vehicle.getRegoExpiryDate() != null && today.isAfter(vehicle.getRegoExpiryDate())) {
            calculatedStatus = 4; // 注册过期
        } else if (vehicle.getInspectionDueDate() != null && today.isAfter(vehicle.getInspectionDueDate())) {
            calculatedStatus = 5; // 车检过期
        } else {
            calculatedStatus = 1; // 可用
        }

        // 根据状态抛出具体的错误信息
        switch (calculatedStatus) {
            case 0:
                throw new BaseException("车辆正在送修中，暂不可用");
            case 2:
                throw new BaseException("车辆正在维修中，暂不可用");
            case 3:
                throw new BaseException("车辆已停用，不可分配");
            case 4:
                String regoMsg = "车辆注册已过期";
                if (vehicle.getRegoExpiryDate() != null) {
                    regoMsg += "（过期日期：" + vehicle.getRegoExpiryDate() + "）";
                }
                throw new BaseException(regoMsg + "，请先更新注册");
            case 5:
                String inspectionMsg = "车辆路检已过期";
                if (vehicle.getInspectionDueDate() != null) {
                    inspectionMsg += "（过期日期：" + vehicle.getInspectionDueDate() + "）";
                }
                throw new BaseException(inspectionMsg + "，请先进行车检");
            case 1:
                // 可用状态，继续后续检查
                log.info("车辆基础状态检查通过，状态：可用");
                break;
            default:
                throw new BaseException("车辆状态异常，无法分配");
        }
    }

    /**
     * 检查车辆动态可用性（vehicle_availability表）
     */
    private void checkVehicleDynamicAvailability(Long vehicleId, LocalDate assignmentDate) {
        try {
            // 检查该日期车辆是否在可用性表中标记为可用
            // 如果没有记录，则默认可用；如果有记录但状态不是available，则不可用
            VehicleAvailabilityVO availability = vehicleAvailabilityMapper.getVehicleAvailabilityByDate(vehicleId, assignmentDate);
            
            if (availability != null) {
                String status = availability.getStatus();
                if (!"available".equals(status)) {
                    String statusDesc = getAvailabilityStatusDescription(status);
                    throw new BaseException("车辆在指定日期不可用，状态：" + statusDesc);
                }
            }
            // 如果没有可用性记录，默认认为可用（这是合理的默认行为）
            
            log.info("车辆动态可用性检查通过");
        } catch (Exception e) {
            if (e instanceof BaseException) {
                throw e;
            }
            log.warn("检查车辆动态可用性时出现异常，默认允许分配：{}", e.getMessage());
            // 如果查询失败，不阻止分配（容错处理）
        }
    }

    /**
     * 获取可用性状态描述
     */
    private String getAvailabilityStatusDescription(String status) {
        if (status == null) return "未知";
        
        switch (status) {
            case "available":
                return "可用";
            case "in_use":
                return "使用中";
            case "maintenance":
                return "维护中";
            case "out_of_service":
                return "停用";
            default:
                return "未知状态(" + status + ")";
        }
    }

    /**
     * 获取当天已分配的导游和车辆列表（用于酒店摆渡分配）
     */
    @Override
    public Map<String, Object> getActiveResourcesForShuttle(LocalDate date) {
        log.info("🚌 获取当天活跃的导游和车辆：日期={}", date);
        
        // 1. 查询当天所有导游车辆分配记录
        List<TourGuideVehicleAssignmentVO> assignments = assignmentMapper.getByDate(date);
        log.info("📊 当天共有 {} 条分配记录", assignments.size());
        
        // 2. 提取去重后的导游列表（已带团的导游）
        List<Map<String, Object>> activeGuides = new ArrayList<>();
        Set<Long> processedGuideIds = new HashSet<>();
        
        for (TourGuideVehicleAssignmentVO assignment : assignments) {
            if (assignment.getGuide() != null) {
                Long guideId = assignment.getGuide().getGuideId();
                if (guideId != null && !processedGuideIds.contains(guideId)) {
                    Map<String, Object> guideInfo = new java.util.HashMap<>();
                    guideInfo.put("id", guideId);
                    guideInfo.put("name", assignment.getGuide().getGuideName());
                    guideInfo.put("destination", assignment.getDestination()); // 当天去的地点
                    if (assignment.getVehicle() != null) {
                        guideInfo.put("vehicle", assignment.getVehicle().getLicensePlate()); // 当天使用的车辆
                    }
                    activeGuides.add(guideInfo);
                    processedGuideIds.add(guideId);
                }
            }
        }
        
        // 3. 提取去重后的车辆列表（已使用的车辆）
        List<Map<String, Object>> activeVehicles = new ArrayList<>();
        Set<Long> processedVehicleIds = new HashSet<>();
        
        for (TourGuideVehicleAssignmentVO assignment : assignments) {
            if (assignment.getVehicle() != null) {
                Long vehicleId = assignment.getVehicle().getVehicleId();
                if (vehicleId != null && !processedVehicleIds.contains(vehicleId)) {
                    Map<String, Object> vehicleInfo = new java.util.HashMap<>();
                    vehicleInfo.put("id", vehicleId);
                    vehicleInfo.put("licensePlate", assignment.getVehicle().getLicensePlate());
                    vehicleInfo.put("vehicleType", assignment.getVehicle().getVehicleType());
                    vehicleInfo.put("seatingCapacity", assignment.getVehicle().getSeatCount());
                    if (assignment.getGuide() != null) {
                        vehicleInfo.put("guideName", assignment.getGuide().getGuideName()); // 当天使用的导游
                    }
                    vehicleInfo.put("destination", assignment.getDestination()); // 当天去的地点
                    activeVehicles.add(vehicleInfo);
                    processedVehicleIds.add(vehicleId);
                }
            }
        }
        
        log.info("✅ 提取完成：活跃导游 {} 人，活跃车辆 {} 辆", activeGuides.size(), activeVehicles.size());
        
        // 4. 返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("activeGuides", activeGuides);
        result.put("activeVehicles", activeVehicles);
        return result;
    }
} 