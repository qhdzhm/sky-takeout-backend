# 订单表与排团表字段映射对照

## 目的
确保拖拽操作时，订单表(`tour_bookings`)的所有字段都能正确映射到排团表(`tour_schedule_order`)中。

## 字段映射表

| 订单表字段 (tour_bookings) | 排团表字段 (tour_schedule_order) | 状态 | 说明 |
|----------------------------|----------------------------------|------|------|
| `booking_id` | `booking_id` | ✅ 已映射 | 订单ID |
| `order_number` | `order_number` | ✅ 已映射 | 订单号 |
| `tour_id` | `tour_id` | ✅ 已映射 | 旅游产品ID |
| `tour_type` | `tour_type` | ✅ 已映射 | 产品类型 |
| `user_id` | `user_id` | ✅ 已映射 | 用户ID |
| `agent_id` | `agent_id` | ✅ 已映射 | 代理商ID |
| `booking_date` | `booking_date` | ✅ 已映射 | 预订日期 |
| `flight_number` | `flight_number` | ✅ 已映射 | 航班号 |
| `arrival_departure_time` | `arrival_departure_time` | ⚠️ 需添加 | 到达航班起飞时间 |
| `arrival_landing_time` | `arrival_landing_time` | ✅ 已映射 | 到达航班降落时间 |
| `return_flight_number` | `return_flight_number` | ✅ 已映射 | 返程航班号 |
| `departure_departure_time` | `departure_departure_time` | ⚠️ 需添加 | 返程航班起飞时间 |
| `departure_landing_time` | `departure_landing_time` | ⚠️ 需添加 | 返程航班降落时间 |
| `tour_start_date` | `tour_start_date` | ✅ 已映射 | 行程开始日期 |
| `tour_end_date` | `tour_end_date` | ✅ 已映射 | 行程结束日期 |
| `pickup_date` | `pickup_date` | ✅ 已映射 | 接客日期 |
| `dropoff_date` | `dropoff_date` | ✅ 已映射 | 送客日期 |
| `pickup_location` | `pickup_location` | ✅ 已映射 | 接客地点 |
| `dropoff_location` | `dropoff_location` | ✅ 已映射 | 送客地点 |
| `service_type` | `service_type` | ✅ 已映射 | 服务类型 |
| `group_size` | `group_size` | ✅ 已映射 | 团队大小 |
| `adult_count` | `adult_count` | ✅ 已映射 | 成人数量 |
| `child_count` | `child_count` | ✅ 已映射 | 儿童数量 |
| `luggage_count` | `luggage_count` | ✅ 已映射 | 行李数量 |
| `passenger_contact` | `passenger_contact` | ✅ 已映射 | 乘客联系方式 |
| `contact_person` | `contact_person` | ✅ 已映射 | 联系人 |
| `contact_phone` | `contact_phone` | ✅ 已映射 | 联系电话 |
| `hotel_level` | `hotel_level` | ✅ 已映射 | 酒店等级 |
| `room_type` | `room_type` | ✅ 已映射 | 房间类型 |
| `hotel_room_count` | `hotel_room_count` | ✅ 已映射 | 酒店房间数量 |
| `hotel_check_in_date` | `hotel_check_in_date` | ✅ 已映射 | 酒店入住日期 |
| `hotel_check_out_date` | `hotel_check_out_date` | ✅ 已映射 | 酒店退房日期 |
| `room_details` | `room_details` | ✅ 已映射 | 房间详情 |
| `special_requests` | `special_requests` | ✅ 已映射 | 特殊要求 |
| `itinerary_details` | `itinerary_details` | ✅ 已映射 | 行程详情 |
| `status` | `status` | ✅ 已映射 | 订单状态 |
| `payment_status` | `payment_status` | ✅ 已映射 | 支付状态 |
| `total_price` | `total_price` | ✅ 已映射 | 总价 |
| `created_at` | `created_at` | ✅ 已映射 | 创建时间 |
| `updated_at` | `updated_at` | ✅ 已映射 | 更新时间 |
| `is_first_order` | `is_first_order` | ✅ 已映射 | 是否首单 |
| `from_referral` | `from_referral` | ✅ 已映射 | 是否来自推荐 |
| `referral_code` | `referral_code` | ✅ 已映射 | 推荐码 |
| `operator_id` | `operator_id` | ✅ 已映射 | 操作员ID |

## 排团表独有字段
| 字段名 | 说明 |
|--------|------|
| `id` | 排团表主键ID |
| `day_number` | 第几天（固定顺序） |
| `tour_date` | 实际行程日期 |
| `title` | 行程标题 |
| `description` | 行程描述 |
| `display_order` | 显示顺序 |
| `tour_name` | 旅游产品名称 |
| `tour_location` | 旅游目的地 |

## 需要处理的问题

### 1. 缺失字段
需要为排团表添加以下字段：
- `arrival_departure_time` - 到达航班起飞时间
- `departure_departure_time` - 返程航班起飞时间  
- `departure_landing_time` - 返程航班降落时间

### 2. 数据完整性
拖拽操作时需要确保：
1. 所有订单表字段都被复制到排团表
2. 特别注意酒店信息、航班信息和备注信息的完整性
3. 保持数据类型和格式的一致性

## 建议的解决步骤

1. **执行数据库字段添加**
   ```sql
   -- 执行 add_missing_fields_to_schedule_table.sql
   ```

2. **更新后端拖拽逻辑**
   - 确保所有字段都被映射
   - 特别处理新增的航班时间字段

3. **前端字段验证**
   - 验证拖拽后数据的完整性
   - 确保酒店信息、备注等关键信息不丢失 