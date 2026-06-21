-- ============================================================
-- SWM/IoT 租户字段迁移脚本 v2（collation 修复版）
-- 将 corp_code → tenant_id（RuoYi-Vue-Pro 租户体系）
-- 执行前请先备份数据库！
-- ============================================================

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

-- 1. 租户映射临时表
DROP TEMPORARY TABLE IF EXISTS _tmp_tenant_mapping;
CREATE TEMPORARY TABLE _tmp_tenant_mapping (
    corp_code VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci PRIMARY KEY,
    tenant_id BIGINT NOT NULL
) ENGINE=InnoDB;

INSERT INTO _tmp_tenant_mapping (corp_code, tenant_id) VALUES
('ZJGGGD', 1), ('ZJGGJS', 2), ('ZJGGSC', 3),
('ZJZK',   4), ('ZHY',    5), ('DFXCYY', 6);

-- ============================================================
-- 2. 逐表：先 DROP（防重复），再 ADD，再 UPDATE
-- ============================================================

-- ---- 通用存储过程：简化重复代码 ----
DROP PROCEDURE IF EXISTS _migrate_tenant;
DELIMITER $$
CREATE PROCEDURE _migrate_tenant(IN tbl_name VARCHAR(128))
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    DECLARE remarks_exists INT DEFAULT 0;
    -- 检查 tenant_id 列是否已存在
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = tbl_name
      AND column_name = 'tenant_id';
    -- 如果已存在则先删除
    IF col_exists > 0 THEN
        SET @s = CONCAT('ALTER TABLE `', tbl_name, '` DROP COLUMN tenant_id');
        PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
    -- 检查 remarks 列是否存在，决定是否用 AFTER remarks
    SELECT COUNT(*) INTO remarks_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = tbl_name
      AND column_name = 'remarks';
    IF remarks_exists > 0 THEN
        SET @s = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN tenant_id BIGINT DEFAULT NULL COMMENT ''租户编号'' AFTER remarks');
    ELSE
        SET @s = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN tenant_id BIGINT DEFAULT NULL COMMENT ''租户编号''');
    END IF;
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    -- 从 corp_code 映射更新 tenant_id（COLLATE 解决 utf8mb4_0900_ai_ci vs utf8mb4_general_ci 冲突）
    SET @s = CONCAT(
        'UPDATE `', tbl_name, '` t ',
        'INNER JOIN _tmp_tenant_mapping m ON t.corp_code COLLATE utf8mb4_general_ci = m.corp_code ',
        'SET t.tenant_id = m.tenant_id'
    );
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

-- ---- 执行迁移（49 张有 corp_code 的表）----

CALL _migrate_tenant('fms_position_archive');
CALL _migrate_tenant('fms_prod_line');
CALL _migrate_tenant('fms_work_group');
CALL _migrate_tenant('fms_worker');

CALL _migrate_tenant('swm_alarm_config');
CALL _migrate_tenant('swm_alarm_config_detail');
CALL _migrate_tenant('swm_alarm_light');
CALL _migrate_tenant('swm_alarm_light_config');
CALL _migrate_tenant('swm_area');
CALL _migrate_tenant('swm_area_access_list');
CALL _migrate_tenant('swm_attendance_log');
CALL _migrate_tenant('swm_attendance_segment');
CALL _migrate_tenant('swm_attendance_summary');
CALL _migrate_tenant('swm_beacon_color_config');
CALL _migrate_tenant('swm_beacon_station');
CALL _migrate_tenant('swm_beacon_station_temp');
CALL _migrate_tenant('swm_daily_attendance');
CALL _migrate_tenant('swm_danger_disposal');
CALL _migrate_tenant('swm_dict_data');
CALL _migrate_tenant('swm_dict_type');
CALL _migrate_tenant('swm_dify');
CALL _migrate_tenant('swm_handle_record');
CALL _migrate_tenant('swm_hazard_source');
CALL _migrate_tenant('swm_helmet_config');
CALL _migrate_tenant('swm_helmet_device');
CALL _migrate_tenant('swm_helmet_device_config');
CALL _migrate_tenant('swm_helmet_subitem');
CALL _migrate_tenant('swm_hidden_danger');
CALL _migrate_tenant('swm_inspection_list');
CALL _migrate_tenant('swm_inspection_plan');
CALL _migrate_tenant('swm_job_log');
CALL _migrate_tenant('swm_media_file');
CALL _migrate_tenant('swm_oneclick_recall');
CALL _migrate_tenant('swm_person');
CALL _migrate_tenant('swm_person_departure');
CALL _migrate_tenant('swm_person_schedule');
CALL _migrate_tenant('swm_person_schedule_log');
CALL _migrate_tenant('swm_person_work_area');
CALL _migrate_tenant('swm_personnel_board');
CALL _migrate_tenant('swm_safety_education');
CALL _migrate_tenant('swm_safety_file_manage');
CALL _migrate_tenant('swm_safety_helmet_order');
CALL _migrate_tenant('swm_safety_person_training');
CALL _migrate_tenant('swm_schedule_time');
CALL _migrate_tenant('swm_site_map_management');
CALL _migrate_tenant('swm_third_api_log');
CALL _migrate_tenant('swm_voice_template');
CALL _migrate_tenant('swm_warning_management');
CALL _migrate_tenant('swm_work_type');

-- ---- 2 张无 corp_code 的 iot 表（仅 ADD）----

DROP PROCEDURE IF EXISTS _add_tenant_only;
DELIMITER $$
CREATE PROCEDURE _add_tenant_only(IN tbl_name VARCHAR(128))
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    DECLARE remarks_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = tbl_name
      AND column_name = 'tenant_id';
    IF col_exists > 0 THEN
        SET @s = CONCAT('ALTER TABLE `', tbl_name, '` DROP COLUMN tenant_id');
        PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
    SELECT COUNT(*) INTO remarks_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = tbl_name
      AND column_name = 'remarks';
    IF remarks_exists > 0 THEN
        SET @s = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN tenant_id BIGINT DEFAULT NULL COMMENT ''租户编号'' AFTER remarks');
    ELSE
        SET @s = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN tenant_id BIGINT DEFAULT NULL COMMENT ''租户编号''');
    END IF;
    PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;

