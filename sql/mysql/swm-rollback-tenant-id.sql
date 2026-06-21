-- ============================================================
-- 回滚脚本：删除已添加的 tenant_id 列
-- 在重新执行 swm-tenant-migration.sql 之前运行此脚本
-- ============================================================

SET @tbl = 'fms_position_archive';  SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'fms_prod_line';         SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'fms_work_group';        SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'fms_worker';            SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_alarm_config';      SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_alarm_config_detail'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_alarm_light';       SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_alarm_light_config'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_area';              SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_area_access_list';  SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_attendance_log';    SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_attendance_segment'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_attendance_summary'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_beacon_color_config'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_beacon_station';    SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_beacon_station_temp'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_daily_attendance';  SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_danger_disposal';   SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_dict_data';         SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_dict_type';         SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_dify';              SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_handle_record';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_hazard_source';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_helmet_config';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_helmet_device';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_helmet_device_config'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_helmet_subitem';    SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_hidden_danger';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_inspection_list';   SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_inspection_plan';   SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_job_log';           SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_media_file';        SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_oneclick_recall';   SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_person';            SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_person_departure';  SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_person_schedule';   SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_person_schedule_log'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_person_work_area';  SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_personnel_board';   SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_safety_education';  SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_safety_file_manage'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_safety_helmet_order'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_safety_person_training'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_schedule_time';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_site_map_management'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_third_api_log';     SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_voice_template';    SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_warning_management'; SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'swm_work_type';         SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'iot_device';            SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @tbl = 'iot_file_upload';       SET @sql = CONCAT('ALTER TABLE `', @tbl, '` DROP COLUMN tenant_id');  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
