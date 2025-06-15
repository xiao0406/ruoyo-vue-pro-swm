package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
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
            String areaName = (String) params.get("areaName");
            String voicePrompt = (String) params.get("voicePrompt");
            String filePath = (String) params.get("filePath");
            String beaconList = (String) params.get("beaconList");
            String beaconColor = (String) params.get("beaconColor");

            // 创建区域对象
            SwmArea swmArea = new SwmArea();
            swmArea.setAreaName(areaName);
            swmArea.setVoicePrompt(voicePrompt);
            swmArea.setFilePath(filePath);

            // 保存区域
            swmAreaService.save(swmArea);

            // 解析信标列表并更新信标
            if (beaconList != null && !beaconList.trim().isEmpty()) {
                swmAreaService.updateBeaconsByCoordinates(swmArea.getId(), beaconList, beaconColor);
            }

            result.put("success", true);
            result.put("message", "保存区域成功！");
            result.put("data", swmArea);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存区域失败：" + e.getMessage());
            logger.error("保存区域失败", e);
        }
        return result;
    }
}