CALL _add_tenant_only('iot_device');
CALL _add_tenant_only('iot_file_upload');

-- ---- 清理 ----
DROP PROCEDURE IF EXISTS _migrate_tenant;
DROP PROCEDURE IF EXISTS _add_tenant_only;
DROP TEMPORARY TABLE IF EXISTS _tmp_tenant_mapping;

-- ============================================================
-- 3. sys_tenant 租户记录（请确认 sys_tenant 表存在后取消注释执行）
-- ============================================================
-- INSERT INTO sys_tenant (id, name, contact_name, status, package_id, expire_time, account_count, creator, create_time, updater, update_time, deleted)
-- VALUES
-- (1, '中建钢构广东有限公司', '管理员', 0, NULL, NULL, -1, '1', NOW(), '1', NOW(), 0),
-- (2, '中建钢构江苏有限公司', '管理员', 0, NULL, NULL, -1, '1', NOW(), '1', NOW(), 0),
-- (3, '中建钢构四川有限公司', '管理员', 0, NULL, NULL, -1, '1', NOW(), '1', NOW(), 0),
-- (4, '中建科工', '管理员', 0, NULL, NULL, -1, '1', NOW(), '1', NOW(), 0),
-- (5, '天津中海油', '管理员', 0, NULL, NULL, -1, '1', NOW(), '1', NOW(), 0),
-- (6, '东方新材', '管理员', 0, NULL, NULL, -1, '1', NOW(), '1', NOW(), 0);
