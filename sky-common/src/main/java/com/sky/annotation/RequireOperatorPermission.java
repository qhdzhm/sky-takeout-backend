package com.sky.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作员权限控制注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOperatorPermission {

    /**
     * 需要的操作员类型
     * tour_master: 排团主管
     * hotel_operator: 酒店专员
     * any: 任何操作员类型
     */
    String operatorType() default "any";

    /**
     * 是否需要排团主管权限
     */
    boolean requireTourMaster() default false;

    /**
     * 是否需要分配订单权限
     */
    boolean requireAssignPermission() default false;

    /**
     * 权限描述（用于错误提示）
     */
    String description() default "需要特定操作员权限";

    /**
     * 是否检查订单操作权限（从请求参数中获取bookingId）
     */
    boolean checkOrderPermission() default false;

    /**
     * 订单ID参数名（当checkOrderPermission=true时使用）
     */
    String bookingIdParam() default "bookingId";
}
