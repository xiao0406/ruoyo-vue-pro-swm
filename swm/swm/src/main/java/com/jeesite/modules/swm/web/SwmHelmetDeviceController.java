/**
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.utils.excel.ExcelExport;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.entity.SwmBeaconStationExport;
import com.jeesite.modules.entity.SwmHelmetDeviceExport;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmHelmetCacheService;
import com.jeesite.modules.swm.entity.SwmSafetyHelmetOrder;
import com.jeesite.modules.swm.service.SwmSafetyHelmetOrderService;
import com.jeesite.modules.sys.utils.ExcelExportUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 头盔设备管理Controller
 * 
 * @author Shawn
 */
@RestController
@RequestMapping(value = "${adminPath}/swmHelmetDevice")
public class SwmHelmetDeviceController extends BaseController {

    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;

    @Autowired
    private SwmPersonService swmPersonService;

    @Autowired
    private SwmHelmetCacheService helmetCacheService;

    @Autowired
    private SwmSafetyHelmetOrderService swmSafetyHelmetOrderService;

    /**
     * 获取单个头盔设备数据
     */
    @GetMapping("get")
    public SwmHelmetDevice get(String id) {
        return swmHelmetDeviceService.get(id);
    }

    /**
     * 查询分页数据
     */
    @GetMapping("list")
    public Page<SwmHelmetDevice> list(SwmHelmetDevice swmHelmetDevice, HttpServletRequest request,
            HttpServletResponse response) {
        swmHelmetDevice.setPage(new Page<>(request, response));
        return swmHelmetDeviceService.findPage(swmHelmetDevice);
    }

    /**
     * 根据头盔编号获取头盔设备
     */
    @GetMapping("getByDeviceId")
    public SwmHelmetDevice getByDeviceId(String deviceId) {
        return swmHelmetDeviceService.getByDeviceId(deviceId);
    }

    /**
     * 查询可用的安全帽列表（未绑定人员的）
     */
    @GetMapping("findAvailableHelmets")
    public List<SwmHelmetDevice> findAvailableHelmets(String keyword) {
        return swmHelmetDeviceService.findAvailableHelmets(keyword);
    }

    /**
     * 根据绑定人员查询设备
     */
    @GetMapping("findByAssignedPerson")
    public List<SwmHelmetDevice> findByAssignedPerson(String assignedPerson) {
        return swmHelmetDeviceService.findByAssignedPerson(assignedPerson);
    }

    /**
     * 根据所属车间查询设备
     */
    @GetMapping("findByWorkshop")
    public List<SwmHelmetDevice> findByWorkshop(String assignedWorkshop) {
        return swmHelmetDeviceService.findByWorkshop(assignedWorkshop);
    }

    /**
     * 根据所属工序查询设备
     */
    @GetMapping("findByProcess")
    public List<SwmHelmetDevice> findByProcess(String assignedProcess) {
        return swmHelmetDeviceService.findByProcess(assignedProcess);
    }

    /**
     * 根据所属班组查询设备
     */
    @GetMapping("findByTeam")
    public List<SwmHelmetDevice> findByTeam(String assignedTeam) {
        return swmHelmetDeviceService.findByTeam(assignedTeam);
    }

    /**
     * 保存数据
     */
    @PostMapping("save")
    public Map<String, Object> save(@RequestBody SwmHelmetDevice swmHelmetDevice) {
        Map<String, Object> result = new HashMap<>();
        swmHelmetDeviceService.save(swmHelmetDevice);
        result.put("success", true);
        result.put("message", "保存安全帽设备成功！");
        return result;
    }

    /**
     * 批量保存数据
     */
    @PostMapping("saveBatch")
    public Map<String, Object> saveBatch(@RequestBody List<SwmHelmetDevice> deviceList) {
        Map<String, Object> result = new HashMap<>();
        swmHelmetDeviceService.saveBatch(deviceList);
        result.put("success", true);
        result.put("message", "批量保存安全帽设备成功！");
        return result;
    }

