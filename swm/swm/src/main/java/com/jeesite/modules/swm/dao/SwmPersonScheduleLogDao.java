package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPersonScheduleLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 人员班次修改日志DAO
 */
@MyBatisDao
public interface SwmPersonScheduleLogDao extends CrudDao<SwmPersonScheduleLog> {
    @Insert("INSERT INTO swm_person_schedule_log " +
            "(id, operate_user, operate_time, target_classes, person_id, " +
            " remark, operate_desc, del_flag) " +
            "VALUES (#{id}, #{operateUser}, #{operateTime}, #{targetClasses}, #{personId}, " +
            "#{remark}, #{operateDesc}, '0')")
    void insertLog(SwmPersonScheduleLog log);

    /**
     * 根据人员ID查询班次修改记录，仅返回 operate_time 和 operate_desc 字段
     * @param id 人员ID（对应数据库 person_id 字段）
     * @return 班次修改记录列表（仅包含操作时间和格式化描述）
     */
    @Select("SELECT operate_time AS operateTime, operate_desc AS operateDesc " +
            "FROM swm_person_schedule_log " +
            "WHERE person_id = #{id} " +  // 匹配数据库中的人员ID字段（person_id）
            "AND del_flag = '0' " +       // 过滤逻辑删除的数据，符合Jeesite规范
            "ORDER BY operate_time DESC") // 按操作时间倒序，最新记录在前
    List<SwmPersonScheduleLog> getClassesRecord(String id);
}
