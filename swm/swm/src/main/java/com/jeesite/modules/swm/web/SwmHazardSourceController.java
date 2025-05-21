/**
 * 危险源信息Controller
 * @author Shawn
 * @version 2025-05-20
 */
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.service.SwmHazardSourceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 危险源信息Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/swmHazardSource")
@Api(value = "危险源管理接口")
public class SwmHazardSourceController extends BaseController {

    @Autowired
    private SwmHazardSourceService swmHazardSourceService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmHazardSource get(String id, boolean isNewRecord) {
        return swmHazardSourceService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmHazardSource swmHazardSource, Model model) {
        model.addAttribute("swmHazardSource", swmHazardSource);
        return "modules/swm/swmHazardSourceList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation(value = "获取危险源列表")
    public Page<SwmHazardSource> listData(SwmHazardSource swmHazardSource, HttpServletRequest request,
            HttpServletResponse response) {
        swmHazardSource.setPage(new Page<>(request, response));
        Page<SwmHazardSource> page = swmHazardSourceService.findPage(swmHazardSource);

        // 手动处理字典数据
        for (SwmHazardSource item : page.getList()) {
            // 危险源类别
            if ("0".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("火灾风险");
            } else if ("1".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("高处坠落风险");
            } else if ("2".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("物体打击风险");
            } else if ("3".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("触电风险");
            } else if ("4".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("机械伤害风险");
            } else if ("5".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("化学品泄漏风险");
            } else if ("99".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("其他风险");
            }

            // 是否加入巡检
            if ("0".equals(item.getIsPatrolIncluded())) {
                item.setIsPatrolIncludedText("否");
            } else if ("1".equals(item.getIsPatrolIncluded())) {
                item.setIsPatrolIncludedText("是");
            }

            // 危险源状态
            if ("0".equals(item.getHazardStatus())) {
                item.setHazardStatusText("待处理");
            } else if ("1".equals(item.getHazardStatus())) {
                item.setHazardStatusText("处理中");
            } else if ("2".equals(item.getHazardStatus())) {
                item.setHazardStatusText("已关闭");
            } else if ("3".equals(item.getHazardStatus())) {
                item.setHazardStatusText("已忽略");
            }
        }

        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation(value = "获取危险源详情")
    public Map<String, Object> form(SwmHazardSource swmHazardSource) {
        Map<String, Object> result = new HashMap<>();
        if (swmHazardSource != null) {
            Map<String, Object> data = new HashMap<>();
            // 复制基本属性
            data.put("id", swmHazardSource.getId());
            data.put("hazardName", swmHazardSource.getHazardName());
            data.put("hazardCategory", swmHazardSource.getHazardCategory());
            data.put("location", swmHazardSource.getLocation());
            data.put("beaconIdentifier", swmHazardSource.getBeaconIdentifier());
            data.put("isPatrolIncluded", swmHazardSource.getIsPatrolIncluded());
            data.put("patrolRecordSummary", swmHazardSource.getPatrolRecordSummary());
            data.put("registrationTime", swmHazardSource.getRegistrationTime());
            data.put("hazardStatus", swmHazardSource.getHazardStatus());
            data.put("remarks", swmHazardSource.getRemarks());

            // 手动设置字典文本
            // 危险源类别
            if ("0".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "火灾风险");
            } else if ("1".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "高处坠落风险");
            } else if ("2".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "物体打击风险");
            } else if ("3".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "触电风险");
            } else if ("4".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "机械伤害风险");
            } else if ("5".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "化学品泄漏风险");
            } else if ("99".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "其他风险");
            }

            // 是否加入巡检
            if ("0".equals(swmHazardSource.getIsPatrolIncluded())) {
                data.put("isPatrolIncludedText", "否");
            } else if ("1".equals(swmHazardSource.getIsPatrolIncluded())) {
                data.put("isPatrolIncludedText", "是");
            }

            // 危险源状态
            if ("0".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "待处理");
            } else if ("1".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "处理中");
            } else if ("2".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "已关闭");
            } else if ("3".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "已忽略");
            }

            result.putAll(data);
        }
        return result;
    }

    /**
     * 保存危险源信息
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation(value = "保存危险源")
    public String save(@Validated SwmHazardSource swmHazardSource) {
        swmHazardSourceService.save(swmHazardSource);
        return renderResult(Global.TRUE, text("保存危险源信息成功！"));
    }

    /**
     * 删除危险源信息
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation(value = "删除危险源")
    public String delete(SwmHazardSource swmHazardSource) {
        swmHazardSourceService.delete(swmHazardSource);
        return renderResult(Global.TRUE, text("删除危险源信息成功！"));
    }

    /**
     * 根据信标编号查询危险源
     */
    @GetMapping("findByBeacon")
    @ResponseBody
    @ApiOperation(value = "根据信标编号查询危险源")
    public SwmHazardSource findByBeacon(String beaconIdentifier) {
        SwmHazardSource hazardSource = new SwmHazardSource();
        hazardSource.setBeaconIdentifier(beaconIdentifier);
        return swmHazardSourceService.findList(hazardSource).isEmpty() ? null
                : swmHazardSourceService.findList(hazardSource).get(0);
    }
}