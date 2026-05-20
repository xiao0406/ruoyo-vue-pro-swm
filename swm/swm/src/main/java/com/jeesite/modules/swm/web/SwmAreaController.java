package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.cache.SwmAreaCache;
import com.jeesite.modules.sys.utils.CorpUtils;
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
import com.jeesite.modules.entity.SwmArea;
import com.jeesite.modules.entity.SwmBeaconStation;
import com.jeesite.modules.swm.service.SwmAreaService;
import com.jeesite.modules.swm.service.SwmBeaconStationService;

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

    @Autowired
    private SwmBeaconStationService swmBeaconStationService;

    @Autowired
    private SwmAreaCache swmAreaCache;
    @Autowired
    private RedisService redisService;

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

    @RequestMapping(value = "test")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public String  test() {

        //区域id -> 区域信息, hash结构，key为区域ID，value为区域详情
        String corpCode = CorpUtils.getCurrentCorpCode();
        String areaIdCacheKey = corpCode+ SwmRedisConstant.RedisSwmKey.AREA_ID_CACHE_KEY;
        SwmArea hget = (SwmArea) redisService.hget(areaIdCacheKey, "2033431438004977664");

        //信标mac地址到信标信息的映射
        String beaconCacheKey = corpCode+ SwmRedisConstant.RedisSwmKey.BEACON_MAC_CACHE_KEY;
        SwmBeaconStation hget1 = (SwmBeaconStation) redisService.hget(beaconCacheKey, "mac142956jy21");



        return "success";
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
            data.put("areaType", swmArea.getAreaType());
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
        //生产区域信标,插入redis中
        swmAreaCache.insertAreaCache(swmArea);
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

        //删除缓存
        swmAreaCache.delete(swmArea);
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
            String areaType = (String) params.get("areaType");
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
            swmArea.setAreaType(areaType);
            swmArea.setAreaColor(beaconColor); // 保存区域颜色
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
     * 保存区域并通过信标ID更新信标
     *
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping(value = "saveAreaWithBeaconIds")
    @ResponseBody
    @ApiOperation("保存区域并通过信标ID更新信标")
    public Map<String, Object> saveAreaWithBeaconIds(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String id = (String) params.get("id");
            String areaName = (String) params.get("areaName");
            String areaType = (String) params.get("areaType");
            String voicePrompt = (String) params.get("voicePrompt");
            String filePath = (String) params.get("filePath");
            List<String> bids = (List<String>) params.get("bids");
            String beaconColor = (String) params.get("beaconColor");
            Boolean isEdit = (Boolean) params.get("isEdit");
            // 获取是否大屏展示字段，默认为false（否）
            Boolean isScreenShow = params.get("isScreenShow") != null 
                ? Boolean.parseBoolean(params.get("isScreenShow").toString()) 
                : false;

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
            swmArea.setAreaType(areaType);
            swmArea.setAreaColor(beaconColor); // 保存区域颜色
            swmArea.setVoicePrompt(voicePrompt);
            swmArea.setFilePath(filePath);
            swmArea.setIsScreenShow(isScreenShow); // 设置是否大屏展示

            // 将 bids 转换为 JSON 字符串并设置到区域对象
            if (bids != null && !bids.isEmpty()) {
                String bIdsJson = JSON.toJSONString(bids);
                swmArea.setBIds(bIdsJson);
                logger.info("设置区域信标ID列表: {}", bIdsJson);
            } else {
                swmArea.setBIds(null);
                logger.info("清空区域信标ID列表");
            }

            // 保存区域（同时保存 b_ids 字段）
            swmAreaService.save(swmArea);

            // 处理信标ID列表并更新信标
            logger.info("处理信标ID列表，区域ID: {}, 信标ID列表: {}, 是否编辑: {}", swmArea.getId(), bids, isEdit);

            if (bids != null && !bids.isEmpty()) {
                // 先检查冲突
                List<Map<String, Object>> conflicts = swmAreaService.checkBeaconConflictsByIds(bids, swmArea.getId());

                if (!conflicts.isEmpty()) {
                    result.put("success", false);
                    result.put("message", "发现信标冲突，无法保存");
                    result.put("conflicts", conflicts);
                    result.put("hasConflicts", true);
                    return result; // 直接返回，阻止保存
                }

                // 没有冲突，继续更新信标
                if (isEdit != null && isEdit) {
                    // 编辑模式：先清空再重新设置，确保移除的信标不再关联该区域
                    logger.info("编辑模式：调用updateAreaBeaconAssociationByIds");
                    swmAreaService.updateAreaBeaconAssociationByIds(swmArea.getId(), bids, beaconColor);
                } else {
                    // 新增模式：直接设置信标关联
                    logger.info("新增模式：调用updateBeaconsByIds");
                    swmAreaService.updateBeaconsByIds(swmArea.getId(), bids, beaconColor);
                }
            } else if (isEdit != null && isEdit) {
                // 编辑模式下，如果信标ID列表为空，则清空该区域的所有信标关联
                logger.info("编辑模式且信标ID列表为空：调用clearBeaconAreaAndColor");
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
    public Map<String, Object> testCoordinateMatch(String coordinateList) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("测试坐标匹配: {}", coordinateList);

            // 解析坐标列表
            Pattern pattern = Pattern.compile("\\((\\d+(?:\\.\\d+)?),\\s*(\\d+(?:\\.\\d+)?)\\)");
            Matcher matcher = pattern.matcher(coordinateList);

            List<Map<String, Object>> matchResults = new ArrayList<>();

            while (matcher.find()) {
                Double pixelX = Double.parseDouble(matcher.group(1));
                Double pixelY = Double.parseDouble(matcher.group(2));

                List<SwmBeaconStation> beacons = swmBeaconStationService.findByPixelCoordinates(pixelX, pixelY);

                Map<String, Object> matchResult = new HashMap<>();
                matchResult.put("coordinates", String.format("(%.0f, %.0f)", pixelX, pixelY));
                matchResult.put("found", beacons != null && !beacons.isEmpty());
                matchResult.put("count", beacons != null ? beacons.size() : 0);

                if (beacons != null && !beacons.isEmpty()) {
                    matchResult.put("beacons", beacons.stream()
                            .map(b -> b.getBeaconId() + " [" + b.getPixelX() + "," + b.getPixelY() + "]")
                            .collect(Collectors.toList()));
                }

                matchResults.add(matchResult);
            }

            result.put("success", true);
            result.put("results", matchResults);
            result.put("message", "坐标匹配测试完成");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "坐标匹配测试失败：" + e.getMessage());
            logger.error("坐标匹配测试失败", e);
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

    /**
     * 检查信标坐标是否已被其他区域使用
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping(value = "checkBeaconConflicts")
    @ResponseBody
    @ApiOperation("检查信标坐标是否已被其他区域使用")
    public Map<String, Object> checkBeaconConflicts(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String coordinateList = (String) params.get("coordinateList");
            String currentAreaId = (String) params.get("currentAreaId"); // 当前区域ID，编辑时传入

            if (coordinateList == null || coordinateList.trim().isEmpty()) {
                result.put("success", true);
                result.put("hasConflicts", false);
                result.put("conflicts", new ArrayList<>());
                result.put("message", "坐标列表为空");
                return result;
            }

            List<Map<String, Object>> conflicts = swmAreaService.checkBeaconConflicts(coordinateList, currentAreaId);

            result.put("success", true);
            result.put("hasConflicts", !conflicts.isEmpty());
            result.put("conflicts", conflicts);
            result.put("message", conflicts.isEmpty() ? "没有发现冲突" : "发现信标冲突");

        } catch (Exception e) {
            result.put("success", false);
            result.put("hasConflicts", false);
            result.put("conflicts", new ArrayList<>());
            result.put("message", "检查失败：" + e.getMessage());
            logger.error("检查信标冲突失败", e);
        }
        return result;
    }

    /**
     * 获取区域选项列表（用于下拉选择）
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "getAreaOptions")
    @ResponseBody
    @ApiOperation("获取区域选项列表")
    public Map<String, Object> getAreaOptions() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有区域，包含逻辑删除的数据，供信标列表和表单回显历史区域名称
            SwmArea queryArea = new SwmArea();
            queryArea.setStatus(null);
            queryArea.getSqlMap().getWhere().disableAutoAddStatusWhere();
            List<SwmArea> areaList = swmAreaService.findList(queryArea);

            // 转换为选项格式
            List<Map<String, Object>> options = new ArrayList<>();
            if (areaList != null && !areaList.isEmpty()) {
                for (SwmArea area : areaList) {
                    Map<String, Object> option = new HashMap<>();
                    option.put("value", area.getId());
                    option.put("label", area.getAreaName());
                    option.put("areaName", area.getAreaName()); // 额外信息
                    options.add(option);
                }
            }

            result.put("success", true);
            result.put("options", options);
            result.put("total", options.size());
            result.put("message", "获取区域选项成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("options", new ArrayList<>());
            result.put("message", "获取区域选项失败: " + e.getMessage());
            logger.error("获取区域选项失败", e);
        }
        return result;
    }

    /**
     * 批量更新区域下所有信标的颜色
     * 
     * @author Shawn
     * @date 2025/06/23
     */
    @PostMapping(value = "updateAreaBeaconColors")
    @ResponseBody
    @ApiOperation("批量更新区域下所有信标的颜色")
    public Map<String, Object> updateAreaBeaconColors(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String areaId = (String) params.get("areaId");
            String beaconColor = (String) params.get("beaconColor");
            String remarks = (String) params.get("remarks");

            if (areaId == null || areaId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "区域ID不能为空！");
                return result;
            }

            if (beaconColor == null || beaconColor.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "颜色值不能为空！");
                return result;
            }

            // 获取区域信息
            SwmArea area = swmAreaService.get(areaId);
            if (area == null) {
                result.put("success", false);
                result.put("message", "区域不存在！");
                return result;
            }

            // 如果有备注需要更新，则更新区域信息
            if (remarks != null && !remarks.trim().isEmpty()) {
                area.setRemarks(remarks);
                swmAreaService.save(area);
                logger.info("更新区域 {} 的备注信息", area.getAreaName());
            }

            // 获取该区域下的所有信标
            List<SwmBeaconStation> beacons = swmBeaconStationService.findByArea(areaId);
            if (beacons == null || beacons.isEmpty()) {
                result.put("success", true);
                result.put("message", "该区域下没有关联的信标，无需更新颜色");
                result.put("updatedCount", 0);
                return result;
            }

            // 批量更新信标颜色
            int updatedCount = 0;
            for (SwmBeaconStation beacon : beacons) {
                beacon.setBeaconColor(beaconColor);
                swmBeaconStationService.save(beacon);
                updatedCount++;
                logger.info("更新信标 {} 的颜色为: {}", beacon.getBeaconId(), beaconColor);
            }

            result.put("success", true);
            result.put("message", String.format("成功更新区域 \"%s\" 下 %d 个信标的颜色", area.getAreaName(), updatedCount));
            result.put("updatedCount", updatedCount);
            logger.info("批量更新区域 {} 下 {} 个信标的颜色为: {}", area.getAreaName(), updatedCount, beaconColor);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量更新信标颜色失败：" + e.getMessage());
            logger.error("批量更新信标颜色失败", e);
        }
        return result;
    }

    /**
     * 检查信标ID冲突
     *
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping(value = "checkBeaconIdConflicts")
    @ResponseBody
    @ApiOperation("检查信标ID冲突")
    public Map<String, Object> checkBeaconIdConflicts(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> beaconIds = (List<String>) params.get("beaconIds");
            String currentAreaId = (String) params.get("currentAreaId");

            if (beaconIds == null || beaconIds.isEmpty()) {
                result.put("success", true);
                result.put("conflicts", new ArrayList<>());
                result.put("message", "没有需要检查的信标ID");
                return result;
            }

            List<Map<String, Object>> conflicts = swmAreaService.checkBeaconConflictsByIds(beaconIds, currentAreaId);

            result.put("success", true);
            result.put("conflicts", conflicts);
            result.put("hasConflicts", !conflicts.isEmpty());
            result.put("message", conflicts.isEmpty() ? "没有发现冲突" : "发现 " + conflicts.size() + " 个冲突");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查信标ID冲突失败：" + e.getMessage());
            logger.error("检查信标ID冲突失败", e);
        }
        return result;
    }

    /**
     * 测试获取区域的 bIds 字段
     *
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "testGetAreaBIds")
    @ResponseBody
    @ApiOperation("测试获取区域的bIds字段")
    public Map<String, Object> testGetAreaBIds(String areaId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (areaId == null || areaId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "区域ID不能为空");
                return result;
            }

            SwmArea area = swmAreaService.get(areaId);
            if (area == null) {
                result.put("success", false);
                result.put("message", "区域不存在");
                return result;
            }

            result.put("success", true);
            result.put("areaId", area.getId());
            result.put("areaName", area.getAreaName());
            result.put("bIds", area.getBIds());
            result.put("message", "获取成功");

            logger.info("获取区域 {} 的 bIds 字段: {}", area.getAreaName(), area.getBIds());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
            logger.error("获取区域bIds失败", e);
        }
        return result;
    }
}
