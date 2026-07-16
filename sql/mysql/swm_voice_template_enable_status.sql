-- Separate voice-template enablement from JeeSite's status-based logical deletion.
SET @enable_status_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'swm_voice_template'
      AND column_name = 'enable_status'
);
SET @enable_status_ddl = IF(
    @enable_status_exists = 0,
    'ALTER TABLE `swm_voice_template` ADD COLUMN `enable_status` varchar(1) NOT NULL DEFAULT ''0'' COMMENT ''Business status: 0 enabled, 1 disabled'' AFTER `status`',
    'SELECT 1'
);
PREPARE enable_status_statement FROM @enable_status_ddl;
EXECUTE enable_status_statement;
DEALLOCATE PREPARE enable_status_statement;

UPDATE `swm_voice_template`
SET `enable_status` = '0'
WHERE `enable_status` IS NULL OR `enable_status` = '';
