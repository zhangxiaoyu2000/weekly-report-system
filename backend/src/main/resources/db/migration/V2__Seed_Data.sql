-- =============================================
-- 周报系统种子数据脚本
-- Version: 2.0
-- Author: System
-- Date: 2025-09-05
-- Description: 插入开发和测试环境的种子数据
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;

-- =============================================
-- 1. 插入部门数据
-- =============================================

-- 插入更多部门层级结构
INSERT INTO `departments` (`id`, `name`, `description`, `parent_id`, `level`, `sort_order`, `status`) VALUES
-- 一级部门
(10, '技术部', '负责技术开发和维护', 1, 2, 1, 'ACTIVE'),
(20, '产品部', '负责产品设计和规划', 1, 2, 2, 'ACTIVE'),
(30, '市场部', '负责市场推广和销售', 1, 2, 3, 'ACTIVE'),
(40, '人事部', '负责人力资源管理', 1, 2, 4, 'ACTIVE'),
(50, '财务部', '负责财务管理和审计', 1, 2, 5, 'ACTIVE'),

-- 二级部门 - 技术部下属
(101, '前端开发组', '负责前端页面和用户界面开发', 10, 3, 1, 'ACTIVE'),
(102, '后端开发组', '负责服务器端和API开发', 10, 3, 2, 'ACTIVE'),
(103, '测试组', '负责软件测试和质量保证', 10, 3, 3, 'ACTIVE'),
(104, '运维组', '负责系统运维和部署', 10, 3, 4, 'ACTIVE'),

-- 二级部门 - 产品部下属
(201, '产品设计组', '负责产品需求分析和设计', 20, 3, 1, 'ACTIVE'),
(202, 'UI/UX设计组', '负责用户界面和体验设计', 20, 3, 2, 'ACTIVE'),

-- 二级部门 - 市场部下属
(301, '市场推广组', '负责市场活动和品牌推广', 30, 3, 1, 'ACTIVE'),
(302, '销售组', '负责产品销售和客户关系', 30, 3, 2, 'ACTIVE');

-- =============================================
-- 2. 插入用户数据
-- =============================================

