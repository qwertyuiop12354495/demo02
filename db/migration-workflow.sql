-- =============================================================================
-- Workflow 数据库增量迁移
-- 依据: docs/database-workflow-design.md
-- 目标库: activity_registration (MySQL 8.0+)
--
-- ⚠️  警告:
--   1. 本脚本不会 DROP 已有业务表
--   2. 请勿在生产环境未经审核直接执行
--   3. 执行前请完整备份数据库
--   4. 部分 ALTER 不可重复执行；若列/表已存在会报错，请按注释跳过
-- =============================================================================

USE `activity_registration`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- Part 1: activity 表扩展 (ALTER TABLE)
-- =============================================================================

-- 1.1 新增 upload_deadline
-- 若已存在请跳过本句
ALTER TABLE `activity`
  ADD COLUMN `upload_deadline` DATETIME(3) NULL
    COMMENT '作品上传截止时间；NULL 表示默认等于 signup_end_time'
    AFTER `signup_end_time`;

-- 1.2 活动状态 OFFLINE → CLOSED（数据迁移）
UPDATE `activity`
SET `status` = 'CLOSED'
WHERE `status` = 'OFFLINE';

-- 1.3 状态 CHECK 约束（若 chk_activity_status_v2 已存在请先 DROP）
-- MySQL 8.0.16+ 支持命名 CHECK；重复执行需先:
--   ALTER TABLE activity DROP CHECK chk_activity_status_v2;
ALTER TABLE `activity`
  ADD CONSTRAINT `chk_activity_status_v2`
    CHECK (`status` IN ('DRAFT', 'PUBLISHED', 'CLOSED'));

-- 1.4 upload_deadline 不得早于报名截止
-- 若 chk_activity_upload_deadline 已存在请先 DROP
ALTER TABLE `activity`
  ADD CONSTRAINT `chk_activity_upload_deadline`
    CHECK (
      `upload_deadline` IS NULL
      OR `signup_end_time` <= `upload_deadline`
    );

-- 说明: 以下字段已满足规格，无需变更
--   title          ← 规格 name/title
--   signup_start_time, signup_end_time
--   created_by     ← 规格 create_user_id
--   created_at     ← 规格 create_time
--   updated_at     ← 规格 update_time
-- 保留 MVP 字段: description, location, start_time, end_time, max_count, current_count