    /**
     * 删除数据
     */
    @PostMapping("delete")
    public Map<String, Object> delete(String id) {
        Map<String, Object> result = new HashMap<>();
        SwmHelmetDevice swmHelmetDevice = new SwmHelmetDevice(id);
        swmHelmetDeviceService.delete(swmHelmetDevice);
        result.put("success", true);
        result.put("message", "删除安全帽设备成功！");
        return result;
    }

    /**
     * 批量删除数据
     */
    @PostMapping("deleteAll")
    public Map<String, Object> deleteAll(String ids) {
        Map<String, Object> result = new HashMap<>();
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmHelmetDevice swmHelmetDevice = new SwmHelmetDevice(id);
            swmHelmetDeviceService.delete(swmHelmetDevice);
        }
        result.put("success", true);
        result.put("message", "批量删除安全帽设备成功！");
        return result;
    }

    /**
     * 更新头盔电量
     */
    @PostMapping("updateBattery")
    public Map<String, Object> updateBattery(String deviceId, Integer batteryLevel) {
        Map<String, Object> result = new HashMap<>();

        SwmHelmetDevice device = swmHelmetDeviceService.getByDeviceId(deviceId);
        if (device == null) {
            result.put("success", false);
            result.put("message", "未找到对应的安全帽设备！");
            return result;
        }

        device.setBatteryLevel(batteryLevel);
        swmHelmetDeviceService.updateDevice(device);

        result.put("success", true);
        result.put("message", "更新安全帽电量成功！");
        result.put("deviceId", deviceId);
        result.put("batteryLevel", batteryLevel);
        return result;
    }

    /**
     * 绑定人员
     * 
     * @author Shawn
     * @date 2025-05-31
     */
    @PostMapping("assignPerson")
    public Map<String, Object> assignPerson(String deviceId, String personId, String personName) {
        Map<String, Object> result = new HashMap<>();

        SwmHelmetDevice device = swmHelmetDeviceService.getByDeviceId(deviceId);
        if (device == null) {
            result.put("success", false);
            result.put("message", "未找到对应的安全帽设备！");
            return result;
        }

        SwmPerson person = swmPersonService.get(personId);
        if (person == null) {
            result.put("success", false);
            result.put("message", "未找到 ID 为 '" + personId + "' 对应的人员信息！");
            return result;
        }

        String personIdCard = person.getIdentityCard();
        if (personIdCard == null || personIdCard.isEmpty()) {
            result.put("success", false);
            result.put("message", "人员 '" + person.getName() + "' 的身份证信息为空！");
            return result;
        }

        // 设备已存在，使用专门的更新方法
        device.setAssignedPerson(personIdCard);
        device.setPersonName(person.getName());
        device.setPersonPhone(person.getPhoneNumber());
        swmHelmetDeviceService.updateDevice(device);

        result.put("success", true);
        result.put("message", "已成功将安全帽绑定到身份证号：" + personIdCard + " (人员：" + personName + ")");
        result.put("deviceId", deviceId);
        result.put("personId", personId);
        result.put("personIdCard", personIdCard);
        return result;
    }

    /**
     * 解绑人员
     * 
     * @author Shawn
     * @date 2025-05-31
     */
    @PostMapping("unassignPerson")
    public Map<String, Object> unassignPerson(String deviceId) {
        Map<String, Object> result = new HashMap<>();

        SwmHelmetDevice device = swmHelmetDeviceService.getByDeviceId(deviceId);
        if (device == null) {
            result.put("success", false);
            result.put("message", "未找到对应的安全帽设备！");
            return result;
        }

        // 使用专门的方法强制清空绑定信息，确保assigned_person字段设置为null
        swmHelmetDeviceService.clearDeviceAssignment(deviceId);

        // 更新安全帽订单表的解绑时间
        try {
            // 查找该设备的使用中订单并解绑
            SwmSafetyHelmetOrder activeOrder = swmSafetyHelmetOrderService.findActiveOrderByDeviceId(deviceId);
            if (activeOrder != null) {
                swmSafetyHelmetOrderService.unbindHelmet(activeOrder.getId());
                logger.info("已更新安全帽订单解绑时间，订单ID: {}", activeOrder.getId());
            }
        } catch (Exception e) {
            logger.error("更新安全帽订单解绑时间失败", e);
        }

        result.put("success", true);
        result.put("message", "解绑人员成功！");
        return result;
    }

    /**
     * 清除缓存
     */
    @PostMapping("clearCache")
    public Map<String, Object> clearCache() {
        Map<String, Object> result = new HashMap<>();
        swmHelmetDeviceService.clearCache();
        result.put("success", true);
        result.put("message", "清除安全帽缓存成功！");
        return result;
    }

    /**
     * 获取安全帽使用记录
     * 根据设备ID查询所有安全帽订单记录
     */
    @GetMapping("getHelmetUsageRecords")
    public Map<String, Object> getHelmetUsageRecords(String deviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> records = swmHelmetDeviceService.getHelmetUsageRecords(deviceId);
            result.put("success", true);
            result.put("data", records);
        } catch (Exception e) {
            logger.error("获取安全帽使用记录失败", e);
            result.put("success", false);
            result.put("message", "获取安全帽使用记录失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取设备和人员的映射关系（从Redis缓存）
     */
    @GetMapping("getCachedMappings")
    public Map<String, Object> getCachedMappings() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> devicePersonMappings = helmetCacheService.getAllDevicePersonMappings();
            Map<String, String> personDeviceMappings = helmetCacheService.getAllPersonDeviceMappings();

            result.put("success", true);
            result.put("devicePersonMappings", devicePersonMappings);
            result.put("personDeviceMappings", personDeviceMappings);
        } catch (Exception e) {
            logger.error("获取缓存映射关系失败", e);
            result.put("success", false);
            result.put("message", "获取缓存映射关系失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据设备ID从缓存中获取分配的人员
     */
    @GetMapping("getCachedAssignedPerson")
    public Map<String, Object> getCachedAssignedPerson(String deviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String assignedPerson = helmetCacheService.getAssignedPersonFromCache(deviceId, swmHelmetDeviceService);
            result.put("success", true);
            result.put("deviceId", deviceId);
            result.put("assignedPerson", assignedPerson);
        } catch (Exception e) {
            logger.error("从缓存获取设备分配人员失败", e);
            result.put("success", false);
            result.put("message", "从缓存获取设备分配人员失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据人员身份证号从缓存中获取分配的设备
     */
    @GetMapping("getCachedAssignedDevice")
    public Map<String, Object> getCachedAssignedDevice(String personId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String assignedDevice = helmetCacheService.getAssignedDeviceFromCache(personId, swmHelmetDeviceService);
            result.put("success", true);
            result.put("personId", personId);
            result.put("assignedDevice", assignedDevice);
        } catch (Exception e) {
            logger.error("从缓存获取人员分配设备失败", e);
            result.put("success", false);
            result.put("message", "从缓存获取人员分配设备失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 手动清空所有Redis缓存（永久删除）
     */
    @PostMapping("clearAllRedisCache")
    public Map<String, Object> clearAllRedisCache() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 清空所有头盔设备相关的Redis缓存
            helmetCacheService.clearAllHelmetCache();

            result.put("success", true);
            result.put("message", "所有Redis缓存已手动清空！");
        } catch (Exception e) {
            logger.error("手动清空Redis缓存失败", e);
            result.put("success", false);
            result.put("message", "手动清空Redis缓存失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 重新初始化Redis缓存
     */
    @PostMapping("reloadCache")
    public Map<String, Object> reloadCache() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 清除现有缓存
            swmHelmetDeviceService.clearCache();

            // 重新初始化缓存
            swmHelmetDeviceService.initCache();

            result.put("success", true);
            result.put("message", "Redis缓存重新初始化成功！");
        } catch (Exception e) {
            logger.error("重新初始化Redis缓存失败", e);
            result.put("success", false);
            result.put("message", "重新初始化Redis缓存失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 检查Redis连接状态
     */
    @GetMapping("checkRedisStatus")
    public Map<String, Object> checkRedisStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean isAvailable = helmetCacheService.isRedisAvailable();
            result.put("success", true);
            result.put("redisAvailable", isAvailable);
            result.put("message", isAvailable ? "Redis连接正常" : "Redis连接异常");
        } catch (Exception e) {
            logger.error("检查Redis状态失败", e);
            result.put("success", false);
            result.put("redisAvailable", false);
            result.put("message", "检查Redis状态失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 保存设备参数配置
     *
     * @param device 设备参数配置数据
     * @return 保存结果
     * @author Shawn
     * @date 2025-08-04
     */
    @PostMapping("saveDeviceConfig")
    public Map<String, Object> saveDeviceConfig(@RequestBody SwmHelmetDevice device) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "设备编号不能为空");
                return result;
            }

            // 根据设备编号查询现有设备
            SwmHelmetDevice existingDevice = swmHelmetDeviceService.getByDeviceId(device.getDeviceId());
            if (existingDevice == null) {
                result.put("success", false);
                result.put("message", "未找到对应的安全帽设备");
                return result;
            }

            // 更新设备参数配置字段
            existingDevice.setServerIp(device.getServerIp());
            existingDevice.setServerPort(device.getServerPort());
            existingDevice.setBluetoothScanWindow(device.getBluetoothScanWindow());
            existingDevice.setGroupDuration(device.getGroupDuration());
            existingDevice.setNormalBeaconCs(device.getNormalBeaconCs());
            existingDevice.setSpecialBeaconCs(device.getSpecialBeaconCs());
            existingDevice.setLocationMode(device.getLocationMode());
            existingDevice.setDeepSleepDuration(device.getDeepSleepDuration());
            existingDevice.setBluetoothScanDuration(device.getBluetoothScanDuration());
            existingDevice.setSendInterval(device.getSendInterval());
            existingDevice.setHazardRetriggerInterval(device.getHazardRetriggerInterval());
            existingDevice.setSleepWakeupTime(device.getSleepWakeupTime());
            existingDevice.setBeaconFilterName(device.getBeaconFilterName());

            // 保存设备配置
            swmHelmetDeviceService.save(existingDevice);

            result.put("success", true);
            result.put("message", "设备参数配置保存成功");
            return result;
        } catch (Exception e) {
            logger.error("保存设备参数配置失败", e);
            result.put("success", false);
            result.put("message", "保存设备参数配置失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 获取设备参数配置
     *
     * @param deviceId 设备编号
     * @return 设备参数配置
     * @author Shawn
     * @date 2025-08-04
     */
    @GetMapping("getDeviceConfig")
    public SwmHelmetDevice getDeviceConfig(@RequestParam String deviceId) {
        return swmHelmetDeviceService.getByDeviceId(deviceId);
    }

    /**
     * 导出分页数据
     */
    @GetMapping("export")
    public String export(SwmHelmetDevice swmHelmetDevice, HttpServletRequest request,
                                      HttpServletResponse response) {

        swmHelmetDevice.setPage(new Page<>(1, 99999));
        Page<SwmHelmetDevice> page = swmHelmetDeviceService.findPage(swmHelmetDevice);

        String name;
        List<SwmHelmetDevice> list =  page.getList();

        String fileName = "安全帽管理导出" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";

        try (ExcelExport ee = new ExcelExport("安全帽管理导出", SwmHelmetDevice.class)) {
            name = ExcelExportUtil.uploadOss(ee.setDataList(list), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return renderResult(Global.TRUE, text("成功！"), name);
    }

    @ApiOperation("模板下载")
    @RequestMapping("/export")
    @ResponseBody
    public String export() throws IOException {
        String name;
        List<SwmHelmetDeviceExport> list = new ArrayList<>();
        String fileName = "安全帽管理导入模板.xlsx";
        try (ExcelExport ee = new ExcelExport("安全帽管理设置", SwmHelmetDeviceExport.class)) {
            name = ExcelExportUtil.uploadOss(ee.setDataList(list), fileName);
        }
        return renderResult(Global.TRUE, text("成功！"), name);
    }

    @ApiOperation("安全帽管理excel导入")
    @RequestMapping("/importData")
    @ResponseBody
    public String importData(MultipartFile file) {
        Integer count = swmHelmetDeviceService.importData(file);
        return renderResult(Global.TRUE, text("数据全部导入成功,共" + count + "条。"));
    }
}