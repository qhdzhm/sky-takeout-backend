package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 代理商价格修改响应DTO
 */
@Data
public class AgentPriceResponseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 价格修改请求ID
     */
    private Long requestId;

    /**
     * 代理商响应：approved-同意，rejected-拒绝
     */
    private String response;

    /**
     * 代理商备注
     */
    private String note;
}

