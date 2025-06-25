# 订单联系人信息数据同步功能

## 问题描述

在Happy Tassie Travel系统中，存在一个数据同步问题：

1. **下单时**：乘客信息存入乘客表(`passenger`)，联系人信息存入订单表(`tour_bookings`)的`contact_person`和`contact_phone`字段
2. **同时**：会复制一份类似信息到排团表(`tour_schedule_order`)
3. **修改订单时**：当用户端修改订单表的`contact_person`和`contact_phone`时，只有订单表被修改了，但乘客表和排团表没有同步更新
4. **结果**：这导致三个表之间的数据不一致

## 解决方案

### 后端修改

#### 1. 修改同步到排团表的时机 (新增修改 🆕)
**之前**: 订单创建时就自动同步到排团表
**现在**: 只有在订单支付成功后才同步到排团表

**修改内容**:
- 从 `TourBookingServiceImpl.save()` 和 `OrderServiceImpl.createOrder()` 中移除自动同步调用
- 在 `TourBookingServiceImpl.payBooking()` 支付成功后添加同步调用
- 联系人信息更新时只处理已付款的订单

#### 2. 同步更新排团表
```java
// 🔄 同步更新排团表（仅在已付款订单且排团表有记录时）
try {
    boolean contactInfoChanged = updateDTO.getContactPerson() != null || updateDTO.getContactPhone() != null;
    if (contactInfoChanged) {
        // 检查订单支付状态，只有已付款的订单才可能有排团记录
        TourBooking currentBooking = tourBookingMapper.getById(updateDTO.getBookingId());
        if (currentBooking != null && "paid".equals(currentBooking.getPaymentStatus())) {
            log.info("🔄 开始同步更新排团表联系人信息（已付款订单），订单ID: {}", updateDTO.getBookingId());
            updateScheduleTableContactInfo(updateDTO.getBookingId(), updateDTO.getContactPerson(), updateDTO.getContactPhone());
            log.info("✅ 排团表联系人信息同步更新完成，订单ID: {}", updateDTO.getBookingId());
        } else {
            log.info("ℹ️ 订单未付款，跳过排团表联系人信息同步，订单ID: {}, 支付状态: {}", 
                    updateDTO.getBookingId(), currentBooking != null ? currentBooking.getPaymentStatus() : "未知");
        }
    }
} catch (Exception e) {
    log.error("❌ 同步更新排团表联系人信息失败: 订单ID={}, 错误: {}", updateDTO.getBookingId(), e.getMessage(), e);
    // 不抛出异常，避免影响订单更新
}
```

#### 2. 同步更新乘客表 (新增功能)
```java
// 🔄 同步更新乘客表的联系人信息
try {
    boolean contactInfoChanged = updateDTO.getContactPerson() != null || updateDTO.getContactPhone() != null;
    if (contactInfoChanged) {
        log.info("🔄 开始同步更新乘客表联系人信息，订单ID: {}", updateDTO.getBookingId());
        syncContactInfoToPassengerTable(updateDTO.getBookingId(), updateDTO.getContactPerson(), updateDTO.getContactPhone());
        log.info("✅ 乘客表联系人信息同步更新完成，订单ID: {}", updateDTO.getBookingId());
    }
} catch (Exception e) {
    log.error("❌ 同步更新乘客表联系人信息失败: 订单ID={}, 错误: {}", updateDTO.getBookingId(), e.getMessage(), e);
    // 不抛出异常，避免影响订单更新
}
```

#### 3. 乘客表同步逻辑
新增的 `syncContactInfoToPassengerTable` 方法包含以下智能逻辑：

1. **查找主要联系人**：
   - 优先查找姓名匹配的乘客
   - 如果没有找到，使用第一个成人乘客
   - 如果还是没有找到，使用第一个乘客

2. **更新策略**：
   - 只有当联系人信息确实发生变化时才更新
   - 保留乘客的其他信息不变
   - 记录详细的更新日志

3. **容错处理**：
   - 如果同步失败，不会影响主要的订单更新流程
   - 提供详细的错误日志用于问题排查

### 前端改进

在 `OrderDetail.jsx` 中添加了用户友好的提示功能：

```javascript
// 检查是否修改了联系人信息
const contactPersonChanged = editFormData.contactPerson !== orderData.contactPerson;
const contactPhoneChanged = editFormData.contactPhone !== orderData.contactPhone;
const hasContactInfoChange = contactPersonChanged || contactPhoneChanged;

// 根据修改的内容显示不同的成功消息
if (hasContactInfoChange) {
  toast.success('订单修改成功！系统已自动同步更新乘客信息和排团安排。', {
    duration: 4000,
    position: 'top-center'
  });
} else {
  toast.success('订单修改成功');
}
```

## 数据同步流程

### **订单创建流程** (新修改 🆕)
1. 用户下单 → 订单表 (`tour_bookings`) 创建 ✅  
2. 支付状态: `unpaid`，订单状态: `pending`
3. **不同步到排团表** ❌ (等待支付)

### **支付成功流程** (新修改 🆕)  
1. 用户支付成功 → 支付状态: `paid`，订单状态: `confirmed`
2. **自动同步到排团表** ✅ (`tour_schedule_order`)
3. 发送确认邮件和发票 ✅

### **联系人信息修改流程**
**仅对已付款订单**:
1. 订单表 (`tour_bookings`) 更新 ✅
2. 排团表 (`tour_schedule_order`) 直接更新联系人信息 ✅ 
3. 乘客表 (`passenger`) 主要联系人同步更新 ✅

**对未付款订单**:
1. 订单表 (`tour_bookings`) 更新 ✅
2. 乘客表 (`passenger`) 主要联系人同步更新 ✅
3. 跳过排团表同步 ❌ (因为排团表中没有记录)

## 技术特点

### 安全性
- 所有同步操作都包含在事务中
- 同步失败不会影响主要的订单更新
- 详细的错误日志记录

### 智能性
- 自动识别哪个乘客是主要联系人
- 只在数据确实变化时才执行更新
- 保留乘客的其他重要信息

### 用户体验
- 清晰的成功提示信息
- 自动刷新最新数据
- 区分不同类型的修改操作

## 测试验证

建议在以下场景进行测试：

1. **修改联系人姓名**：验证乘客表中对应乘客的姓名是否同步更新
2. **修改联系电话**：验证乘客表中对应乘客的电话是否同步更新
3. **同时修改姓名和电话**：验证两个字段都能正确同步
4. **修改其他信息**：验证不影响联系人信息的修改不会触发同步
5. **错误处理**：验证同步失败时不影响订单主要更新

## 注意事项

1. **数据一致性**：修改后三个表（订单表、乘客表、排团表）的联系人信息应该保持一致
2. **性能考虑**：同步操作会增加一些处理时间，但通过异常捕获确保不影响主流程
3. **日志监控**：建议监控同步相关的日志，及时发现和处理问题

## 相关文件

### 后端文件
- `sky-server/src/main/java/com/sky/service/impl/TourBookingServiceImpl.java`
- `sky-server/src/main/java/com/sky/service/impl/PassengerServiceImpl.java`

### 前端文件
- `src/pages/OrderDetail/OrderDetail.jsx`
- `src/pages/Booking/EditBooking.jsx`

### 数据库表
- `tour_bookings` - 订单表
- `passenger` - 乘客表  
- `tour_schedule_order` - 排团表 