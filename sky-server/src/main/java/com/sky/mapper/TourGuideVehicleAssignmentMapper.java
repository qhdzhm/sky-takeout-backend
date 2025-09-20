package com.sky.mapper;

import com.sky.entity.TourGuideVehicleAssignment;
import com.sky.vo.TourGuideVehicleAssignmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 导游车辆游客分配Mapper接口
 */
@Mapper
public interface TourGuideVehicleAssignmentMapper {

    /**
     * 插入分配记录
     */
    void insert(TourGuideVehicleAssignment assignment);

    /**
     * 批量插入分配记录
     */
    void batchInsert(List<TourGuideVehicleAssignment> assignments);

    /**
     * 根据ID查询分配记录
     */
    TourGuideVehicleAssignmentVO getById(Long id);

    /**
     * 根据日期查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByDate(@Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 根据日期范围查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByDateRange(@Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 根据目的地查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByDestination(@Param("destination") String destination, 
                                                        @Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 根据目的地模糊匹配查询分配记录（支持简写地点名称）
     */
    List<Object> getByDestinationWithFuzzyMatch(@Param("location") String location, 
                                               @Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 根据导游ID查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByGuideId(@Param("guideId") Long guideId, 
                                                    @Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 根据车辆ID查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByVehicleId(@Param("vehicleId") Long vehicleId, 
                                                      @Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 更新分配记录
     */
    void update(TourGuideVehicleAssignment assignment);

    /**
     * 更新乘客详情
     */
    void updatePassengerDetails(@Param("id") Long id, @Param("passengerDetails") String passengerDetails);

    /**
     * 删除分配记录
     */
    void deleteById(Long id);

    /**
     * 根据订单ID列表查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> getByBookingIds(@Param("bookingIds") List<Long> bookingIds);

    /**
     * 分页查询分配记录
     */
    List<TourGuideVehicleAssignmentVO> pageQuery(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("destination") String destination,
                                                 @Param("guideName") String guideName,
                                                 @Param("licensePlate") String licensePlate,
                                                 @Param("status") String status);

    /**
     * 统计指定日期的分配数量
     */
    int countByDate(@Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 检查导游在指定日期是否已有分配
     */
    boolean checkGuideAssigned(@Param("guideId") Long guideId, 
                               @Param("assignmentDate") LocalDate assignmentDate);

    /**
     * 检查车辆在指定日期是否已有分配
     */
    boolean checkVehicleAssigned(@Param("vehicleId") Long vehicleId, 
                                 @Param("assignmentDate") LocalDate assignmentDate);
} 