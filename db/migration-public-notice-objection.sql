-- =============================================================================
-- 手工公示异议说明字段
-- =============================================================================

USE `activity_registration`;

ALTER TABLE `public_notice`
  ADD COLUMN `objection_note` VARCHAR(2000) NULL
    COMMENT '异议说明' AFTER `content`;
