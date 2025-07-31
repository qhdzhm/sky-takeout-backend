# 🎯 客服聊天系统数据库优化执行指南

## 📋 **问题描述**
- 用户信息显示不正确（应该是agentId等，不是普通用户）
- 客服信息显示"未分配"
- 消息数显示都是0
- 数据库设计混淆：users、agents、agent_operators三种用户类型但查询时只关联users表

## 🛠️ **解决方案总览**
1. 数据库表结构优化（添加用户类型字段）
2. 创建统一用户视图
3. 更新查询逻辑（Mapper.xml）
4. 前端字段映射修复

## 📝 **执行步骤**

### **第1步：备份数据库**
```sql
-- 创建备份（可选但推荐）
mysqldump -u root -p happy_tassie_travel > backup_$(date +%Y%m%d_%H%M%S).sql
```

### **第2步：执行数据库结构优化**
```sql
-- 按顺序执行 database_optimization.sql 中的语句：

-- 1. 添加用户类型字段
ALTER TABLE service_session 
ADD COLUMN user_type TINYINT NOT NULL DEFAULT 1 
COMMENT '用户类型 1-普通用户(users) 2-代理商(agents) 3-代理商操作员(agent_operators)' 
AFTER user_id;

-- 2. 添加索引
ALTER TABLE service_session 
ADD INDEX idx_user_type_id (user_type, user_id);

-- 3. 创建统一用户视图（执行 database_optimization.sql 中的视图创建语句）

-- 4. 数据迁移（更新现有数据的用户类型）
```

### **第3步：验证数据迁移结果**
```sql
-- 查看用户类型分布
SELECT 
    user_type,
    CASE user_type
        WHEN 1 THEN '普通用户'
        WHEN 2 THEN '代理商'
        WHEN 3 THEN '代理商操作员'
        ELSE '未知类型'
    END as user_type_desc,
    COUNT(*) as session_count
FROM service_session 
GROUP BY user_type;

-- 测试查询效果
SELECT 
    ss.id,
    ss.user_id,
    ss.user_type,
    vu.display_name as user_display_name,
    vu.user_table_source
FROM service_session ss
LEFT JOIN v_unified_users vu ON ss.user_id = vu.id AND ss.user_type = vu.user_type
ORDER BY ss.create_time DESC
LIMIT 5;
```

### **第4步：更新后端代码**
```bash
# 替换 ServiceSessionMapper.xml
cp ServiceSessionMapper_Updated.xml sky-server/src/main/resources/mapper/ServiceSessionMapper.xml
```

### **第5步：前端代码已自动更新**
- ✅ 用户信息列：显示用户类型和正确的用户名
- ✅ 消息数列：显示总消息数和未读消息数

### **第6步：重启服务测试**
```bash
# 重启后端服务
# 刷新后台管理页面
# 检查会话管理页面的数据显示
```

## 🎯 **预期结果**

执行完成后，你应该看到：

### **用户信息列**
```
代理商 ray | ID: 123
代理商操作员 操作员A | ID: 456  
普通用户 张三 | ID: 789
```

### **客服信息列**
```
李四 (如果已分配客服)
未分配 (如果未分配客服)
```

### **消息数列**
```
🟢 5 (总消息数)
🔴 2 (未读消息数，如果有的话)
```

## ⚠️ **注意事项**

1. **数据迁移**：现有数据会根据ID自动匹配到对应的用户类型
2. **向后兼容**：如果某些记录无法匹配，会保持默认值并显示"用户ID"
3. **性能影响**：新增索引会提高查询性能
4. **测试建议**：在生产环境执行前，请先在测试环境验证

## 🔍 **故障排除**

### **如果用户信息仍然显示不正确**
```sql
-- 检查数据迁移是否成功
SELECT user_type, COUNT(*) FROM service_session GROUP BY user_type;

-- 手动修复特定记录的用户类型
UPDATE service_session SET user_type = 2 WHERE user_id IN (SELECT id FROM agents);
UPDATE service_session SET user_type = 3 WHERE user_id IN (SELECT id FROM agent_operators);
```

### **如果消息数仍然为0**
```sql
-- 检查 service_message 表是否存在数据
SELECT COUNT(*) FROM service_message;

-- 如果没有消息表或数据，这是正常的
-- 消息数会在有实际消息时正确显示
```

## 📞 **完成确认**

执行完成后，请确认：
- [ ] 用户信息正确显示（代理商、操作员、普通用户）
- [ ] 客服信息正确显示
- [ ] 消息数显示正常
- [ ] 筛选和搜索功能正常工作

如有问题，请提供具体的错误信息和控制台日志。 