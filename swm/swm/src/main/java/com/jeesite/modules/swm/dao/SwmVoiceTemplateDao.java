/**
 * 语音模板表DAO接口
 * @author zwf
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;

/**
 * 语音模板表DAO接口
 * 
 * @author zwf
 */
@MyBatisDao
public interface SwmVoiceTemplateDao extends CrudDao<SwmVoiceTemplate> {
    
} 