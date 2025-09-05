package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 价格修改请求DTO
 */
@Data
public class PriceModificationRequestDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer bookingId;

    /**
     * 新价格
     */
    private BigDecimal newPrice;

    /**
     * 修改原因
     */
    private String reason;
}

