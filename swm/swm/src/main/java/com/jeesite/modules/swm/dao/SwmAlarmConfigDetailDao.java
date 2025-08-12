package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.entity.SwmAlarmConfigDetail;

import java.util.List;

/**
 * swm_alarm_config_detailDAO接口
 * @author gjl
 * @version 2025-08-11
 */
@MyBatisDao
public interface SwmAlarmConfigDetailDao extends CrudDao<SwmAlarmConfigDetail> {
    /**
     * 查询是否存在多条信息
     * @param swmAlarmConfigDetail
     * @return
     */
    SwmAlarmConfigDetail findByMainId(SwmAlarmConfigDetail swmAlarmConfigDetail);

    /**
     * 根据角色查询人员编码
     * @param roles
     * @return
     */
    List<String> findByRoleIds(List<String> roles);

    /**
     *
     * @param asList
     * @return
     */
    List<String> findByUserIds(List<String> users);

    SwmAlarmConfig getByAlarmKey(String mainKey);

    SwmAlarmConfigDetail findByMainKey(SwmAlarmConfigDetail swmAlarmConfigDetail);

}