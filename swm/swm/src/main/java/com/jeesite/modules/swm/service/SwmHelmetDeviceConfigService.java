package com.jeesite.modules.swm.service;

import com.jeesite.common.service.CrudService;
import com.jeesite.common.idgen.IdGen;
import com.jeesite.modules.swm.dao.SwmHelmetDeviceConfigDao;
import com.jeesite.modules.swm.entity.SwmHelmetDeviceConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 头盔设备配置Service
 * @author Shawn
 * @date 2025-01-08
 */
@Service
@Transactional(readOnly = true)
public class SwmHelmetDeviceConfigService extends CrudService<SwmHelmetDeviceConfigDao, SwmHelmetDeviceConfig> {
    
    /**
     * 保存或更新配置
     * @param config 配置对象
     */
    @Transactional(readOnly = false)
    public void saveOrUpdateConfig(SwmHelmetDeviceConfig config) {
        // 生成ID（如果没有）
        if (StringUtils.isBlank(config.getId())) {
            config.setId(IdGen.nextId());
        }
        
        // 设置创建人和更新人信息
        config.preInsert();
        
        // 执行插入或更新
        dao.saveOrUpdateConfig(config);
    }
}