-- =============================================================================
-- Part 2: work 表 (CREATE TABLE)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `work` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id`     BIGINT UNSIGNED  NOT NULL                COMMENT '活动ID',
  `teacher_id`      BIGINT UNSIGNED  NOT NULL                COMMENT '教师用户ID',
  `title`           VARCHAR(200)     NOT NULL                COMMENT '作品标题',
  `category`        VARCHAR(100)     NULL                    COMMENT '作品类别',
  `equipment`       VARCHAR(200)     NULL                    COMMENT '使用设备',
  `duration`        INT UNSIGNED     NULL                    COMMENT '时长(秒)',
  `province_name`   VARCHAR(100)     NOT NULL                COMMENT '省',
  `city_name`       VARCHAR(100)     NOT NULL                COMMENT '市',
  `district_name`   VARCHAR(100)     NOT NULL                COMMENT '区/县',
  `school_name`     VARCHAR(200)     NOT NULL                COMMENT '学校',
  `current_step`    VARCHAR(32)      NOT NULL DEFAULT 'SCHOOL' COMMENT '当前流程步骤',
  `current_status`  VARCHAR(32)      NOT NULL DEFAULT 'DRAFT'  COMMENT '当前状态',
  `final_score`     DECIMAL(8,2)     NULL                    COMMENT '终局分数',
  `final_result`    VARCHAR(32)      NOT NULL DEFAULT 'PENDING' COMMENT '终局结果',
  `deleted`         TINYINT          NOT NULL DEFAULT 0      COMMENT '软删: 0正常 1已删',
  `created_at`      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at`      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  -- 生成列: 仅 deleted=0 时参与唯一约束，允许删除后再次报名
  `active_teacher_id`  BIGINT UNSIGNED GENERATED ALWAYS AS (
    IF(`deleted` = 0, `teacher_id`, NULL)
  ) STORED COMMENT '活跃教师ID(生成列)',
  `active_activity_id` BIGINT UNSIGNED GENERATED ALWAYS AS (
    IF(`deleted` = 0, `activity_id`, NULL)
  ) STORED COMMENT '活跃活动ID(生成列)',

  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_work_teacher_activity_active` (`active_teacher_id`, `active_activity_id`),

  KEY `idx_work_activity_id` (`activity_id`),
  KEY `idx_work_teacher_id` (`teacher_id`),
  KEY `idx_work_step_status` (`current_step`, `current_status`),
  KEY `idx_work_scope` (`province_name`, `city_name`, `district_name`, `school_name`),

  CONSTRAINT `fk_work_activity`
    FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_work_teacher`
    FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_work_current_step`
    CHECK (`current_step` IN (
      'SCHOOL', 'DISTRICT', 'CITY', 'PROVINCE',
      'SCORE_DISTRICT', 'SCORE_CITY', 'SCORE_PROVINCE', 'COMPLETED'
    )),

  CONSTRAINT `chk_work_current_status`
    CHECK (`current_status` IN (
      'DRAFT', 'SUBMITTED', 'REVISION_REQUIRED', 'APPROVED'
    )),

  CONSTRAINT `chk_work_final_result`
    CHECK (`final_result` IN (
      'PENDING', 'PROMOTED', 'ELIMINATED', 'AWARD', 'NOT_AWARDED'
    )),

  CONSTRAINT `chk_work_deleted`
    CHECK (`deleted` IN (0, 1)),

  CONSTRAINT `chk_work_final_score`
    CHECK (`final_score` IS NULL OR (`final_score` >= 0 AND `final_score` <= 100)),

  CONSTRAINT `chk_work_duration`
    CHECK (`duration` IS NULL OR `duration` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='教师作品表';

-- =============================================================================
-- Part 3: work_file 表 (CREATE TABLE)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `work_file` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `work_id`     BIGINT UNSIGNED  NOT NULL                COMMENT '作品ID',
  `file_name`   VARCHAR(255)     NOT NULL                COMMENT '原始文件名',
  `file_url`    VARCHAR(500)     NOT NULL                COMMENT '存储URL/路径',
  `file_type`   VARCHAR(50)      NOT NULL                COMMENT '文件类型/MIME',
  `file_size`   BIGINT UNSIGNED  NOT NULL                COMMENT '文件大小(字节)',
  `deleted`     TINYINT          NOT NULL DEFAULT 0      COMMENT '软删: 0正常 1已删',
  `created_at`  DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传时间',

  PRIMARY KEY (`id`),

  KEY `idx_work_file_work_id` (`work_id`),
  KEY `idx_work_file_work_deleted` (`work_id`, `deleted`),

  CONSTRAINT `fk_work_file_work`
    FOREIGN KEY (`work_id`) REFERENCES `work` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_work_file_deleted`
    CHECK (`deleted` IN (0, 1)),

  CONSTRAINT `chk_work_file_size`
    CHECK (`file_size` > 0 AND `file_size` <= 52428800)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='作品附件表';

