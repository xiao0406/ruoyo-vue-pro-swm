-- 监控设备页面由 JeeSite 权限码迁移到 RuoYi Controller 使用的权限码。
UPDATE `system_menu`
SET `permission` = 'swm:monitor-device:query', `update_time` = NOW()
WHERE `id` = 901331 AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT seed.id, seed.name, seed.permission, 3, seed.sort, 901331, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 90133101 AS id, '监控设备查询' AS name, 'swm:monitor-device:query' AS permission, 10 AS sort
  UNION ALL SELECT 90133102, '监控设备新增', 'swm:monitor-device:create', 20
  UNION ALL SELECT 90133103, '监控设备修改', 'swm:monitor-device:update', 30
  UNION ALL SELECT 90133104, '监控设备删除', 'swm:monitor-device:delete', 40
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` menu
  WHERE menu.`permission` COLLATE utf8mb4_unicode_ci = seed.permission COLLATE utf8mb4_unicode_ci
    AND menu.`deleted` = b'0'
);
