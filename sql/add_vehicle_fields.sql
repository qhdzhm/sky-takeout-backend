-- 添加车辆管理新字段：ACN, Maker, Model, Year
-- 执行日期: 2025-09-23

ALTER TABLE vehicles 
ADD COLUMN acn VARCHAR(50) COMMENT 'ACN号码',
ADD COLUMN maker VARCHAR(100) COMMENT '制造商',
ADD COLUMN model VARCHAR(100) COMMENT '车型',
ADD COLUMN year INT COMMENT '年份';

-- 更新注释，说明新字段的用途
ALTER TABLE vehicles COMMENT = '车辆管理表 - 包含基本信息、状态、ACN、制造商、车型、年份等信息';

-- 为新字段添加索引以提升查询性能
CREATE INDEX idx_vehicles_acn ON vehicles(acn);
CREATE INDEX idx_vehicles_maker ON vehicles(maker);
CREATE INDEX idx_vehicles_model ON vehicles(model);
CREATE INDEX idx_vehicles_year ON vehicles(year);
