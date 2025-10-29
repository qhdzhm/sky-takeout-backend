# 小团功能实现指南

## 概述
添加"是否小团"选项，类似于"是否包含酒店"功能。用户可以选择是否升级为小团，系统会根据设置的小团差价自动计算额外费用。

## 数据库修改 ✅ 已完成

### 1. group_tours表
```sql
ALTER TABLE group_tours 
ADD COLUMN small_group_price_difference DECIMAL(10,2) DEFAULT 0.00 
COMMENT '小团差价（每人每天）' AFTER guide_fee;
```

### 2. day_tours表
```sql
ALTER TABLE day_tours 
ADD COLUMN small_group_price_difference DECIMAL(10,2) DEFAULT 0.00 
COMMENT '小团差价（每人）' AFTER guide_fee;
```

### 3. 示例数据
- ID=31的产品（塔斯马尼亚5日4晚跟团游-升级14人精品小团）
- 小团差价设置为：100元/人/天

## 后端修改

### 1. 实体类修改

#### GroupTour.java
```java
/** 小团差价（每人每天） */
private BigDecimal smallGroupPriceDifference;
```

#### DayTour.java  
```java
/** 小团差价（每人） */
private BigDecimal smallGroupPriceDifference;
```

#### GroupTourDTO.java / DayTourDTO.java
添加相同字段

### 2. Mapper修改

#### GroupTourMapper.java
在查询SQL中添加字段：
```java
@Select("SELECT ..., small_group_price_difference AS smallGroupPriceDifference ...")
```

#### DayTourMapper.java
同样添加字段映射

### 3. 订单实体修改

#### TourBooking.java
```java
/** 是否小团 */
private Boolean isSmallGroup;

/** 小团差价 */
private BigDecimal smallGroupPriceDifference;
```

### 4. 价格计算逻辑修改

#### BookingService.calculateTourPrice()
```java
// 添加小团费用计算
if (isSmallGroup && smallGroupPriceDifference != null) {
    BigDecimal smallGroupFee = smallGroupPriceDifference
        .multiply(new BigDecimal(adultCount))
        .multiply(new BigDecimal(nights)); // 跟团游按天数计算
    totalPrice = totalPrice.add(smallGroupFee);
}
```

## 前端修改

### 1. AgentBooking.jsx

#### 添加状态管理
```javascript
const [formData, setFormData] = useState({
    ...
    include_hotel: true,
    is_small_group: false, // 新增：是否小团
    ...
});
```

#### 添加UI组件（在"是否包含酒店"附近）
```jsx
{/* 是否小团选项 */}
<Form.Group className="mb-3">
    <Form.Check 
        type="checkbox"
        id="is_small_group"
        label="升级为精品小团（每人每天加$100）"
        checked={formData.is_small_group || false}
        onChange={(e) => {
            const isSmallGroup = e.target.checked;
            setFormData(prev => ({
                ...prev,
                is_small_group: isSmallGroup
            }));
        }}
    />
    <Form.Text className="text-muted">
        <FaUsers className="me-1" />
        精品小团提供更私密的旅行体验，人数限制在12-14人
    </Form.Text>
</Form.Group>
```

#### 更新价格计算参数
```javascript
const calculationParams = {
    ...
    isSmallGroup: formData.is_small_group, // 新增
    smallGroupPriceDifference: tourData?.smallGroupPriceDifference || 0, // 新增
};
```

#### 更新订单提交数据
```javascript
const bookingData = {
    ...
    isSmallGroup: formData.is_small_group, // 新增
    smallGroupPriceDifference: tourData?.smallGroupPriceDifference, // 新增
};
```

### 2. 产品编辑页面

#### GroupTourDetail.jsx / DayTourDetail.jsx

添加小团差价编辑字段：
```jsx
<Form.Group className="mb-3">
    <Form.Label>小团差价（元/人/天）</Form.Label>
    <Form.Control
        type="number"
        step="0.01"
        value={formData.smallGroupPriceDifference || 0}
        onChange={(e) => setFormData({
            ...formData,
            smallGroupPriceDifference: e.target.value
        })}
        placeholder="例如：100"
    />
    <Form.Text className="text-muted">
        设置升级为小团时，每人每天的额外费用
    </Form.Text>
</Form.Group>
```

## 价格计算公式

### 跟团游（Group Tour）
```
基础价格 = 产品价格 × 成人数 + 儿童价格 × 儿童数
酒店费用 = (如果includeHotel) ? 酒店价格 × 房间数 × 晚数 : 0
小团费用 = (如果isSmallGroup) ? 小团差价 × 成人数 × 天数 : 0
总价格 = 基础价格 + 酒店费用 + 小团费用
```

### 一日游（Day Tour）
```
基础价格 = 产品价格 × 成人数 + 儿童价格 × 儿童数
小团费用 = (如果isSmallGroup) ? 小团差价 × 成人数 : 0
总价格 = 基础价格 + 小团费用
```

## UI/UX设计建议

### 1. 位置
- 放在"酒店信息"section之后
- 作为独立的checkbox选项
- 显示差价信息，让用户清楚额外费用

### 2. 交互
- 默认不选中（false）
- 勾选后实时更新总价
- 显示清晰的价格差异
- 添加tooltip说明小团的优势

### 3. 价格展示
```
原价：$1,200
酒店费用：$300
小团升级：+$400 (4人 × $100 × 1天)
────────────────
总计：$1,900
```

## 测试案例

### 案例1：不选小团
```
产品：南部3日游（$460）
人数：2成人
酒店：包含（$100/晚）
小团：否
预期：460×2 + 100×2×2 = $1,320
```

### 案例2：选择小团
```
产品：南部3日游（$460）
人数：2成人
酒店：包含（$100/晚）
小团：是（$100/人/天）
小团额外费用：100×2×3 = $600
预期：460×2 + 100×2×2 + 600 = $1,920
```

### 案例3：精品小团产品
```
产品：塔斯马尼亚5日4晚跟团游-升级14人精品小团（$1512）
人数：1成人
酒店：包含
小团：是（$100/人/天）
小团额外费用：100×1×5 = $500
预期：1512 + 酒店费用 + 500
```

## 文件修改清单

### 后端
- [ ] `GroupTour.java` - 添加smallGroupPriceDifference字段
- [ ] `DayTour.java` - 添加smallGroupPriceDifference字段
- [ ] `GroupTourDTO.java` - 添加字段
- [ ] `DayTourDTO.java` - 添加字段
- [ ] `TourBooking.java` - 添加isSmallGroup字段
- [ ] `GroupTourMapper.java` - 更新SQL查询
- [ ] `DayTourMapper.java` - 更新SQL查询
- [ ] `BookingService.java` - 更新价格计算逻辑

### 前端
- [ ] `AgentBooking.jsx` - 添加小团选项UI和逻辑
- [ ] `GroupTourDetail.jsx` - 添加小团差价编辑字段
- [ ] `DayTourDetail.jsx` - 添加小团差价编辑字段
- [ ] `GroupTours.jsx` - 列表显示小团差价（可选）
- [ ] `DayTours.jsx` - 列表显示小团差价（可选）

## 注意事项

1. **向后兼容**：is_small_group默认为false，不影响现有订单
2. **数据验证**：smallGroupPriceDifference必须≥0
3. **权限控制**：只有中介账号才能看到和使用此功能
4. **价格显示**：订单详情页要显示小团费用明细
5. **编辑限制**：订单确认后，小团选项不能修改



