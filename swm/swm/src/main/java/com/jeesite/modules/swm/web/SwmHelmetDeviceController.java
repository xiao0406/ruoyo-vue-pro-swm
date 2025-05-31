/**
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        swmHelmetDeviceService.updateDevice(device);

        result.put("success", true);
        result.put("message", "已成功将安全帽绑定到身份证号：" + personIdCard + " (人员：" + personName + ")");
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

        // 设备已存在，使用专门的更新方法
        device.setAssignedPerson("");
        swmHelmetDeviceService.updateDevice(device);

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
}