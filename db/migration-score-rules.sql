-- =============================================================================
-- 活动评分权重扩展
-- 依据: ScoreService 评分规则
-- =============================================================================

USE `activity_registration`;

ALTER TABLE `activity`
  ADD COLUMN `manual_score_weight` DECIMAL(5,4) NOT NULL DEFAULT 1.0000
    COMMENT '人工分权重' AFTER `status`,
  ADD COLUMN `ai_score_weight` DECIMAL(5,4) NOT NULL DEFAULT 0.0000
    COMMENT 'AI分权重' AFTER `manual_score_weight`;
