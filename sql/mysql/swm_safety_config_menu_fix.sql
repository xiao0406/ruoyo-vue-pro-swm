-- SWM safety/config menu fix.
-- Run this script when migrated SWM safety/config pages exist in Vue3 but do
-- not appear in the sidebar dynamic menu.

SET @now = NOW();

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900100, 'SWM', '', 1, 900, 0, 'swm', 'ep:monitor', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = 'swm' AND `deleted` = b'0'
);

SELECT @swm_root_id := `id`
FROM `system_menu`
WHERE `parent_id` = 0 AND `path` = 'swm' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900300, '安全管理', '', 1, 20, @swm_root_id, 'safety', 'ep:warning', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_root_id AND `path` = 'safety' AND `deleted` = b'0'
);

SELECT @swm_safety_id := `id`
FROM `system_menu`
WHERE `parent_id` = @swm_root_id AND `path` = 'safety' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900310, '安全教育', 'swm:safety-education:query', 2, 10, @swm_safety_id,
       'safety-education', 'ep:reading',
       'swm/safetyEducation/list', 'ViewsSwmSafetyEducationList',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `component` = 'swm/safetyEducation/list' AND `deleted` = b'0'
);

UPDATE `system_menu`
SET `name` = '安全教育',
    `permission` = 'swm:safety-education:query',
    `component_name` = 'ViewsSwmSafetyEducationList',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/safetyEducation/list' AND `deleted` = b'0';

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.`id`, m.`id`, '1', @now, '1', @now, b'0', r.`tenant_id`
FROM `system_role` r
JOIN `system_menu` m ON m.`deleted` = b'0'
  AND (
    m.`id` IN (@swm_root_id, @swm_safety_id)
    OR m.`component` = 'swm/safetyEducation/list'
  )
WHERE r.`code` IN ('super_admin', 'tenant_admin')
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );
