-- 修复lisi用户的role字段为空的问题
-- 根据之前的数据，lisi应该是操作员角色（role=1）

-- 1. 首先查看lisi用户的当前状态
SELECT id, username, name, role, email, avatar, last_login_time 
FROM employees 
WHERE username = 'lisi';

-- 2. 修复lisi用户的role字段
UPDATE employees 
SET role = 1, 
    email = COALESCE(email, '123123123123@123.com'),  -- 如果email为空，设置默认值
    update_time = NOW()
WHERE username = 'lisi' AND role IS NULL;

-- 3. 验证修复结果
SELECT id, username, name, role, email, avatar, last_login_time 
FROM employees 
WHERE username = 'lisi';

-- 4. 检查所有role为空的员工
SELECT id, username, name, role, create_time, update_time 
FROM employees 
WHERE role IS NULL;