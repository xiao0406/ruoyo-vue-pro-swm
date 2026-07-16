-- SWM full menu fix for migrated Vue3 pages.
-- Run this after frontend files are migrated but pages are missing from the
-- sidebar. It creates/normalizes dynamic menu records and grants them to
-- built-in super_admin / tenant_admin roles.
--
-- Not inserted because the Vue3 target files are still missing:
--   /swm-screen-new              -> migrated to data-screen/swm-new/index
--   /emergencyInquiry            -> no Vue3 file found
--   /swm/helmetLocation/index    -> no Vue3 file found

SET @now = NOW();

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900100, 'SWM', '', 1, 900, 0, '/swm', 'ep:monitor', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/swm' AND `deleted` = b'0'
);

SELECT @swm_root_id := `id`
FROM `system_menu`
WHERE `parent_id` = 0 AND `path` = '/swm' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900300, '安全管理', '', 1, 20, @swm_root_id, 'safety', 'ep:warning', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = @swm_root_id AND `path` = 'safety' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900200, '人员管理', '', 1, 30, @swm_root_id, 'personnel', 'ep:user', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = @swm_root_id AND `path` = 'personnel' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 900400, '配置管理', '', 1, 40, @swm_root_id, 'config', 'ep:setting', '', '',
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = @swm_root_id AND `path` = 'config' AND `deleted` = b'0'
);

SELECT @swm_safety_id := `id`
FROM `system_menu`
WHERE `parent_id` = @swm_root_id AND `path` = 'safety' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

SELECT @swm_personnel_id := `id`
FROM `system_menu`
WHERE `parent_id` = @swm_root_id AND `path` = 'personnel' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

SELECT @swm_config_id := `id`
FROM `system_menu`
WHERE `parent_id` = @swm_root_id AND `path` = 'config' AND `deleted` = b'0'
ORDER BY `id`
LIMIT 1;

