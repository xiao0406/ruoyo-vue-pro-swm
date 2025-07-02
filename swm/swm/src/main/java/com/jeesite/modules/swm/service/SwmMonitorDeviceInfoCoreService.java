package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.dao.SwmMonitorDeviceInfoDao;
import com.jeesite.modules.swm.entity.SwmMonitorDeviceInfo;
import com.jeesite.modules.swm.util.RsaUtil;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RefreshScope
public class SwmMonitorDeviceInfoCoreService {
    @Resource
    private SwmMonitorDeviceInfoDao swmMonitorDeviceInfoDao;

    public Page<SwmMonitorDeviceInfo> officeDeviceListUpdate(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
        // 不再强制设置recType为"0"，让用户可以通过参数控制
        // 如果没有传递任何参数，则返回全部数据

        List<SwmMonitorDeviceInfo> swmMonitorDeviceInfos = swmMonitorDeviceInfoDao
                .officeDeviceList(swmMonitorDeviceInfo);
        swmMonitorDeviceInfos.forEach(monitorDevice -> {
            String encrypt = "";
            try {
                // 检查loadSource是否为null或空
                if (monitorDevice.getLoadSource() != null && !monitorDevice.getLoadSource().isEmpty()) {
                    encrypt = RsaUtil.getEncrypt(monitorDevice.getLoadSource());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            monitorDevice.setCode(encrypt);
        });
        Page<SwmMonitorDeviceInfo> page = swmMonitorDeviceInfo.getPage();
        return page.setList(swmMonitorDeviceInfos);
    }

    // 大屏展示监控
    public List<SwmMonitorDeviceInfo> monitorInfo() throws Exception {
        SwmMonitorDeviceInfo swmMonitorDeviceInfo = new SwmMonitorDeviceInfo();
        swmMonitorDeviceInfo.setRecType("0");
        swmMonitorDeviceInfo.setName("一楼大门");
        List<SwmMonitorDeviceInfo> monitorOne = swmMonitorDeviceInfoDao.officeDeviceList(swmMonitorDeviceInfo);
        swmMonitorDeviceInfo.setName("一楼展厅");
        List<SwmMonitorDeviceInfo> monitorTwo = swmMonitorDeviceInfoDao.officeDeviceList(swmMonitorDeviceInfo);
        if (CollectionUtils.isEmpty(monitorTwo)) {
            throw new Exception("请维护监控数据！");
        }
        monitorOne.add(monitorTwo.get(0));
        monitorOne.forEach(monitorDevice -> {
            String encrypt = "";
            try {
                // 检查loadSource是否为null或空
                if (monitorDevice.getLoadSource() != null && !monitorDevice.getLoadSource().isEmpty()) {
                    encrypt = RsaUtil.getEncrypt(monitorDevice.getLoadSource());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            monitorDevice.setCode(encrypt);
        });
        return monitorOne;
    }
}
