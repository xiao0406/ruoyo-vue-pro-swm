/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPersonDeparture;
import com.jeesite.modules.swm.service.SwmPersonDepartureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 离职登记表controller
 * 
 * @author Shawn
 */
@Controller
@RequestMapping(value = "${adminPath}/swmPersonDeparture")
public class SwmPersonDepartureController extends BaseController {

    @Autowired
    private SwmPersonDepartureService swmPersonDepartureService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmPersonDeparture get(String id, boolean isNewRecord) {
        return swmPersonDepartureService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmPersonDeparture swmPersonDeparture, Model model) {
        model.addAttribute("swmPersonDeparture", swmPersonDeparture);
        return "modules/swm/swmPersonDepartureList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmPersonDeparture> listData(SwmPersonDeparture swmPersonDeparture, HttpServletRequest request,
            HttpServletResponse response) {
        Page<SwmPersonDeparture> page = swmPersonDepartureService.findPage(new Page<>(request, response),
                swmPersonDeparture);
        return page;
    }

    /**
     * 处理枚举文本显示
     * 
     * @param departure 离职记录
     */
    private void handleEnumTextDisplay(SwmPersonDeparture departure) {
        // 添加枚举显示文本
        if (departure.getHelmetReturned() != null) {
            if ("1".equals(departure.getHelmetReturned())) {
                departure.setHelmetReturnedText("是");
            } else if ("0".equals(departure.getHelmetReturned())) {
                departure.setHelmetReturnedText("否");
            }
        }

        if (departure.getDepartureType() != null) {
            if ("1".equals(departure.getDepartureType())) {
                departure.setDepartureTypeText("正常离职");
            } else if ("0".equals(departure.getDepartureType())) {
                departure.setDepartureTypeText("异常离职");
            }
        }
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    public Map<String, Object> form(SwmPersonDeparture swmPersonDeparture, Model model) {
        Map<String, Object> result = new HashMap<>();
        if (swmPersonDeparture != null) {
            SwmPersonDeparture departure = swmPersonDepartureService.get(swmPersonDeparture.getId());
            if (departure != null) {
                // 处理枚举文本
                handleEnumTextDisplay(departure);

                Map<String, Object> personData = new HashMap<>();
                // 复制基本属性
                personData.put("id", departure.getId());
                personData.put("name", departure.getName());
                personData.put("personType", departure.getPersonType());
                personData.put("gender", departure.getGender());
                personData.put("company", departure.getCompany());
                personData.put("department", departure.getDepartment());
                personData.put("workProcess", departure.getWorkProcess());
                personData.put("team", departure.getTeam());
                personData.put("jobType", departure.getJobType());
                personData.put("safetyHelmetId", departure.getSafetyHelmetId());
                personData.put("identityCard", departure.getIdentityCard());
                personData.put("phoneNumber", departure.getPhoneNumber());
                personData.put("remarks", departure.getRemarks());

                // 处理离职相关信息
                personData.put("personnelStatus", departure.getPersonnelStatus());
                personData.put("helmetReturned", departure.getHelmetReturned());
                personData.put("helmetReturnedText", departure.getHelmetReturnedText());
                personData.put("departureType", departure.getDepartureType());
                personData.put("departureTypeText", departure.getDepartureTypeText());
                personData.put("departureReason", departure.getDepartureReason());
                personData.put("departureDate", departure.getDepartureDate());

                result.putAll(personData);
            }
        }
        return result;
    }

    /**
     * 保存离职记录
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmPersonDeparture swmPersonDeparture) {
        swmPersonDepartureService.save(swmPersonDeparture);
        return renderResult(Global.TRUE, text("保存离职记录成功！"));
    }

    /**
     * 删除离职记录
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmPersonDeparture swmPersonDeparture) {
        swmPersonDepartureService.delete(swmPersonDeparture);
        return renderResult(Global.TRUE, text("删除离职记录成功！"));
    }

    /**
     * 批量删除离职记录
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmPersonDeparture swmPersonDeparture = swmPersonDepartureService.get(id);
            if (swmPersonDeparture != null) {
                swmPersonDepartureService.delete(swmPersonDeparture);
            }
        }
        return renderResult(Global.TRUE, text("删除离职记录成功！"));
    }

    /**
     * 根据身份证查询离职记录
     */
    @GetMapping(value = "findByIdentityCard")
    @ResponseBody
    public Map<String, Object> findByIdentityCard(@RequestParam("identityCard") String identityCard) {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonDeparture query = new SwmPersonDeparture();
            query.setIdentityCard(identityCard);

            // 添加分页参数，不限制记录数
            Page<SwmPersonDeparture> page = new Page<>(1, 100);
            page = swmPersonDepartureService.findPage(page, query);

            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                result.put("success", true);
                result.put("hasRecord", true);
                result.put("data", page.getList());
                result.put("message", "查询到离职记录信息");
            } else {
                result.put("success", true);
                result.put("hasRecord", false);
                result.put("message", "未查询到离职记录信息");
            }
        } catch (Exception e) {
            logger.error("查询离职记录信息异常", e);
            result.put("success", false);
            result.put("message", "查询离职记录信息失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据身份证查询离职记录（不分页）
     */
    @GetMapping(value = "findListByIdentityCard")
    @ResponseBody
    public Map<String, Object> findListByIdentityCard(@RequestParam("identityCard") String identityCard) {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonDeparture query = new SwmPersonDeparture();
            query.setIdentityCard(identityCard);
            query.setStatus("0"); // 正常状态

            // 直接查询列表，不使用分页
            List<SwmPersonDeparture> list = swmPersonDepartureService.findList(query);

            if (list != null && !list.isEmpty()) {
                // 处理枚举文本
                for (SwmPersonDeparture departure : list) {
                    handleEnumTextDisplay(departure);
                }

                result.put("success", true);
                result.put("hasRecord", true);
                result.put("data", list);
                result.put("message", "查询到离职记录信息");
            } else {
                result.put("success", true);
                result.put("hasRecord", false);
                result.put("message", "未查询到离职记录信息");
            }
        } catch (Exception e) {
            logger.error("查询离职记录信息异常", e);
            result.put("success", false);
            result.put("message", "查询离职记录信息失败：" + e.getMessage());
        }

        return result;
    }
}