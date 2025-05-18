/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.swm.service.SwmBeaconStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信标基站管理Controller
 * 
 * @author Shawn
 */
@Controller
@RequestMapping(value = "${adminPath}/swmBeaconStation")
@Api(value = "信标基站管理接口")
public class SwmBeaconStationController extends BaseController {

    @Autowired
    private SwmBeaconStationService swmBeaconStationService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmBeaconStation get(String id, boolean isNewRecord) {
        return swmBeaconStationService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmBeaconStation swmBeaconStation, Model model) {
        model.addAttribute("swmBeaconStation", swmBeaconStation);
        return "modules/swm/swmBeaconStationList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmBeaconStation> listData(SwmBeaconStation swmBeaconStation, HttpServletRequest request,
            HttpServletResponse response) {
        Page<SwmBeaconStation> page = swmBeaconStationService.findPage(new Page<>(request, response), swmBeaconStation);
        return page;
    }

    /**
     * 获取所有信标基站列表(用于下拉框选择)
     */
    @GetMapping(value = "listForSelect")
    @ResponseBody
    @ApiOperation(value = "获取所有信标基站列表用于下拉框选择")
    public List<Map<String, Object>> listForSelect() {
        // 创建查询条件，可根据需求添加筛选条件
        SwmBeaconStation swmBeaconStation = new SwmBeaconStation();
        // 获取全部信标基站数据
        List<SwmBeaconStation> beaconList = swmBeaconStationService.findList(swmBeaconStation);
        // 转换为下拉框需要的格式
        return swmBeaconStationService.convertToSelectList(beaconList);
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    public Map<String, Object> form(SwmBeaconStation swmBeaconStation, Model model) {
        Map<String, Object> result = new HashMap<>();
        if (swmBeaconStation != null) {
            Map<String, Object> data = new HashMap<>();
            // 复制基本属性
            data.put("id", swmBeaconStation.getId());
            data.put("beaconId", swmBeaconStation.getBeaconId());
            data.put("beaconType", swmBeaconStation.getBeaconType());
            data.put("controlType", swmBeaconStation.getControlType());
            data.put("location", swmBeaconStation.getLocation());
            data.put("mapCoord", swmBeaconStation.getMapCoord());
            data.put("gpsCoord", swmBeaconStation.getGpsCoord());
            data.put("beaconStatus", swmBeaconStation.getBeaconStatus());
            data.put("beaconStatusText", swmBeaconStation.getBeaconStatusText());
            data.put("deployStatus", swmBeaconStation.getDeployStatus());
            data.put("deployStatusText", swmBeaconStation.getDeployStatusText());
            data.put("remarks", swmBeaconStation.getRemarks());

            result.putAll(data);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation(value = "保存信标基站")
    public String save(@Validated SwmBeaconStation swmBeaconStation) {
        swmBeaconStationService.save(swmBeaconStation);
        return renderResult(Global.TRUE, text("保存信标基站成功！"));
    }

    /**
     * 删除数据
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation(value = "删除信标基站")
    public String delete(SwmBeaconStation swmBeaconStation) {
        swmBeaconStationService.delete(swmBeaconStation);
        return renderResult(Global.TRUE, text("删除信标基站成功！"));
    }

    /**
     * 批量删除数据
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    @ApiOperation(value = "批量删除信标基站")
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmBeaconStation swmBeaconStation = swmBeaconStationService.get(id);
            if (swmBeaconStation != null) {
                swmBeaconStationService.delete(swmBeaconStation);
            }
        }
        return renderResult(Global.TRUE, text("删除信标基站成功！"));
    }

    /**
     * 根据信标编号获取基站
     */
    @GetMapping("getByBeaconId")
    @ResponseBody
    @ApiOperation(value = "根据信标编号获取基站")
    public SwmBeaconStation getByBeaconId(String beaconId) {
        return swmBeaconStationService.getByBeaconId(beaconId);
    }

    /**
     * 查询指定位置的基站列表
     */
    @GetMapping("findByLocation")
    @ResponseBody
    @ApiOperation(value = "查询指定位置的基站列表")
    public List<SwmBeaconStation> findByLocation(String location) {
        return swmBeaconStationService.findByLocation(location);
    }

    /**
     * 查询在线状态的基站列表
     */
    @GetMapping("findOnlineBeacons")
    @ResponseBody
    @ApiOperation(value = "查询在线状态的基站列表")
    public List<SwmBeaconStation> findOnlineBeacons() {
        return swmBeaconStationService.findOnlineBeacons();
    }
}