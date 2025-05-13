/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.apache.commons.lang3.StringUtils;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 人员信息导入监听器
 */
public class SwmPersonImportListener extends AnalysisEventListener<SwmPersonExcelModel> {

    private static final Logger logger = LoggerFactory.getLogger(SwmPersonImportListener.class);
    private static final Pattern IDENTITY_CARD_PATTERN = Pattern
            .compile("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern
            .compile("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$");

    // 人员状态映射
    private static final Map<String, String> PERSONNEL_STATUS_MAP = new HashMap<>();
    // 安全教育状态映射
    private static final Map<String, String> SAFETY_EDUCATION_MAP = new HashMap<>();

    static {
        // 初始化人员状态映射
        PERSONNEL_STATUS_MAP.put("在职", SwmPerson.PersonStatusEnum.ACTIVE);
        PERSONNEL_STATUS_MAP.put("离职", SwmPerson.PersonStatusEnum.INACTIVE);

        // 初始化安全教育状态映射
        SAFETY_EDUCATION_MAP.put("未开始", SwmPerson.SafetyEducationEnum.NOT_STARTED);
        SAFETY_EDUCATION_MAP.put("已培训", SwmPerson.SafetyEducationEnum.COMPLETED);
    }

    // 成功导入的数据列表
    private final List<SwmPerson> successList = new ArrayList<>();

    // 导入失败的数据列表
    private final List<SwmPersonExcelModel> errorList = new ArrayList<>();

    // 总记录数
    private int total = 0;

    private final SwmPersonService swmPersonService;

    public SwmPersonImportListener(SwmPersonService swmPersonService) {
        this.swmPersonService = swmPersonService;
    }

    @Override
    public void invoke(SwmPersonExcelModel excelModel, AnalysisContext analysisContext) {
        total++;
        try {
            // 校验数据
            validateData(excelModel);

            // 转换为实体类
            SwmPerson person = convertToPerson(excelModel);

            // 保存数据
            swmPersonService.save(person);

            // 添加到成功列表
            successList.add(person);
        } catch (Exception e) {
            logger.error("导入人员数据失败", e);
            excelModel.setErrorMsg(e.getMessage());
            errorList.add(excelModel);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        logger.info("Excel解析完成，总记录数：{}，成功：{}，失败：{}",
                total, successList.size(), errorList.size());
    }

    /**
     * 校验Excel数据
     */
    private void validateData(SwmPersonExcelModel model) {
        StringBuilder errorMsg = new StringBuilder();

        // 姓名不能为空
        if (StringUtils.isBlank(model.getName())) {
            errorMsg.append("姓名不能为空; ");
        }

        // 身份证号码校验
        if (StringUtils.isNotBlank(model.getIdentityCard())
                && !IDENTITY_CARD_PATTERN.matcher(model.getIdentityCard()).matches()) {
            errorMsg.append("身份证号码格式不正确; ");
        }

        // 手机号码校验
        if (StringUtils.isNotBlank(model.getPhoneNumber())
                && !PHONE_NUMBER_PATTERN.matcher(model.getPhoneNumber()).matches()) {
            errorMsg.append("手机号码格式不正确; ");
        }

        // 人员状态校验
        if (StringUtils.isNotBlank(model.getPersonnelStatus())
                && !PERSONNEL_STATUS_MAP.containsKey(model.getPersonnelStatus())
                && !model.getPersonnelStatus().equals("0")
                && !model.getPersonnelStatus().equals("1")) {
            errorMsg.append("人员状态必须为'在职'或'离职'; ");
        }

        // 安全教育状态校验
        if (StringUtils.isNotBlank(model.getSafetyEducation())
                && !SAFETY_EDUCATION_MAP.containsKey(model.getSafetyEducation())
                && !model.getSafetyEducation().equals("0")
                && !model.getSafetyEducation().equals("1")) {
            errorMsg.append("安全教育状态必须为'未开始'或'已培训'; ");
        }

        if (errorMsg.length() > 0) {
            throw new RuntimeException(errorMsg.toString());
        }
    }

    /**
     * 将Excel模型转换为实体类
     */
    private SwmPerson convertToPerson(SwmPersonExcelModel model) {
        SwmPerson person = new SwmPerson();
        person.setName(model.getName());
        person.setPersonType(model.getPersonType());
        person.setGender(model.getGender());
        person.setCompany(model.getCompany());
        person.setDepartment(model.getDepartment());
        person.setWorkProcess(model.getWorkProcess());
        person.setTeam(model.getTeam());
        person.setJobType(model.getJobType());
        person.setSafetyHelmetId(model.getSafetyHelmetId());

        // 处理安全教育状态
        String safetyEducation = model.getSafetyEducation();
        if (StringUtils.isNotBlank(safetyEducation)) {
            if (SAFETY_EDUCATION_MAP.containsKey(safetyEducation)) {
                person.setSafetyEducation(SAFETY_EDUCATION_MAP.get(safetyEducation));
            } else if ("0".equals(safetyEducation) || "1".equals(safetyEducation)) {
                person.setSafetyEducation(safetyEducation);
            } else {
                // 默认为未开始
                person.setSafetyEducation(SwmPerson.SafetyEducationEnum.NOT_STARTED);
            }
        } else {
            // 默认为未开始
            person.setSafetyEducation(SwmPerson.SafetyEducationEnum.NOT_STARTED);
        }

        person.setIdentityCard(model.getIdentityCard());
        person.setPhoneNumber(model.getPhoneNumber());

        // 处理人员状态
        String personnelStatus = model.getPersonnelStatus();
        if (StringUtils.isNotBlank(personnelStatus)) {
            if (PERSONNEL_STATUS_MAP.containsKey(personnelStatus)) {
                person.setPersonnelStatus(PERSONNEL_STATUS_MAP.get(personnelStatus));
            } else if ("0".equals(personnelStatus) || "1".equals(personnelStatus)) {
                person.setPersonnelStatus(personnelStatus);
            } else {
                // 默认为在职
                person.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
            }
        } else {
            // 默认为在职
            person.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
        }

        person.setRemarks(model.getRemarks());

        // 设置默认状态为0（正常）
        person.setStatus("0");

        return person;
    }

    public List<SwmPerson> getSuccessList() {
        return successList;
    }

    public List<SwmPersonExcelModel> getErrorList() {
        return errorList;
    }

    public int getTotal() {
        return total;
    }
}