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

    @Select("SELECT * " +
            "FROM `swm_schedule_time` a " +
            "WHERE 1=1 " +
            "AND a.`shift_type` = #{scheduleTimeQuery.shiftType} " +
            "AND a.`status` = 0 " +
            "ORDER BY a.update_date DESC")
    List<SwmScheduleTime> findListSingle(@Param("scheduleTimeQuery") SwmScheduleTime scheduleTimeQuery);
} 