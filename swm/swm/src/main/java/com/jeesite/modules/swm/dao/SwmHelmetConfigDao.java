package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHelmetConfig;
import java.util.List;
import java.util.Map;

/**
 * 安全帽主配置表DAO接口
 * 
 * @author zwf
 * @version 2025-05-20
 * @update 2025/06/24 by Shawn - 添加获取车间数据方法
 */
@MyBatisDao
public interface SwmHelmetConfigDao extends CrudDao<SwmHelmetConfig> {

    /**
     * 获取车间数据
     * 
     * @author Shawn
     * @date 2025/06/24
     * @description 查询fms_position_archive表中type='CJ'的车间数据
     * @return 车间数据列表
     */
    List<Map<String, Object>> getWorkshopData();

}