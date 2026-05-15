ALTER TABLE swm_helmet_device
    ADD COLUMN device_source VARCHAR(20) COMMENT '设备来源（字典：swm_device_source，0-科利特，1-中泰）'
        AFTER hat_off_alarm_interval;



ALTER TABLE swm_schedule_time
    ADD COLUMN noon_end_time VARCHAR(8) COMMENT '中午下班卡'
        AFTER start_time;

ALTER TABLE swm_schedule_time
    ADD COLUMN after_start_time VARCHAR(8) COMMENT '下午上班卡'
        AFTER noon_end_time;


ALTER TABLE swm_daily_attendance
    ADD COLUMN noon_end_time time COMMENT '中午下班时间'
        AFTER clock_in_date;

ALTER TABLE swm_daily_attendance
    ADD COLUMN noon_end_date datetime COMMENT '中午下班完整时间'
        AFTER noon_end_time;

ALTER TABLE swm_daily_attendance
    ADD COLUMN after_start_time time COMMENT '下午上班时间'
        AFTER noon_end_date;

ALTER TABLE swm_daily_attendance
    ADD COLUMN after_start_date datetime COMMENT '下午上班完整时间'
        AFTER after_start_time;

# ========================= 2026/05/15 ==============================================
ALTER TABLE swm_area
    ADD COLUMN is_screen_show BOOLEAN NOT NULL DEFAULT false COMMENT '是否屏幕显示：0-否，1-是'
        AFTER b_ids;