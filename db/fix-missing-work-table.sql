USE `activity_registration`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `work`;

CREATE TABLE `work` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `activity_id`     BIGINT UNSIGNED  NOT NULL,
  `teacher_id`      BIGINT UNSIGNED  NOT NULL,
  `title`           VARCHAR(200)     NOT NULL,
  `category`        VARCHAR(100)     NULL,
  `equipment`       VARCHAR(200)     NULL,
  `duration`        INT UNSIGNED     NULL,
  `province_name`   VARCHAR(100)     NOT NULL,
  `city_name`       VARCHAR(100)     NOT NULL,
  `district_name`   VARCHAR(100)     NOT NULL,
  `school_name`     VARCHAR(200)     NOT NULL,
  `current_step`    VARCHAR(32)      NOT NULL DEFAULT 'SCHOOL',
  `current_status`  VARCHAR(32)      NOT NULL DEFAULT 'DRAFT',
  `final_score`     DECIMAL(8,2)     NULL,
  `final_result`    VARCHAR(32)      NOT NULL DEFAULT 'PENDING',
  `deleted`         TINYINT          NOT NULL DEFAULT 0,
  `created_at`      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `active_teacher_id`  BIGINT UNSIGNED GENERATED ALWAYS AS (
    IF(`deleted` = 0, `teacher_id`, NULL)
  ) STORED,
  `active_activity_id` BIGINT UNSIGNED GENERATED ALWAYS AS (
    IF(`deleted` = 0, `activity_id`, NULL)
  ) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_teacher_activity_active` (`active_teacher_id`, `active_activity_id`),
  KEY `idx_work_activity_id` (`activity_id`),
  KEY `idx_work_teacher_id` (`teacher_id`),
  KEY `idx_work_step_status` (`current_step`, `current_status`),
  KEY `idx_work_scope` (`province_name`, `city_name`, `district_name`, `school_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
