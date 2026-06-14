-- =============================================================================
-- 活动报名系统 - 数据库初始化脚本
-- 基于: docs/database-design.md
-- 数据库: MySQL 8.0+
-- 注意: 本脚本仅用于初始化，请手动执行，勿在生产环境未经审核直接运行
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 创建数据库
-- -----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `activity_registration`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `activity_registration`;

-- -----------------------------------------------------------------------------
-- 2. 删除已存在表（按依赖逆序）
-- -----------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `activity_registration`;
DROP TABLE IF EXISTS `activity`;
DROP TABLE IF EXISTS `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 3. 系统用户表 sys_user
-- -----------------------------------------------------------------------------
CREATE TABLE `sys_user` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)      NOT NULL                COMMENT '登录名',
  `password`    VARCHAR(255)     NOT NULL                COMMENT 'BCrypt 加密密码',
  `nickname`    VARCHAR(50)      NOT NULL                COMMENT '显示昵称',
  `role`        VARCHAR(20)      NOT NULL DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
  `status`      TINYINT          NOT NULL DEFAULT 1      COMMENT '账号状态: 1正常 0禁用',
  `created_at`  DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at`  DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='系统用户表';

-- -----------------------------------------------------------------------------
-- 4. 活动表 activity
--    必选字段: title, description, location, start_time, end_time,
--              signup_start_time, signup_end_time, max_count, current_count, status
-- -----------------------------------------------------------------------------
CREATE TABLE `activity` (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`             VARCHAR(200)     NOT NULL                COMMENT '活动标题',
  `description`       TEXT             NULL                    COMMENT '活动描述',
  `location`          VARCHAR(200)     NULL                    COMMENT '地点/线上说明',
  `start_time`        DATETIME(3)      NULL                    COMMENT '活动开始时间',
  `end_time`          DATETIME(3)      NULL                    COMMENT '活动结束时间',
  `signup_start_time` DATETIME(3)      NOT NULL                COMMENT '报名开始时间',
  `signup_end_time`   DATETIME(3)      NOT NULL                COMMENT '报名截止时间',
  `max_count`         INT UNSIGNED     NOT NULL                COMMENT '人数上限',
  `current_count`     INT UNSIGNED     NOT NULL DEFAULT 0      COMMENT '当前已通过人数',
  `status`            VARCHAR(20)      NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT / PUBLISHED / OFFLINE',
  `created_by`        BIGINT UNSIGNED  NOT NULL                COMMENT '创建人ID',
  `created_at`        DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at`        DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (`id`),
  KEY `idx_activity_status` (`status`),
  KEY `idx_activity_signup_end` (`signup_end_time`),
  KEY `idx_activity_created_by` (`created_by`),
  KEY `idx_activity_status_signup_end` (`status`, `signup_end_time`),

  CONSTRAINT `fk_activity_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_activity_max_count`
    CHECK (`max_count` >= 1),

  CONSTRAINT `chk_activity_current_count`
    CHECK (`current_count` <= `max_count`),

  CONSTRAINT `chk_activity_signup_time`
    CHECK (`signup_start_time` < `signup_end_time`),

  CONSTRAINT `chk_activity_event_time`
    CHECK (`start_time` IS NULL OR `end_time` IS NULL OR `start_time` <= `end_time`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='活动表';

-- -----------------------------------------------------------------------------
-- 5. 活动报名表 activity_registration
--    必选字段: user_id, activity_id, status, apply_time, audit_time, audit_remark
--    唯一约束: uk_user_activity 保证同一用户同一活动仅一条报名记录
-- -----------------------------------------------------------------------------
CREATE TABLE `activity_registration` (
  `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT UNSIGNED  NOT NULL                COMMENT '报名用户ID',
  `activity_id`  BIGINT UNSIGNED  NOT NULL                COMMENT '活动ID',
  `status`       VARCHAR(20)      NOT NULL                COMMENT '状态: PENDING / APPROVED / REJECTED / CANCELLED',
  `apply_time`   DATETIME(3)      NOT NULL                COMMENT '用户提交报名时间',
  `audit_time`   DATETIME(3)      NULL                    COMMENT '审核时间',
  `audit_remark` VARCHAR(500)     NULL                    COMMENT '审核备注(如拒绝原因)',
  `apply_remark` VARCHAR(500)     NULL                    COMMENT '用户报名备注(可选)',
  `audited_by`   BIGINT UNSIGNED  NULL                    COMMENT '审核人ID',
  `created_at`   DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
  `updated_at`   DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
  KEY `idx_registration_activity_id` (`activity_id`),
  KEY `idx_registration_activity_status` (`activity_id`, `status`),
  KEY `idx_registration_user_apply_time` (`user_id`, `apply_time` DESC),

  CONSTRAINT `fk_registration_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_registration_activity`
    FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_registration_audited_by`
    FOREIGN KEY (`audited_by`) REFERENCES `sys_user` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `chk_registration_status`
    CHECK (`status` IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='活动报名表';

-- -----------------------------------------------------------------------------
-- 6. 种子数据
-- -----------------------------------------------------------------------------
-- 生产环境仅执行本脚本完成建库建表，勿导入种子数据。
-- 本地开发联调请另行执行: db/dev-seed.sql

-- =============================================================================
-- 初始化完成
-- =============================================================================
