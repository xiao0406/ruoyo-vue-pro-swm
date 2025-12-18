package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHelmetConfig;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;

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

    /**
     * 获取车间-产线-班组数据
     * 
     * @author Shawn
     * @date 2025/06/24
     * @description 根据用户要求的SQL查询车间-产线-班组数据，
     *              将workshop_name、line_name、work_group_name拼接作为名称，
     *              将id保存到key字段中用于颜色配置
     * @return 车间-产线-班组数据列表
     */
    List<Map<String, Object>> getWorkshopLineGroupData();

    /**
     * 获取人员类型字典数据
     * 
     * @author Shawn
     * @date 2025/06/24
     * @description 获取字典类型为person_type_enum的字典数据，
     *              用于"按人员类型展示"的颜色配置
     * @return 人员类型字典数据列表
     */
    List<Map<String, Object>> getPersonTypeEnumData();

    /**
     * 获取工种数据
     * 
     * @author Shawn
     * @date 2025/06/24
     * @description 获取 swm_work_type 表中的工种数据，
     *              用于"按工种展示"的颜色配置
     * @return 工种数据列表
     */
    List<Map<String, Object>> getWorkTypeEnumData();

    SwmHelmetDevice getByDeviceId(String deviceId);
}