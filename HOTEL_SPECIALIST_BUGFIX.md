# 酒店专员功能 - MyBatis 参数错误修复

## 问题描述

在实现酒店专员筛选功能时，前端请求后端 API 出现 500 错误：

```
Parameter 'hotelSpecialist' not found. Available parameters are [param5, param6, checkOutDate, guestPhone, hotelId, param3, param4, checkInDate, param1, status, guestName, param2]
```

## 错误原因

虽然我们在以下地方添加了 `hotelSpecialist` 字段：
1. ✅ 数据库表结构（`hotel_bookings.hotel_specialist`）
2. ✅ 实体类（`HotelBooking.java`）
3. ✅ DTO 和 VO（`HotelBookingDTO.java`, `HotelBookingVO.java`）
4. ✅ XML Mapper 查询语句和结果映射
5. ✅ 前端界面和请求参数

但是**遗漏了在后端接口方法签名中添加参数**，导致 MyBatis 无法找到对应的参数。

## 具体修复

### 1. Mapper 接口 (`HotelBookingMapper.java`)

**修复前：**
```java
List<HotelBookingVO> pageQuery(@Param("status") String status,
                               @Param("guestName") String guestName,
                               @Param("guestPhone") String guestPhone,
                               @Param("hotelId") Integer hotelId,
                               @Param("checkInDate") LocalDate checkInDate,
                               @Param("checkOutDate") LocalDate checkOutDate);
```

**修复后：**
```java
List<HotelBookingVO> pageQuery(@Param("status") String status,
                               @Param("guestName") String guestName,
                               @Param("guestPhone") String guestPhone,
                               @Param("hotelId") Integer hotelId,
                               @Param("hotelSpecialist") String hotelSpecialist,  // 新增
                               @Param("checkInDate") LocalDate checkInDate,
                               @Param("checkOutDate") LocalDate checkOutDate);
```

### 2. Service 接口 (`HotelBookingService.java`)

**修复前：**
```java
PageResult pageQuery(Integer page, Integer pageSize, String status, String guestName, String guestPhone,
                     Integer hotelId, LocalDate checkInDate, LocalDate checkOutDate);
```

**修复后：**
```java
PageResult pageQuery(Integer page, Integer pageSize, String status, String guestName, String guestPhone,
                     Integer hotelId, String hotelSpecialist, LocalDate checkInDate, LocalDate checkOutDate);
```

### 3. Service 实现 (`HotelBookingServiceImpl.java`)

**修复前：**
```java
public PageResult pageQuery(Integer page, Integer pageSize, String status, String guestName, String guestPhone,
                           Integer hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
    // ...
    Page<HotelBookingVO> pageResult = (Page<HotelBookingVO>) hotelBookingMapper.pageQuery(
        status, guestName, guestPhone, hotelId, checkInDate, checkOutDate);
    // ...
}
```

**修复后：**
```java
public PageResult pageQuery(Integer page, Integer pageSize, String status, String guestName, String guestPhone,
                           Integer hotelId, String hotelSpecialist, LocalDate checkInDate, LocalDate checkOutDate) {
    // ...
    Page<HotelBookingVO> pageResult = (Page<HotelBookingVO>) hotelBookingMapper.pageQuery(
        status, guestName, guestPhone, hotelId, hotelSpecialist, checkInDate, checkOutDate);
    // ...
}
```

### 4. Controller (`HotelBookingController.java`)

**修复前：**
```java
@GetMapping("/page")
public Result<PageResult> pageQuery(
        // ... 其他参数
        @RequestParam(required = false) Integer hotelId,
        @RequestParam(required = false) LocalDate checkInDate,
        @RequestParam(required = false) LocalDate checkOutDate) {
    // ...
    PageResult pageResult = hotelBookingService.pageQuery(page, pageSize, status, guestName, guestPhone,
            hotelId, checkInDate, checkOutDate);
    // ...
}
```

**修复后：**
```java
@GetMapping("/page")
public Result<PageResult> pageQuery(
        // ... 其他参数
        @RequestParam(required = false) Integer hotelId,
        @ApiParam(name = "hotelSpecialist", value = "酒店专员")
        @RequestParam(required = false) String hotelSpecialist,  // 新增
        @RequestParam(required = false) LocalDate checkInDate,
        @RequestParam(required = false) LocalDate checkOutDate) {
    // ...
    PageResult pageResult = hotelBookingService.pageQuery(page, pageSize, status, guestName, guestPhone,
            hotelId, hotelSpecialist, checkInDate, checkOutDate);
    // ...
}
```

## 学习要点

1. **参数一致性**：从 Controller → Service → Mapper → XML，所有层的方法参数必须保持一致
2. **MyBatis 参数映射**：XML 中使用的参数名必须在 Mapper 接口中用 `@Param` 注解声明
3. **分层修改**：添加新功能时，需要从上到下逐层修改所有相关方法签名

## 验证方法

1. 重启后端服务
2. 访问前端酒店预订管理页面
3. 确认页面能正常加载数据
4. 测试酒店专员筛选功能
5. 测试"只看我的"开关功能

## 相关文件

- `sky-server/src/main/java/com/sky/mapper/HotelBookingMapper.java`
- `sky-server/src/main/java/com/sky/service/HotelBookingService.java`
- `sky-server/src/main/java/com/sky/service/impl/HotelBookingServiceImpl.java`
- `sky-server/src/main/java/com/sky/controller/admin/HotelBookingController.java`
