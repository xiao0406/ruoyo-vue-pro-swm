package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmArea;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.swm.service.SwmAreaService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 区域管理Controller
 * 
 * @author Shawn
 * @version 2025-06-22
 */
@Controller
@RequestMapping(value = "${adminPath}/swmArea")
@Api(value = "区域管理接口", tags = "区域管理接口")
public class SwmAreaController extends BaseController {

    @Autowired
    private SwmAreaService swmAreaService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmArea get(String id, boolean isNewRecord) {
        return swmAreaService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmArea swmArea, Model model) {
        model.addAttribute("swmArea", swmArea);
        return "modules/swm/swmAreaList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmArea> listData(SwmArea swmArea, HttpServletRequest request, HttpServletResponse response) {
        swmArea.setPage(new Page<>(request, response));
        Page<SwmArea> page = swmAreaService.findPage(swmArea);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ApiOperation("表单页面")
    public String form(SwmArea swmArea, Model model) {
        model.addAttribute("swmArea", swmArea);
        return "modules/swm/swmAreaForm";
    }

    /**
     * 获取区域表单数据(JSON格式)
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "formData")
    @ResponseBody
    @ApiOperation("获取区域表单数据")
    public Map<String, Object> formData(SwmArea swmArea, Model model) {
        Map<String, Object> result = new HashMap<>();
        if (swmArea != null) {
            Map<String, Object> data = new HashMap<>();
            // 复制基本属性
            data.put("id", swmArea.getId());
            data.put("areaName", swmArea.getAreaName());
            data.put("workShop", swmArea.getWorkShop());
            data.put("voicePrompt", swmArea.getVoicePrompt());
            data.put("filePath", swmArea.getFilePath());
            data.put("status", swmArea.getStatus());
            data.put("remarks", swmArea.getRemarks());

            result.putAll(data);
        }
        return result;
    }

    /**
     * 获取全部区域列表(用于信标地图显示)
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "listAll")
    @ResponseBody
    @ApiOperation(value = "获取全部区域列表用于地图显示")
    public Map<String, Object> listAll(SwmArea swmArea) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取全部区域数据，不分页
            java.util.List<SwmArea> areaList = swmAreaService.findList(swmArea);

            result.put("success", true);
            result.put("list", areaList);
            result.put("total", areaList.size());
            result.put("message", "获取区域列表成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取区域列表失败: " + e.getMessage());
            logger.error("获取区域列表失败", e);
        }
        return result;
    }

    /**
     * 保存区域
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存区域")
    public String save(@Validated SwmArea swmArea) {
        swmAreaService.save(swmArea);
        return renderResult(Global.TRUE, text("保存区域成功！"));
    }

    /**
     * 删除区域
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除区域")
    public String delete(SwmArea swmArea) {
        swmAreaService.delete(swmArea);
        return renderResult(Global.TRUE, text("删除区域成功！"));
    }

    /**
     * 删除区域并清空相关信标
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping(value = "deleteAreaWithBeacons")
    @ResponseBody
    @ApiOperation("删除区域并清空相关信标")
    public Map<String, Object> deleteAreaWithBeacons(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String areaId = (String) params.get("id");

            if (areaId == null || areaId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "区域ID不能为空！");
                return result;
            }

            // 先获取区域信息
            SwmArea area = swmAreaService.get(areaId);
            if (area == null) {
                result.put("success", false);
                result.put("message", "区域不存在！");
                return result;
            }

            // 删除区域并清空相关信标
            swmAreaService.deleteAreaWithBeacons(areaId);

            result.put("success", true);
            result.put("message", "删除区域成功！");
            logger.info("成功删除区域: {}", area.getAreaName());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除区域失败：" + e.getMessage());
            logger.error("删除区域失败", e);
        }
        return result;
    }

    /**
     * 保存区域并更新信标
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping(value = "saveAreaWithBeacons")
    @ResponseBody
    @ApiOperation("保存区域并更新信标")
    public Map<String, Object> saveAreaWithBeacons(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String id = (String) params.get("id");
            String areaName = (String) params.get("areaName");
            String voicePrompt = (String) params.get("voicePrompt");
            String filePath = (String) params.get("filePath");
            String beaconList = (String) params.get("beaconList");
            String beaconColor = (String) params.get("beaconColor");
            Boolean isEdit = (Boolean) params.get("isEdit");

            SwmArea swmArea;
            if (isEdit != null && isEdit && id != null && !id.trim().isEmpty()) {
                // 编辑模式：获取现有区域
                swmArea = swmAreaService.get(id);
                if (swmArea == null) {
                    result.put("success", false);
                    result.put("message", "区域不存在！");
                    return result;
                }
            } else {
                // 新增模式：创建新区域对象
                swmArea = new SwmArea();
            }

            // 设置区域属性
            swmArea.setAreaName(areaName);
            swmArea.setVoicePrompt(voicePrompt);
            swmArea.setFilePath(filePath);

            // 保存区域
            swmAreaService.save(swmArea);

            // 解析信标列表并更新信标
            logger.info("处理信标列表，区域ID: {}, 信标列表: {}, 是否编辑: {}", swmArea.getId(), beaconList, isEdit);

            if (beaconList != null && !beaconList.trim().isEmpty()) {
                if (isEdit != null && isEdit) {
                    // 编辑模式：先清空再重新设置，确保移除的信标不再关联该区域
                    logger.info("编辑模式：调用updateAreaBeaconAssociation");
                    swmAreaService.updateAreaBeaconAssociation(swmArea.getId(), beaconList, beaconColor);
                } else {
                    // 新增模式：直接设置信标关联
                    logger.info("新增模式：调用updateBeaconsByCoordinates");
                    swmAreaService.updateBeaconsByCoordinates(swmArea.getId(), beaconList, beaconColor);
                }
            } else if (isEdit != null && isEdit) {
                // 编辑模式下，如果信标列表为空，则清空该区域的所有信标关联
                logger.info("编辑模式且信标列表为空：调用clearBeaconAreaAndColor");
                swmAreaService.clearBeaconAreaAndColor(swmArea.getId());
            }

            result.put("success", true);
            result.put("message", isEdit != null && isEdit ? "更新区域成功！" : "保存区域成功！");
            result.put("data", swmArea);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存区域失败：" + e.getMessage());
            logger.error("保存区域失败", e);
        }
        return result;
    }

    /**
     * 获取区域对应的信标坐标列表
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "getAreaBeaconCoordinates")
    @ResponseBody
    @ApiOperation("获取区域对应的信标坐标列表")
    public Map<String, Object> getAreaBeaconCoordinates(String areaId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String coordinates = swmAreaService.getAreaBeaconCoordinates(areaId);
            result.put("success", true);
            result.put("coordinates", coordinates);
            result.put("message", "获取信标坐标成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("coordinates", "");
            result.put("message", "获取信标坐标失败：" + e.getMessage());
            logger.error("获取信标坐标失败", e);
        }
        return result;
    }

    /**
     * 测试清空区域信标关联
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "testClearBeacons")
    @ResponseBody
    @ApiOperation("测试清空区域信标关联")
    public Map<String, Object> testClearBeacons(String areaId) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("测试清空区域 {} 的信标关联", areaId);
            swmAreaService.clearBeaconAreaAndColor(areaId);
            result.put("success", true);
            result.put("message", "清空成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清空失败：" + e.getMessage());
            logger.error("清空失败", e);
        }
        return result;
    }

    /**
     * 测试坐标匹配
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "testCoordinateMatch")
    @ResponseBody
    @ApiOperation("测试坐标匹配")
    public Map<String, Object> testCoordinateMatch(String coordinates) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("测试坐标匹配: {}", coordinates);
            result.put("success", true);
            result.put("message", "测试完成，请查看日志");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试失败：" + e.getMessage());
            logger.error("测试失败", e);
        }
        return result;
    }

    /**
     * 查询区域下的信标状态
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "checkAreaBeacons")
    @ResponseBody
    @ApiOperation("查询区域下的信标状态")
    public Map<String, Object> checkAreaBeacons(String areaId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SwmBeaconStation> beacons = swmAreaService.getBeaconsByArea(areaId);
            result.put("success", true);
            result.put("beacons", beacons);
            result.put("count", beacons != null ? beacons.size() : 0);
            result.put("message", "查询成功");

            // 记录详细信息
            if (beacons != null) {
                for (SwmBeaconStation beacon : beacons) {
                    logger.info("区域 {} 下的信标: ID={}, BeaconId={}, 坐标=({}, {})",
                            areaId, beacon.getId(), beacon.getBeaconId(), beacon.getPixelX(), beacon.getPixelY());
                }
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            logger.error("查询失败", e);
        }
        return result;
    }
}