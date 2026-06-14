-- =============================================================================
-- 开发环境专用种子数据 — 禁止在生产环境执行
-- 账号凭证请通过团队安全渠道分发，勿在仓库中写明明文口令
-- =============================================================================

USE `activity_registration`;

INSERT INTO `sys_user` (
  `username`, `password`, `nickname`, `role`, `status`,
  `province_name`, `city_name`, `district_name`, `school_name`
)
VALUES
  (
    'admin',
    '$2a$10$auoJXA771voMcGFrdjwo..qHqvayiOMEsciuMguUi0g9SeHnADHQS',
    '系统管理员',
    'ADMIN',
    1,
    '广东省',
    '深圳市',
    '南山区',
    NULL
  ),
  (
    'user1',
    '$2a$10$auoJXA771voMcGFrdjwo..qHqvayiOMEsciuMguUi0g9SeHnADHQS',
    '测试用户',
    'USER',
    1,
    '广东省',
    '深圳市',
    '南山区',
    '示例中学'
  )
ON DUPLICATE KEY UPDATE
  `password` = VALUES(`password`),
  `nickname` = VALUES(`nickname`),
  `role` = VALUES(`role`),
  `status` = VALUES(`status`),
  `province_name` = VALUES(`province_name`),
  `city_name` = VALUES(`city_name`),
  `district_name` = VALUES(`district_name`),
  `school_name` = VALUES(`school_name`);

INSERT INTO `activity` (
  `title`, `description`, `location`,
  `start_time`, `end_time`,
  `signup_start_time`, `signup_end_time`,
  `max_count`, `current_count`, `status`, `created_by`
)
VALUES (
  '2026 春季技术分享会',
  '面向开发者的技术交流活动，包含主题演讲与自由讨论环节。',
  '线上腾讯会议',
  DATE_ADD(NOW(), INTERVAL 7 DAY),
  DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 3 HOUR,
  NOW(),
  DATE_ADD(NOW(), INTERVAL 5 DAY),
  100,
  0,
  'PUBLISHED',
  (SELECT `id` FROM `sys_user` WHERE `username` = 'admin' LIMIT 1)
);
