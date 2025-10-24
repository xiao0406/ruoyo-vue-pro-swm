package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmAlarmConfigDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 报警配置Service
 *
 * @author zwf
 * @version 2025-06-18
 */
@Service
@Transactional(readOnly = true)
public class SwmAlarmConfigService extends CrudService<SwmAlarmConfigDao, SwmAlarmConfig> {

    private static final Logger logger = LoggerFactory.getLogger(SwmAlarmConfigService.class);

    /**
     * 获取单条数据
     */
    @Override
    public SwmAlarmConfig get(SwmAlarmConfig swmAlarmConfig) {
        return super.get(swmAlarmConfig);
    }

    /**
     * 根据报警唯一标识key获取配置
     * 
     * @param alarmKey 报警唯一标识key
     * @return 报警配置
     */
    public SwmAlarmConfig getByAlarmKey(String alarmKey) {
        return dao.getByAlarmKey(alarmKey);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmAlarmConfig> findPage(SwmAlarmConfig swmAlarmConfig) {
        return super.findPage(swmAlarmConfig);
    }

    /**
     * 查询所有启用的报警配置
     * 
     * @return 启用的报警配置列表
     */
    public List<SwmAlarmConfig> findAllEnabled() {
        return dao.findAllEnabled();
    }

    /**
     * 查询所有需要弹窗确认的报警配置
     * 
     * @return 需要弹窗确认的报警配置列表
     */
    public List<SwmAlarmConfig> findAllNeedConfirm() {
        return dao.findAllNeedConfirm();
    }

    /**
     * 保存数据（插入或更新）
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmAlarmConfig swmAlarmConfig) {
        // 检查是否有相同的alarm_key
        SwmAlarmConfig existing = dao.getByAlarmKey(swmAlarmConfig.getAlarmKey());
        if (existing != null && !existing.getId().equals(swmAlarmConfig.getId())) {
            throw new RuntimeException("报警唯一标识key已存在: " + swmAlarmConfig.getAlarmKey());
        }
        
        // 设置默认值
        if (swmAlarmConfig.getEnableAlarm() == null) {
            swmAlarmConfig.setEnableAlarm(1); // 默认启用
        }
        
        // 如果不启用报警，则不需要弹窗确认，并清空弹窗位置
        if (swmAlarmConfig.getEnableAlarm() == 0) {
            swmAlarmConfig.setNeedConfirm(0);
            swmAlarmConfig.setDialogPosition(null);
            logger.debug("报警未启用，自动设置不需要弹窗确认，并清空弹窗位置");
        } else {
            // 如果未设置是否需要弹窗确认，默认不需要
            if (swmAlarmConfig.getNeedConfirm() == null) {
                swmAlarmConfig.setNeedConfirm(0);
            }
            
            // 如果不需要弹窗确认，清空弹窗位置
            if (swmAlarmConfig.getNeedConfirm() == 0) {
                swmAlarmConfig.setDialogPosition(null);
                logger.debug("不需要弹窗确认，清空弹窗位置");
            } else if (swmAlarmConfig.getDialogPosition() == null || swmAlarmConfig.getDialogPosition().isEmpty()) {
                // 如果需要弹窗确认但未设置位置，使用默认位置
                swmAlarmConfig.setDialogPosition("center");
                logger.debug("需要弹窗确认但未设置位置，使用默认位置: center");
            }
        }
        
        // 如果是更新操作且已存在ID，先获取原记录以确保正确更新
        if (swmAlarmConfig.getId() != null && !swmAlarmConfig.getId().isEmpty()) {
            SwmAlarmConfig originalRecord = super.get(swmAlarmConfig.getId());
            if (originalRecord != null) {
                // 显式检查是否需要清空弹窗位置，确保原值会被覆盖
                if ((swmAlarmConfig.getEnableAlarm() == 0 || swmAlarmConfig.getNeedConfirm() == 0) 
                        && originalRecord.getDialogPosition() != null) {
                    logger.info("检测到需要清空已存在的弹窗位置: {}", originalRecord.getDialogPosition());
                    swmAlarmConfig.setDialogPosition(null);
                }
                
                // 使用自定义更新方法，确保dialog_position字段被更新
                swmAlarmConfig.preUpdate();
                dao.updateWithDialogPosition(swmAlarmConfig);
                logger.info("使用自定义更新方法更新报警配置: {}", swmAlarmConfig.getAlarmName());
                return;
            }
        }
        
        // 对于新增或其他情况，使用父类的保存方法
        super.save(swmAlarmConfig);
        logger.info("保存报警配置成功: {}", swmAlarmConfig.getAlarmName());
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmAlarmConfig swmAlarmConfig) {
        super.delete(swmAlarmConfig);
        logger.info("删除报警配置成功: {}", swmAlarmConfig.getId());
    }

    /**
     * 根据报警名称查询对应报警配置
     * @param alarmName 报警名称
     * @return
     */
    public SwmAlarmConfig getByAlarmName(String alarmName){
        List<SwmAlarmConfig> swmAlarmConfigList = dao.getByAlarmName(alarmName);
        if(CollectionUtils.isNotEmpty(swmAlarmConfigList)){
            return swmAlarmConfigList.get(0);
        }
        return null;
    }
} 