-- 插入管理层用户
INSERT INTO `users` (`id`, `username`, `email`, `password`, `first_name`, `last_name`, `role`, `department_id`, `status`) VALUES
-- 部门经理
(10, 'tech_manager', 'tech.manager@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '张', '伟', 'MANAGER', 10, 'ACTIVE'),
(11, 'product_manager', 'product.manager@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '李', '娜', 'MANAGER', 20, 'ACTIVE'),
(12, 'marketing_manager', 'marketing.manager@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '王', '强', 'MANAGER', 30, 'ACTIVE'),
(13, 'hr_manager', 'hr.manager@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '刘', '芳', 'MANAGER', 40, 'ACTIVE'),
(14, 'finance_manager', 'finance.manager@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '陈', '明', 'MANAGER', 50, 'ACTIVE'),

-- 技术部员工
(100, 'frontend_dev1', 'frontend1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '赵', '磊', 'EMPLOYEE', 101, 'ACTIVE'),
(101, 'frontend_dev2', 'frontend2@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '孙', '丽', 'EMPLOYEE', 101, 'ACTIVE'),
(102, 'backend_dev1', 'backend1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '周', '杰', 'EMPLOYEE', 102, 'ACTIVE'),
(103, 'backend_dev2', 'backend2@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '吴', '娟', 'EMPLOYEE', 102, 'ACTIVE'),
(104, 'tester1', 'tester1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '郑', '涛', 'EMPLOYEE', 103, 'ACTIVE'),
(105, 'devops1', 'devops1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '冯', '静', 'EMPLOYEE', 104, 'ACTIVE'),

-- 产品部员工
(200, 'product_designer1', 'pd1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '蒋', '文', 'EMPLOYEE', 201, 'ACTIVE'),
(201, 'ui_designer1', 'ui1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '韩', '雪', 'EMPLOYEE', 202, 'ACTIVE'),
(202, 'ux_designer1', 'ux1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '杨', '洋', 'EMPLOYEE', 202, 'ACTIVE'),

-- 市场部员工
(300, 'marketing1', 'marketing1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '朱', '亮', 'EMPLOYEE', 301, 'ACTIVE'),
(301, 'sales1', 'sales1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '徐', '敏', 'EMPLOYEE', 302, 'ACTIVE'),

-- 人事和财务员工
(400, 'hr1', 'hr1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '林', '华', 'EMPLOYEE', 40, 'ACTIVE'),
(500, 'accountant1', 'acc1@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', '何', '鹏', 'EMPLOYEE', 50, 'ACTIVE');

-- =============================================
-- 3. 更新部门经理关系
-- =============================================
UPDATE `departments` SET `manager_id` = 10 WHERE `id` = 10;  -- 技术部
UPDATE `departments` SET `manager_id` = 11 WHERE `id` = 20;  -- 产品部
UPDATE `departments` SET `manager_id` = 12 WHERE `id` = 30;  -- 市场部
UPDATE `departments` SET `manager_id` = 13 WHERE `id` = 40;  -- 人事部
UPDATE `departments` SET `manager_id` = 14 WHERE `id` = 50;  -- 财务部

-- =============================================
-- 4. 插入更多周报模板
-- =============================================
INSERT INTO `templates` (`id`, `name`, `description`, `content`, `fields`, `is_default`, `is_public`, `created_by`, `department_id`, `status`) VALUES

-- 技术部专用模板
(10, '技术开发周报模板', '适用于技术开发人员的周报模板', 
'# 技术开发周报 - {{week_start}} 至 {{week_end}}

## 本周开发进度
{{development_progress}}

## 完成的功能/任务
{{completed_features}}

## 代码提交统计
- 提交次数: {{commit_count}}
- 代码行数: {{code_lines}}
- Bug修复: {{bug_fixes}}

## 技术问题和解决方案
{{technical_issues}}

## 代码审查情况
{{code_review}}

## 下周开发计划
{{next_week_dev_plan}}

## 需要的技术支持
{{technical_support}}
', 
'{"fields": [{"name": "development_progress", "label": "开发进度", "type": "textarea", "required": true}, {"name": "completed_features", "label": "完成功能", "type": "textarea", "required": true}, {"name": "commit_count", "label": "提交次数", "type": "number", "required": false}, {"name": "code_lines", "label": "代码行数", "type": "number", "required": false}, {"name": "bug_fixes", "label": "Bug修复", "type": "number", "required": false}, {"name": "technical_issues", "label": "技术问题", "type": "textarea", "required": false}, {"name": "code_review", "label": "代码审查", "type": "textarea", "required": false}, {"name": "next_week_dev_plan", "label": "下周计划", "type": "textarea", "required": true}, {"name": "technical_support", "label": "技术支持", "type": "textarea", "required": false}]}',
FALSE, TRUE, 10, 10, 'ACTIVE'),

-- 产品部专用模板
(11, '产品设计周报模板', '适用于产品设计人员的周报模板',
'# 产品设计周报 - {{week_start}} 至 {{week_end}}

## 产品需求分析
{{requirement_analysis}}

## 设计方案进展
{{design_progress}}

## 用户研究和反馈
{{user_research}}

## 原型设计完成情况
{{prototype_status}}

## 与开发团队协作情况
{{dev_collaboration}}

## 下周产品计划
{{next_week_product_plan}}

## 需要的资源支持
{{resource_needed}}
',
'{"fields": [{"name": "requirement_analysis", "label": "需求分析", "type": "textarea", "required": true}, {"name": "design_progress", "label": "设计进展", "type": "textarea", "required": true}, {"name": "user_research", "label": "用户研究", "type": "textarea", "required": false}, {"name": "prototype_status", "label": "原型状态", "type": "textarea", "required": false}, {"name": "dev_collaboration", "label": "团队协作", "type": "textarea", "required": false}, {"name": "next_week_product_plan", "label": "下周计划", "type": "textarea", "required": true}, {"name": "resource_needed", "label": "资源需求", "type": "textarea", "required": false}]}',
FALSE, TRUE, 11, 20, 'ACTIVE'),

-- 市场部专用模板
(12, '市场推广周报模板', '适用于市场推广人员的周报模板',
'# 市场推广周报 - {{week_start}} 至 {{week_end}}

## 市场活动执行情况
{{marketing_activities}}

## 销售数据统计
{{sales_statistics}}

## 客户反馈收集
{{customer_feedback}}

## 竞品分析
{{competitor_analysis}}

## 推广渠道效果
{{channel_performance}}

## 下周市场计划
{{next_week_marketing_plan}}

## 预算使用情况
{{budget_usage}}
',
'{"fields": [{"name": "marketing_activities", "label": "市场活动", "type": "textarea", "required": true}, {"name": "sales_statistics", "label": "销售统计", "type": "textarea", "required": true}, {"name": "customer_feedback", "label": "客户反馈", "type": "textarea", "required": false}, {"name": "competitor_analysis", "label": "竞品分析", "type": "textarea", "required": false}, {"name": "channel_performance", "label": "渠道效果", "type": "textarea", "required": false}, {"name": "next_week_marketing_plan", "label": "下周计划", "type": "textarea", "required": true}, {"name": "budget_usage", "label": "预算使用", "type": "textarea", "required": false}]}',
FALSE, TRUE, 12, 30, 'ACTIVE');

-- =============================================
-- 5. 插入示例周报数据
-- =============================================
INSERT INTO `weekly_reports` (`id`, `user_id`, `template_id`, `title`, `content`, `summary`, `status`, `week_start`, `week_end`, `submitted_at`, `priority`) VALUES

-- 技术部周报
(1000, 100, 10, '前端开发第1周工作总结', 
'# 技术开发周报 - 2025-08-26 至 2025-09-01

## 本周开发进度
完成了用户登录页面和主页面的开发，包括响应式设计和基本的表单验证功能。

## 完成的功能/任务
1. 登录页面UI实现
2. 主页面布局设计
3. 用户认证流程前端对接
4. 响应式设计适配移动端

## 代码提交统计
- 提交次数: 12
- 代码行数: 850
- Bug修复: 3

## 技术问题和解决方案
遇到CSS兼容性问题，通过引入PostCSS和Autoprefixer解决了浏览器兼容问题。

## 下周开发计划
1. 完成周报列表页面
2. 实现周报创建功能
3. 优化页面加载性能
', 
'完成登录和主页面开发，解决CSS兼容性问题，下周继续周报功能开发', 
'SUBMITTED', '2025-08-26', '2025-09-01', '2025-09-01 18:30:00', 'NORMAL'),

(1001, 102, 10, '后端API开发第1周进展',
'# 技术开发周报 - 2025-08-26 至 2025-09-01

## 本周开发进度
完成了用户认证相关的后端API开发，包括注册、登录、JWT token管理等功能。

## 完成的功能/任务
1. 用户注册API
2. 用户登录API
3. JWT token生成和验证
4. 用户信息查询API
5. 数据库连接和JPA配置

## 代码提交统计
- 提交次数: 15
- 代码行数: 1200
- Bug修复: 5

## 技术问题和解决方案
JWT token刷新机制设计，通过双token机制(access token + refresh token)保证安全性。

## 下周开发计划
1. 完成周报CRUD API
2. 实现文件上传功能
3. 添加数据验证和异常处理
',
'完成用户认证API开发，实现JWT双token机制，下周开发周报相关API',
'PUBLISHED', '2025-08-26', '2025-09-01', '2025-09-01 19:00:00', 'HIGH'),

-- 产品部周报
(1002, 200, 11, '产品需求分析第1周总结',
'# 产品设计周报 - 2025-08-26 至 2025-09-01

## 产品需求分析
完成了周报系统的核心需求调研，与各部门进行了深入沟通，收集了详细的功能需求。

## 设计方案进展
1. 完成了系统架构设计
2. 制定了用户角色权限方案
3. 设计了周报模板机制
4. 规划了评论和审批流程

## 用户研究和反馈
通过访谈10位潜在用户，收集到以下关键反馈：
- 希望有多种模板选择
- 需要移动端友好的界面
- 希望有数据统计和分析功能

## 下周产品计划
1. 完成产品原型设计
2. 制定详细的PRD文档
3. 与技术团队进行需求评审
',
'完成需求调研和架构设计，收集用户反馈，下周制作产品原型',
'REVIEWED', '2025-08-26', '2025-09-01', '2025-09-01 17:45:00', 'HIGH');

-- =============================================
-- 6. 插入评论数据
-- =============================================
INSERT INTO `comments` (`id`, `report_id`, `user_id`, `parent_id`, `content`, `type`, `is_private`) VALUES
(1000, 1000, 10, NULL, '前端页面设计很不错，用户体验考虑得很周到。建议下周可以加上loading效果。', 'COMMENT', FALSE),
(1001, 1000, 104, NULL, '建议添加更多的单元测试，特别是表单验证部分。', 'SUGGESTION', FALSE),
(1002, 1000, 100, 1000, '谢谢建议！Loading效果已经加入下周计划。', 'COMMENT', FALSE),

(1003, 1001, 10, NULL, 'API设计规范，JWT机制很安全。代码质量也很高。', 'APPROVAL', FALSE),
(1004, 1001, 103, NULL, '我会配合进行API测试，需要你提供详细的接口文档。', 'COMMENT', FALSE),
(1005, 1001, 102, 1004, '好的，明天发给你详细的API文档和Postman collection。', 'COMMENT', FALSE),

(1006, 1002, 11, NULL, '需求调研很全面，用户反馈很有价值。原型设计时重点关注移动端体验。', 'COMMENT', FALSE),
(1007, 1002, 10, NULL, '架构设计方案很合理，期待看到详细的PRD文档。', 'APPROVAL', FALSE);

-- =============================================
-- 7. 更新统计数据
-- =============================================

-- 更新模板使用次数
UPDATE `templates` SET `usage_count` = 2 WHERE `id` = 10;
UPDATE `templates` SET `usage_count` = 1 WHERE `id` = 11;

-- 更新周报查看次数
UPDATE `weekly_reports` SET `view_count` = 15 WHERE `id` = 1000;
UPDATE `weekly_reports` SET `view_count` = 23 WHERE `id` = 1001;
UPDATE `weekly_reports` SET `view_count` = 8 WHERE `id` = 1002;

-- 更新用户最后登录时间
UPDATE `users` SET `last_login` = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) WHERE `id` > 1;

-- =============================================
-- 8. 插入更多历史周报数据(用于测试)
-- =============================================
INSERT INTO `weekly_reports` (`user_id`, `template_id`, `title`, `content`, `summary`, `status`, `week_start`, `week_end`, `submitted_at`, `priority`, `view_count`) VALUES

-- 上周的周报
(100, 1, '前端开发第0周准备工作', '主要进行了环境搭建和技术选型...', '环境搭建完成', 'PUBLISHED', '2025-08-19', '2025-08-25', '2025-08-25 17:00:00', 'NORMAL', 12),
(102, 1, '后端环境搭建周报', '完成了开发环境的配置和基础框架搭建...', '基础框架完成', 'PUBLISHED', '2025-08-19', '2025-08-25', '2025-08-25 16:30:00', 'NORMAL', 18),
(200, 1, '产品前期调研周报', '进行了市场调研和竞品分析...', '完成前期调研', 'PUBLISHED', '2025-08-19', '2025-08-25', '2025-08-25 18:15:00', 'NORMAL', 9),

-- 本周草稿
(101, 1, '前端组件开发进展', '正在开发通用UI组件库...', '组件开发中', 'DRAFT', '2025-09-02', '2025-09-08', NULL, 'NORMAL', 2),
(103, 1, '后端服务优化工作', '对现有API进行性能优化...', 'API优化进行中', 'DRAFT', '2025-09-02', '2025-09-08', NULL, 'NORMAL', 1);

-- =============================================
-- 结束标记
-- =============================================
-- 种子数据插入完成