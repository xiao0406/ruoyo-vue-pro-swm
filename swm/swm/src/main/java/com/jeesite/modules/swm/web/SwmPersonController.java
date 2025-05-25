/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonDeparture;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.excel.SwmPersonExcelModel;
import com.jeesite.modules.swm.excel.SwmPersonImportListener;
import com.jeesite.modules.swm.service.SwmPersonDepartureService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.utils.BatchOperationsUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Date;
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

    @Autowired
    private SwmPersonDepartureService swmPersonDepartureService;

    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;

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
    public Map<String, Object> listData(SwmPerson swmPerson, HttpServletRequest request, HttpServletResponse response) {
        Page<SwmPerson> page = swmPersonService.findPage(new Page<>(request, response), swmPerson);

        // 构建包含额外字段的响应数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> enhancedList = new ArrayList<>();

        // 处理每个Person对象，添加枚举的文本显示
        for (SwmPerson person : page.getList()) {
            Map<String, Object> personMap = new HashMap<>();

            // 复制原始对象的所有属性
            personMap.put("id", person.getId());
            personMap.put("createBy", person.getCreateBy());
            personMap.put("updateDate", person.getUpdateDate());
            personMap.put("updateBy", person.getUpdateBy());
            personMap.put("status", person.getStatus());
            personMap.put("createDate", person.getCreateDate());
            personMap.put("remarks", person.getRemarks());
            personMap.put("name", person.getName());
            personMap.put("personType", person.getPersonType());
            personMap.put("gender", person.getGender());
            personMap.put("company", person.getCompany());
            personMap.put("department", person.getDepartment());
            personMap.put("workProcess", person.getWorkProcess());
            personMap.put("team", person.getTeam());
            personMap.put("jobType", person.getJobType());
            personMap.put("safetyHelmetId", person.getSafetyHelmetId());
            personMap.put("personnelStatus", person.getPersonnelStatus());
            personMap.put("safetyEducation", person.getSafetyEducation());
            personMap.put("identityCard", person.getIdentityCard());
            personMap.put("phoneNumber", person.getPhoneNumber());
            personMap.put("helmetReturned", person.getHelmetReturned());
            personMap.put("departureType", person.getDepartureType());
            personMap.put("departureReason", person.getDepartureReason());
            personMap.put("departureDate", person.getDepartureDate());
            personMap.put("isNewRecord", person.getIsNewRecord());

            // 添加枚举文本显示值
            personMap.put("personnelStatusText", person.getPersonnelStatusText());
            personMap.put("safetyEducationText", person.getSafetyEducationText());
            personMap.put("helmetReturnedText", person.getHelmetReturnedText());
            personMap.put("departureTypeText", person.getDepartureTypeText());

            // 添加到列表
            enhancedList.add(personMap);
        }

        // 构建分页结果
        result.put("list", enhancedList);
        result.put("count", page.getCount());
        result.put("pageNo", page.getPageNo());
        result.put("pageSize", page.getPageSize());

        return result;
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
            personData.put("departureDate", swmPerson.getDepartureDate()); // 添加离职时间

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
            // 先读取Excel数据进行预处理和检查
            List<SwmPersonExcelModel> excelData = new ArrayList<>();
            EasyExcel.read(inputStream, SwmPersonExcelModel.class, new AnalysisEventListener<SwmPersonExcelModel>() {
                @Override
                public void invoke(SwmPersonExcelModel data, AnalysisContext context) {
                    excelData.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().doRead();

            // 检查身份证重复的在职人员
            List<String> duplicateIdentityCards = new ArrayList<>();
            Map<String, String> duplicatePersons = new HashMap<>();

            for (SwmPersonExcelModel model : excelData) {
                // 只检查具有身份证号的数据
                if (StringUtils.isNotBlank(model.getIdentityCard())) {
                    // 所有导入的人员都是在职状态
                    boolean isActive = true;

                    if (isActive) {
                        // 查询数据库中是否已存在相同身份证的在职人员
                        SwmPerson existingPerson = swmPersonService.getByIdentityCard(model.getIdentityCard());
                        if (existingPerson != null
                                && SwmPerson.PersonStatusEnum.ACTIVE.equals(existingPerson.getPersonnelStatus())) {
                            duplicateIdentityCards.add(model.getIdentityCard());
                            duplicatePersons.put(model.getIdentityCard(),
                                    String.format("姓名: %s, 身份证: %s", existingPerson.getName(),
                                            existingPerson.getIdentityCard()));
                        }
                    }
                }
            }

            // 如果存在重复的在职人员身份证，返回错误
            if (!duplicateIdentityCards.isEmpty()) {
                result.put("success", false);
                result.put("hasDuplicates", true);
                result.put("duplicateIdentityCards", duplicateIdentityCards);
                result.put("duplicatePersons", duplicatePersons);

                StringBuilder message = new StringBuilder("导入失败：存在相同身份证的在职人员，请检查以下身份证：");
                for (String key : duplicatePersons.keySet()) {
                    message.append("\n").append(duplicatePersons.get(key));
                }
                result.put("message", message.toString());

                return result;
            }

            // 没有重复，继续导入过程
            // 重新打开文件流进行实际导入
            try (InputStream secondInputStream = file.getInputStream()) {
                // 创建Excel读取监听器
                SwmPersonImportListener listener = new SwmPersonImportListener(swmPersonService);

                // 读取Excel
                ExcelReader excelReader = EasyExcel.read(secondInputStream, SwmPersonExcelModel.class, listener)
                        .build();
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
            }
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
            swmPerson.setDepartureDate(new Date()); // 设置为当前的服务器时间

            // 保存更新人员表记录
            swmPersonService.save(swmPerson);

            // 创建并保存离职记录到离职表，这是必须成功的步骤
            if (swmPersonDepartureService == null) {
                logger.error("swmPersonDepartureService为空，无法创建离职记录");
                return renderResult(Global.FALSE, text("离职记录服务异常，请联系管理员"));
            }

            // 创建离职记录并保存
            SwmPersonDeparture departure = new SwmPersonDeparture();

            // 复制基本信息
            departure.setName(swmPerson.getName());
            departure.setPersonType(swmPerson.getPersonType());
            departure.setGender(swmPerson.getGender());
            departure.setCompany(swmPerson.getCompany());
            departure.setDepartment(swmPerson.getDepartment());
            departure.setWorkProcess(swmPerson.getWorkProcess());
            departure.setTeam(swmPerson.getTeam());
            departure.setJobType(swmPerson.getJobType());
            departure.setSafetyHelmetId(swmPerson.getSafetyHelmetId());
            departure.setSafetyEducation(swmPerson.getSafetyEducation());
            departure.setIdentityCard(swmPerson.getIdentityCard());
            departure.setPhoneNumber(swmPerson.getPhoneNumber());

            // 设置离职相关信息
            departure.setPersonnelStatus(swmPerson.getPersonnelStatus());
            departure.setHelmetReturned(swmPerson.getHelmetReturned());
            departure.setDepartureType(swmPerson.getDepartureType());
            departure.setDepartureReason(swmPerson.getDepartureReason());
            departure.setDepartureDate(swmPerson.getDepartureDate());
            departure.setRemarks(swmPerson.getRemarks());

            // 保存离职记录
            swmPersonDepartureService.save(departure);

            if (departure.getId() == null || departure.getId().isEmpty()) {
                logger.error("保存离职记录失败，人员ID：{}", personId);
                return renderResult(Global.FALSE, text("保存离职记录失败，请重试"));
            }

            logger.info("成功创建离职记录，ID：{}, 姓名：{}", departure.getId(), departure.getName());
            return renderResult(Global.TRUE, text("人员离职处理成功"));
        } catch (Exception e) {
            logger.error("处理人员离职异常", e);
            return renderResult(Global.FALSE, text("处理人员离职失败：" + e.getMessage()));
        }
    }

    /**
     * 根据身份证查询离职人员
     */
    @GetMapping(value = "findDepartedByIdentityCard")
    @ResponseBody
    public Map<String, Object> findDepartedByIdentityCard(@RequestParam("identityCard") String identityCard) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 优先从离职表查询数据
            SwmPersonDeparture query = new SwmPersonDeparture();
            query.setIdentityCard(identityCard);
            List<SwmPersonDeparture> departureList = swmPersonDepartureService.findList(query);

            if (departureList != null && !departureList.isEmpty()) {
                result.put("success", true);
                result.put("hasRecord", true);
                result.put("data", departureList);
                result.put("message", "查询到离职记录信息");
                return result;
            }

            // 如果离职表没有数据，再从人员表查询
            List<SwmPerson> departedPersons = swmPersonService.findDepartedByIdentityCard(identityCard);

            if (departedPersons != null && !departedPersons.isEmpty()) {
                result.put("success", true);
                result.put("hasRecord", true);
                result.put("data", departedPersons);
                result.put("message", "查询到离职人员信息");
            } else {
                result.put("success", true);
                result.put("hasRecord", false);
                result.put("message", "未查询到离职人员信息");
            }
        } catch (Exception e) {
            logger.error("查询离职人员信息异常", e);
            result.put("success", false);
            result.put("message", "查询离职人员信息失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取离职人员详情
     */
    @GetMapping(value = "getDepartureDetail")
    @ResponseBody
    public Map<String, Object> getDepartureDetail(@RequestParam("id") String id) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从离职表中查询详情
            SwmPersonDeparture departure = swmPersonDepartureService.get(id);
            if (departure != null) {
                result.put("success", true);
                result.put("data", departure);
            } else {
                // 如果离职表中没有数据，再从人员表查询
                SwmPerson person = swmPersonService.get(id);
                if (person != null) {
                    // 创建一个Map来保存人员信息和枚举文本
                    Map<String, Object> personMap = new HashMap<>();

                    // 复制基本属性
                    personMap.put("id", person.getId());
                    personMap.put("createBy", person.getCreateBy());
                    personMap.put("updateDate", person.getUpdateDate());
                    personMap.put("updateBy", person.getUpdateBy());
                    personMap.put("status", person.getStatus());
                    personMap.put("createDate", person.getCreateDate());
                    personMap.put("remarks", person.getRemarks());
                    personMap.put("name", person.getName());
                    personMap.put("personType", person.getPersonType());
                    personMap.put("gender", person.getGender());
                    personMap.put("company", person.getCompany());
                    personMap.put("department", person.getDepartment());
                    personMap.put("workProcess", person.getWorkProcess());
                    personMap.put("team", person.getTeam());
                    personMap.put("jobType", person.getJobType());
                    personMap.put("safetyHelmetId", person.getSafetyHelmetId());
                    personMap.put("personnelStatus", person.getPersonnelStatus());
                    personMap.put("safetyEducation", person.getSafetyEducation());
                    personMap.put("identityCard", person.getIdentityCard());
                    personMap.put("phoneNumber", person.getPhoneNumber());
                    personMap.put("helmetReturned", person.getHelmetReturned());
                    personMap.put("departureType", person.getDepartureType());
                    personMap.put("departureReason", person.getDepartureReason());
                    personMap.put("departureDate", person.getDepartureDate());
                    personMap.put("isNewRecord", person.getIsNewRecord());

                    // 添加枚举文本显示值
                    personMap.put("personnelStatusText", person.getPersonnelStatusText());
                    personMap.put("safetyEducationText", person.getSafetyEducationText());
                    personMap.put("helmetReturnedText", person.getHelmetReturnedText());
                    personMap.put("departureTypeText", person.getDepartureTypeText());

                    result.put("success", true);
                    result.put("data", personMap);
                } else {
                    result.put("success", false);
                    result.put("message", "未找到离职人员详情");
                }
            }
        } catch (Exception e) {
            logger.error("获取离职人员详情异常", e);
            result.put("success", false);
            result.put("message", "获取离职人员详情失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取可用的安全帽列表
     */
    @GetMapping(value = "getAvailableHelmets")
    @ResponseBody
    public Map<String, Object> getAvailableHelmets(@RequestParam(value = "keyword", required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询可用的安全帽
            List<SwmHelmetDevice> helmets = swmHelmetDeviceService.findAvailableHelmets(keyword);
            result.put("success", true);
            result.put("data", helmets);
        } catch (Exception e) {
            logger.error("获取可用安全帽列表异常", e);
            result.put("success", false);
            result.put("message", "获取可用安全帽列表失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 绑定安全帽
     */
    @PostMapping(value = "bindHelmet")
    @ResponseBody
    public String bindHelmet(@RequestBody Map<String, String> params) {
        try {
            String personId = params.get("personId");
            String helmetId = params.get("helmetId");

            // 获取人员信息
            SwmPerson person = swmPersonService.get(personId);
            if (person == null) {
                return renderResult(Global.FALSE, text("人员不存在"));
            }

            // 获取安全帽信息
            SwmHelmetDevice helmet = swmHelmetDeviceService.getByHelmetId(helmetId);
            if (helmet == null) {
                return renderResult(Global.FALSE, text("安全帽不存在"));
            }

            // 检查安全帽是否已被绑定
            if (helmet.getAssignedPerson() != null && !helmet.getAssignedPerson().isEmpty()) {
                return renderResult(Global.FALSE, text("该安全帽已被绑定，请选择其他安全帽"));
            }

            // 更新人员的安全帽编号
            person.setSafetyHelmetId(helmetId);
            swmPersonService.save(person);

            // 更新安全帽的绑定信息
            helmet.setAssignedPerson(person.getName());
            helmet.setAssignedWorkshop(person.getDepartment());
            helmet.setAssignedProcess(person.getWorkProcess());
            helmet.setAssignedTeam(person.getTeam());
            swmHelmetDeviceService.save(helmet);

            return renderResult(Global.TRUE, text("安全帽绑定成功"));
        } catch (Exception e) {
            logger.error("绑定安全帽异常", e);
            return renderResult(Global.FALSE, text("绑定安全帽失败：" + e.getMessage()));
        }
    }

    /**
     * 解绑安全帽
     */
    @PostMapping(value = "unbindHelmet")
    @ResponseBody
    public String unbindHelmet(@RequestBody Map<String, String> params) {
        try {
            String personId = params.get("personId");

            // 获取人员信息
            SwmPerson person = swmPersonService.get(personId);
            if (person == null) {
                return renderResult(Global.FALSE, text("人员不存在"));
            }

            // 获取人员当前绑定的安全帽
            String helmetId = person.getSafetyHelmetId();
            if (helmetId == null || helmetId.isEmpty()) {
                return renderResult(Global.FALSE, text("该人员未绑定安全帽"));
            }

            // 更新安全帽的绑定信息
            SwmHelmetDevice helmet = swmHelmetDeviceService.getByHelmetId(helmetId);
            if (helmet != null) {
                helmet.setAssignedPerson(null);
                helmet.setAssignedWorkshop(null);
                helmet.setAssignedProcess(null);
                helmet.setAssignedTeam(null);
                swmHelmetDeviceService.save(helmet);
            }

            // 更新人员的安全帽编号
            person.setSafetyHelmetId(null);
            swmPersonService.save(person);

            return renderResult(Global.TRUE, text("安全帽解绑成功"));
        } catch (Exception e) {
            logger.error("解绑安全帽异常", e);
            return renderResult(Global.FALSE, text("解绑安全帽失败：" + e.getMessage()));
        }
    }

    /**
     * 清除人员关联的安全帽
     * 用于离职处理过程中，当选择"已归还安全帽"时调用
     */
    @PostMapping(value = "clearSafetyHelmet")
    @ResponseBody
    public String clearSafetyHelmet(@RequestParam("id") String personId) {
        try {
            // 获取人员信息
            SwmPerson person = swmPersonService.get(personId);
            if (person == null) {
                return renderResult(Global.FALSE, text("人员不存在"));
            }

            // 获取人员当前绑定的安全帽
            String helmetId = person.getSafetyHelmetId();
            if (helmetId == null || helmetId.isEmpty()) {
                return renderResult(Global.TRUE, text("该人员未绑定安全帽"));
            }

            // 更新安全帽的绑定信息
            SwmHelmetDevice helmet = swmHelmetDeviceService.getByHelmetId(helmetId);
            if (helmet != null) {
                helmet.setAssignedPerson("");
                helmet.setAssignedWorkshop("");
                helmet.setAssignedProcess("");
                helmet.setAssignedTeam("");
                swmHelmetDeviceService.save(helmet);
                logger.info("离职归还安全帽：已解除安全帽{}的绑定", helmetId);
            }

            // 更新人员的安全帽编号
            person.setSafetyHelmetId("");
            swmPersonService.save(person);
            logger.info("离职归还安全帽：已清除人员{}的安全帽关联", person.getName());

            return renderResult(Global.TRUE, text("安全帽解绑成功"));
        } catch (Exception e) {
            logger.error("清除安全帽关联异常", e);
            return renderResult(Global.FALSE, text("清除安全帽关联失败：" + e.getMessage()));
        }
    }
}