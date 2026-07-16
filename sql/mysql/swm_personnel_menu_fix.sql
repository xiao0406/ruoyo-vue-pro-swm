-- SWM personnel management menu fix.
-- Run this script on the current MySQL database when personnel pages exist in
-- yudao-ui-admin-vue3 but are not visible in the sidebar.

SET @now = NOW();

-- Root: SWM
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

-- Directory: personnel management
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900200, '人员管理', '', 1, 30, @swm_root_id, 'personnel', 'ep:user', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_root_id AND `path` = 'personnel' AND `deleted` = b'0'
);
SELECT @swm_personnel_id := `id`
FROM `system_menu`
WHERE `parent_id` = @swm_root_id AND `path` = 'personnel' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

-- Personnel pages
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900210, '人员登记', 'swm:person:query', 2, 10, @swm_personnel_id, 'person-list', 'ep:user-filled',
       'swm/personnel/PersonList', 'ViewsSwmPersonnelPersonList',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/personnel/PersonList' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900220, '人员看板', 'swm:personnel-board:query', 2, 20, @swm_personnel_id, 'personnel-board', 'ep:data-board',
       'swm/personnelBoard/PersonnelBoardList', 'ViewsSwmPersonnelBoardPersonnelBoardList',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/personnelBoard/PersonnelBoardList' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900230, '排班管理', 'swm:person-schedule:query', 2, 30, @swm_personnel_id, 'attendance-schedule', 'ep:calendar',
       'swm/Attendance/AttendanceList', 'ViewsSwmAttendanceAttendanceList',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/Attendance/AttendanceList' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900240, '日考勤记录', 'swm:daily-attendance:query', 2, 40, @swm_personnel_id, 'daily-attendance', 'ep:date',
       'swm/dailyAttendance/index', 'ViewsSwmDailyAttendanceIndex',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/dailyAttendance/index' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900250, '周考勤记录', 'swm:daily-attendance:query', 2, 50, @swm_personnel_id, 'weekly-attendance', 'ep:calendar',
       'swm/weekly/index', 'ViewsSwmWeeklyAttendanceIndex',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/weekly/index' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900260, '月考勤记录', 'swm:daily-attendance:query', 2, 60, @swm_personnel_id, 'monthly-attendance', 'ep:calendar',
       'swm/monthlyAttendance/index', 'ViewsSwmMonthlyAttendanceIndex',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/monthlyAttendance/index' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900270, '行动轨迹', 'swm:person-track:query', 2, 70, @swm_personnel_id, 'person-track', 'ep:location',
       'swm/persontrack/personTrack', 'ViewsSwmPersontrackPersonTrack',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @swm_personnel_id AND `component` = 'swm/persontrack/personTrack' AND `deleted` = b'0'
);

-- Normalize existing migrated menu records. Some copied pages were present but
-- had stale component_name values from other pages, which breaks keep-alive and
-- can make dynamic routes behave inconsistently.
UPDATE `system_menu`
SET `name` = '人员登记',
    `permission` = 'swm:person:query',
    `component_name` = 'ViewsSwmPersonnelPersonList',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/personnel/PersonList' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '人员看板',
    `permission` = 'swm:personnel-board:query',
    `component_name` = 'ViewsSwmPersonnelBoardPersonnelBoardList',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/personnelBoard/PersonnelBoardList' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '排班管理',
    `permission` = 'swm:person-schedule:query',
    `component_name` = 'ViewsSwmAttendanceAttendanceList',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/Attendance/AttendanceList' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '日考勤记录',
    `permission` = 'swm:daily-attendance:query',
    `component_name` = 'ViewsSwmDailyAttendanceIndex',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/dailyAttendance/index' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '周考勤记录',
    `permission` = 'swm:daily-attendance:query',
    `component_name` = 'ViewsSwmWeeklyAttendanceIndex',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/weekly/index' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '月考勤记录',
    `permission` = 'swm:daily-attendance:query',
    `component_name` = 'ViewsSwmMonthlyAttendanceIndex',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/monthlyAttendance/index' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '行动轨迹',
    `permission` = 'swm:person-track:query',
    `component_name` = 'ViewsSwmPersontrackPersonTrack',
    `status` = 0,
    `visible` = b'1',
    `update_time` = @now
WHERE `component` = 'swm/persontrack/personTrack' AND `deleted` = b'0';

-- Grant to built-in admin/tenant-admin roles when they exist. Super admin often bypasses
-- role-menu filtering, but this keeps ordinary tenant admin accounts from missing the menu.
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.`id`, m.`id`, '1', @now, '1', @now, b'0', r.`tenant_id`
FROM `system_role` r
JOIN `system_menu` m ON m.`deleted` = b'0'
  AND (
    m.`id` IN (@swm_root_id, @swm_personnel_id)
    OR m.`component` IN (
      'swm/personnel/PersonList',
      'swm/personnelBoard/PersonnelBoardList',
      'swm/Attendance/AttendanceList',
      'swm/dailyAttendance/index',
      'swm/weekly/index',
      'swm/monthlyAttendance/index',
      'swm/persontrack/personTrack'
    )
  )
WHERE r.`code` IN ('super_admin', 'tenant_admin')
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );
