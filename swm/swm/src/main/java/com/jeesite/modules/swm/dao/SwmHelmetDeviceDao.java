/**
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jeesite.common.entity.Page;

/**
 * 头盔设备管理DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmHelmetDeviceDao extends CrudDao<SwmHelmetDevice> {

    /**
     * 查询可用的安全帽列表（未绑定人员的）
     */
    List<SwmHelmetDevice> findAvailableHelmets(@Param("keyword") String keyword);

    /**
     * 根据头盔编号查询设备
     * 注意：此方法可由实体类注解查询实现，但为保持兼容性暂时保留
     */
    SwmHelmetDevice getByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据绑定人员ID查询设备
     */
    List<SwmHelmetDevice> findByAssignedPerson(@Param("assignedPerson") String assignedPerson);

    /**
     * 根据所属车间查询设备
     */
    List<SwmHelmetDevice> findByWorkshop(@Param("assignedWorkshop") String assignedWorkshop);

    /**
     * 根据所属工序查询设备
     */
    List<SwmHelmetDevice> findByProcess(@Param("assignedProcess") String assignedProcess);

    /**
     * 根据所属班组查询设备
     */
    List<SwmHelmetDevice> findByTeam(@Param("assignedTeam") String assignedTeam);

    /**
     * 强制清空设备绑定信息（将assigned_person等字段设置为null）
     * 
     * @author Shawn
     * @date 2025-05-31
     */
    int clearDeviceAssignment(@Param("deviceId") String deviceId);

    /**
     * 带有人员关联信息的列表查询，关联fms_worker、fms_work_group和fms_position_archive表
     * 用于替代实体类默认查询
     * 
     * @author Shawn
     * @date 2025-06-01
     */
    List<SwmHelmetDevice> findHelmetDeviceListWithRelations(SwmHelmetDevice swmHelmetDevice);

    /**
     * 带有人员关联信息的计数查询，用于替代框架默认的计数查询
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    Long findHelmetDeviceCountWithRelations(SwmHelmetDevice swmHelmetDevice);

    /**
     * 根据设备ID列表查询设备详情（带人员关联信息）
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    List<SwmHelmetDevice> findHelmetDeviceListByDeviceIds(SwmHelmetDevice device);

    List<SwmHelmetDevice> findDeviceCorpMapping(SwmHelmetDevice device);

    List<SwmHelmetDevice> findListInit(SwmHelmetDevice queryCondition);

    List<SwmHelmetDevice> findHelmetDeviceListByIds(List<String> deviceIds);

    List<String> findDeviceIdsByDeviceSource(SwmHelmetDevice device);
}