-- =============================================================================
-- Part 4: work_revision_feedback 表 (CREATE TABLE)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `work_revision_feedback` (
  `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `work_id`      BIGINT UNSIGNED  NOT NULL                COMMENT '作品ID',
  `review_step`  VARCHAR(32)      NOT NULL                COMMENT '退回时所在步骤',
  `round_no`     INT UNSIGNED     NOT NULL                COMMENT '退回轮次',
  `feedback`     VARCHAR(2000)    NOT NULL                COMMENT '退回意见',
  `reviewer_id`  BIGINT UNSIGNED  NOT NULL                COMMENT '审核人ID',
  `created_at`   DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

  PRIMARY KEY (`id`),

  KEY `idx_wrf_work_id` (`work_id`),
  KEY `idx_wrf_work_step` (`work_id`, `review_step`),

  CONSTRAINT `fk_wrf_work`
    FOREIGN KEY (`work_id`) REFERENCES `work` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_wrf_reviewer`
    FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_wrf_review_step`
    CHECK (`review_step` IN (
      'SCHOOL', 'DISTRICT', 'CITY', 'PROVINCE',
      'SCORE_DISTRICT', 'SCORE_CITY', 'SCORE_PROVINCE', 'COMPLETED'
    )),

  CONSTRAINT `chk_wrf_round_no`
    CHECK (`round_no` >= 1)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='作品退回修改意见表';

-- =============================================================================
-- Part 5: review_record 表 (CREATE TABLE)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `review_record` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `work_id`       BIGINT UNSIGNED  NOT NULL                COMMENT '作品ID',
  `activity_id`   BIGINT UNSIGNED  NOT NULL                COMMENT '活动ID(冗余)',
  `reviewer_id`   BIGINT UNSIGNED  NOT NULL                COMMENT '审核/评委ID',
  `review_level`  VARCHAR(32)      NOT NULL                COMMENT '审核/打分级别',
  `manual_score`  DECIMAL(8,2)     NULL                    COMMENT '人工评分',
  `ai_score`      DECIMAL(8,2)     NULL                    COMMENT 'AI辅助分',
  `final_score`   DECIMAL(8,2)     NULL                    COMMENT '该条记录最终分',
  `result`        VARCHAR(32)      NULL                    COMMENT '审核/打分结果',
  `created_at`    DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_review_work_level_reviewer` (`work_id`, `review_level`, `reviewer_id`),

  KEY `idx_review_work_level` (`work_id`, `review_level`),
  KEY `idx_review_reviewer_level` (`reviewer_id`, `review_level`),

  CONSTRAINT `fk_review_work`
    FOREIGN KEY (`work_id`) REFERENCES `work` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_review_activity`
    FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_review_reviewer`
    FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_review_level`
    CHECK (`review_level` IN (
      'SCHOOL', 'DISTRICT', 'CITY', 'PROVINCE',
      'SCORE_DISTRICT', 'SCORE_CITY', 'SCORE_PROVINCE', 'COMPLETED'
    )),

  CONSTRAINT `chk_review_manual_score`
    CHECK (`manual_score` IS NULL OR (`manual_score` >= 0 AND `manual_score` <= 100)),

  CONSTRAINT `chk_review_ai_score`
    CHECK (`ai_score` IS NULL OR (`ai_score` >= 0 AND `ai_score` <= 100)),

  CONSTRAINT `chk_review_final_score`
    CHECK (`final_score` IS NULL OR (`final_score` >= 0 AND `final_score` <= 100))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='审核与打分记录表';

-- =============================================================================
-- Part 6: public_notice 表 (CREATE TABLE)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `public_notice` (
  `id`                   BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`                VARCHAR(200)     NOT NULL                COMMENT '公示标题',
  `content`              TEXT             NOT NULL                COMMENT '公示内容',
  `notice_type`          VARCHAR(32)      NOT NULL                COMMENT '公示类型',
  `visible_scope_type`   VARCHAR(32)      NOT NULL                COMMENT '可见范围类型',
  `province_name`        VARCHAR(100)     NULL                    COMMENT '省',
  `city_name`            VARCHAR(100)     NULL                    COMMENT '市',
  `district_name`        VARCHAR(100)     NULL                    COMMENT '区/县',
  `school_name`          VARCHAR(200)     NULL                    COMMENT '学校',
  `created_by`           BIGINT UNSIGNED  NOT NULL                COMMENT '创建人ID',
  `publish_time`         DATETIME(3)      NULL                    COMMENT '发布时间',
  `created_at`           DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at`           DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (`id`),

  KEY `idx_notice_publish` (`publish_time`),
  KEY `idx_notice_scope` (`visible_scope_type`, `province_name`, `city_name`),
  KEY `idx_notice_created_by` (`created_by`),

  CONSTRAINT `fk_notice_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_notice_visible_scope_type`
    CHECK (`visible_scope_type` IN ('PUBLIC', 'PROVINCE', 'CITY', 'DISTRICT', 'SCHOOL')),

  CONSTRAINT `chk_notice_type`
    CHECK (`notice_type` IN ('REVIEW_RESULT', 'SCORE_RESULT', 'AWARD_LIST', 'GENERAL'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='结果公示表';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- Part 7: 迁移后验证 SQL（手动执行，勿纳入自动化）
-- =============================================================================
-- SHOW COLUMNS FROM activity LIKE 'upload_deadline';
-- SELECT status, COUNT(*) FROM activity GROUP BY status;
-- SHOW CREATE TABLE work\G
-- SHOW INDEX FROM work;
-- SHOW INDEX FROM review_record;
-- SELECT COUNT(*) FROM activity_registration;  -- 确认旧表数据仍在

-- =============================================================================
-- 迁移完成
-- =============================================================================
