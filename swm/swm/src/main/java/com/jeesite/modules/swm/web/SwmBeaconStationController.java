/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.utils.excel.ExcelExport;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.swm.service.SwmBeaconStationService;
import com.jeesite.modules.sys.utils.ExcelExportUtil;
import com.jeesite.modules.entity.SwmBeaconStationExport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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
     * 获取全部信标基站列表(用于信标地图显示)
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "listAll")
    @ResponseBody
    @ApiOperation(value = "获取全部信标基站列表用于地图显示")
    public Map<String, Object> listAll(SwmBeaconStation swmBeaconStation) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取全部信标基站数据，不分页
            List<SwmBeaconStation> beaconList = swmBeaconStationService.findList(swmBeaconStation);

            result.put("success", true);
            result.put("list", beaconList);
            result.put("total", beaconList.size());
            result.put("message", "获取信标列表成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            result.put("message", "获取信标列表失败：" + e.getMessage());
        }
        return result;
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
     * 获取危险源类型的信标基站列表(用于下拉框选择)
     * 
     * @author Shawn
     * @date 2025-07-27
     */
    @GetMapping(value = "listDangerousSourceForSelect")
    @ResponseBody
    @ApiOperation(value = "获取危险源类型的信标基站列表用于下拉框选择")
    public List<Map<String, Object>> listDangerousSourceForSelect() {
        // 调用Service方法获取危险源类型的信标列表
        return swmBeaconStationService.getDangerousSourceBeaconSelectList();
    }

    /**
     * 获取可用的危险源类型信标基站列表(排除已使用的)
     * 
     * @author Shawn
     * @date 2025-07-27
     */
    @GetMapping(value = "listAvailableDangerousSourceForSelect")
    @ResponseBody
    @ApiOperation(value = "获取可用的危险源类型信标基站列表(排除已使用的)")
    public List<Map<String, Object>> listAvailableDangerousSourceForSelect(String excludeHazardSourceId) {
        // 调用过滤已使用信标的方法
        return swmBeaconStationService.getAvailableDangerousSourceBeaconSelectList(excludeHazardSourceId);
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
            data.put("deviceName", swmBeaconStation.getDeviceName());
            data.put("beaconType", swmBeaconStation.getBeaconType());
            data.put("beaconColor", swmBeaconStation.getBeaconColor());
            data.put("controlType", swmBeaconStation.getControlType());
            data.put("location", swmBeaconStation.getLocation());
            data.put("area", swmBeaconStation.getArea());
            data.put("mapCoord", swmBeaconStation.getMapCoord());
            data.put("pixelX", swmBeaconStation.getPixelX());
            data.put("pixelY", swmBeaconStation.getPixelY());
            data.put("realX", swmBeaconStation.getRealX());
            data.put("realY", swmBeaconStation.getRealY());
            data.put("gpsCoord", swmBeaconStation.getGpsCoord());
            data.put("gpsLongitude", swmBeaconStation.getGpsLongitude());
            data.put("gpsLatitude", swmBeaconStation.getGpsLatitude());
            data.put("beaconStatus", swmBeaconStation.getBeaconStatus());
            data.put("beaconStatusText", swmBeaconStation.getBeaconStatusText());
            data.put("deployStatus", swmBeaconStation.getDeployStatus());
            data.put("deployStatusText", swmBeaconStation.getDeployStatusText());
            data.put("streamUrl", swmBeaconStation.getStreamUrl());
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
    public String save(@RequestBody SwmBeaconStation swmBeaconStation) {
        try {
            // 手动验证：如果不是电子围栏，则beaconId不能为空
            if (!"2".equals(swmBeaconStation.getBeaconType())
                    && (swmBeaconStation.getBeaconId() == null || swmBeaconStation.getBeaconId().trim().isEmpty())) {
                return renderResult(Global.FALSE, text("MAC地址不能为空！"));
            }

            swmBeaconStationService.save(swmBeaconStation);
            return renderResult(Global.TRUE, text("保存信标基站成功！"));
        } catch (RuntimeException e) {
            // 处理业务异常，如重复MAC地址等
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("已存在")) {
                return renderResult(Global.FALSE, errorMessage);
            }
            return renderResult(Global.FALSE, text("保存信标基站失败：") + errorMessage);
        } catch (Exception e) {
            logger.error("保存信标基站失败", e);
            return renderResult(Global.FALSE, text("保存信标基站失败，请联系管理员！"));
        }
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
     * 根据MAC地址获取基站
     */
    @GetMapping("getByBeaconId")
    @ResponseBody
    @ApiOperation(value = "根据MAC地址获取基站")
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

    /**
     * 查询没有区域ID的信标列表
     *
     * @author Shawn
     * @date 2025-01-14
     */
    @GetMapping("findBeaconsWithoutArea")
    @ResponseBody
    @ApiOperation(value = "获取没有区域ID的信标列表")
    public Map<String, Object> findBeaconsWithoutArea() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SwmBeaconStation> beaconList = swmBeaconStationService.findBeaconsWithoutArea();

            result.put("success", true);
            result.put("data", beaconList);
            result.put("total", beaconList != null ? beaconList.size() : 0);
            result.put("message", "查询成功");

            logger.info("查询没有区域ID的信标成功，共找到 {} 个信标", beaconList != null ? beaconList.size() : 0);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", new ArrayList<>());
            result.put("total", 0);
            result.put("message", "查询失败: " + e.getMessage());
            logger.error("查询没有区域ID的信标失败", e);
        }
        return result;
    }

    /**
     * 根据GPS坐标范围查询基站列表
     */
    @GetMapping("findByGpsRange")
    @ResponseBody
    @ApiOperation(value = "根据GPS坐标范围查询基站列表")
    public List<SwmBeaconStation> findByGpsRange(Double minLongitude, Double maxLongitude,
            Double minLatitude, Double maxLatitude) {
        return swmBeaconStationService.findByGpsRange(minLongitude, maxLongitude, minLatitude, maxLatitude);
    }

    /**
     * 根据图纸像素坐标范围查询基站列表
     */
    @GetMapping("findByPixelRange")
    @ResponseBody
    @ApiOperation(value = "根据图纸像素坐标范围查询基站列表")
    public List<SwmBeaconStation> findByPixelRange(Double minX, Double maxX, Double minY, Double maxY) {
        return swmBeaconStationService.findByPixelRange(minX, maxX, minY, maxY);
    }

    /**
     * 更新基站坐标信息
     */
    @PostMapping("updateCoordinates")
    @ResponseBody
    @ApiOperation(value = "更新基站坐标信息")
    public String updateCoordinates(String id, Double pixelX, Double pixelY,
            Double gpsLongitude, Double gpsLatitude) {
        SwmBeaconStation swmBeaconStation = swmBeaconStationService.get(id);
        if (swmBeaconStation == null) {
            return renderResult(Global.FALSE, text("基站不存在！"));
        }

        swmBeaconStation.setPixelX(pixelX);
        swmBeaconStation.setPixelY(pixelY);
        swmBeaconStation.setGpsLongitude(gpsLongitude);
        swmBeaconStation.setGpsLatitude(gpsLatitude);

        swmBeaconStationService.save(swmBeaconStation);
        return renderResult(Global.TRUE, text("更新基站坐标成功！"));
    }

    /**
     * 根据区域查询基站列表
     */
    @GetMapping("findByArea")
    @ResponseBody
    @ApiOperation(value = "根据区域查询基站列表")
    public List<SwmBeaconStation> findByArea(String area) {
        return swmBeaconStationService.findByArea(area);
    }

    /**
     * 获取所有区域列表
     */
    @GetMapping("getAllAreas")
    @ResponseBody
    @ApiOperation(value = "获取所有区域列表")
    public List<String> getAllAreas() {
        return swmBeaconStationService.findAllAreas();
    }

    /**
     * 更新基站区域信息
     */
    @PostMapping("updateArea")
    @ResponseBody
    @ApiOperation(value = "更新基站区域信息")
    public String updateArea(String id, String area) {
        SwmBeaconStation swmBeaconStation = swmBeaconStationService.get(id);
        if (swmBeaconStation == null) {
            return renderResult(Global.FALSE, text("基站不存在！"));
        }

        swmBeaconStation.setArea(area);
        swmBeaconStationService.save(swmBeaconStation);
        return renderResult(Global.TRUE, text("更新基站区域成功！"));
    }

    /**
     * 根据颜色查询基站列表
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @GetMapping("findByBeaconColor")
    @ResponseBody
    @ApiOperation(value = "根据颜色查询基站列表")
    public List<SwmBeaconStation> findByBeaconColor(String beaconColor) {
        SwmBeaconStation queryParam = new SwmBeaconStation();
        queryParam.setBeaconColor(beaconColor);
        return swmBeaconStationService.findList(queryParam);
    }

    /**
     * 更新基站颜色信息
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping("updateBeaconColor")
    @ResponseBody
    @ApiOperation(value = "更新基站颜色信息")
    public String updateBeaconColor(String id, String beaconColor) {
        SwmBeaconStation swmBeaconStation = swmBeaconStationService.get(id);
        if (swmBeaconStation == null) {
            return renderResult(Global.FALSE, text("基站不存在！"));
        }

        swmBeaconStation.setBeaconColor(beaconColor);
        swmBeaconStationService.save(swmBeaconStation);
        return renderResult(Global.TRUE, text("更新基站颜色成功！"));
    }


    @ApiOperation("模板下载")
    @RequestMapping("/export")
    @ResponseBody
    public String export() throws IOException {
        String name;
        List<SwmBeaconStationExport> list = new ArrayList<>();
        String fileName = "信标管理导入模板.xlsx";
        try (ExcelExport ee = new ExcelExport("信标管理设置", SwmBeaconStationExport.class)) {
            name = ExcelExportUtil.uploadOss(ee.setDataList(list), fileName);
        }
        return renderResult(Global.TRUE, text("成功！"), name);
    }

    @ApiOperation("信标管理excel导入")
    @RequestMapping("/importData")
    @ResponseBody
    public String importData(MultipartFile file) {
        Integer count = swmBeaconStationService.importData(file);
        return renderResult(Global.TRUE, text("数据全部导入成功,共" + count + "条。"));
    }

    /**
     * 获取信标位置信息（用于前端地图显示）
     * 
     * @author System
     * @date 2026-05-13
     * @return 信标位置信息Map，格式为：{"80:ec:cc:d2:3c:28": {"location": "L4", "x": 2134.00, "y": 3127.00}}
     */
    @GetMapping(value = "getBeaconLocationForMap")
    @ResponseBody
    @ApiOperation(value = "获取信标位置信息用于前端地图显示")
    public Map<String, Map<String, Object>> getBeaconLocationForMap() {
        return swmBeaconStationService.getBeaconLocationForMap();
    }
}