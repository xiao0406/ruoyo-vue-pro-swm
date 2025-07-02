package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmMonitorDeviceInfoDao;
import com.jeesite.modules.swm.entity.SwmMonitorDeviceInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 监控设备信息Service
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmMonitorDeviceInfoService extends CrudService<SwmMonitorDeviceInfoDao, SwmMonitorDeviceInfo> {

    /**
     * 获取单条数据
     * 
     * @param swmMonitorDeviceInfo
     * @return
     */
    @Override
    public SwmMonitorDeviceInfo get(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        return super.get(swmMonitorDeviceInfo);
    }

    /**
     * 查询分页数据
     * 
     * @param swmMonitorDeviceInfo 查询条件
     * @return
     */
    @Override
    public Page<SwmMonitorDeviceInfo> findPage(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        return super.findPage(swmMonitorDeviceInfo);
    }

    /**
     * 查询列表数据
     * 
     * @param swmMonitorDeviceInfo
     * @return
     */
    @Override
    public List<SwmMonitorDeviceInfo> findList(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        return super.findList(swmMonitorDeviceInfo);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmMonitorDeviceInfo
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        super.save(swmMonitorDeviceInfo);
    }

    /**
     * 更新状态
     * 
     * @param swmMonitorDeviceInfo
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        super.updateStatus(swmMonitorDeviceInfo);
    }

    /**
     * 删除数据
     * 
     * @param swmMonitorDeviceInfo
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        super.delete(swmMonitorDeviceInfo);
    }

    /**
     * 查询机构下的设备列表
     * 
     * @param swmMonitorDeviceInfo 查询条件
     * @return 设备列表
     */
    public List<SwmMonitorDeviceInfo> officeDeviceList(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        return dao.officeDeviceList(swmMonitorDeviceInfo);
    }
}
