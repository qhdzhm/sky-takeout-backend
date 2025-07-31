package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 并发控制结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrencyControlResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 冲突类型
     */
    private ConflictType conflictType;

    /**
     * 冲突详情（版本冲突时返回最新的版本信息）
     */
    private ConflictDetail conflictDetail;

    /**
     * 冲突类型枚举
     */
    public enum ConflictType {
        NONE,           // 无冲突
        VERSION,        // 版本冲突
        CONCURRENT_EDIT // 并发编辑冲突
    }

    /**
     * 冲突详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDetail implements Serializable {
        private Integer recordId;
        private Integer currentVersion;
        private Integer expectedVersion;
        private String lastModifiedBy;
        private LocalDateTime lastModifiedTime;
    }

    // 静态工厂方法
    public static ConcurrencyControlResult success(String message) {
        return ConcurrencyControlResult.builder()
                .success(true)
                .message(message)
                .conflictType(ConflictType.NONE)
                .build();
    }

    public static ConcurrencyControlResult versionConflict(String message, ConflictDetail detail) {
        return ConcurrencyControlResult.builder()
                .success(false)
                .message(message)
                .conflictType(ConflictType.VERSION)
                .conflictDetail(detail)
                .build();
    }

    public static ConcurrencyControlResult concurrentEditConflict(String message) {
        return ConcurrencyControlResult.builder()
                .success(false)
                .message(message)
                .conflictType(ConflictType.CONCURRENT_EDIT)
                .build();
    }

    public static ConcurrencyControlResult error(String message) {
        return ConcurrencyControlResult.builder()
                .success(false)
                .message(message)
                .conflictType(ConflictType.NONE)
                .build();
    }
} 