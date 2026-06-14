-- =============================================================================
-- 用户辖区与角色扩展迁移
-- 依据: docs/database-workflow-design.md, docs/gap-analysis.md
-- 注意: 请勿在生产环境未经审核直接执行
-- =============================================================================

USE `activity_registration`;

-- 扩展 role 字段长度以容纳新角色枚举值
ALTER TABLE `sys_user`
  MODIFY COLUMN `role` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色';

-- 若列已存在请跳过对应 ADD COLUMN
ALTER TABLE `sys_user`
  ADD COLUMN `province_name` VARCHAR(100) NULL COMMENT '省' AFTER `role`,
  ADD COLUMN `city_name` VARCHAR(100) NULL COMMENT '市' AFTER `province_name`,
  ADD COLUMN `district_name` VARCHAR(100) NULL COMMENT '区/县' AFTER `city_name`,
  ADD COLUMN `school_name` VARCHAR(200) NULL COMMENT '学校' AFTER `district_name`;

-- =============================================================================
-- 迁移完成
-- =============================================================================
