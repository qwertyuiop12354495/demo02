-- =============================================================================
-- Workflow 全角色测试账号（仅开发/演示环境）
-- 统一密码明文: Test@123456
-- BCrypt: $2a$10$4rMFiv3iz3ROgshzITxzSOmuEhHmWIaaVmvLSCusA1Y7wmRaJGnvu
-- 辖区基准: 广东省 / 深圳市 / 南山区 / 示例中学
-- =============================================================================

USE `activity_registration`;

SET NAMES utf8mb4;

-- 统一 BCrypt 哈希（密码 Test@123456）
SET @pwd = '$2a$10$4rMFiv3iz3ROgshzITxzSOmuEhHmWIaaVmvLSCusA1Y7wmRaJGnvu';

INSERT INTO `sys_user` (
  `username`, `password`, `nickname`, `role`, `status`,
  `province_name`, `city_name`, `district_name`, `school_name`
) VALUES
  ('teacher1',        @pwd, '教师甲',   'TEACHER',           1, '广东省', '深圳市', '南山区', '示例中学'),
  ('school_admin',    @pwd, '校管',     'SCHOOL_ADMIN',      1, '广东省', '深圳市', '南山区', '示例中学'),
  ('district_admin',  @pwd, '区管',     'DISTRICT_ADMIN',    1, '广东省', '深圳市', '南山区', NULL),
  ('city_admin',      @pwd, '市管',     'CITY_ADMIN',        1, '广东省', '深圳市', NULL,     NULL),
  ('province_admin',  @pwd, '省管',     'PROVINCE_ADMIN',    1, '广东省', NULL,     NULL,     NULL),
  ('district_reviewer1', @pwd, '区评委1', 'DISTRICT_REVIEWER', 1, '广东省', '深圳市', '南山区', NULL),
  ('district_reviewer2', @pwd, '区评委2', 'DISTRICT_REVIEWER', 1, '广东省', '深圳市', '南山区', NULL),
  ('city_reviewer1',    @pwd, '市评委1', 'CITY_REVIEWER',     1, '广东省', '深圳市', NULL,     NULL),
  ('city_reviewer2',    @pwd, '市评委2', 'CITY_REVIEWER',     1, '广东省', '深圳市', NULL,     NULL),
  ('province_reviewer1', @pwd, '省评委1', 'PROVINCE_REVIEWER', 1, '广东省', NULL,     NULL,     NULL),
  ('province_reviewer2', @pwd, '省评委2', 'PROVINCE_REVIEWER', 1, '广东省', NULL,     NULL,     NULL)
ON DUPLICATE KEY UPDATE
  `password` = VALUES(`password`),
  `nickname` = VALUES(`nickname`),
  `role` = VALUES(`role`),
  `status` = VALUES(`status`),
  `province_name` = VALUES(`province_name`),
  `city_name` = VALUES(`city_name`),
  `district_name` = VALUES(`district_name`),
  `school_name` = VALUES(`school_name`);

-- 兼容旧账号：admin -> 省管，user1 -> 教师
UPDATE `sys_user` SET
  `password` = @pwd,
  `nickname` = '省管(旧admin)',
  `role` = 'PROVINCE_ADMIN',
  `province_name` = '广东省',
  `city_name` = NULL,
  `district_name` = NULL,
  `school_name` = NULL
WHERE `username` = 'admin';

UPDATE `sys_user` SET
  `password` = @pwd,
  `nickname` = '教师(旧user1)',
  `role` = 'TEACHER',
  `province_name` = '广东省',
  `city_name` = '深圳市',
  `district_name` = '南山区',
  `school_name` = '示例中学'
WHERE `username` = 'user1';
