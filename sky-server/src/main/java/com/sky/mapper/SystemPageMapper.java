package com.sky.mapper;

import com.sky.entity.SystemPage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统页面Mapper接口
 */
@Mapper
public interface SystemPageMapper {

    /**
     * 查询所有系统页面
     */
    @Select("SELECT * FROM system_pages WHERE status = 1 ORDER BY sort_order")
    List<SystemPage> findAll();

    /**
     * 根据ID查询页面
     */
    @Select("SELECT * FROM system_pages WHERE id = #{id}")
    SystemPage findById(Long id);

    /**
     * 根据页面路径查询页面
     */
    @Select("SELECT * FROM system_pages WHERE page_path = #{pagePath}")
    SystemPage findByPagePath(String pagePath);

    /**
     * 根据页面分组查询页面
     */
    @Select("SELECT * FROM system_pages WHERE page_group = #{pageGroup} AND status = 1 ORDER BY sort_order")
    List<SystemPage> findByPageGroup(String pageGroup);

    /**
     * 插入页面
     */
    @Insert("INSERT INTO system_pages (page_path, page_name, page_group, page_group_icon, page_icon, " +
            "permission_level, is_required, description, sort_order, status, created_at, updated_at) " +
            "VALUES (#{pagePath}, #{pageName}, #{pageGroup}, #{pageGroupIcon}, #{pageIcon}, " +
            "#{permissionLevel}, #{isRequired}, #{description}, #{sortOrder}, #{status}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SystemPage systemPage);

    /**
     * 更新页面
     */
    @Update("UPDATE system_pages SET page_path = #{pagePath}, page_name = #{pageName}, " +
            "page_group = #{pageGroup}, page_group_icon = #{pageGroupIcon}, page_icon = #{pageIcon}, " +
            "permission_level = #{permissionLevel}, is_required = #{isRequired}, " +
            "description = #{description}, sort_order = #{sortOrder}, status = #{status}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    void update(SystemPage systemPage);

    /**
     * 删除页面
     */
    @Update("UPDATE system_pages SET status = 0, updated_at = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查询页面分组列表
     */
    @Select("SELECT DISTINCT page_group, page_group_icon FROM system_pages WHERE status = 1 ORDER BY MIN(sort_order)")
    @Results({
        @Result(property = "pageGroup", column = "page_group"),
        @Result(property = "pageGroupIcon", column = "page_group_icon")
    })
    List<SystemPage> findPageGroups();

    /**
     * 根据权限等级查询页面
     */
    @Select("SELECT * FROM system_pages WHERE permission_level = #{permissionLevel} AND status = 1 ORDER BY sort_order")
    List<SystemPage> findByPermissionLevel(String permissionLevel);

    /**
     * 查询必需权限页面
     */
    @Select("SELECT * FROM system_pages WHERE is_required = 1 AND status = 1 ORDER BY sort_order")
    List<SystemPage> findRequiredPages();
}

