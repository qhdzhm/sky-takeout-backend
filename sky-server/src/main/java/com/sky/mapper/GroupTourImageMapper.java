package com.sky.mapper;

import com.sky.entity.GroupTourImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跟团游图片数据访问层
 */
@Mapper
public interface GroupTourImageMapper {

    /**
     * 插入跟团游图片
     *
     * @param groupTourImage 跟团游图片实体
     * @return 影响行数
     */
    int insert(GroupTourImage groupTourImage);

    /**
     * 根据ID查询跟团游图片
     *
     * @param id 图片ID
     * @return 跟团游图片实体
     */
    GroupTourImage selectById(Integer id);

    /**
     * 根据跟团游ID查询图片列表
     *
     * @param groupTourId 跟团游ID
     * @return 图片列表
     */
    List<GroupTourImage> selectByGroupTourId(Integer groupTourId);

    /**
     * 查询跟团游的第一张图片
     *
     * @param groupTourId 跟团游ID
     * @return 跟团游图片实体
     */
    GroupTourImage selectFirstByGroupTourId(Integer groupTourId);

    /**
     * 删除图片
     *
     * @param id 图片ID
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 重置跟团游的所有图片为非主图
     *
     * @param groupTourId 跟团游ID
     * @return 影响行数
     */
    int resetPrimaryImage(Integer groupTourId);

    /**
     * 设置图片为主图
     *
     * @param id 图片ID
     * @return 影响行数
     */
    int setPrimaryImage(Integer id);

    /**
     * 更新跟团游的主图URL
     *
     * @param groupTourId 跟团游ID
     * @param imageUrl    图片URL
     * @return 影响行数
     */
    int updateGroupTourImageUrl(@Param("groupTourId") Integer groupTourId, @Param("imageUrl") String imageUrl);

    /**
     * 更新图片位置
     *
     * @param id       图片ID
     * @param position 位置
     * @return 影响行数
     */
    int updatePosition(@Param("id") Integer id, @Param("position") Integer position);

    /**
     * 更新图片描述
     *
     * @param id          图片ID
     * @param description 描述
     * @return 影响行数
     */
    int updateDescription(@Param("id") Integer id, @Param("description") String description);
} 