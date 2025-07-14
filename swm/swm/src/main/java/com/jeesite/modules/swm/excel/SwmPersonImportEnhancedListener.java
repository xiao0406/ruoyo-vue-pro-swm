/**
 * @author Shawn
 * @date 2025-01-15
 */
package com.jeesite.modules.swm.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.jeesite.common.collect.ListUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.SpringUtils;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.OrgValidationService;
import com.jeesite.modules.swm.service.SwmPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 人员导入增强监听器
 * 支持名称到编码的自动转换和更严格的验证
 */
public class SwmPersonImportEnhancedListener extends AnalysisEventListener<SwmPersonExcelEnhancedModel> {

    private static final Logger logger = LoggerFactory.getLogger(SwmPersonImportEnhancedListener.class);

    private static final int BATCH_COUNT = 1000;

    private List<SwmPersonExcelEnhancedModel> list = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private int totalCount = 0;
    private int successCount = 0;
    private int errorCount = 0;

    private SwmPersonService swmPersonService;
    private OrgValidationService orgValidationService;

    public SwmPersonImportEnhancedListener() {
        this.swmPersonService = SpringUtils.getBean(SwmPersonService.class);
        this.orgValidationService = SpringUtils.getBean(OrgValidationService.class);
    }

    @Override
    public void invoke(SwmPersonExcelEnhancedModel data, AnalysisContext context) {
        totalCount++;
        int rowIndex = context.readRowHolder().getRowIndex() + 1;

        try {
            // 步骤1：基础数据验证
            List<String> rowErrors = new ArrayList<>();
            if (!validateBasicData(data, rowIndex, rowErrors)) {
                errors.addAll(rowErrors);
                errorCount++;
                return;
            }

            // 步骤2：名称到编码转换（关键新增功能）
            if (!convertNamesToCodes(data, rowIndex, rowErrors)) {
                errors.addAll(rowErrors);
                errorCount++;
                return;
            }

            // 步骤3：业务逻辑验证
            if (!validateBusinessLogic(data, rowIndex, rowErrors)) {
                errors.addAll(rowErrors);
                errorCount++;
                return;
            }

            // 步骤4：层级关系验证（使用转换后的编码）
            if (!validateCascadeRelationship(data, rowIndex, rowErrors)) {
                errors.addAll(rowErrors);
                errorCount++;
                return;
            }

            // 验证通过，添加到处理列表
            list.add(data);

            if (list.size() >= BATCH_COUNT) {
                saveData();
            }

        } catch (Exception e) {
            logger.error("处理第{}行数据时发生异常: {}", rowIndex, e.getMessage(), e);
            errors.add("第" + rowIndex + "行处理时发生异常: " + e.getMessage());
            errorCount++;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        logger.info("所有数据解析完成，共处理{}条记录，成功{}条，失败{}条", totalCount, successCount, errorCount);
    }

    /**
     * 名称到编码转换（核心新增功能）
     * 
     * @param data     数据对象
     * @param rowIndex 行号
     * @param errors   错误列表
     * @return 是否转换成功
     */
    private boolean convertNamesToCodes(SwmPersonExcelEnhancedModel data, int rowIndex, List<String> errors) {
        // 只有内部员工才需要转换组织架构字段
        if (!data.isInternalPersonnel()) {
            return true; // 外部员工直接返回成功
        }

        // 使用新的层级转换方法
        OrgValidationService.HierarchyConversionResult hierarchyResult = orgValidationService
                .convertHierarchyNamesToCodes(
                        data.getCompany(),
                        data.getDepartment(),
                        data.getProdLine(),
                        data.getTeam(),
                        data.getJobType());

        if (!hierarchyResult.isSuccess()) {
            errors.add("第" + rowIndex + "行，组织架构字段转换失败: " + hierarchyResult.getErrorMessage());
            return false;
        }

        // 设置转换后的编码值
        if (StringUtils.isNotBlank(hierarchyResult.getCompanyId())) {
            data.setCompany(hierarchyResult.getCompanyId());
        }
        if (StringUtils.isNotBlank(hierarchyResult.getDepartmentId())) {
            data.setDepartment(hierarchyResult.getDepartmentId());
        }
        if (StringUtils.isNotBlank(hierarchyResult.getProdLineId())) {
            data.setProdLine(hierarchyResult.getProdLineId());
        }
        if (StringUtils.isNotBlank(hierarchyResult.getTeamId())) {
            data.setTeam(hierarchyResult.getTeamId());
        }
        if (StringUtils.isNotBlank(hierarchyResult.getJobTypeId())) {
            data.setJobType(hierarchyResult.getJobTypeId());
        }

        // 记录转换信息
        for (String conversionMessage : hierarchyResult.getConversionMessages()) {
            warnings.add("第" + rowIndex + "行，" + conversionMessage);
        }

        return true;
    }

    /**
     * 基础数据验证
     */
    private boolean validateBasicData(SwmPersonExcelEnhancedModel data, int rowIndex, List<String> errors) {
        boolean valid = true;

        // 必填字段验证
        if (StringUtils.isBlank(data.getName())) {
            errors.add("第" + rowIndex + "行，姓名不能为空");
            valid = false;
        }

        if (StringUtils.isBlank(data.getGender())) {
            errors.add("第" + rowIndex + "行，性别不能为空");
            valid = false;
        }

        if (StringUtils.isBlank(data.getPersonType())) {
            errors.add("第" + rowIndex + "行，人员类型不能为空");
            valid = false;
        }

        if (StringUtils.isBlank(data.getIsExternalPersonnel())) {
            errors.add("第" + rowIndex + "行，是否厂内员工不能为空");
            valid = false;
        }

        // 格式验证
        if (StringUtils.isNotBlank(data.getIdentityCard())) {
            if (!data.getIdentityCard().matches("^\\d{15}$|^\\d{18}$|^\\d{17}[Xx]$")) {
                errors.add("第" + rowIndex + "行，身份证号码格式不正确");
                valid = false;
            }
        }

        if (StringUtils.isNotBlank(data.getPhoneNumber())) {
            if (!data.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
                errors.add("第" + rowIndex + "行，手机号码格式不正确");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * 业务逻辑验证
     */
    private boolean validateBusinessLogic(SwmPersonExcelEnhancedModel data, int rowIndex, List<String> errors) {
        boolean valid = true;

        // 内部员工组织架构字段必填验证
        if (data.isInternalPersonnel()) {
            if (StringUtils.isBlank(data.getCompany())) {
                errors.add("第" + rowIndex + "行，厂内员工的所属单位不能为空");
                valid = false;
            }
            if (StringUtils.isBlank(data.getDepartment())) {
                errors.add("第" + rowIndex + "行，厂内员工的所属车间不能为空");
                valid = false;
            }
            if (StringUtils.isBlank(data.getProdLine())) {
                errors.add("第" + rowIndex + "行，厂内员工的产线不能为空");
                valid = false;
            }
            if (StringUtils.isBlank(data.getTeam())) {
                errors.add("第" + rowIndex + "行，厂内员工的所属班组不能为空");
                valid = false;
            }
            if (StringUtils.isBlank(data.getJobType())) {
                errors.add("第" + rowIndex + "行，厂内员工的所属工种不能为空");
                valid = false;
            }
        }

        // 人员类型验证
        if (StringUtils.isNotBlank(data.getPersonType())) {
            if (!SwmPerson.PersonTypeEnum.WORKER.equals(data.getPersonType()) &&
                    !SwmPerson.PersonTypeEnum.MANAGER.equals(data.getPersonType())) {
                errors.add("第" + rowIndex + "行，人员类型只能是：0（工人）或1（管理者）");
                valid = false;
            }
        }

        // 性别验证
        if (StringUtils.isNotBlank(data.getGender())) {
            if (!"男".equals(data.getGender()) && !"女".equals(data.getGender())) {
                errors.add("第" + rowIndex + "行，性别只能是：男或女");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * 层级关系验证（使用转换后的编码）
     */
    private boolean validateCascadeRelationship(SwmPersonExcelEnhancedModel data, int rowIndex, List<String> errors) {
        // 只有内部员工才需要验证层级关系
        if (!data.isInternalPersonnel()) {
            return true;
        }

        try {
            OrgValidationService.ValidationResult result = orgValidationService.validateCascade(
                    data.getCompany(), data.getDepartment(), data.getProdLine(), data.getTeam());

            if (!result.isValid()) {
                errors.add("第" + rowIndex + "行，组织架构层级关系验证失败: " + result.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("第{}行层级关系验证发生异常", rowIndex, e);
            errors.add("第" + rowIndex + "行，层级关系验证发生异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (ListUtils.isEmpty(list)) {
            return;
        }

        try {
            // 转换为SwmPerson对象
            List<SwmPerson> persons = list.stream().map(excelModel -> {
                SwmPerson person = new SwmPerson();
                person.setName(excelModel.getName());
                person.setPersonType(excelModel.getPersonType());
                person.setGender(excelModel.getGender());
                person.setIdentityCard(excelModel.getIdentityCard());
                person.setPhoneNumber(excelModel.getPhoneNumber());
                person.setIsExternalPersonnel(excelModel.getIsExternalPersonnel());

                // 组织架构字段
                if (excelModel.isInternalPersonnel()) {
                    person.setCompany(excelModel.getCompany());
                    person.setDepartment(excelModel.getDepartment());
                    person.setProdLine(excelModel.getProdLine());
                    person.setTeam(excelModel.getTeam());
                    person.setJobType(excelModel.getJobType());
                } else {
                    // 外部员工清空组织架构字段
                    person.setCompany(null);
                    person.setDepartment(null);
                    person.setProdLine(null);
                    person.setTeam(null);
                    person.setJobType(null);
                }

                // 设置默认值
                person.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
                person.setSafetyEducation(SwmPerson.SafetyEducationEnum.NOT_STARTED);
                person.setHelmetReturned(SwmPerson.HelmetReturnedEnum.NO);

                return person;
            }).collect(Collectors.toList());

            // 逐个保存
            for (SwmPerson person : persons) {
                swmPersonService.save(person);
                successCount++;
            }

        } catch (Exception e) {
            logger.error("保存数据时发生异常", e);
            errors.add("保存数据时发生异常: " + e.getMessage());
            errorCount += list.size();
        } finally {
            list.clear();
        }
    }

    /**
     * 获取导入结果
     */
    public ImportResult getImportResult() {
        ImportResult result = new ImportResult();
        result.setTotalCount(totalCount);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setErrors(errors);
        result.setWarnings(warnings);
        return result;
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private int totalCount;
        private int successCount;
        private int errorCount;
        private List<String> errors;
        private List<String> warnings;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }
}