-- Migrate shared JeeSite SWM dictionaries into RuoYi global dictionaries.
-- system_dict_type/system_dict_data are @TenantIgnore and available to all tenants.

DROP TEMPORARY TABLE IF EXISTS `tmp_system_dict_type_seed`;
CREATE TEMPORARY TABLE `tmp_system_dict_type_seed` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(100) NOT NULL,
  `remark` VARCHAR(500) NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_system_dict_type_seed`
(`id`, `name`, `type`, `remark`, `create_time`, `update_time`) VALUES
(1923224000000000003, '智慧劳动力-预警类型枚举', 'warning_content_enum', '预警报警记录', '2025-05-16 11:52:00', '2025-11-07 09:26:49'),
(1923224000000000001, '预警管理-预警性质枚举', 'warning_type_enum', '预警管理', '2025-05-16 11:50:00', '2025-07-07 21:56:01'),
(1923225000000000001, '智慧劳动力-危险源类别枚举', 'hazard_category_enum', '智慧劳动力', '2025-05-16 12:00:00', '2025-05-16 12:00:00'),
(1923225000000000002, '智慧劳动力-是否加入巡检枚举', 'is_patrol_included_enum', '智慧劳动力', '2025-05-16 12:02:00', '2025-05-16 12:02:00'),
(1923225000000000003, '智慧劳动力-危险源状态枚举', 'hazard_status_enum', '智慧劳动力', '2025-05-16 12:03:00', '2025-05-16 12:03:00'),
(1800000000000000001, '智慧安全帽-是否厂内员工', 'external_personnel_enum', '是否厂外人员字典', '2025-07-03 19:03:23', '2025-07-04 09:57:55'),
(1990692700457451520, '智慧安全帽-电池电量', 'helmet_battery_enum', NULL, '2025-11-18 16:05:00', '2025-11-18 16:05:00');

UPDATE `system_dict_type` target
JOIN `tmp_system_dict_type_seed` seed
  ON target.`type` COLLATE utf8mb4_unicode_ci = seed.`type` COLLATE utf8mb4_unicode_ci
 AND target.`deleted` = b'0'
SET target.`name` = seed.`name`,
    target.`status` = 0,
    target.`remark` = seed.`remark`,
    target.`updater` = '1',
    target.`update_time` = seed.`update_time`,
    target.`deleted_time` = NULL;

INSERT INTO `system_dict_type`
(`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`)
SELECT seed.`id`, seed.`name`, seed.`type`, 0, seed.`remark`, '1', seed.`create_time`,
       '1', seed.`update_time`, b'0', NULL
FROM `tmp_system_dict_type_seed` seed
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_type` target
  WHERE target.`type` COLLATE utf8mb4_unicode_ci = seed.`type` COLLATE utf8mb4_unicode_ci
    AND target.`deleted` = b'0'
);

