package com.sky.mapper;

import com.sky.entity.ProductAgentDiscount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品中介折扣配置Mapper接口
 */
@Mapper
public interface ProductAgentDiscountMapper {

    /**
     * 根据代理商ID、产品类型和产品ID查询折扣配置
     * @param agentId 代理商ID
     * @param productType 产品类型
     * @param productId 产品ID
     * @return 折扣配置
     */
    ProductAgentDiscount findByAgentAndProduct(@Param("agentId") Long agentId, 
                                              @Param("productType") String productType, 
                                              @Param("productId") Long productId);

    /**
     * 根据等级ID、产品类型和产品ID查询折扣配置
     * @param levelId 等级ID
     * @param productType 产品类型
     * @param productId 产品ID
     * @return 折扣配置
     */
    ProductAgentDiscount findByLevelAndProduct(@Param("levelId") Long levelId, 
                                             @Param("productType") String productType, 
                                             @Param("productId") Long productId);

    /**
     * 根据代理商ID查询所有可用的折扣配置
     * @param agentId 代理商ID
     * @return 折扣配置列表
     */
    List<ProductAgentDiscount> findByAgentId(@Param("agentId") Long agentId);

    /**
     * 根据等级ID查询所有折扣配置
     * @param levelId 等级ID
     * @return 折扣配置列表
     */
    List<ProductAgentDiscount> findByLevelId(@Param("levelId") Long levelId);

    /**
     * 根据产品查询所有折扣配置
     * @param productType 产品类型
     * @param productId 产品ID
     * @return 折扣配置列表
     */
    List<ProductAgentDiscount> findByProduct(@Param("productType") String productType, 
                                           @Param("productId") Long productId);

    /**
     * 插入折扣配置
     * @param discount 折扣配置
     * @return 影响行数
     */
    int insert(ProductAgentDiscount discount);

    /**
     * 更新折扣配置
     * @param discount 折扣配置
     * @return 影响行数
     */
    int update(ProductAgentDiscount discount);

    /**
     * 删除折扣配置
     * @param id 配置ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量更新某个等级的折扣率
     * @param levelId 等级ID
     * @param productType 产品类型
     * @param discountRate 新的折扣率
     * @return 影响行数
     */
    int batchUpdateDiscountRate(@Param("levelId") Long levelId, 
                               @Param("productType") String productType, 
                               @Param("discountRate") BigDecimal discountRate);

    /**
     * 批量插入产品折扣配置
     * @param discounts 折扣配置列表
     * @return 影响行数
     */
    int batchInsert(@Param("discounts") List<ProductAgentDiscount> discounts);

    /**
     * 根据ID查询折扣配置
     * @param id 配置ID
     * @return 折扣配置
     */
    ProductAgentDiscount findById(@Param("id") Long id);
} 