-- 修复task_type字段长度问题
-- 增加task_type字段的长度以避免数据截断

-- 增加task_type字段长度从VARCHAR(20)到VARCHAR(50)
ALTER TABLE tasks MODIFY COLUMN task_type VARCHAR(50) NOT NULL;

-- 验证字段长度修改
DESCRIBE tasks;