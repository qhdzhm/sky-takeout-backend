# 优惠券和推荐系统 SQL 说明文档

## 📋 文件清单

| 文件名 | 说明 | 必需 |
|--------|------|------|
| `00_execute_all_coupon_and_referral_setup.sql` | 统一执行脚本（推荐） | ⭐ |
| `01_add_referral_fields_to_users.sql` | 完善 users 表 | ✅ |
| `02_create_coupon_templates_table.sql` | 创建优惠券模板表 | ✅ |
| `03_create_user_coupons_table.sql` | 创建用户优惠券表 | ✅ |
| `04_create_coupon_usage_records_table.sql` | 创建优惠券使用记录表 | ✅ |
| `05_create_referral_records_table.sql` | 创建推荐记录表 | ✅ |
| `06_create_coupon_grant_logs_table.sql` | 创建优惠券发放日志表 | ✅ |

---

## 🚀 快速执行（推荐方式）

### 方式1：使用统一脚本（最简单）

```bash
# 在 MySQL 命令行中执行
mysql -u root -p happy_tassie_travel < 00_execute_all_coupon_and_referral_setup.sql
```

### 方式2：逐个执行

如果统一脚本执行有问题，可以按顺序逐个执行：

```bash
# 1. 完善 users 表
mysql -u root -p happy_tassie_travel < 01_add_referral_fields_to_users.sql

# 2. 创建优惠券模板表
mysql -u root -p happy_tassie_travel < 02_create_coupon_templates_table.sql

# 3. 创建用户优惠券表
mysql -u root -p happy_tassie_travel < 03_create_user_coupons_table.sql

# 4. 创建优惠券使用记录表
mysql -u root -p happy_tassie_travel < 04_create_coupon_usage_records_table.sql

# 5. 创建推荐记录表
mysql -u root -p happy_tassie_travel < 05_create_referral_records_table.sql

# 6. 创建优惠券发放日志表
mysql -u root -p happy_tassie_travel < 06_create_coupon_grant_logs_table.sql
```

### 方式3：在 MySQL Workbench 或其他 GUI 工具中执行

1. 打开 MySQL Workbench
2. 连接到 `happy_tassie_travel` 数据库
3. 打开每个 SQL 文件
4. 按顺序执行（从 01 到 06）

---

## 📊 数据库表关系图

```
┌─────────────────┐
│     users       │
│  (用户表)       │
│ - user_id (PK)  │
│ - invite_code ✨│  ← 新增字段
│ - referred_by ✨│  ← 新增字段
└────────┬────────┘
         │
         │ 1:N
         │
         ├──────────────────────────────────────┐
         │                                      │
         │                                      │
┌────────▼─────────────┐              ┌────────▼──────────────┐
│  referral_records    │              │  user_coupons         │
│   (推荐记录表)       │              │  (用户优惠券表)       │
│ - referrer_id (FK)   │              │ - user_id (FK)        │
│ - referee_id (FK)    │              │ - template_id (FK)    │
│ - reward_coupon_id ──┼──────────────┤ - coupon_code         │
└──────────────────────┘              │ - status              │
                                      └───────┬───────────────┘
                                              │
                                              │ 1:N
                                              │
                                      ┌───────▼───────────────┐
                                      │ coupon_usage_records  │
                                      │ (优惠券使用记录表)    │
                                      │ - user_coupon_id (FK) │
                                      │ - order_id            │
                                      │ - discount_amount     │
                                      └───────────────────────┘

┌──────────────────────┐
│ coupon_templates     │
│ (优惠券模板表)       │
│ - id (PK)            │
│ - template_code      │
│ - discount_type      │
│ - discount_value     │
└────────┬─────────────┘
         │
         │ 1:N
         │
┌────────▼─────────────┐
│ coupon_grant_logs    │
│ (优惠券发放日志)     │
│ - template_id (FK)   │
│ - user_id (FK)       │
│ - grant_type         │
└──────────────────────┘
```

---

## 📝 表结构说明

### 1. users 表（新增字段）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `invite_code` | VARCHAR(20) | 邀请码（唯一） |
| `referred_by` | BIGINT | 推荐人用户ID |

### 2. coupon_templates（优惠券模板表）

**核心字段**:
- `template_code`: 模板代码（唯一标识）
- `discount_type`: 折扣类型（PERCENTAGE/FIXED/CASHBACK）
- `discount_value`: 折扣值
- `min_order_amount`: 最小订单金额
- `apply_scope`: 适用范围（ALL/CATEGORY/PRODUCT）
- `validity_type`: 有效期类型（FIXED/RELATIVE）
- `stackable`: 是否可叠加

