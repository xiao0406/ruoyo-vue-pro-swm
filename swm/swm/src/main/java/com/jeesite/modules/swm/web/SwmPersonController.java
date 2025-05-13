/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 人员登记表controller
 * 
 * @author Shawn
 */
@Controller
@RequestMapping(value = "${adminPath}/swmPerson")
public class SwmPersonController extends BaseController {

    @Autowired
    private SwmPersonService swmPersonService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmPerson get(String id, boolean isNewRecord) {
        return swmPersonService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmPerson swmPerson, Model model) {
        model.addAttribute("swmPerson", swmPerson);
        return "modules/swm/swmPersonList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmPerson> listData(SwmPerson swmPerson, HttpServletRequest request, HttpServletResponse response) {
        Page<SwmPerson> page = swmPersonService.findPage(new Page<>(request, response), swmPerson);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    public SwmPerson form(SwmPerson swmPerson, Model model) {
        return swmPerson;
    }

    /**
     * 保存人员登记
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmPerson swmPerson) {
        swmPersonService.save(swmPerson);
        return renderResult(Global.TRUE, text("保存人员登记成功！"));
    }

    /**
     * 删除人员登记
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmPerson swmPerson) {
        swmPersonService.delete(swmPerson);
        return renderResult(Global.TRUE, text("删除人员登记成功！"));
    }

    /**
     * 批量删除人员登记
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmPerson swmPerson = swmPersonService.get(id);
            if (swmPerson != null) {
                swmPersonService.delete(swmPerson);
            }
        }
        return renderResult(Global.TRUE, text("删除人员登记成功！"));
    }
}