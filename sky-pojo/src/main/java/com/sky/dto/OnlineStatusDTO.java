package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线状态更新DTO
 */
@Data
public class OnlineStatusDTO implements Serializable {

    private Integer onlineStatus;
} 