**初始数据**:
- ✅ `REFERRAL_REWARD_50`: 推荐好友奖励券（$50返现）
- ✅ `NEW_USER_50`: 新人专享券（立减$50）

### 3. user_coupons（用户优惠券表）

**核心字段**:
- `user_id`: 用户ID
- `template_id`: 模板ID
- `coupon_code`: 优惠券代码（唯一）
- `status`: 状态（AVAILABLE/USED/EXPIRED/LOCKED）
- `use_count` / `use_limit`: 使用次数控制
- `source_type`: 来源（REGISTER/REFERRAL/ACTIVITY/COMPENSATION）

### 4. coupon_usage_records（优惠券使用记录表）

**核心字段**:
- `user_coupon_id`: 用户优惠券ID
- `order_id`: 订单ID
- `original_amount`: 原始金额
- `discount_amount`: 优惠金额
- `final_amount`: 最终金额

### 5. referral_records（推荐记录表）

**核心字段**:
- `referrer_id`: 推荐人ID
- `referee_id`: 被推荐人ID
- `referral_status`: 推荐状态（PENDING/COMPLETED/REWARDED）
- `first_order_id`: 首单ID
- `reward_coupon_id`: 奖励优惠券ID

### 6. coupon_grant_logs（优惠券发放日志表）

**核心字段**:
- `grant_type`: 发放类型（AUTO/MANUAL/ACTIVITY/REFERRAL）
- `status`: 发放状态（SUCCESS/FAILED）
- `fail_reason`: 失败原因

---

## ✅ 执行后验证

执行完所有SQL后，运行以下查询验证：

```sql
-- 1. 查看所有优惠券相关表
SHOW TABLES LIKE '%coupon%';

-- 2. 查看推荐相关表
SHOW TABLES LIKE '%referral%';

-- 3. 查看 users 表新增字段
DESC users;

-- 4. 查看优惠券模板初始数据
SELECT * FROM coupon_templates;

-- 5. 查看表的行数
SELECT 'coupon_templates' AS table_name, COUNT(*) AS row_count FROM coupon_templates
UNION ALL
SELECT 'user_coupons', COUNT(*) FROM user_coupons
UNION ALL
SELECT 'coupon_usage_records', COUNT(*) FROM coupon_usage_records
UNION ALL
SELECT 'referral_records', COUNT(*) FROM referral_records
UNION ALL
SELECT 'coupon_grant_logs', COUNT(*) FROM coupon_grant_logs;
```

**预期结果**:
- ✅ coupon_templates: 2 行（推荐奖励券 + 新人券）
- ✅ 其他表: 0 行（暂无数据）

---

## 🔧 常见问题

### Q1: 执行时提示 "Table already exists"

**解决方案**: 所有表使用 `CREATE TABLE IF NOT EXISTS`，不会重复创建。如果需要重建，先删除：

```sql
DROP TABLE IF EXISTS coupon_grant_logs;
DROP TABLE IF EXISTS coupon_usage_records;
DROP TABLE IF EXISTS user_coupons;
DROP TABLE IF EXISTS referral_records;
DROP TABLE IF EXISTS coupon_templates;
-- 注意：不要删除 users 表！
```

### Q2: 外键约束错误

**原因**: users 表中的 `user_id` 字段名可能与其他表不一致。

**解决方案**: 检查 users 表的主键字段名：

```sql
SHOW CREATE TABLE users;
```

如果主键不是 `user_id`，修改 SQL 文件中的外键定义。

### Q3: 字符集问题

**解决方案**: 确保所有表使用 `utf8mb4`：

```sql
ALTER TABLE coupon_templates CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📚 下一步

SQL 执行完成后，需要：

1. **后端开发**:
   - 创建实体类（Entity）
   - 创建 Mapper 接口
   - 创建 Service 层
   - 创建 Controller 层

2. **前端开发**:
   - 我的优惠券页面
   - 订单结算优惠券选择
   - 推荐邀请页面

3. **测试**:
   - 优惠券发放流程
   - 推荐奖励流程
   - 订单使用优惠券

---

## 📞 支持

如有问题，请查看：
- `优惠券系统设计方案-完整版.md`
- `用户推荐系统实现方案.md`

---

**版本**: v1.0  
**创建日期**: 2025-10-28  
**状态**: 可执行 ✅




