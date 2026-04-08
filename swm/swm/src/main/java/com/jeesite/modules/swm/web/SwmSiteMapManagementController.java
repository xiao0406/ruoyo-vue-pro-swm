package com.jeesite.modules.swm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import com.jeesite.modules.swm.service.SwmSiteMapManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 场地底图管理表Controller
 * 
 * @author zwf
 * @version 2025-05-30
 */
@Controller
@RequestMapping(value = "${adminPath}/swmSiteMapManagement")
public class SwmSiteMapManagementController extends BaseController {

    @Autowired
    private SwmSiteMapManagementService swmSiteMapManagementService;

    @Value("${JIAI.url}")
    private String jiaiUrl;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmSiteMapManagement get(String id, boolean isNewRecord) {
        return swmSiteMapManagementService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmSiteMapManagement swmSiteMapManagement, Model model) {
        model.addAttribute("swmSiteMapManagement", swmSiteMapManagement);
        return "modules/swm/swmSiteMapManagementList";
    }

    /**
     * 查询列表数据
     * @author Shawn @date 2026-04-07 新增 hasChildren 字段填充
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmSiteMapManagement> listData(SwmSiteMapManagement swmSiteMapManagement, HttpServletRequest request,
            HttpServletResponse response) {
        swmSiteMapManagement.setPage(new Page<>(request, response));
        // 完全移除状态过滤条件
        swmSiteMapManagement.setStatus(null);
        // 设置为不使用全局状态过滤
        swmSiteMapManagement.getSqlMap().getWhere().disableAutoAddStatusWhere();
        Page<SwmSiteMapManagement> page = swmSiteMapManagementService.findPage(swmSiteMapManagement);
        // 为每条记录填充 hasChildren，告诉前端该节点下面是否还有子节点
        swmSiteMapManagementService.fillHasChildren(page.getList());
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmSiteMapManagement swmSiteMapManagement, Model model) {
        model.addAttribute("swmSiteMapManagement", swmSiteMapManagement);
        return "modules/swm/swmSiteMapManagementForm";
    }

    /**
     * 查询子节点列表（懒加载）
     * 前端点击一个节点时调用，返回该节点下的直接子节点
     * 首次进页面传 parentId=0 查出所有厂区
     * @param parentId 父节点ID
     * @return 子节点列表，每个节点带 hasChildren 标记
     * @author Shawn @date 2026-04-02
     */
    @RequestMapping(value = "children", method = RequestMethod.GET)
    @ResponseBody
    public List<SwmSiteMapManagement> children(
            @RequestParam(value = "parentId", defaultValue = "0") String parentId) {
        // 查询该父节点下的直接子节点，Service层会自动填充hasChildren
        return swmSiteMapManagementService.findChildren(parentId);
    }

