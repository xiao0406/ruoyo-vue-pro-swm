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
        // 处理枚举显示值
        for (SwmPerson person : page.getList()) {
            handleEnumTextDisplay(person);
        }
        return page;
    }

    /**
     * 处理枚举显示值
     */
    private void handleEnumTextDisplay(SwmPerson person) {
        if (person.getPersonnelStatus() != null) {
            person.setPersonnelStatus(person.getPersonnelStatusText());
        }
        if (person.getSafetyEducation() != null) {
            person.setSafetyEducation(person.getSafetyEducationText());
        }
        // 处理新增字段的显示
        if (person.getHelmetReturned() != null) {
            person.setHelmetReturned(person.getHelmetReturnedText());
        }
        if (person.getDepartureType() != null) {
            person.setDepartureType(person.getDepartureTypeText());
        }
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    public Map<String, Object> form(SwmPerson swmPerson, Model model) {
        Map<String, Object> result = new HashMap<>();
        if (swmPerson != null) {
            Map<String, Object> personData = new HashMap<>();
            // 复制基本属性
            personData.put("id", swmPerson.getId());
            personData.put("name", swmPerson.getName());
            personData.put("personType", swmPerson.getPersonType());
            personData.put("gender", swmPerson.getGender());
            personData.put("company", swmPerson.getCompany());
            personData.put("department", swmPerson.getDepartment());
            personData.put("workProcess", swmPerson.getWorkProcess());
            personData.put("team", swmPerson.getTeam());
            personData.put("jobType", swmPerson.getJobType());
            personData.put("safetyHelmetId", swmPerson.getSafetyHelmetId());
            personData.put("identityCard", swmPerson.getIdentityCard());
            personData.put("phoneNumber", swmPerson.getPhoneNumber());
            personData.put("remarks", swmPerson.getRemarks());

            // 处理枚举值
            personData.put("personnelStatus", swmPerson.getPersonnelStatus());
            personData.put("personnelStatusText", swmPerson.getPersonnelStatusText());
            personData.put("safetyEducation", swmPerson.getSafetyEducation());
            personData.put("safetyEducationText", swmPerson.getSafetyEducationText());

            // 添加新字段
            personData.put("helmetReturned", swmPerson.getHelmetReturned());
            personData.put("helmetReturnedText", swmPerson.getHelmetReturnedText());
            personData.put("departureType", swmPerson.getDepartureType());
            personData.put("departureTypeText", swmPerson.getDepartureTypeText());
            personData.put("departureReason", swmPerson.getDepartureReason());

            result.putAll(personData);
        }
        return result;
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

    /**
     * 获取人员状态和安全教育枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 人员状态选项
        Map<String, String> personnelStatusOptions = new HashMap<>();
        personnelStatusOptions.put(SwmPerson.PersonStatusEnum.ACTIVE,
                SwmPerson.PersonStatusEnum.getText(SwmPerson.PersonStatusEnum.ACTIVE));
        personnelStatusOptions.put(SwmPerson.PersonStatusEnum.INACTIVE,
                SwmPerson.PersonStatusEnum.getText(SwmPerson.PersonStatusEnum.INACTIVE));
        result.put("personnelStatus", personnelStatusOptions);

        // 安全教育选项
        Map<String, String> safetyEducationOptions = new HashMap<>();
        safetyEducationOptions.put(SwmPerson.SafetyEducationEnum.NOT_STARTED,
                SwmPerson.SafetyEducationEnum.getText(SwmPerson.SafetyEducationEnum.NOT_STARTED));
        safetyEducationOptions.put(SwmPerson.SafetyEducationEnum.COMPLETED,
                SwmPerson.SafetyEducationEnum.getText(SwmPerson.SafetyEducationEnum.COMPLETED));
        result.put("safetyEducation", safetyEducationOptions);

        // 是否归还安全帽选项
        Map<String, String> helmetReturnedOptions = new HashMap<>();
        helmetReturnedOptions.put(SwmPerson.HelmetReturnedEnum.YES,
                SwmPerson.HelmetReturnedEnum.getText(SwmPerson.HelmetReturnedEnum.YES));
        helmetReturnedOptions.put(SwmPerson.HelmetReturnedEnum.NO,
                SwmPerson.HelmetReturnedEnum.getText(SwmPerson.HelmetReturnedEnum.NO));
        result.put("helmetReturned", helmetReturnedOptions);

        // 离职类型选项
        Map<String, String> departureTypeOptions = new HashMap<>();
        departureTypeOptions.put(SwmPerson.DepartureTypeEnum.NORMAL,
                SwmPerson.DepartureTypeEnum.getText(SwmPerson.DepartureTypeEnum.NORMAL));
        departureTypeOptions.put(SwmPerson.DepartureTypeEnum.ABNORMAL,
                SwmPerson.DepartureTypeEnum.getText(SwmPerson.DepartureTypeEnum.ABNORMAL));
        result.put("departureType", departureTypeOptions);

        return result;
    }

    /**
     * 处理人员离职
     */
    @PostMapping(value = "handleDeparture", consumes = "application/json")
    @ResponseBody
    public String handleDeparture(@RequestBody Map<String, Object> params) {
        try {
            String personId = (String) params.get("id");
            String helmetReturned = (String) params.get("helmetReturned");
            String departureType = (String) params.get("departureType");
            String departureReason = (String) params.get("departureReason");

            logger.info("处理人员离职: personId={}, helmetReturned={}, departureType={}, departureReason={}",
                    personId, helmetReturned, departureType, departureReason);

            // 获取人员信息
            SwmPerson swmPerson = swmPersonService.get(personId);
            if (swmPerson == null) {
                return renderResult(Global.FALSE, text("人员不存在"));
            }

            // 设置离职相关信息
            swmPerson.setPersonnelStatus(SwmPerson.PersonStatusEnum.INACTIVE); // 设置为离职状态
            swmPerson.setHelmetReturned(helmetReturned);
            swmPerson.setDepartureType(departureType);
            swmPerson.setDepartureReason(departureReason);

            // 保存更新
            swmPersonService.save(swmPerson);

            return renderResult(Global.TRUE, text("人员离职处理成功"));
        } catch (Exception e) {
            logger.error("处理人员离职异常", e);
            return renderResult(Global.FALSE, text("处理人员离职失败：" + e.getMessage()));
        }
    }
}