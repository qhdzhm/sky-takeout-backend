package com.sky.service;

import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.dto.UpdateTourLocationDTO;
import com.sky.vo.TourScheduleVO;
import com.sky.vo.HotelCustomerStatisticsVO;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 行程排序业务接口
 */
public interface TourScheduleOrderService {

    /**
     * 通过订单ID获取行程排序
     * @param bookingId 订单ID
     * @return 行程排序视图对象列表
     */
    List<TourScheduleVO> getSchedulesByBookingId(Integer bookingId);

    /**
     * 通过日期范围获取行程排序
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 行程排序视图对象列表
     */
    List<TourScheduleVO> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 保存单个行程排序
     * @param tourScheduleOrderDTO 行程排序DTO
     * @return 保存结果
     */
    boolean saveSchedule(TourScheduleOrderDTO tourScheduleOrderDTO);

    /**
     * 批量保存行程排序
     * @param batchSaveDTO 批量保存DTO
     * @return 保存结果
     */
    boolean saveBatchSchedules(TourScheduleBatchSaveDTO batchSaveDTO);

    /**
     * 根据日期和地点获取导游车辆分配信息
     * @param date 日期
     * @param location 地点
     * @return 分配信息列表
     */
    List<Object> getAssignmentByDateAndLocation(LocalDate date, String location);

    /**
     * 根据订单号搜索行程排序
     * @param orderNumber 订单号
     * @return 行程排序视图对象列表
     */
    List<TourScheduleVO> getSchedulesByOrderNumber(String orderNumber);

    /**
     * 根据联系人姓名搜索行程排序
     * @param contactPerson 联系人姓名
     * @return 行程排序视图对象列表
     */
    List<TourScheduleVO> getSchedulesByContactPerson(String contactPerson);

    /**
     * 获取可选的一日游产品列表（用于额外行程）
     * @param params 查询参数
     * @return 一日游产品列表
     */
    List<Map<String, Object>> getAvailableDayTours(Map<String, Object> params);

    /**
     * 删除行程排序
     * @param scheduleId 行程排序ID
     * @return 删除结果
     */
    boolean deleteSchedule(Integer scheduleId);

    /**
     * 更新导游备注
     * @param scheduleId 行程排序ID
     * @param guideRemarks 导游备注
     * @return 更新结果
     */
    boolean updateGuideRemarks(Integer scheduleId, String guideRemarks);

    /**
     * 更新特殊要求（备注）
     * @param scheduleId 行程排序ID
     * @param specialRequests 特殊要求
     * @return 更新结果
     */
    boolean updateSpecialRequests(Integer scheduleId, String specialRequests);

    /**
     * 根据酒店名称和日期统计住在该酒店的所有客人
     * @param hotelName 酒店名称
     * @param tourDate 旅游日期
     * @return 酒店客人统计信息
     */
    HotelCustomerStatisticsVO getHotelCustomerStatistics(String hotelName, LocalDate tourDate);

    /**
     * 更新订单游玩地点 - 用于同车订票拖拽功能
     * @param updateLocationDTO 更新地点请求DTO
     * @return 更新结果
     */
    boolean updateTourLocation(UpdateTourLocationDTO updateLocationDTO);

} 