    /**
     * 获取节点详情
     * 前端点击树节点时，在右侧展示完整信息
     * @param id 节点ID
     * @return 节点完整数据
     * @author Shawn @date 2026-04-02
     */
    @RequestMapping(value = "detail", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> detail(@RequestParam String id) {
        Map<String, Object> result = new HashMap<>();

        // 根据ID查询完整数据
        SwmSiteMapManagement map = swmSiteMapManagementService.get(id);
        if (map == null) {
            result.put("success", false);
            result.put("message", "未找到该节点数据");
            return result;
        }

        result.put("success", true);
        result.put("data", map);
        return result;
    }

    /**
     * 保存场地底图管理
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmSiteMapManagement swmSiteMapManagement) {
        swmSiteMapManagementService.save(swmSiteMapManagement);
        return renderResult(Global.TRUE, text("保存场地底图管理成功！"));
    }

    /**
     * 删除场地底图管理
     * 如果该节点下还有子节点，会拒绝删除
     */
    @DeleteMapping(value = "delete")
    @ResponseBody
    public String delete(SwmSiteMapManagement swmSiteMapManagement) {
        try {
            swmSiteMapManagementService.delete(swmSiteMapManagement);
            return renderResult(Global.TRUE, text("删除场地底图管理成功！"));
        } catch (RuntimeException e) {
            // 捕获子节点校验异常，返回友好提示
            return renderResult(Global.FALSE, text(e.getMessage()));
        }
    }

    /**
     * 删除场地底图管理（JSON方式）
     */
    @RequestMapping(value = "deleteJson", method = { RequestMethod.DELETE, RequestMethod.POST })
    @ResponseBody
    public String deleteJson(@RequestBody(required = false) Map<String, Object> params,
            @RequestParam(required = false) String id, HttpServletRequest request) {
        // 尝试从JSON请求体获取ID
        if (params != null && params.get("id") != null) {
            id = params.get("id").toString();
        }

        // 如果JSON中没有ID，尝试从URL参数获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
        }

        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("删除失败：ID不能为空！"));
        }

        try {
            SwmSiteMapManagement swmSiteMapManagement = new SwmSiteMapManagement(id);
            swmSiteMapManagementService.delete(swmSiteMapManagement);
            return renderResult(Global.TRUE, text("删除场地底图管理成功！"));
        } catch (RuntimeException e) {
            // 捕获子节点校验异常，返回友好提示
            return renderResult(Global.FALSE, text(e.getMessage()));
        }
    }

    /**
     * 批量删除场地底图管理
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmSiteMapManagement swmSiteMapManagement = swmSiteMapManagementService.get(id);
            if (swmSiteMapManagement != null) {
                swmSiteMapManagementService.delete(swmSiteMapManagement);
            }
        }
        return renderResult(Global.TRUE, text("批量删除场地底图管理成功！"));
    }

    /**
     * 获取场地底图管理列表（用于下拉选择）
     */
    @RequestMapping(value = "getMapList")
    @ResponseBody
    public List<Map<String, Object>> getMapList() {
        List<Map<String, Object>> result = new ArrayList<>();

        List<SwmSiteMapManagement> mapList = swmSiteMapManagementService.findList(new SwmSiteMapManagement());
        for (SwmSiteMapManagement map : mapList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", map.getId());
            item.put("mapName", map.getMapName());
            item.put("projectId", map.getProjectId());
            item.put("filePath", map.getFilePath());

            result.add(item);
        }

        return result;
    }

    /**
     * 启用底图
     */
    @RequestMapping(value = "enable", method = { RequestMethod.POST })
    @ResponseBody
    public String enable(@RequestParam(required = false) String id,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        // 如果参数中没有ID，尝试从请求中获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
        }

        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("操作失败：ID不能为空！"));
        }

        try {
            // 调用服务层方法启用底图（启用该底图同时会禁用其他底图）
            swmSiteMapManagementService.changeStatus(id, "0");
            return renderResult(Global.TRUE, text("启用场地底图成功！"));
        } catch (Exception e) {
            return renderResult(Global.FALSE, text("操作失败：" + e.getMessage()));
        }
    }

    /**
     * 禁用底图
     */
    @RequestMapping(value = "disable", method = { RequestMethod.POST })
    @ResponseBody
    public String disable(@RequestParam(required = false) String id,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 如果参数中没有ID，尝试从请求中获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
        }

        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("操作失败：ID不能为空！"));
        }

        try {
            // 调用服务层方法禁用底图
            swmSiteMapManagementService.changeStatus(id, "1");
            return renderResult(Global.TRUE, text("禁用场地底图成功！"));
        } catch (Exception e) {
            return renderResult(Global.FALSE, text("操作失败：" + e.getMessage()));
        }
    }

    /**
     * 获取已启用底图
     * 
     * @author Shawn
     * @date 2025-01-14
     */
    @RequestMapping(value = "getEnabledMap")
    @ResponseBody
    public Map<String, Object> getEnabledMap() {
        Map<String, Object> result = new HashMap<>();

        // 查询系统中启用的底图
        SwmSiteMapManagement query = new SwmSiteMapManagement();
        query.setStatus("0"); // 0表示启用
        List<SwmSiteMapManagement> maps = swmSiteMapManagementService.findList(query);

        if (maps != null && !maps.isEmpty()) {
            SwmSiteMapManagement map = maps.get(0);

            // 创建底图数据对象
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("id", map.getId());
            mapData.put("mapName", map.getMapName());
            mapData.put("projectId", map.getProjectId());
            mapData.put("filePath", map.getFilePath());
            mapData.put("mapSize", map.getMapSize());
            mapData.put("scale", map.getScale());
            mapData.put("is3d", map.getIs3d());

            // 解析filePath中的JSON字符串获取URL
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> filePathJson = objectMapper.readValue(map.getFilePath(), Map.class);
                String url = (String) filePathJson.get("url");
                mapData.put("url", url);
            } catch (Exception e) {
                // 如果解析JSON失败，返回原始filePath
                mapData.put("url", map.getFilePath());
            }

            result.put("success", true);
            result.put("data", mapData);
            result.put("message", "获取底图成功");
        } else {
            result.put("success", false);
            result.put("data", null);
            result.put("message", "系统中没有启用的底图");
        }

        return result;
    }

    /**
     * 获取建筑和楼层选项列表（用于信标所属建筑/楼层下拉）
     *
     * @author Shawn @date 2026-04-07
     */
    @RequestMapping(value = "getBuildingFloorOptions", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getBuildingFloorOptions() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 直接返回前端可用的 options 结构，保持和所属区域下拉接口风格一致。
            List<Map<String, Object>> options = swmSiteMapManagementService.getBuildingFloorOptions();
            result.put("success", true);
            result.put("options", options);
            result.put("total", options.size());
            result.put("message", "获取建筑楼层选项成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("options", new ArrayList<>());
            result.put("message", "获取建筑楼层选项失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 
     * 
     * @author Shawn
     * @date 2025-06-09
     *       蓝牙 iBeacon 定位对接说明
     *       GET api/MapInfo/GetDetailMapInfo
     */
    @RequestMapping(value = "getThirdPartyMapInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getThirdPartyMapInfo() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 第三方API配置
            String apiUrl = jiaiUrl + "/api/MapInfo/GetDetailMapInfo";
            String encodedCredentials = "VXJhZGlvOlVyQGRpbzIwMTg=";

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);
            headers.set("Content-Type", "application/json");

            // 创建HTTP实体
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发起HTTP请求
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            // 处理响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && Boolean.TRUE.equals(responseBody.get("Success"))) {
                    result.put("success", true);
                    result.put("data", responseBody);
                    result.put("message", "获取第三方底图信息成功");
                } else {
                    result.put("success", false);
                    result.put("data", null);
                    result.put("message", "第三方API返回失败：" +
                            (responseBody != null ? responseBody.get("ErrorText") : "未知错误"));
                }
            } else {
                result.put("success", false);
                result.put("data", null);
                result.put("message", "请求失败，HTTP状态码：" + response.getStatusCode());
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("message", "调用第三方API异常：" + e.getMessage());
        }

        return result;
    }
}
