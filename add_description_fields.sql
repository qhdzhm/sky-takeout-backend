-- 为一日游和多日游表添加简短描述和概述配图字段
-- 执行前请先备份数据库！

-- 修改一日游表
ALTER TABLE day_tours 
ADD COLUMN short_description VARCHAR(500) NULL COMMENT '简短描述-用于标题下方' AFTER description,
ADD COLUMN overview_image VARCHAR(500) NULL COMMENT '概述配图URL' AFTER short_description;

-- 修改多日游表
ALTER TABLE group_tours 
ADD COLUMN short_description VARCHAR(500) NULL COMMENT '简短描述-用于标题下方' AFTER description,
ADD COLUMN overview_image VARCHAR(500) NULL COMMENT '概述配图URL' AFTER short_description;

-- 验证字段是否添加成功
DESCRIBE day_tours;
DESCRIBE group_tours;


