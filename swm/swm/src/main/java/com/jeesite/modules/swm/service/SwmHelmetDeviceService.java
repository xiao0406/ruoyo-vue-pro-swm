/**
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmHelmetDeviceDao;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 头盔设备管理服务
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmHelmetDeviceService extends CrudService<SwmHelmetDeviceDao, SwmHelmetDevice> {

    private static final Logger logger = LoggerFactory.getLogger(SwmHelmetDeviceService.class);

    // 简单的内存缓存，用于缓存头盔信息
    private final ConcurrentMap<String, SwmHelmetDevice> helmetCache = new ConcurrentHashMap<>();

    /**
     * 获取单条数据
     */
    @Override
    public SwmHelmetDevice get(String id) {
        return super.get(id);
    }

    /**
     * 根据头盔编号获取头盔设备
     */
    public SwmHelmetDevice getByHelmetId(String helmetId) {
        // 先从缓存中查找
        SwmHelmetDevice cachedDevice = helmetCache.get(helmetId);
        if (cachedDevice != null) {
            logger.debug("从缓存中获取安全帽: {}", helmetId);
            return cachedDevice;
        }

        // 缓存中没有，从数据库查询
        SwmHelmetDevice result = dao.getByHelmetId(helmetId);
        if (result != null) {
            // 放入缓存
            helmetCache.put(helmetId, result);
        }

        return result;
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmHelmetDevice> findPage(SwmHelmetDevice device) {
        return super.findPage(device);
    }

    /**
     * 查询可用的安全帽列表（未绑定人员的）
     */
    public List<SwmHelmetDevice> findAvailableHelmets(String keyword) {
        return dao.findAvailableHelmets(keyword);
    }

    /**
     * 根据绑定人员查询设备
     */
    public List<SwmHelmetDevice> findByAssignedPerson(String assignedPerson) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedPerson(assignedPerson);
        return this.findList(device);
    }

    /**
     * 根据所属车间查询设备
     */
    public List<SwmHelmetDevice> findByWorkshop(String assignedWorkshop) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedWorkshop(assignedWorkshop);
        return this.findList(device);
    }

    /**
     * 根据所属工序查询设备
     */
    public List<SwmHelmetDevice> findByProcess(String assignedProcess) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedProcess(assignedProcess);
        return this.findList(device);
    }

    /**
     * 根据所属班组查询设备
     */
    public List<SwmHelmetDevice> findByTeam(String assignedTeam) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedTeam(assignedTeam);
        return this.findList(device);
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmHelmetDevice device) {
        super.save(device);

        // 更新缓存
        if (device.getHelmetId() != null) {
            helmetCache.put(device.getHelmetId(), device);
            logger.debug("更新安全帽缓存: {}", device.getHelmetId());
        }
    }

    /**
     * 批量保存数据
     */
    @Transactional(readOnly = false)
    public void saveBatch(List<SwmHelmetDevice> deviceList) {
        for (SwmHelmetDevice device : deviceList) {
            this.save(device);
        }
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmHelmetDevice device) {
        super.delete(device);

        // 从缓存中移除
        if (device.getHelmetId() != null) {
            helmetCache.remove(device.getHelmetId());
            logger.debug("从缓存中移除安全帽: {}", device.getHelmetId());
        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        helmetCache.clear();
        logger.info("已清除安全帽缓存");
    }
}