package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmMediaFile;

/**
 * 媒体文件管理DAO接口
 * @author zwf
 * @version 2023-07-01
 */
@MyBatisDao
public interface SwmMediaFileDao extends CrudDao<SwmMediaFile> {
    
} 