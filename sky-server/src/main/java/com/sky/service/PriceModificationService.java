package com.sky.service;

import com.sky.dto.AgentPriceResponseDTO;
import com.sky.dto.PriceModificationRequestDTO;
import com.sky.result.PageResult;
import com.sky.vo.PriceModificationVO;

import java.time.LocalDate;

/**
 * 价格修改服务接口
 */
public interface PriceModificationService {

    /**
     * 创建价格修改请求
     * @param requestDTO 价格修改请求DTO
     * @return 操作结果消息
     */
    String createPriceModificationRequest(PriceModificationRequestDTO requestDTO);

    /**
     * 代理商响应价格修改请求
     * @param responseDTO 代理商响应DTO
     * @return 操作结果消息
     */
    String agentResponseToRequest(AgentPriceResponseDTO responseDTO);

    /**
     * 管理后台分页查询价格修改请求
     * @param status 状态
     * @param modificationType 修改类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult pageQuery(String status, 
                        String modificationType,
                        LocalDate startDate, 
                        LocalDate endDate,
                        Integer page, 
                        Integer pageSize);

    /**
     * 代理商分页查询价格修改请求
     * @param agentId 代理商ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
        PageResult pageQueryByAgent(Long agentId, 
                               String status, 
                               LocalDate startDate, 
                               LocalDate endDate,
                               Integer page, 
                               Integer pageSize);

    /**
     * 代理商根据订单ID分页查询价格修改请求
     * @param agentId 代理商ID
     * @param bookingId 订单ID
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult pageQueryByBookingId(Long agentId,
                                   Long bookingId,
                                   String status,
                                   Integer page,
                                   Integer pageSize);

    /**
     * 根据ID获取价格修改请求详情
     * @param id 请求ID
     * @return 价格修改请求详情
     */
    PriceModificationVO getById(Long id);

}