DROP TEMPORARY TABLE IF EXISTS `tmp_swm_menu_seed`;
CREATE TEMPORARY TABLE `tmp_swm_menu_seed` (
  `id` BIGINT NOT NULL,
  `group_key` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `permission` VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` INT NOT NULL,
  `path` VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `component` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `component_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_swm_menu_seed`
(`id`, `group_key`, `name`, `permission`, `sort`, `path`, `icon`, `component`, `component_name`)
VALUES
-- Cockpit
(901000, 'root', '驾驶舱', 'swm:dashboard:query', 10, 'screen-new', 'ep:data-analysis', 'data-screen/swm-new/index', 'ViewsDataScreenSwmNewIndex'),

-- Safety Management
(901100, 'safety', '异常状态报警记录', 'swm:warning:query', 10, 'warning-list', 'ep:warning', 'swm/warning/WarningList', 'ViewsSwmWarningWarningList'),
(901110, 'safety', '报警处置记录', 'swm:handle-record:query', 20, 'warning-record', 'ep:tickets', 'swm/warningRecord/WarningRecordList', 'ViewsSwmWarningRecordWarningRecordList'),
(901120, 'safety', '危险区域闯入提示', 'swm:hazard-source:query', 30, 'hazard-source', 'ep:place', 'swm/hazardSource/HazardSourceList', 'ViewsSwmHazardSourceHazardSourceList'),
(901130, 'safety', '应急召回', 'swm:one-click-recall:query', 40, 'one-key-recall', 'ep:bell', 'swm/oneKeyRecall/oneKeyRecall', 'ViewsSwmOneKeyRecallOneKeyRecall'),
(901140, 'safety', '语音模板管理', 'swm:voice-template:query', 50, 'voice-template', 'ep:microphone', 'swm/voiceTemplate/index', 'ViewsSwmVoiceTemplateIndex'),
(901150, 'safety', '应急呼叫', 'swm:sos:query', 60, 'sos', 'ep:phone', 'swm/sos/SosList', 'ViewsSwmSosSosList'),
(901160, 'safety', '安全教育', 'swm:safety-education:query', 70, 'safety-education', 'ep:reading', 'swm/safetyEducation/list', 'ViewsSwmSafetyEducationList'),
(901161, 'safety', '隐患列表', 'swm:hidden-danger:query', 80, 'hidden-danger', 'ep:list', 'swm/hiddenDanger/HiddenDangerList', 'ViewsSwmHiddenDangerHiddenDangerList'),
(901162, 'safety', '隐患处置列表', 'swm:danger-disposal:query', 90, 'danger-disposal', 'ep:finished', 'swm/dangerDisposal/DangerDisposalList', 'ViewsSwmDangerDisposalDangerDisposalList'),
(901163, 'safety', '巡检计划', 'swm:inspection-plan:query', 100, 'inspection-plan', 'ep:calendar', 'swm/inspectionPlan/index', 'ViewsSwmInspectionPlanIndex'),
(901164, 'safety', '巡检任务', 'swm:inspection-list:query', 110, 'inspection-list', 'ep:tickets', 'swm/inspectionList/index', 'ViewsSwmInspectionListIndex'),
(901165, 'safety', '安全教育视频', 'swm:media-file:query', 120, 'safety-videos', 'ep:video-play', 'swm/safetyVideos/list', 'ViewsSwmSafetyVideosList'),
(901166, 'safety', '视频培训记录', 'swm:safety-person-training:query', 130, 'safety-person-training', 'ep:histogram', 'swm/swmSafetyPersonTraining/list', 'ViewsSwmSwmSafetyPersonTrainingList'),

-- AI report is a direct child of SWM in production.
(901010, 'root', 'AI日报', 'swm:dify:query', 20, 'ai-report', 'ep:document', 'swm/swmDify/list', 'ViewsSwmSwmDifyList'),

-- Personnel Management
(901200, 'personnel', '人员登记', 'swm:person:query', 10, 'person-list', 'ep:user-filled', 'swm/personnel/PersonList', 'ViewsSwmPersonnelPersonList'),
(901210, 'personnel', '人员看板', 'swm:personnel-board:query', 20, 'personnel-board', 'ep:data-board', 'swm/personnelBoard/PersonnelBoardList', 'ViewsSwmPersonnelBoardPersonnelBoardList'),
(901220, 'personnel', '排班管理', 'swm:person-schedule:query', 30, 'attendance-schedule', 'ep:calendar', 'swm/Attendance/AttendanceList', 'ViewsSwmAttendanceAttendanceList'),
(901230, 'personnel', '日考勤记录', 'swm:daily-attendance:query', 40, 'daily-attendance', 'ep:date', 'swm/dailyAttendance/index', 'ViewsSwmDailyAttendanceIndex'),
(901240, 'personnel', '周考勤记录', 'swm:daily-attendance:query', 50, 'weekly-attendance', 'ep:calendar', 'swm/weekly/index', 'ViewsSwmWeeklyAttendanceIndex'),
(901250, 'personnel', '月考勤记录', 'swm:daily-attendance:query', 60, 'monthly-attendance', 'ep:calendar', 'swm/monthlyAttendance/index', 'ViewsSwmMonthlyAttendanceIndex'),
(901260, 'personnel', '行动轨迹', 'swm:person-track:query', 70, 'person-track', 'ep:location', 'swm/persontrack/personTrack', 'ViewsSwmPersontrackPersonTrack'),

-- Configuration Management
(901300, 'config', '颜色配置', 'swm:beacon-color-config:query', 10, 'color-config', 'ep:brush', 'swm/colorConfig/index', 'ViewsSwmColorConfigIndex'),
(901310, 'config', '站点地图管理', 'swm:site-map:query', 20, 'site-map', 'ep:map-location', 'swm/siteMapManagement/index', 'ViewsSwmSiteMapManagementIndex'),
(901320, 'config', '报警配置', 'swm:alarm-config:query', 30, 'alarm-config', 'ep:setting', 'swm/alarmConfig/AlarmConfigList', 'ViewsSwmAlarmConfigAlarmConfigList'),
(901330, 'config', '视频管理', 'swm:video-management:query', 40, 'video-management', 'ep:video-camera', 'swm/videoManagement/index', 'ViewsSwmVideoManagementIndex'),
(901331, 'config', '监控设备管理', 'swm:monitor-device:query', 50, 'monitoring-equipment', 'ep:video-camera', 'swm/monitoringEquipment/monitoringEquipmentManagement', 'ViewsSwmMonitoringEquipmentMonitoringEquipmentManagement'),
(901340, 'config', '原始消息日志', 'swm:raw-message-log:query', 60, 'raw-message-log', 'ep:document', 'swm/rawMessageLog/index', 'ViewsSwmRawMessageLogIndex'),
(901350, 'config', '外部坐标数据', 'swm:external-coordinate-data:query', 70, 'external-coordinate-data', 'ep:position', 'swm/externalCoordinateData/index', 'ViewsSwmExternalCoordinateDataIndex'),
(901360, 'config', '区域围栏数据', 'swm:area-fence-data:query', 80, 'area-fence-data', 'ep:guide', 'swm/areaFenceData/index', 'ViewsSwmAreaFenceDataIndex'),
(901370, 'config', 'TCP设备指令日志', 'swm:tcp-device-command-log:query', 90, 'tcp-device-command-log', 'ep:connection', 'swm/tcpDeviceCommandLog/index', 'ViewsSwmTcpDeviceCommandLogIndex'),
(901380, 'config', '车间管理', 'swm:workshop:query', 110, 'swm-workshop', 'ep:office-building', 'swm/swmWorkshop/list', 'ViewsSwmSwmWorkshopList'),
(901390, 'config', '产线管理', 'swm:prod-line:query', 120, 'swm-prod-line', 'ep:operation', 'swm/swmProdLine/list', 'ViewsSwmSwmProdLineList'),
(901400, 'config', '班组管理', 'swm:work-group:query', 130, 'swm-work-group', 'ep:coordinate', 'swm/swmWorkGroup/list', 'ViewsSwmSwmWorkGroupList'),
(901410, 'config', '安全帽设备', 'swm:helmet-device:query', 140, 'helmet-device', 'ep:cpu', 'swm/helmetdevice/list', 'ViewsSwmHelmetdeviceList'),
(901420, 'config', '信标管理', 'swm:beacon-station:query', 150, 'beacon', 'ep:location-information', 'swm/beacon/BeaconList', 'ViewsSwmBeaconBeaconList'),
(901430, 'config', '信标地图', 'swm:beacon-map:query', 160, 'beacon-map', 'ep:map-location', 'swm/beaconMap/beaconBase', 'ViewsSwmBeaconMapBeaconBase'),
(901440, 'config', '报警灯', 'swm:alarm-light:query', 170, 'alarm-light', 'ep:alarm-clock', 'swm/alarmLight/index', 'ViewsSwmAlarmLightIndex'),
(901450, 'config', '字典类型', 'swm:dict-type:query', 180, 'dict-type', 'ep:collection', 'swm/dictType/list', 'ViewsSwmDictTypeList'),
(901460, 'config', '字典数据', 'swm:dict-data:query', 190, 'dict-data', 'ep:list', 'swm/dictData/list', 'ViewsSwmDictDataList');

-- Normalize already-copied old SWM menu records by path first. During migration
-- many databases already contain rows such as `/swm/personnel/PersonList` with
-- stale JeeSite component values; if we only insert by component, the sidebar can
-- still show the old broken route. Path + expected parent is the stable identity.
UPDATE `system_menu` m
JOIN `tmp_swm_menu_seed` s ON m.`path` COLLATE utf8mb4_unicode_ci = s.`path` COLLATE utf8mb4_unicode_ci
  AND m.`parent_id` = CASE s.`group_key`
    WHEN 'safety' THEN @swm_safety_id
    WHEN 'personnel' THEN @swm_personnel_id
    WHEN 'config' THEN @swm_config_id
    ELSE @swm_root_id
  END
SET m.`name` = s.`name`,
    m.`permission` = s.`permission`,
    m.`type` = 2,
    m.`sort` = s.`sort`,
    m.`icon` = s.`icon`,
    m.`component` = s.`component`,
    m.`component_name` = s.`component_name`,
    m.`status` = 0,
    m.`visible` = b'1',
    m.`keep_alive` = b'1',
    m.`update_time` = @now
WHERE m.`deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT s.`id`, s.`name`, s.`permission`, 2, s.`sort`,
       CASE s.`group_key`
         WHEN 'safety' THEN @swm_safety_id
         WHEN 'personnel' THEN @swm_personnel_id
         WHEN 'config' THEN @swm_config_id
         ELSE @swm_root_id
       END,
       s.`path`, s.`icon`, s.`component`, s.`component_name`,
       0, b'1', b'1', b'1', '1', @now, '1', @now, b'0'
FROM `tmp_swm_menu_seed` s
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` m
  WHERE m.`component` COLLATE utf8mb4_unicode_ci = s.`component` COLLATE utf8mb4_unicode_ci
    AND m.`deleted` = b'0'
);

UPDATE `system_menu` m
JOIN `tmp_swm_menu_seed` s ON s.`component` COLLATE utf8mb4_unicode_ci = m.`component` COLLATE utf8mb4_unicode_ci
SET m.`name` = s.`name`,
    m.`permission` = s.`permission`,
    m.`sort` = s.`sort`,
    m.`parent_id` = CASE s.`group_key`
      WHEN 'safety' THEN @swm_safety_id
      WHEN 'personnel' THEN @swm_personnel_id
      WHEN 'config' THEN @swm_config_id
      ELSE @swm_root_id
    END,
    m.`path` = s.`path`,
    m.`icon` = s.`icon`,
    m.`component_name` = s.`component_name`,
    m.`status` = 0,
    m.`visible` = b'1',
    m.`keep_alive` = b'1',
    m.`update_time` = @now
WHERE m.`deleted` = b'0';

-- Hide duplicated rows left by repeated migration attempts. Keep the earliest
-- active menu id for each migrated component so the dynamic router receives one
-- canonical route per page.
UPDATE `system_menu` duplicate_menu
JOIN `system_menu` canonical_menu
  ON canonical_menu.`component` = duplicate_menu.`component`
  AND canonical_menu.`deleted` = b'0'
  AND canonical_menu.`id` < duplicate_menu.`id`
JOIN `tmp_swm_menu_seed` s ON s.`component` COLLATE utf8mb4_unicode_ci = duplicate_menu.`component` COLLATE utf8mb4_unicode_ci
SET duplicate_menu.`deleted` = b'1',
    duplicate_menu.`update_time` = @now
WHERE duplicate_menu.`deleted` = b'0';

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.`id`, m.`id`, '1', @now, '1', @now, b'0', r.`tenant_id`
FROM `system_role` r
JOIN `system_menu` m ON m.`deleted` = b'0'
  AND (
    m.`id` IN (@swm_root_id, @swm_safety_id, @swm_personnel_id, @swm_config_id)
    OR EXISTS (
      SELECT 1 FROM `tmp_swm_menu_seed` s
      WHERE s.`component` COLLATE utf8mb4_unicode_ci = m.`component` COLLATE utf8mb4_unicode_ci
    )
  )
WHERE r.`code` IN ('super_admin', 'tenant_admin')
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_swm_menu_seed`;
