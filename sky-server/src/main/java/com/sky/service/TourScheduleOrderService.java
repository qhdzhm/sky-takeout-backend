package com.sky.service;

import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.vo.TourScheduleVO;

import java.time.LocalDate;
import java.util.List;

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
     * 初始化订单的行程排序
     * @param bookingId 订单ID
     * @return 初始化结果
     */
    boolean initOrderSchedules(Integer bookingId);
} 