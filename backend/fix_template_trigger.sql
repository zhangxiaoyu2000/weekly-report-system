-- 修复template_id触发器问题
-- 删除引用不存在字段的触发器

-- 删除模板使用量更新触发器（因为template_id字段已被删除）
DROP TRIGGER IF EXISTS trg_update_template_usage;

-- 验证触发器是否被删除
SHOW TRIGGERS WHERE `Trigger` = 'trg_update_template_usage';