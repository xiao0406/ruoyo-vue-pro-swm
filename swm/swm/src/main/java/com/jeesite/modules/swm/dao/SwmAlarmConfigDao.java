package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;

import java.util.List;

/**
 * 预警报警配置DAO接口
 *
 * @author zwf
 * @version 2025-06-18
 */
@MyBatisDao
public interface SwmAlarmConfigDao extends CrudDao<SwmAlarmConfig> {
    
    /**
     * 根据报警唯一标识key获取报警配置
     * 
     * @param alarmKey 报警唯一标识key
     * @return 报警配置对象
     */
    SwmAlarmConfig getByAlarmKey(String alarmKey);
    
    /**
     * 查询所有启用的报警配置
     * 
     * @return 报警配置列表
     */
    List<SwmAlarmConfig> findAllEnabled();
    
    /**
     * 查询所有需要弹窗确认的报警配置
     * 
     * @return 需要弹窗确认的报警配置列表
     */
    List<SwmAlarmConfig> findAllNeedConfirm();
    
    /**
     * 自定义更新方法，确保包含dialog_position字段
     * 
     * @param swmAlarmConfig 报警配置
     * @return 影响行数
     */
    int updateWithDialogPosition(SwmAlarmConfig swmAlarmConfig);
} 