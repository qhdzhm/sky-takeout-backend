package com.sky.vo;

import com.sky.dto.TourDTO;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 旅游产品推荐响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourRecommendationResponse {
    
    /**
     * 推荐产品列表
     */
    private List<TourDTO> recommendedTours;
    
    /**
     * 推荐理由/描述
     */
    private String recommendationReason;
    
    /**
     * 查询类型 (day/group/mixed)
     */
    private String queryType;
    
    /**
     * 提取的关键词
     */
    private List<String> extractedKeywords;
    
    /**
     * 是否有更多结果
     */
    private Boolean hasMore;
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 搜索建议
     */
    private String searchSuggestion;
} 