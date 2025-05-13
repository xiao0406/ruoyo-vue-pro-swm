/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.excel.SwmPersonExcelModel;
import com.jeesite.modules.swm.excel.SwmPersonImportListener;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.utils.BatchOperationsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 下载导入人员Excel模板
     */
    @GetMapping(value = "importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("人员信息导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 创建模板并写入到响应流
        EasyExcel.write(response.getOutputStream(), SwmPersonExcelModel.class)
                .sheet("人员信息")
                .doWrite(new ArrayList<>());
    }

    /**
     * 导入人员Excel
     */
    @PostMapping(value = "importExcel")
    @ResponseBody
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择文件上传");
            return result;
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 创建Excel读取监听器
            SwmPersonImportListener listener = new SwmPersonImportListener(swmPersonService);

            // 读取Excel
            ExcelReader excelReader = EasyExcel.read(inputStream, SwmPersonExcelModel.class, listener).build();
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);
            excelReader.finish();

            // 获取结果
            List<SwmPerson> successList = listener.getSuccessList();
            List<SwmPersonExcelModel> errorList = listener.getErrorList();

            // 返回导入结果
            result.put("success", true);
            result.put("total", listener.getTotal());
            result.put("successCount", successList.size());
            result.put("errorCount", errorList.size());
            result.put("message", "导入成功" + successList.size() + "条，失败" + errorList.size() + "条");

        } catch (Exception e) {
            logger.error("导入Excel异常", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量保存人员
     */
    @PostMapping(value = "batchSave")
    @ResponseBody
    public String batchSave(@RequestBody List<SwmPerson> personList) {
        if (personList == null || personList.isEmpty()) {
            return renderResult(Global.FALSE, text("保存数据为空"));
        }

        try {
            // 分批处理，每次处理100条
            List<List<SwmPerson>> batchList = BatchOperationsUtil.batchCutting(personList, 100);

            for (List<SwmPerson> batch : batchList) {
                for (SwmPerson person : batch) {
                    swmPersonService.save(person);
                }
            }

            return renderResult(Global.TRUE, text("批量保存人员成功！共" + personList.size() + "条"));
        } catch (Exception e) {
            logger.error("批量保存人员异常", e);
            return renderResult(Global.FALSE, text("批量保存人员失败：" + e.getMessage()));
        }
    }
}