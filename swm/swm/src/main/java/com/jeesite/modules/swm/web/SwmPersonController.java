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
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.utils.excel.ExcelExport;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.enums.SyncDataOperateTypeEnum;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.excel.*;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.swm.util.MqSendUtil;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.ExcelExportUtil;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.jeesite.modules.utils.R;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import com.jeesite.modules.utils.BatchOperationsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private OrgValidationService orgValidationService;

    @Autowired
    private SwmPersonDepartureService swmPersonDepartureService;

    @Autowired
    private SwmSafetyEducationService swmSafetyEducationService;

    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;

    @Autowired
    private SwmSafetyHelmetOrderService swmSafetyHelmetOrderService;

    @Autowired
    private TDengineService tdengineService;

    @Autowired
    private SwmHelmetCacheService helmetCacheService;

    @Value("${tdengine.dbname}")
    private String tdengineDbName;
    @Autowired
    private RedisService redisService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MqSendUtil mqSendUtil;
    @Autowired
    private SwmDictDataService swmDictDataService;



    // 延迟获取SwmPersonCacheService，避免循环依赖
    private SwmPersonCacheService getPersonCacheService() {
        try {
            return applicationContext.getBean(SwmPersonCacheService.class);
        } catch (Exception e) {
            logger.warn("获取SwmPersonCacheService失败", e);
            return null;
        }
    }

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
            personMap.put("prodLine", person.getProdLine());
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
            personMap.put("isExternalPersonnel", person.getIsExternalPersonnel());
            personMap.put("isNewRecord", person.getIsNewRecord());
            personMap.put("dept", person.getDept());
            personMap.put("position", person.getPosition());

            // 添加枚举文本显示值
            personMap.put("personnelStatusText", person.getPersonnelStatusText());
            personMap.put("safetyEducationText", person.getSafetyEducationText());
            personMap.put("helmetReturnedText", person.getHelmetReturnedText());
            personMap.put("departureTypeText", person.getDepartureTypeText());
            personMap.put("powerOnStatus", person.getPowerOnStatus());
            personMap.put("personNumber", person.getPersonNumber());
            personMap.put("age", person.getAge());
            personMap.put("urgentPerson", person.getUrgentPerson());
            personMap.put("urgentPhoneNumber", person.getUrgentPhoneNumber());
            //血型
            personMap.put("bloodType", person.getBloodType());

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
            personData.put("prodLine", swmPerson.getProdLine());
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
            personData.put("isExternalPersonnel", swmPerson.getIsExternalPersonnel()); // 补充字段

            personData.put("dept", swmPerson.getDept());
            personData.put("position", swmPerson.getPosition());
            personData.put("personNumber", swmPerson.getPersonNumber());
            personData.put("age", swmPerson.getAge());
            personData.put("urgentPerson", swmPerson.getUrgentPerson());
            personData.put("urgentPhoneNumber", swmPerson.getUrgentPhoneNumber());

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
        // ========== 步骤1：识别操作类型 ==========
        String operateType = identifyOperateType(swmPerson);
        logger.info("识别到操作类型：{}，人员ID：{}，安全帽ID：{}",
                operateType, swmPerson.getId(), swmPerson.getSafetyHelmetId());
        // 检查身份证号码是否已存在（新增时或修改身份证时）
        if (StringUtils.isNotBlank(swmPerson.getIdentityCard())) {
            SwmPerson existingPerson = swmPersonService.getByIdentityCard(swmPerson.getIdentityCard());

            // 如果是新增，或者是修改但身份证号码不是当前记录的
            if (existingPerson != null && (swmPerson.getIsNewRecord() || !existingPerson.getId().equals(swmPerson.getId()))) {

                // 检查是否存在相同身份证的在职人员
                if (SwmPerson.PersonStatusEnum.ACTIVE.equals(existingPerson.getPersonnelStatus())) {
                    return renderResult(Global.FALSE, text("身份证号码已存在，该人员已在职：" + existingPerson.getName()));
                }
            }
        }

        swmPersonService.save(swmPerson);

        // ========== 发送MQ消息 ==========
        SwmPerson fullPerson = swmPersonService.get(swmPerson.getId()); //查询完整的字段
        mqSendUtil.sendPersonSingleChangeMsg(operateType, fullPerson);
        return renderResult(Global.TRUE, text("保存人员登记成功！"));
    }


    /**
     * 重构：精准识别操作类型（解决绑定/编辑混淆问题）
     * 规则：
     * 1. 新增人员：无ID（isNewRecord=true）
     * 2. 绑定安全帽：有ID + 安全帽ID发生变更（新增/修改）
     * 3. 编辑人员：有ID + 核心字段变更 + 安全帽ID未变更
     */
    private String identifyOperateType(SwmPerson swmPerson) {
        // ========== 0. 最顶层防护：swmPerson为null的极端场景 ==========
        if (swmPerson == null) {
            logger.error("swmPerson对象为null，无法识别操作类型，默认返回新增");
            return SyncDataOperateTypeEnum.PERSON_ADD.getCode();
        }

        // ========== 1. 处理personId为空的场景（新增核心判定） ==========
        String personId = swmPerson.getId(); // 此时swmPerson非null，getId()不会报错
        boolean isPersonIdEmpty = StringUtils.isBlank(personId);

        // 场景1：新增人员 → isNewRecord=true 或 personId为空
        if (swmPerson.getIsNewRecord() || isPersonIdEmpty) {
            logger.info("人员ID为空/新增标识为true，判定为新增人员：personId={}", personId);
            return SyncDataOperateTypeEnum.PERSON_ADD.getCode();
        }

        // ========== 2. 后续逻辑不变（已确保无NPE） ==========
        SwmPerson oldPerson = swmPersonService.get(personId);
        if (oldPerson == null) {
            logger.warn("人员ID存在但数据库无记录，兜底判定为新增人员：personId={}", personId);
            return SyncDataOperateTypeEnum.PERSON_ADD.getCode();
        }

        String oldHelmetId = oldPerson.getSafetyHelmetId();
        String newHelmetId = swmPerson.getSafetyHelmetId();
        boolean isOldHelmetEmpty = StringUtils.isBlank(oldHelmetId);
        boolean isNewHelmetEmpty = StringUtils.isBlank(newHelmetId);

        boolean isHelmetBind = false;
        if (isOldHelmetEmpty && !isNewHelmetEmpty) {
            isHelmetBind = true;
        } else if (!isOldHelmetEmpty && !isNewHelmetEmpty && !oldHelmetId.equals(newHelmetId)) {
            isHelmetBind = true;
        }

        if (isHelmetBind) {
            logger.info("人员{}安全帽ID变更，判定为绑定安全帽：old={}, new={}", personId, oldHelmetId, newHelmetId);
            return SyncDataOperateTypeEnum.PERSON_BIND_HELMET.getCode();
        }

        boolean isCoreFieldChanged = !StringUtils.equals(oldPerson.getName(), swmPerson.getName())
                || !StringUtils.equals(oldPerson.getGender(), swmPerson.getGender())
                || !StringUtils.equals(oldPerson.getPhoneNumber(), swmPerson.getPhoneNumber())
                || !StringUtils.equals(oldPerson.getIdentityCard(), swmPerson.getIdentityCard())
                || !StringUtils.equals(oldPerson.getPersonnelStatus(), swmPerson.getPersonnelStatus());

        if (isCoreFieldChanged) {
            logger.info("人员{}核心字段变更，判定为编辑人员", personId);
            return SyncDataOperateTypeEnum.PERSON_EDIT.getCode();
        }

        logger.warn("人员{}无任何字段（含安全帽）变更，兜底判定为编辑人员", personId);
        return SyncDataOperateTypeEnum.PERSON_EDIT.getCode();
    }




    /**
     * 批量修改人员登记
     */
    @PostMapping(value = "updateBatch")
    @ResponseBody
    public String updateBatch(@Validated SwmPerson swmPerson) {
        if (CollectionUtils.isNotEmpty(swmPerson.getIds())){
            return renderResult(Global.FALSE, text("所传主键id不能为空"));
        }
        swmPersonService.updateBatch(swmPerson);
        return renderResult(Global.TRUE, text("批量修改人员登记成功！"));
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
        List<String> idList = Arrays.stream(idArray)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        for (String id : idArray) {
            SwmPerson swmPerson = swmPersonService.get(id);
            if (swmPerson != null) {
                swmPersonService.delete(swmPerson);
            }
        }
        // ========== 发送MQ消息 ==========
        mqSendUtil.sendBatchDeleteMqMessage(SyncDataOperateTypeEnum.PERSON_BATCH_DELETE.getCode(), idList);
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

        try {
            // 创建模板并写入到响应流
            EasyExcel.write(response.getOutputStream(), SwmPersonExcelModel.class)
                    .sheet("人员信息")
                    .doWrite(new ArrayList<>());
        } catch (Exception e) {
            logger.error("生成Excel模板失败", e);
            // 返回错误信息
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"success\":false,\"message\":\"模板生成失败，请稍后重试\"}");
        }
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
                                && StringUtils.equals("0", StringUtils.trimToEmpty(existingPerson.getStatus()))
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
                SwmPersonImportListener listener = new SwmPersonImportListener(
                        swmPersonService,
                        swmHelmetDeviceService,
                        swmSafetyHelmetOrderService);

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
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("导入成功").append(successList.size()).append("条，失败").append(errorList.size()).append("条");
                if (listener.getHelmetBindSuccessCount() > 0) {
                    messageBuilder.append("，绑定安全帽").append(listener.getHelmetBindSuccessCount()).append("条");
                }
                if (listener.getHelmetRebindCount() > 0) {
                    messageBuilder.append("，重新绑定").append(listener.getHelmetRebindCount()).append("条");
                }
                if (listener.getHelmetBindFailCount() > 0) {
                    messageBuilder.append("，安全帽绑定失败").append(listener.getHelmetBindFailCount()).append("条");
                }
                result.put("message", messageBuilder.toString());
                result.put("helmetBindSuccess", listener.getHelmetBindSuccessCount());
                result.put("helmetRebind", listener.getHelmetRebindCount());
                result.put("helmetBindFail", listener.getHelmetBindFailCount());
                result.put("helmetBindSuccessRows", listener.getHelmetBindSuccessRows());
                result.put("helmetRebindRows", listener.getHelmetRebindRows());
                result.put("helmetBindFailRows", listener.getHelmetBindFailRows());
            }
        } catch (Exception e) {
            logger.error("导入Excel异常", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 下载增强版导入人员Excel模板（包含组织架构数据）
     */
    @GetMapping(value = "importTemplateEnhanced")
    public void importTemplateEnhanced(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("人员信息导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try {
            // 创建增强版模板并写入到响应流
            EasyExcel.write(response.getOutputStream(), SwmPersonExcelEnhancedModel.class)
                    .sheet("人员信息")
                    .doWrite(new ArrayList<>());
        } catch (Exception e) {
            logger.error("生成Excel模板失败", e);
            // 返回错误信息
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"success\":false,\"message\":\"模板生成失败，请稍后重试\"}");
        }
    }

    /**
     * 导入人员Excel（增强版，支持组织架构验证）
     */
    @PostMapping(value = "importExcelEnhanced")
    @ResponseBody
    public Map<String, Object> importExcelEnhanced(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择文件上传");
            return result;
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 先读取Excel数据进行预处理和检查
            List<SwmPersonExcelEnhancedModel> excelData = new ArrayList<>();
            EasyExcel.read(inputStream, SwmPersonExcelEnhancedModel.class,
                    new AnalysisEventListener<SwmPersonExcelEnhancedModel>() {
                        @Override
                        public void invoke(SwmPersonExcelEnhancedModel data, AnalysisContext context) {
                            excelData.add(data);
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                        }
                    }).sheet().doRead();

            // 检查身份证重复的在职人员
            List<String> duplicateIdentityCards = new ArrayList<>();
            Map<String, String> duplicatePersons = new HashMap<>();

            SwmPerson person = new SwmPerson();
            person.setStatus(SwmPerson.STATUS_NORMAL);
            List<SwmPerson> personList = swmPersonService.findList(person);
            Map<String, SwmPerson> personMap = personList.stream().collect(Collectors.toMap(SwmPerson::getIdentityCard, Function.identity(), (existing, replacement) -> existing));
            for (SwmPersonExcelEnhancedModel model : excelData) {
                // 只检查具有身份证号的数据
                if (StringUtils.isNotBlank(model.getIdentityCard())) {
                    // 查询数据库中是否已存在相同身份证的在职人员
                    //SwmPerson existingPerson = swmPersonService.getByIdentityCard(model.getIdentityCard());
                    SwmPerson existingPerson = personMap.get(model.getIdentityCard());
                    if (existingPerson != null
                            && SwmPerson.PersonStatusEnum.ACTIVE.equals(existingPerson.getPersonnelStatus())) {
                        duplicateIdentityCards.add(model.getIdentityCard());
                        duplicatePersons.put(model.getIdentityCard(),
                                String.format("姓名: %s, 身份证: %s", existingPerson.getName(),
                                        existingPerson.getIdentityCard()));
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
                // 创建增强版Excel读取监听器
                SwmPersonImportEnhancedListener listener = new SwmPersonImportEnhancedListener();

                // 读取Excel
                EasyExcel.read(secondInputStream, SwmPersonExcelEnhancedModel.class, listener)
                        .sheet()
                        .doRead();

                // 获取结果
                SwmPersonImportEnhancedListener.ImportResult importResult = listener.getImportResult();

                // ========== 构建MQ消息数据 ==========
                List<SwmPerson> fullPersons = listener.getSuccessImportedPersons().stream()
                        // 过滤ID非空的人员（核心校验）
                        .filter(personForMq -> StringUtils.isNotBlank(personForMq.getId()))
                        // 提取人员ID
                        .map(SwmPerson::getId)
                        // 先收集为Set去重，再批量查库（一步到位）
                        .collect(Collectors.collectingAndThen(
                                Collectors.toSet(), // 去重：避免重复ID查库
                                ids -> ids.isEmpty()
                                        ? new ArrayList<>() // 无有效ID时返回空列表
                                        : swmPersonService.findListByIds(ids) // 有ID则批量查完整记录
                        ));


                // ========== 发送批量导入MQ消息 ==========
                if (!fullPersons.isEmpty()) {
                    // 生成唯一的批量导入ID（用于MQ消息标识）
                    mqSendUtil.sendPersonBatchChangeMsg(SyncDataOperateTypeEnum.PERSON_IMPORT.getCode(), fullPersons);
                } else {
                    logger.warn("============批量导入Excel无成功数据，不发送MQ============");
                }

                // 判断导入是否成功：只有没有错误时才算成功
                boolean isSuccess = importResult.getErrorCount() == 0;

                // 返回导入结果
                result.put("success", isSuccess);
                result.put("total", importResult.getTotalCount());
                result.put("successCount", importResult.getSuccessCount());
                result.put("errorCount", importResult.getErrorCount());
                result.put("errors", importResult.getErrors());
                result.put("warnings", importResult.getWarnings());
                result.put("helmetBindSuccess", importResult.getHelmetBindSuccessCount());
                result.put("helmetRebind", importResult.getHelmetRebindCount());
                result.put("helmetBindFail", importResult.getHelmetBindFailCount());
                result.put("helmetBindSuccessRows", importResult.getHelmetBindSuccessRows());
                result.put("helmetRebindRows", importResult.getHelmetRebindRows());
                result.put("helmetBindFailRows", importResult.getHelmetBindFailRows());

                // 构建详细消息
                StringBuilder message = new StringBuilder();
                message.append("导入完成：成功").append(importResult.getSuccessCount()).append("条，失败")
                        .append(importResult.getErrorCount()).append("条");
                if (!importResult.getWarnings().isEmpty()) {
                    message.append("，转换").append(importResult.getWarnings().size()).append("条");
                }
                if (importResult.getHelmetBindSuccessCount() > 0) {
                    message.append("，绑定安全帽").append(importResult.getHelmetBindSuccessCount()).append("条");
                }
                if (importResult.getHelmetRebindCount() > 0) {
                    message.append("，重新绑定").append(importResult.getHelmetRebindCount()).append("条");
                }
                if (importResult.getHelmetBindFailCount() > 0) {
                    message.append("，安全帽绑定失败").append(importResult.getHelmetBindFailCount()).append("条");
                }
                result.put("message", message.toString());
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

            // ========== 发送MQ消息 ==========
            SwmPerson fullPerson = swmPersonService.get(personId);
            mqSendUtil.sendPersonSingleChangeMsg(SyncDataOperateTypeEnum.PERSON_DEPARTURE.getCode(), fullPerson);
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
                    personMap.put("prodLine", person.getProdLine());
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
            SwmHelmetDevice helmet = swmHelmetDeviceService.getByDeviceId(helmetId);
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

            // ========== 发送MQ消息 ==========
            SwmPerson fullPerson = swmPersonService.get(person.getId()); //查询完整的字段
            mqSendUtil.sendPersonSingleChangeMsg(SyncDataOperateTypeEnum.PERSON_BIND_HELMET.getCode(), fullPerson);

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

            // 使用专门的方法强制清空安全帽绑定信息，确保assigned_person字段设置为null
            swmHelmetDeviceService.clearDeviceAssignment(helmetId);

            // 更新安全帽订单表的解绑时间
            try {
                // 查找该设备的使用中订单并解绑
                SwmSafetyHelmetOrder activeOrder = swmSafetyHelmetOrderService.findActiveOrderByDeviceId(helmetId);
                if (activeOrder != null) {
                    swmSafetyHelmetOrderService.unbindHelmet(activeOrder.getId());
                    logger.info("已更新安全帽订单解绑时间，订单ID: {}", activeOrder.getId());
                }
            } catch (Exception e) {
                logger.error("更新安全帽订单解绑时间失败", e);
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

            // 使用专门的方法强制清空安全帽绑定信息，确保assigned_person字段设置为null
            swmHelmetDeviceService.clearDeviceAssignment(helmetId);

            // 更新安全帽订单表的解绑时间
            try {
                // 查找该设备的使用中订单并解绑
                SwmSafetyHelmetOrder activeOrder = swmSafetyHelmetOrderService.findActiveOrderByDeviceId(helmetId);
                if (activeOrder != null) {
                    swmSafetyHelmetOrderService.unbindHelmet(activeOrder.getId());
                    logger.info("离职归还安全帽：已更新安全帽订单解绑时间，订单ID: {}", activeOrder.getId());
                }
            } catch (Exception e) {
                logger.error("离职归还安全帽：更新安全帽订单解绑时间失败", e);
            }

            logger.info("离职归还安全帽：已解除安全帽{}的绑定", helmetId);

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

    /**
     * 根据身份证从缓存中查询在职人员信息
     */
    @GetMapping(value = "getActivePersonFromCache")
    @ResponseBody
    public Map<String, Object> getActivePersonFromCache(@RequestParam("identityCard") String identityCard) {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonCacheService cacheService = getPersonCacheService();
            if (cacheService == null) {
                result.put("success", false);
                result.put("message", "缓存服务不可用");
                return result;
            }

            Map<String, Object> personInfo = cacheService.getActivePersonByIdentityCard(identityCard);

            if (personInfo != null) {
                result.put("success", true);
                result.put("data", personInfo);
                result.put("message", "从缓存中查询到人员信息");
            } else {
                result.put("success", true);
                result.put("data", null);
                result.put("message", "缓存中未找到该身份证对应的在职人员");
            }

        } catch (Exception e) {
            logger.error("从缓存查询人员信息异常", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping(value = "getCacheStats")
    @ResponseBody
    public Map<String, Object> getCacheStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonCacheService cacheService = getPersonCacheService();
            if (cacheService == null) {
                result.put("success", false);
                result.put("message", "缓存服务不可用");
                return result;
            }

            Map<String, Object> stats = cacheService.getCacheStats();
            result.put("success", true);
            result.put("data", stats);
            result.put("message", "获取缓存统计信息成功");

        } catch (Exception e) {
            logger.error("获取缓存统计信息异常", e);
            result.put("success", false);
            result.put("message", "获取缓存统计信息失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 重新加载在职人员缓存
     */
    @PostMapping(value = "reloadPersonCache")
    @ResponseBody
    public String reloadPersonCache() {
        try {
            SwmPersonCacheService cacheService = getPersonCacheService();
            if (cacheService == null) {
                return renderResult(Global.FALSE, text("缓存服务不可用"));
            }

            cacheService.reloadActivePersonCache();
            return renderResult(Global.TRUE, text("重新加载在职人员缓存成功"));
        } catch (Exception e) {
            logger.error("重新加载在职人员缓存异常", e);
            return renderResult(Global.FALSE, text("重新加载在职人员缓存失败：" + e.getMessage()));
        }
    }

    /**
     * 清除在职人员缓存
     */
    @PostMapping(value = "clearPersonCache")
    @ResponseBody
    public String clearPersonCache() {
        try {
            SwmPersonCacheService cacheService = getPersonCacheService();
            if (cacheService == null) {
                return renderResult(Global.FALSE, text("缓存服务不可用"));
            }

            cacheService.clearActivePersonCache();
            return renderResult(Global.TRUE, text("清除在职人员缓存成功"));
        } catch (Exception e) {
            logger.error("清除在职人员缓存异常", e);
            return renderResult(Global.FALSE, text("清除在职人员缓存失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有在职人员缓存信息
     */
    @GetMapping(value = "getAllActivePersonsFromCache")
    @ResponseBody
    public Map<String, Object> getAllActivePersonsFromCache() {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonCacheService cacheService = getPersonCacheService();
            if (cacheService == null) {
                result.put("success", false);
                result.put("message", "缓存服务不可用");
                return result;
            }

            Map<Object, Object> allPersons = cacheService.getAllActivePersons();
            result.put("success", true);
            result.put("data", allPersons);
            result.put("total", allPersons != null ? allPersons.size() : 0);
            result.put("message", "获取所有在职人员缓存信息成功");

        } catch (Exception e) {
            logger.error("获取所有在职人员缓存信息异常", e);
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 从缓存中获取当天有坐标数据的在职人员信息，附带身份证号
     * 
     * @return 人员信息映射
     * @author Shawn
     * @date 2025-01-17
     */
    @GetMapping(value = "getAllActivePersonsWithIdCardFromCache")
    @ResponseBody
    public Map<String, Object> getAllActivePersonsWithIdCardFromCache(
            @RequestParam(value = "keyword", required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonCacheService cacheService = getPersonCacheService();
            if (cacheService == null) {
                result.put("success", false);
                result.put("message", "缓存服务不可用");
                return result;
            }

            // 从Redis内存中取出所有在职人员数据
            Map<Object, Object> allPersons = cacheService.getAllActivePersons();
            if (allPersons == null || allPersons.isEmpty()) {
                result.put("success", true);
                result.put("data", new ArrayList<>());
                result.put("total", 0);
                result.put("message", "没有在职人员数据");
                return result;
            }

            // 收集所有身份证号，同时应用关键词过滤
            List<String> allIdCards = new ArrayList<>();
            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            String lowerKeyword = hasKeyword ? keyword.toLowerCase() : "";

            for (Map.Entry<Object, Object> entry : allPersons.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> personData = (Map<String, Object>) entry.getValue();
                    String idCard = (String) personData.get("identityCard");

                    // 如果有关键词，判断是否匹配
                    if (hasKeyword) {
                        boolean matched = false;

                        // 匹配姓名
                        String name = (String) personData.get("name");
                        if (name != null && name.toLowerCase().contains(lowerKeyword)) {
                            matched = true;
                        }

                        // 匹配身份证号
                        if (!matched && idCard != null && idCard.toLowerCase().contains(lowerKeyword)) {
                            matched = true;
                        }

                        // 匹配所属单位
                        if (!matched) {
                            String company = (String) personData.get("company");
                            if (company != null && company.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配所属车间
                        if (!matched) {
                            String department = (String) personData.get("department");
                            if (department != null && department.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配产线
                        if (!matched) {
                            String prodLine = (String) personData.get("prodLine");
                            if (prodLine != null && prodLine.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配所属班组
                        if (!matched) {
                            String team = (String) personData.get("team");
                            if (team != null && team.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配工种
                        if (!matched) {
                            String jobType = (String) personData.get("jobType");
                            if (jobType != null && jobType.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配设备编号
                        if (!matched) {
                            String department = (String) personData.get("deviceId");
                            if (department != null && department.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 如果不匹配，跳过此人员
                        if (!matched) {
                            continue;
                        }
                    }

                    // 添加有效的身份证号
                    if (idCard != null && !idCard.trim().isEmpty()) {
                        allIdCards.add(idCard);
                    }
                }
            }

            if (allIdCards.isEmpty()) {
                result.put("success", true);
                result.put("data", new ArrayList<>());
                result.put("total", 0);
                result.put("message", hasKeyword ? "没有匹配关键词的人员" : "没有有效的身份证号码");
                return result;
            }

            // 1. 批量查询当天有坐标数据的身份证
            Set<String> idCardsWithCoordinates = batchCheckCoordinateDataToday(allIdCards);

            if (idCardsWithCoordinates.isEmpty()) {
                result.put("success", true);
                result.put("data", new ArrayList<>());
                result.put("total", 0);
                result.put("message", "没有当天有坐标数据的人员");
                return result;
            }

            // 2. 批量查询设备ID映射
            Map<String, String> idCardToDeviceMap = batchGetDeviceIdsByIdCards(idCardsWithCoordinates);

            // 3. 批量查询电量信息
            Map<String, Integer> deviceToBatteryMap = batchGetBatteryLevels(
                    new ArrayList<>(idCardToDeviceMap.values()));

            // 4. 批量查询位置信息
            Map<String, String> idCardToLocationMap = batchGetLocations(idCardsWithCoordinates);

            // 5. 批量查询运动状态
            Map<String, String> idCardToMotionStatusMap = batchGetMotionStatuses(idCardsWithCoordinates);

            // 过滤并构建结果数据
            List<Map<String, Object>> personsWithCoordinates = new ArrayList<>();

            for (Map.Entry<Object, Object> entry : allPersons.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> personData = (Map<String, Object>) entry.getValue();

                    // 获取身份证信息
                    String idCard = (String) personData.get("identityCard");

                    // 如果有关键词，判断是否匹配
                    if (hasKeyword) {
                        boolean matched = false;

                        // 匹配姓名
                        String name = (String) personData.get("name");
                        if (name != null && name.toLowerCase().contains(lowerKeyword)) {
                            matched = true;
                        }

                        // 匹配身份证号
                        if (!matched && idCard != null && idCard.toLowerCase().contains(lowerKeyword)) {
                            matched = true;
                        }

                        // 匹配所属单位
                        if (!matched) {
                            String company = (String) personData.get("company");
                            if (company != null && company.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配所属车间
                        if (!matched) {
                            String department = (String) personData.get("department");
                            if (department != null && department.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配产线
                        if (!matched) {
                            String prodLine = (String) personData.get("prodLine");
                            if (prodLine != null && prodLine.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配所属班组
                        if (!matched) {
                            String team = (String) personData.get("team");
                            if (team != null && team.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 匹配工种
                        if (!matched) {
                            String jobType = (String) personData.get("jobType");
                            if (jobType != null && jobType.toLowerCase().contains(lowerKeyword)) {
                                matched = true;
                            }
                        }

                        // 如果不匹配，跳过此人员
                        if (!matched) {
                            continue;
                        }
                    }

                    // 检查是否有坐标数据
                    if (idCard != null && !idCard.isEmpty() && idCardsWithCoordinates.contains(idCard)) {
                        // 获取设备编号
                        String deviceId = idCardToDeviceMap.get(idCard);

                        // 获取电量信息
                        Integer batteryLevel = deviceId != null ? deviceToBatteryMap.get(deviceId) : null;

                        // 获取位置信息
                        String location = idCardToLocationMap.get(idCard);

                        // 如果没有查询到位置信息，使用默认位置
                        if (location == null || location.trim().isEmpty()) {
                            location = String.format("%s%s",
                                    personData.get("company") != null ? personData.get("company") : "天津厂",
                                    personData.get("department") != null ? personData.get("department") : "一车间");
                        }

                        // 获取运动状态
                        String motionStatus = idCardToMotionStatusMap.get(idCard);

                        // 如果没有查询到运动状态，默认为运动状态
                        if (motionStatus == null || motionStatus.trim().isEmpty()) {
                            motionStatus = "运动";
                        }

                        // 创建增强的人员信息，添加设备编号、电量、位置和运动状态信息
                        Map<String, Object> enhancedPersonData = new HashMap<>(personData);
                        enhancedPersonData.put("deviceId", deviceId);
                        enhancedPersonData.put("safetyHelmetId", deviceId); // 兼容前端字段名
                        enhancedPersonData.put("batteryLevel", batteryLevel); // 电量信息
                        enhancedPersonData.put("location", location); // 位置信息
                        enhancedPersonData.put("motionStatus", motionStatus); // 运动状态信息
                        // 添加ID作为唯一标识
                        enhancedPersonData.put("id", entry.getKey());

                        personsWithCoordinates.add(enhancedPersonData);
                    }
                }
            }

            result.put("success", true);
            result.put("data", personsWithCoordinates);
            result.put("total", personsWithCoordinates.size());
            result.put("message", "获取当天有坐标数据的在职人员缓存信息成功");

        } catch (Exception e) {
            logger.error("获取当天有坐标数据的在职人员缓存信息异常", e);
            result.put("success", false);
            result.put("message", "获取失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量检查身份证号列表当天是否有坐标数据
     * 
     * @param idCards 身份证号列表
     * @return 有坐标数据的身份证号集合
     */
    private Set<String> batchCheckCoordinateDataToday(List<String> idCards) {
        Set<String> result = new HashSet<>();
        if (idCards == null || idCards.isEmpty()) {
            return result;
        }
        idCards.clear();
        String corpCode = CorpUtils.getCurrentCorpCode();
        Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
        if (deviceIds != null) {
            for (Object deviceId : deviceIds) {
                String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                idCards.add(currentPerson);
                result.add(currentPerson);
            }
        }

        return result;
//        try {
//            // 获取当前日期的开始和结束时间
////            String currentDate = cn.hutool.core.date.DateUtil.today();
////            String startTime = currentDate + " 00:00:00";
////            String endTime = currentDate + " 23:59:59";
//            Date now = new Date();
//            Date oneMinuteAgo = DateUtil.offsetDay(now, -7);
//            String startTime = DateUtil.formatDateTime(oneMinuteAgo);
//            String endTime = DateUtil.formatDateTime(now);
//
//            // 构建批量查询SQL，使用IN子句
//            StringBuilder sqlBuilder = new StringBuilder();
//            sqlBuilder.append("SELECT DISTINCT id_card FROM ").append(tdengineDbName)
//                    .append(".external_coordinate_data WHERE id_card IN (");
//
//            // 添加身份证号列表
//            for (int i = 0; i < idCards.size(); i++) {
//                if (i > 0) {
//                    sqlBuilder.append(",");
//                }
//                sqlBuilder.append("'").append(idCards.get(i)).append("'");
//            }
////
//            sqlBuilder.append(") AND time >= '").append(startTime)
//                    .append("' AND time <= '").append(endTime).append("'");
//
//            logger.debug("批量检查坐标数据SQL: {}", sqlBuilder.toString());
//
//            // 执行查询
//            R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
//
//            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
//                cn.hutool.json.JSONObject data = queryResult.getData();
//                cn.hutool.json.JSONArray rows = data.getJSONArray("data");
//
//                if (rows != null) {
//                    for (int i = 0; i < rows.size(); i++) {
//                        cn.hutool.json.JSONArray row = rows.getJSONArray(i);
//                        if (row != null && row.size() > 0) {
//                            String idCard = row.getStr(0);
//                            if (idCard != null && !idCard.isEmpty()) {
//                                result.add(idCard);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("批量检查坐标数据异常", e);
//        }
//
//        return result;
    }

    /**
     * 批量获取身份证号对应的设备ID
     * 
     * @param idCards 身份证号集合
     * @return 身份证号到设备ID的映射
     */
    private Map<String, String> batchGetDeviceIdsByIdCards(Set<String> idCards) {
        Map<String, String> result = new HashMap<>();
        if (idCards == null || idCards.isEmpty()) {
            return result;
        }

        try {
            // 使用缓存批量获取设备ID
            for (String idCard : idCards) {
                String deviceId = helmetCacheService.getAssignedDeviceFromCache(idCard, swmHelmetDeviceService);
                if (deviceId != null && !deviceId.isEmpty()) {
                    result.put(idCard, deviceId);
                }
            }
        } catch (Exception e) {
            logger.error("批量获取设备ID异常", e);
        }

        return result;
    }

    /**
     * 批量获取设备电量信息
     * 
     * @param deviceIds 设备ID列表
     * @return 设备ID到电量的映射
     */
    private Map<String, Integer> batchGetBatteryLevels(List<String> deviceIds) {
        Map<String, Integer> result = new HashMap<>();
        if (deviceIds == null || deviceIds.isEmpty()) {
            return result;
        }

        try {
            // 构建批量查询SQL
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT device_id, bat_l FROM ").append(tdengineDbName)
                    .append("." + TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+ " WHERE device_id IN (");

            // 添加设备ID列表
            for (int i = 0; i < deviceIds.size(); i++) {
                if (i > 0) {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append("'").append(deviceIds.get(i)).append("'");
            }

            sqlBuilder.append(") AND time <= NOW() AND time >= NOW() - 5m ORDER BY device_id, time DESC");

            logger.info("批量查询电量SQL: {}", sqlBuilder.toString());

            // 执行查询
            R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());

            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                cn.hutool.json.JSONObject data = queryResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                // 记录已处理的设备ID，避免重复
                Set<String> processedDevices = new HashSet<>();

                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() > 1) {
                            String deviceId = row.getStr(0);

                            // 只处理每个设备的第一条记录（最新的）
                            if (!processedDevices.contains(deviceId)) {
                                Integer batteryLevel = row.getInt(1);
                                result.put(deviceId, batteryLevel);
                                processedDevices.add(deviceId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("批量获取电量信息异常", e);
        }

        return result;
    }

    /**
     * 批量获取身份证号对应的位置信息
     * 
     * @param idCards 身份证号集合
     * @return 身份证号到位置信息的映射
     */
    private Map<String, String> batchGetLocations(Set<String> idCards) {
        Map<String, String> result = new HashMap<>();
        if (idCards == null || idCards.isEmpty()) {
            return result;
        }

        try {
            // 构建批量查询SQL
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT id_card, area_name FROM ").append(tdengineDbName).append(".")
                    .append(TdengineSuperTableConstant.AREA_FENCE_DATA)
                    .append(" WHERE id_card IN (");

            // 添加身份证号列表
            int i = 0;
            for (String idCard : idCards) {
                if (i > 0) {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append("'").append(idCard).append("'");
                i++;
            }

            sqlBuilder.append(") AND time <= NOW() AND time >= NOW() - 30m ORDER BY id_card, time DESC");

            logger.debug("批量查询位置SQL: {}", sqlBuilder.toString());

            // 执行查询
            R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());

            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                cn.hutool.json.JSONObject data = queryResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                // 记录已处理的身份证号，避免重复
                Set<String> processedIdCards = new HashSet<>();

                if (rows != null) {
                    for (int j = 0; j < rows.size(); j++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(j);
                        if (row != null && row.size() > 1) {
                            String idCard = row.getStr(0);

                            // 只处理每个身份证的第一条记录（最新的）
                            if (!processedIdCards.contains(idCard)) {
                                String areaName = row.getStr(1);
                                result.put(idCard, areaName);
                                processedIdCards.add(idCard);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("批量获取位置信息异常", e);
        }

        return result;
    }

    /**
     * 批量获取身份证号对应的运动状态
     * 
     * @param idCards 身份证号集合
     * @return 身份证号到运动状态的映射
     */
    private Map<String, String> batchGetMotionStatuses(Set<String> idCards) {
        Map<String, String> result = new HashMap<>();
        if (idCards == null || idCards.isEmpty()) {
            return result;
        }

        try {
            // 构建批量查询SQL
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT id_card, COUNT(*) FROM ").append(tdengineDbName)
                    .append(".helmet_ca_sos WHERE id_card IN (");

            // 添加身份证号列表
            int i = 0;
            for (String idCard : idCards) {
                if (i > 0) {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append("'").append(idCard).append("'");
                i++;
            }

            sqlBuilder.append(
                    ") AND time <= NOW() AND time >= NOW() - 2m AND (type = '1' OR type = '6') GROUP BY id_card");

            logger.debug("批量查询运动状态SQL: {}", sqlBuilder.toString());

            // 执行查询
            R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());

            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                cn.hutool.json.JSONObject data = queryResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                if (rows != null) {
                    for (int j = 0; j < rows.size(); j++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(j);
                        if (row != null && row.size() > 1) {
                            String idCard = row.getStr(0);
                            int count = Integer.parseInt(row.get(1).toString());
                            // 有静默报警记录的为静止状态，否则为运动状态
                            result.put(idCard, count > 0 ? "静止" : "运动");
                        }
                    }
                }

                // 对于没有查询到结果的身份证，默认为运动状态
                for (String idCard : idCards) {
                    if (!result.containsKey(idCard)) {
                        result.put(idCard, "运动");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("批量获取运动状态异常", e);
        }

        return result;
    }

    /**
     * 检查身份证号码是否已存在
     */
    @GetMapping(value = "checkIdentityCard")
    @ResponseBody
    public Map<String, Object> checkIdentityCard(@RequestParam("identityCard") String identityCard,
            @RequestParam(value = "excludeId", required = false) String excludeId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (StringUtils.isBlank(identityCard)) {
                result.put("success", true);
                result.put("exists", false);
                result.put("message", "身份证号码为空");
                return result;
            }

            SwmPerson existingPerson = swmPersonService.getByIdentityCard(identityCard);

            if (existingPerson == null) {
                result.put("success", true);
                result.put("exists", false);
                result.put("message", "身份证号码可用");
            } else {
                // 如果是编辑模式，排除当前记录
                if (StringUtils.isNotBlank(excludeId) && excludeId.equals(existingPerson.getId())) {
                    result.put("success", true);
                    result.put("exists", false);
                    result.put("message", "身份证号码可用");
                } else if (SwmPerson.PersonStatusEnum.ACTIVE.equals(existingPerson.getPersonnelStatus())) {
                    // 存在相同身份证的在职人员
                    result.put("success", true);
                    result.put("exists", true);
                    result.put("existingPerson", existingPerson.getName());
                    result.put("message", "身份证号码已存在，该人员已在职：" + existingPerson.getName());
                } else {
                    // 存在相同身份证的离职人员，提示但允许使用
                    result.put("success", true);
                    result.put("exists", false);
                    result.put("hasInactivePerson", true);
                    result.put("inactivePerson", existingPerson.getName());
                    result.put("message", "该身份证号码存在离职记录：" + existingPerson.getName());
                }
            }

        } catch (Exception e) {
            logger.error("检查身份证号码异常", e);
            result.put("success", false);
            result.put("message", "检查身份证号码失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 统计安全教育完成情况
     * 
     * @return 安全教育统计信息
     * @author Shawn
     * @date 2025-01-18
     */
    @GetMapping(value = "getSafetyEducationStats")
    @ResponseBody
    public Map<String, Object> getSafetyEducationStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询所有在职人员的安全教育情况
            SwmPerson query = new SwmPerson();
            query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // 只统计在职人员

            List<SwmPerson> activePersons = swmPersonService.findList(query);

            // 统计总人数
            int totalCount = activePersons.size();

            // 统计已完成安全教育的人数
            int completedCount = 0;
            int notStartedCount = 0;

            for (SwmPerson person : activePersons) {
                if (SwmPerson.SafetyEducationEnum.COMPLETED.equals(person.getSafetyEducation())) {
                    completedCount++;
                } else if (SwmPerson.SafetyEducationEnum.NOT_STARTED.equals(person.getSafetyEducation())) {
                    notStartedCount++;
                }
            }

            // 计算完成率（保留两位小数）
            double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0;

            // 构建返回结果
            result.put("success", true);
            result.put("totalCount", totalCount); // 总人数
            result.put("completedCount", completedCount); // 已完成人数
            result.put("notStartedCount", notStartedCount); // 未开始人数
            result.put("completionRate", Math.round(completionRate * 100.0) / 100.0); // 完成率（百分比，保留两位小数）
            result.put("message", String.format("统计完成：总计%d人，已完成安全教育%d人，完成率%.2f%%",
                    totalCount, completedCount, completionRate));

        } catch (Exception e) {
            logger.error("统计安全教育完成情况异常", e);
            result.put("success", false);
            result.put("message", "统计安全教育完成情况失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 测试接口 - 验证循环依赖是否解决
     */
    @GetMapping(value = "testCacheService")
    @ResponseBody
    public Map<String, Object> testCacheService() {
        Map<String, Object> result = new HashMap<>();

        try {
            SwmPersonCacheService cacheService = getPersonCacheService();

            if (cacheService != null) {
                result.put("success", true);
                result.put("message", "缓存服务可用");
                result.put("serviceAvailable", true);

                // 测试Redis连接
                boolean redisAvailable = cacheService.isRedisAvailable();
                result.put("redisAvailable", redisAvailable);

            } else {
                result.put("success", false);
                result.put("message", "缓存服务不可用");
                result.put("serviceAvailable", false);
            }

        } catch (Exception e) {
            logger.error("测试缓存服务异常", e);
            result.put("success", false);
            result.put("message", "测试失败：" + e.getMessage());
            result.put("serviceAvailable", false);
        }

        return result;
    }

    /**
     * 根据部门条件查询在职人员
     * 
     * @param departmentCondition 部门条件参数，可以是车间ID、班组ID、产线ID、组织编码或身份证号
     * @return 符合条件的在职人员列表
     * @author Shawn
     * @date 2025/01/18
     */
    @GetMapping(value = "findPersonsByDepartmentCondition")
    @ResponseBody
    public List<SwmPerson> findPersonsByDepartmentCondition(
            @RequestParam("departmentCondition") String departmentCondition) {
        try {
            logger.info("根据部门条件查询在职人员，条件: {}", departmentCondition);

            if (departmentCondition == null || departmentCondition.trim().isEmpty()) {
                logger.warn("部门条件为空，返回空列表");
                return new ArrayList<>();
            }

            List<SwmPerson> personList = swmPersonService.findPersonsByDepartmentCondition(departmentCondition);

            if (personList == null || personList.isEmpty()) {
                logger.info("根据条件 [{}] 未找到人员记录", departmentCondition);
                return new ArrayList<>();
            }

            logger.info("根据条件 [{}] 找到 {} 名人员", departmentCondition, personList.size());
            return personList;

        } catch (Exception e) {
            logger.error("根据部门条件查询人员异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 搜索人员（支持姓名、身份证、电话搜索）
     *
     * @param keyword    搜索关键词
     * @param searchType 搜索类型：name(姓名)、idCard(身份证)、phone(电话)、all(全部)
     * @param pageNo     页码
     * @param pageSize   页面大小
     * @return 搜索结果
     * @author Shawn
     * @date 2025-06-21
     */
    @GetMapping(value = "searchPersons")
    @ResponseBody
    public Map<String, Object> searchPersons(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", defaultValue = "all") String searchType,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "50") Integer pageSize) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (StringUtils.isBlank(keyword)) {
                result.put("success", true);
                result.put("list", new ArrayList<>());
                result.put("total", 0);
                result.put("message", "搜索关键词为空");
                return result;
            }

            String trimmedKeyword = keyword.trim();
            List<SwmPerson> personList;

            if ("name".equals(searchType)) {
                // 只搜索姓名
                SwmPerson query = new SwmPerson();
                query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
                query.setName(trimmedKeyword);
                personList = swmPersonService.findList(query);
            } else if ("idCard".equals(searchType)) {
                // 只搜索身份证
                SwmPerson query = new SwmPerson();
                query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
                query.setIdentityCard(trimmedKeyword);
                personList = swmPersonService.findList(query);
            } else if ("phone".equals(searchType)) {
                // 只搜索电话
                SwmPerson query = new SwmPerson();
                query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
                query.setPhoneNumber(trimmedKeyword);
                personList = swmPersonService.findList(query);
            } else {
                // 搜索所有字段（姓名、身份证、电话）
                personList = swmPersonService.searchPersonsByKeyword(trimmedKeyword);
            }

            // 应用分页（手动分页）
            int total = personList.size();
            int startIndex = (pageNo - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);

            List<SwmPerson> pagedList;
            if (startIndex < total) {
                pagedList = personList.subList(startIndex, endIndex);
            } else {
                pagedList = new ArrayList<>();
            }

            // 构建返回结果
            result.put("success", true);
            result.put("list", pagedList);
            result.put("total", total);
            result.put("message", String.format("搜索完成，找到 %d 条记录", total));

        } catch (Exception e) {
            logger.error("搜索人员异常", e);
            result.put("success", false);
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            result.put("message", "搜索失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量完成安全教育
     * 
     * @param personIds 人员ID列表，以逗号分隔
     * @return 处理结果
     */
    @PostMapping(value = "batchCompleteSafetyEducation")
    @ResponseBody
    public Map<String, Object> batchCompleteSafetyEducation(@RequestParam("personIds") String personIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("批量完成安全教育，人员IDs: {}", personIds);

            if (personIds == null || personIds.isEmpty()) {
                result.put("result", "error");
                result.put("message", "未提供人员ID列表");
                return result;
            }

            // 解析人员ID列表
            String[] idArray = personIds.split(",");
            int successCount = 0;
            int totalCount = idArray.length;
            List<String> failedNames = new ArrayList<>();

            // 存储需要发送MQ的人员数据
//            Set<String> ids = new HashSet<>();

            for (String personId : idArray) {
                try {
                    SwmPerson person = swmPersonService.get(personId);
                    if (person != null) {
                        // 只更新安全教育状态为未开始的人员
                        if (SwmPerson.SafetyEducationEnum.NOT_STARTED.equals(person.getSafetyEducation())) {
                            person.setSafetyEducation(SwmPerson.SafetyEducationEnum.COMPLETED);
                            swmPersonService.save(person);
                            successCount++;
//                            ids.add(personId);
                            logger.info("更新人员 [{}] 的安全教育状态为已完成", person.getName());
                        } else {
                            logger.info("人员 [{}] 的安全教育状态已为已完成，跳过更新", person.getName());
                            // 已完成的也计入成功数
                            successCount++;
                        }
                    } else {
                        logger.warn("根据人员ID [{}] 未找到人员记录", personId);
                        failedNames.add("ID:" + personId);
                    }
                } catch (Exception e) {
                    logger.error("更新人员ID [{}] 的安全教育状态时出现异常: {}", personId, e.getMessage());
                    failedNames.add("ID:" + personId);
                }
            }

//            List<SwmPerson> fullPersons = swmPersonService.findListByIds(ids);
//
//            // ========== 发送MQ批量消息 ==========
//            if (!fullPersons.isEmpty()) {
//                mqSendUtil.sendPersonBatchChangeMsg(SyncDataOperateTypeEnum.PERSON_COMPLETE_EDUCATION.getCode(), fullPersons);
//            }

            result.put("result", "success");
            result.put("message", String.format("批量完成安全教育成功，共处理 %d 条记录，成功 %d 条", totalCount, successCount));
            result.put("successCount", successCount);
            result.put("totalCount", totalCount);

            if (!failedNames.isEmpty()) {
                result.put("failedCount", failedNames.size());
                result.put("failedNames", String.join(", ", failedNames));
            }

            return result;
        } catch (Exception e) {
            logger.error("批量完成安全教育出现异常", e);
            result.put("result", "error");
            result.put("message", "批量完成安全教育失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 根据身份证号查询该人员参与的所有安全教育记录
     */
    @GetMapping(value = "getSafetyEducationByIdentityCard")
    @ResponseBody
    public Map<String, Object> getSafetyEducationByIdentityCard(@RequestParam("identityCard") String identityCard) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (identityCard == null || identityCard.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "身份证号不能为空");
                return result;
            }

            // 查询该人员参与的所有安全教育记录
            List<SwmSafetyEducation> educationList = swmSafetyEducationService.findByIdentityCard(identityCard);

            // 处理返回数据，添加枚举文本显示
            List<Map<String, Object>> processedList = new ArrayList<>();
            for (SwmSafetyEducation education : educationList) {
                Map<String, Object> educationMap = new HashMap<>();

                // 基本信息
                educationMap.put("id", education.getId());
                educationMap.put("theme", education.getTheme());
                educationMap.put("contentDescription", education.getContentDescription());
                educationMap.put("safetyEducationType", education.getSafetyEducationType());
                educationMap.put("startTime", education.getStartTime());
                educationMap.put("participants", education.getParticipants());
                educationMap.put("participantsName", education.getParticipantsName());
                educationMap.put("safetyStatus", education.getSafetyStatus());
                educationMap.put("participationType", education.getParticipationType());
                educationMap.put("attachmentUrl", education.getAttachmentUrl());
                educationMap.put("createTime", education.getCreateTime());
                educationMap.put("updateTime", education.getUpdateTime());

                // 添加枚举文本显示
                educationMap.put("safetyEducationTypeText", education.getSafetyEducationTypeText());
                educationMap.put("participationTypeText", education.getParticipationTypeText());
                educationMap.put("safetyStatusText", education.getSafetyStatusText());

                processedList.add(educationMap);
            }

            result.put("success", true);
            result.put("data", processedList);
            result.put("total", processedList.size());
            result.put("message", "查询成功");

        } catch (Exception e) {
            logger.error("查询人员安全教育记录异常", e);
            result.put("success", false);
            result.put("message", "查询安全教育记录失败：" + e.getMessage());
        }

        return result;
    }

    @ApiOperation("人员台账excel导入（切换车间、产线、班组）")
    @RequestMapping("/importData")
    @ResponseBody
    public String importData(MultipartFile file) {
        Integer count = swmPersonService.importData(file);
        return renderResult(Global.TRUE, text("数据全部导入成功,共" + count + "条。"));
    }


    @ApiOperation("人员台账切换班组excel导出")
    @PostMapping("teamTempExport")
    @ResponseBody
    public String teamTempExport() {

        String name;
        String fileName = "班组模板导出" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";
        try (ExcelExport ee = new ExcelExport("班组模板导出", SwmPersonSwitcWorkshopImport.class)) {
            List<SwmPersonSwitcWorkshopImport> list = new ArrayList<>();
            name = ExcelExportUtil.uploadOss(ee.setDataList(list), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return renderResult(Global.TRUE, text("成功！"), name);
    }


    /**
     * 导出分页数据
     */
    @ApiOperation("人员台账excel导出")
    @GetMapping("export")
    @ResponseBody
    public String export(SwmPerson swmPerson, HttpServletRequest request, HttpServletResponse response) {

        Page<SwmPerson> page = swmPersonService.findPage(new Page<>(1, 99999), swmPerson);
        SwmDictData dictData = new SwmDictData();
        dictData.setDictType("person_type_enum");
        List<SwmDictData> personTypeList = swmDictDataService.findList(dictData);
        if (personTypeList == null || personTypeList.isEmpty()){
            return renderResult(Global.FALSE, text("请先添加人员类型字典"));
        }
        Map<String, String> personTypeMap = personTypeList.stream().collect(Collectors.toMap(SwmDictData::getDictValue, SwmDictData::getDictLabel));


        String name;
        List<SwmPerson> list = page.getList();
        List<SwmPersonDtoExport> exportList = new ArrayList<>();
        for (SwmPerson person : list) {
            SwmPersonDtoExport export = new SwmPersonDtoExport();
            BeanUtils.copyProperties(person, export);
            export.setPersonType(personTypeMap.get(person.getPersonType()));
            exportList.add(export);
        }

        String fileName = "人员台账导出" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";

        try (ExcelExport ee = new ExcelExport("人员台账导出", SwmPersonDtoExport.class)) {
            name = ExcelExportUtil.uploadOss(ee.setDataList(exportList), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return renderResult(Global.TRUE, text("成功！"), name);
    }

}
