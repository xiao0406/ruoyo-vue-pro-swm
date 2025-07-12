package com.jeesite.modules.swm.service;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.collect.ListUtils;
import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmMonitorDeviceInfoDao;
import com.jeesite.modules.swm.entity.SwmMonitorDeviceInfo;
import com.jeesite.modules.sys.dao.MonitorDeviceInfoDao;
import com.jeesite.modules.sys.entity.MonitorDeviceInfo;
import com.jeesite.modules.sys.entity.Role;
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

    /**
     * 查询公司列表
     */
    public List<JSONObject> companyTreeData() {

        List<JSONObject> mapList = ListUtils.newArrayList();
        SwmMonitorDeviceInfo where = new SwmMonitorDeviceInfo();
        where.setRecType("1");
        List<SwmMonitorDeviceInfo> list = this.findList(where);

        for(int i = 0; i < list.size(); ++i) {
            SwmMonitorDeviceInfo e = (SwmMonitorDeviceInfo)list.get(i);
            if ("0".equals(e.getStatus())) {
                JSONObject map = new JSONObject();
                map.put("id", e.getId());
                map.put("pId", e.getParentId());
                map.put("code", e.getId());
                map.put("name", e.getName());
                map.put("title", e.getName());
                map.put("isParent", !e.getIsTreeLeaf());
                map.put("treeSort", e.getTreeSort());
                mapList.add(map);
            }
        }

        return mapList;

    }


    public List<JSONObject> roleTreeData() {
        List<JSONObject> mapList = ListUtils.newArrayList();
        Role where = new Role();
        where.setStatus("0");
        where.setUserType("employee");
        List<Role> list = ((SwmMonitorDeviceInfoDao)this.dao).findAllRoleList(where);

        for(int i = 0; i < list.size(); ++i) {
            Role e = (Role)list.get(i);
            if ("0".equals(e.getStatus())) {
                JSONObject map = new JSONObject();
                map.put("id", e.getId());
                map.put("pId", "0");
                map.put("code", e.getViewCode());
                map.put("name", e.getRoleName());
                map.put("title", e.getRoleName());
                map.put("isParent", false);
                mapList.add(map);
            }
        }

        return mapList;
    }
}
