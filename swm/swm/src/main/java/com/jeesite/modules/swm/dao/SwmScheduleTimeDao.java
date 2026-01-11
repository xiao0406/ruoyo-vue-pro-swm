package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmScheduleTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 排班时间管理表DAO接口
 * 
 * @author zwf
 * @version 2025-05-15
 */
@MyBatisDao
public interface SwmScheduleTimeDao extends CrudDao<SwmScheduleTime> {

    @Select("SELECT a.`id` AS id, " +
            "a.`shift_type` AS shiftType, " +
            "a.`start_time` AS startTime, " +
            "a.`end_time` AS endTime, " +
            "a.`rest_time` AS restTime, " +
            "a.`rest_days` AS restDays, " +
            "a.`status` AS status, " +
            "a.`create_by` AS createBy, " +
            "a.`create_date` AS createDate, " +
            "a.`update_by` AS updateBy, " +
            "a.`update_date` AS updateDate, " +
            "a.`remarks` AS remarks, " +
            "a.`corp_code` AS corpCode, " +
            "a.`corp_name` AS corpName " +
            "FROM `swm_schedule_time` a " +
            "WHERE 1=1 " +
            "AND a.`shift_type` LIKE CONCAT('%', #{scheduleTimeQuery.shiftType}, '%') " +
            "AND a.`status` = 0 " +
            "AND (1=1 OR a.corp_code IS NOT NULL) " +
            "ORDER BY a.update_date DESC")
    List<SwmScheduleTime> findListSingle(@Param("scheduleTimeQuery") SwmScheduleTime scheduleTimeQuery);
} 