DROP TEMPORARY TABLE IF EXISTS `tmp_system_dict_data_seed`;
CREATE TEMPORARY TABLE `tmp_system_dict_data_seed` (
  `id` BIGINT NOT NULL,
  `sort` INT NOT NULL,
  `label` VARCHAR(100) NOT NULL,
  `value` VARCHAR(100) NOT NULL,
  `dict_type` VARCHAR(100) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_system_dict_data_seed`
(`id`, `sort`, `label`, `value`, `dict_type`, `create_time`, `update_time`) VALUES
(1939950701346516992, 80,  '跌落报警', '跌落报警', 'warning_content_enum', '2025-07-01 15:34:24', '2025-07-01 15:34:24'),
(1939950802659930112, 110, '脱帽报警', '脱帽报警', 'warning_content_enum', '2025-07-01 15:34:49', '2025-07-01 15:34:49'),
(1939951096231849984, 200, '危险区域闯入提示', '危险区域闯入提示', 'warning_content_enum', '2025-07-01 15:35:59', '2025-11-06 16:54:39'),
(1946226331897024512, 380, '应急呼叫', '应急呼叫', 'warning_content_enum', '2025-07-18 23:11:31', '2025-11-06 17:10:34'),
(1955122472739303424, 440, '长时间静止报警', '长时间静止报警', 'warning_content_enum', '2025-08-12 12:21:37', '2025-08-12 12:22:07'),
(1923224100000000001, 10,  '主动报警', '1', 'warning_type_enum', '2025-05-16 11:50:10', '2025-11-08 14:45:02'),
(1923224100000000002, 20,  '被动报警', '2', 'warning_type_enum', '2025-05-16 11:50:20', '2025-11-08 14:45:07'),
(1923225100000000001, 10,  '气站', '0', 'hazard_category_enum', '2025-05-16 12:00:10', '2025-06-26 10:55:09'),
(1923225100000000002, 20,  '吊钩', '1', 'hazard_category_enum', '2025-05-16 12:00:20', '2025-06-26 10:55:19'),
(1923225100000000003, 30,  '油漆库', '2', 'hazard_category_enum', '2025-05-16 12:00:30', '2025-06-26 10:55:27'),
(1923225100000000004, 40,  '空压机房', '3', 'hazard_category_enum', '2025-05-16 12:00:40', '2025-06-26 10:55:37'),
(1923225100000000005, 50,  '机械伤害风险', '4', 'hazard_category_enum', '2025-05-16 12:00:50', '2025-05-16 12:00:50'),
(1923225100000000006, 60,  '化学品泄漏风险', '5', 'hazard_category_enum', '2025-05-16 12:01:00', '2025-05-16 12:01:00'),
(1923225100000000007, 70,  '其他风险', '99', 'hazard_category_enum', '2025-05-16 12:01:10', '2025-05-16 12:01:10'),
(1923225200000000001, 10,  '否', '0', 'is_patrol_included_enum', '2025-05-16 12:02:10', '2025-05-16 12:02:10'),
(1923225200000000002, 20,  '是', '1', 'is_patrol_included_enum', '2025-05-16 12:02:20', '2025-05-16 12:02:20'),
(1923225300000000001, 10,  '待处理', '0', 'hazard_status_enum', '2025-05-16 12:03:10', '2025-05-16 12:03:10'),
(1923225300000000002, 20,  '处理中', '1', 'hazard_status_enum', '2025-05-16 12:03:20', '2025-05-16 12:03:20'),
(1923225300000000003, 30,  '已关闭', '2', 'hazard_status_enum', '2025-05-16 12:03:30', '2025-05-16 12:03:30'),
(1923225300000000004, 40,  '已忽略', '3', 'hazard_status_enum', '2025-05-16 12:03:40', '2025-05-16 12:03:40'),
(1940953376151760896, 30,  '是', '1', 'external_personnel_enum', '2025-07-04 09:58:41', '2025-07-04 09:58:41'),
(1940953417511792640, 60,  '否', '0', 'external_personnel_enum', '2025-07-04 09:58:51', '2025-07-04 09:58:51'),
(1990692816182493184, 30,  '电量少于30%', '30', 'helmet_battery_enum', '2025-11-18 16:05:27', '2025-11-18 16:05:27'),
(1990692879235465216, 60,  '电量少于50%', '50', 'helmet_battery_enum', '2025-11-18 16:05:42', '2025-11-18 16:05:42');

UPDATE `system_dict_data` target
JOIN `tmp_system_dict_data_seed` seed
  ON target.`dict_type` COLLATE utf8mb4_unicode_ci = seed.`dict_type` COLLATE utf8mb4_unicode_ci
 AND target.`value` COLLATE utf8mb4_unicode_ci = seed.`value` COLLATE utf8mb4_unicode_ci
 AND target.`deleted` = b'0'
SET target.`sort` = seed.`sort`,
    target.`label` = seed.`label`,
    target.`status` = 0,
    target.`color_type` = 'default',
    target.`updater` = '1',
    target.`update_time` = seed.`update_time`;

INSERT INTO `system_dict_data`
(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`,
 `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT seed.`id`, seed.`sort`, seed.`label`, seed.`value`, seed.`dict_type`, 0, 'default', NULL,
       NULL, '1', seed.`create_time`, '1', seed.`update_time`, b'0'
FROM `tmp_system_dict_data_seed` seed
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data` target
  WHERE target.`dict_type` COLLATE utf8mb4_unicode_ci = seed.`dict_type` COLLATE utf8mb4_unicode_ci
    AND target.`value` COLLATE utf8mb4_unicode_ci = seed.`value` COLLATE utf8mb4_unicode_ci
    AND target.`deleted` = b'0'
);

DROP TEMPORARY TABLE IF EXISTS `tmp_system_dict_data_seed`;
DROP TEMPORARY TABLE IF EXISTS `tmp_system_dict_type_seed`;
