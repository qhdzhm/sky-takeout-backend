package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 酒店预订邮件DTO
 */
@Data
public class HotelBookingEmailDTO implements Serializable {

    /**
     * 预订ID
     */
    private Integer bookingId;

    /**
     * 收件人邮箱
     */
    private String to;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;
} 