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
import java.util.List;
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

        // 其他校验逻辑...

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
        person.setSafetyEducation(model.getSafetyEducation());
        person.setIdentityCard(model.getIdentityCard());
        person.setPhoneNumber(model.getPhoneNumber());
        person.setPersonnelStatus(model.getPersonnelStatus());
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