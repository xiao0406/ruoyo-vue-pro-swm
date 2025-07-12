package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmMonitorDeviceInfo;
import com.jeesite.modules.sys.entity.Role;

import java.util.List;

/**
 * 监控设备信息DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmMonitorDeviceInfoDao extends CrudDao<SwmMonitorDeviceInfo> {

    /**
     * 查询机构下的设备列表
     * 
     * @param swmMonitorDeviceInfo 查询条件
     * @return 设备列表
     */
    List<SwmMonitorDeviceInfo> officeDeviceList(SwmMonitorDeviceInfo swmMonitorDeviceInfo);

    List<Role> findAllRoleList(Role where);
}
