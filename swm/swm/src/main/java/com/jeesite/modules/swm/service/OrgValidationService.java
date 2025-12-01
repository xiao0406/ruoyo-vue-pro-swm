/**
 * @author Shawn
 * @date 2025-01-15
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonDao;
import com.jeesite.modules.swm.dao.SwmCommonOptionsDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织架构验证服务
 */
@Service
public class OrgValidationService extends CrudService<SwmPersonDao, SwmPerson> {

    @Autowired
    private SwmPersonDao swmPersonDao;

    @Autowired
    private SwmCommonOptionsDao swmCommonOptionsDao;

    /**
     * 组织架构验证树
     */
    public static class OrgValidationTree {
        private Map<String, Map<String, Map<String, Set<String>>>> tree = new HashMap<>();
        // company -> department -> prodLine -> teams

        public void addPath(String company, String department, String prodLine, String team) {
            tree.computeIfAbsent(company, k -> new HashMap<>())
                    .computeIfAbsent(department, k -> new HashMap<>())
                    .computeIfAbsent(prodLine, k -> new HashSet<>())
                    .add(team);
        }

        public boolean validateCompany(String company) {
            return tree.containsKey(company);
        }

        public boolean validateDepartment(String company, String department) {
            return tree.containsKey(company) && tree.get(company).containsKey(department);
        }

        public boolean validateProdLine(String company, String department, String prodLine) {
            return validateDepartment(company, department)
                    && tree.get(company).get(department).containsKey(prodLine);
        }

        public boolean validateTeam(String company, String department, String prodLine, String team) {
            return validateProdLine(company, department, prodLine)
                    && tree.get(company).get(department).get(prodLine).contains(team);
        }

        public Set<String> getCompanies() {
            return tree.keySet();
        }

        public Set<String> getDepartments(String company) {
            return tree.getOrDefault(company, new HashMap<>()).keySet();
        }

        public Set<String> getProdLines(String company, String department) {
            return tree.getOrDefault(company, new HashMap<>())
                    .getOrDefault(department, new HashMap<>()).keySet();
        }

        public Set<String> getTeams(String company, String department, String prodLine) {
            return tree.getOrDefault(company, new HashMap<>())
                    .getOrDefault(department, new HashMap<>())
                    .getOrDefault(prodLine, new HashSet<>());
        }
    }

    /**
     * 工种选项
     */
    public static class WorkTypeOption {
        private String workType;

        public WorkTypeOption(String workType) {
            this.workType = workType;
        }

        public String getWorkType() {
            return workType;
        }

        public void setWorkType(String workType) {
            this.workType = workType;
        }
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private String errorCode;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public ValidationResult(boolean valid, String message, String errorCode) {
            this.valid = valid;
            this.message = message;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * 名称转换结果
     */
    public static class NameConversionResult {
        private String originalValue;
        private String convertedValue;
        private boolean isConverted;
        private String errorMessage;

        public NameConversionResult(String originalValue, String convertedValue, boolean isConverted) {
            this.originalValue = originalValue;
            this.convertedValue = convertedValue;
            this.isConverted = isConverted;
        }

        public NameConversionResult(String originalValue, String errorMessage) {
            this.originalValue = originalValue;
            this.errorMessage = errorMessage;
            this.isConverted = false;
        }

        public String getOriginalValue() {
            return originalValue;
        }

        public String getConvertedValue() {
            return convertedValue;
        }

        public boolean isConverted() {
            return isConverted;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 构建组织架构验证树
     */
    public OrgValidationTree buildOrgValidationTree() {
        OrgValidationTree tree = new OrgValidationTree();

        // 从数据库获取现有的组织架构数据
        SwmPerson criteria = new SwmPerson();
        criteria.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // 查询在职人员
        List<SwmPerson> activePersons = findList(criteria);

        for (SwmPerson person : activePersons) {
            String company = person.getCompany();
            String department = person.getDepartment();
            String prodLine = person.getProdLine();
            String team = person.getTeam();

            if (StringUtils.isNotBlank(company) && StringUtils.isNotBlank(department)
                    && StringUtils.isNotBlank(prodLine) && StringUtils.isNotBlank(team)) {
                tree.addPath(company, department, prodLine, team);
            }
        }

        return tree;
    }

    /**
     * 验证层级关系
     */
    public ValidationResult validateCascade(String companyId, String departmentId, String prodLineId, String teamId) {
        // 使用数据库查询进行更准确的验证

        // 验证车间是否属于指定单位
        if (StringUtils.isNotBlank(companyId) && StringUtils.isNotBlank(departmentId)) {
            boolean validDepartment = swmCommonOptionsDao.validateDepartmentBelongsToCompany(companyId, departmentId);
            if (!validDepartment) {
                return new ValidationResult(false, "车间不属于指定单位", "DEPARTMENT_NOT_BELONG_TO_COMPANY");
            }
        }

        // 验证产线是否属于指定车间
        if (StringUtils.isNotBlank(departmentId) && StringUtils.isNotBlank(prodLineId)) {
            boolean validProdLine = swmCommonOptionsDao.validateProdLineBelongsToDepartment(departmentId, prodLineId);
            if (!validProdLine) {
                return new ValidationResult(false, "产线不属于指定车间", "PRODLINE_NOT_BELONG_TO_DEPARTMENT");
            }
        }

        // 验证班组是否属于指定产线
        if (StringUtils.isNotBlank(prodLineId) && StringUtils.isNotBlank(teamId)) {
            boolean validTeam = swmCommonOptionsDao.validateTeamBelongsToProdLine(prodLineId, teamId);
            if (!validTeam) {
                return new ValidationResult(false, "班组不属于指定产线", "TEAM_NOT_BELONG_TO_PRODLINE");
            }
        }

        return new ValidationResult(true, "层级关系验证通过");
    }

    /**
     * 检测字段是否为编码格式
     * 编码通常是数字或字母数字组合，名称通常包含中文
     */
    public boolean isCodeFormat(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        // 编码格式：纯数字或字母数字组合，不包含中文
        return value.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * 转换公司名称为编码
     */
    public NameConversionResult convertCompanyNameToCode(String companyInput) {
        if (StringUtils.isBlank(companyInput)) {
            return new NameConversionResult(companyInput, companyInput, false);
        }

        // 如果已经是编码格式，直接返回
        if (isCodeFormat(companyInput)) {
            return new NameConversionResult(companyInput, companyInput, false);
        }

        // 查询编码
        try {
            String code = swmCommonOptionsDao.getCompanyCodeByName(companyInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(companyInput, code, true);
            } else {
                return new NameConversionResult(companyInput, "未找到公司名称对应的编码: " + companyInput);
            }
        } catch (Exception e) {
            return new NameConversionResult(companyInput, "查询公司编码时发生错误: " + e.getMessage());
        }
    }

    /**
     * 转换车间名称为编码（在指定单位下查找）
     */
    public NameConversionResult convertDepartmentNameToCode(String companyId, String departmentInput) {
        if (StringUtils.isBlank(departmentInput)) {
            return new NameConversionResult(departmentInput, departmentInput, false);
        }

        // 如果已经是编码格式，直接返回
        if (isCodeFormat(departmentInput)) {
            return new NameConversionResult(departmentInput, departmentInput, false);
        }

        // 需要单位ID才能查找车间
        if (StringUtils.isBlank(companyId)) {
            return new NameConversionResult(departmentInput, "查询车间编码需要单位ID参数");
        }

        // 查询编码
        try {
            String code = swmCommonOptionsDao.getDepartmentCodeByName(companyId, departmentInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(departmentInput, code, true);
            } else {
                return new NameConversionResult(departmentInput,
                        "未找到车间名称对应的编码: " + departmentInput + "（所属单位：" + companyId + "）");
            }
        } catch (Exception e) {
            return new NameConversionResult(departmentInput, "查询车间编码时发生错误: " + e.getMessage());
        }
    }

    /**
     * 转换产线名称为编码（在指定车间下查找）
     */
    public NameConversionResult convertProdLineNameToCode(String departmentId, String prodLineInput) {
        if (StringUtils.isBlank(prodLineInput)) {
            return new NameConversionResult(prodLineInput, prodLineInput, false);
        }

        // 如果已经是编码格式，直接返回
        if (isCodeFormat(prodLineInput)) {
            return new NameConversionResult(prodLineInput, prodLineInput, false);
        }

        // 需要车间ID才能查找产线
        if (StringUtils.isBlank(departmentId)) {
            return new NameConversionResult(prodLineInput, "查询产线编码需要车间ID参数");
        }

        // 查询编码
        try {
            String code = swmCommonOptionsDao.getProdLineCodeByName(departmentId, prodLineInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(prodLineInput, code, true);
            } else {
                return new NameConversionResult(prodLineInput,
                        "未找到产线名称对应的编码: " + prodLineInput + "（所属车间：" + departmentId + "）");
            }
        } catch (Exception e) {
            return new NameConversionResult(prodLineInput, "查询产线编码时发生错误: " + e.getMessage());
        }
    }

    /**
     * 转换班组名称为编码（在指定产线下查找）
     */
    public NameConversionResult convertTeamNameToCode(String prodLineId, String teamInput) {
        if (StringUtils.isBlank(teamInput)) {
            return new NameConversionResult(teamInput, teamInput, false);
        }

        // 如果已经是编码格式，直接返回
        if (isCodeFormat(teamInput)) {
            return new NameConversionResult(teamInput, teamInput, false);
        }

        // 需要产线ID才能查找班组
        if (StringUtils.isBlank(prodLineId)) {
            return new NameConversionResult(teamInput, "查询班组编码需要产线ID参数");
        }

        // 查询编码
        try {
            String code = swmCommonOptionsDao.getTeamCodeByName(prodLineId, teamInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(teamInput, code, true);
            } else {
                return new NameConversionResult(teamInput, "未找到班组名称对应的编码: " + teamInput + "（所属产线：" + prodLineId + "）");
            }
        } catch (Exception e) {
            return new NameConversionResult(teamInput, "查询班组编码时发生错误: " + e.getMessage());
        }
    }

    /**
     * 转换工种名称为编码(工种的编码和名称相同)
     */
    public NameConversionResult convertJobTypeNameToCode(String jobTypeInput) {
        if (StringUtils.isBlank(jobTypeInput)) {
            return new NameConversionResult(jobTypeInput, jobTypeInput, false);
        }

        // 查询验证工种是否存在
        try {

            List<DictData> deptDictList1 = DictUtils.getDictList("swm_job_type_0");
            List<DictData> deptDictList2 = DictUtils.getDictList("swm_job_type_1");
            List<DictData> deptDictList3 = DictUtils.getDictList("swm_job_type_2");
            List<DictData> deptDictList4 = DictUtils.getDictList("swm_job_type_3");
            List<DictData> deptDictList = new ArrayList<>();
            deptDictList.addAll(deptDictList1);
            deptDictList.addAll(deptDictList2);
            deptDictList.addAll(deptDictList3);
            deptDictList.addAll(deptDictList4);
            if (deptDictList == null){
                return new NameConversionResult(jobTypeInput, "未找到工种: " + jobTypeInput);
            }
            Map<String, String> jobDictMap = deptDictList.stream()
                    .collect(Collectors.toMap(
                            DictData::getDictLabelRaw,
                            DictData::getDictLabelRaw,
                            (v1, v2) -> v2));

            String code = jobDictMap.get(jobTypeInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(jobTypeInput, jobTypeInput, true);
            } else {
                return new NameConversionResult(jobTypeInput, "未找到工种: " + jobTypeInput);
            }
        } catch (Exception e) {
            return new NameConversionResult(jobTypeInput, "查询工种时发生错误: " + e.getMessage());
        }
    }

    /**
     * 转换人员类型名称为编码
     */
    public NameConversionResult convertPersonTypeNameToCode(String personTypeInput) {
        if (StringUtils.isBlank(personTypeInput)) {
            return new NameConversionResult(personTypeInput, personTypeInput, false);
        }

        // 如果已经是编码格式，直接返回
        if (isCodeFormat(personTypeInput)) {
            // 验证编码是否有效
            if ("0".equals(personTypeInput) || "1".equals(personTypeInput)) {
                return new NameConversionResult(personTypeInput, personTypeInput, false);
            } else {
                return new NameConversionResult(personTypeInput, "无效的人员类型编码: " + personTypeInput);
            }
        }

        // 先尝试直接转换常见的输入格式
        String normalizedInput = normalizePersonTypeInput(personTypeInput);
        if (normalizedInput != null) {
            return new NameConversionResult(personTypeInput, normalizedInput, true);
        }

        // 查询字典表进行转换
        try {
            String code = swmCommonOptionsDao.getPersonTypeCodeByName(personTypeInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(personTypeInput, code, true);
            } else {
                return new NameConversionResult(personTypeInput, "未找到人员类型: " + personTypeInput +
                        "（支持格式：工人/0/worker，管理者/管理员/1/manager）");
            }
        } catch (Exception e) {
            return new NameConversionResult(personTypeInput, "查询人员类型时发生错误: " + e.getMessage());
        }
    }


    /**
     * 转换部门名称为编码
     * @param deptInput
     * @return
     */
    private NameConversionResult convertDeptNameToCode(String deptInput) {
        if (StringUtils.isBlank(deptInput)) {
            return new NameConversionResult(deptInput, deptInput, false);
        }

        // 查询验证部门是否存在
        try {
            List<DictData> deptDictList = DictUtils.getDictList("swm_dept");
            if (deptDictList == null){
                return new NameConversionResult(deptInput, "未找到部门: " + deptInput);
            }
            Map<String, String> deptDictMap = deptDictList.stream()
                    .collect(Collectors.toMap(
                            DictData::getDictLabelRaw,
                            DictData::getDictValue,
                            (v1, v2) -> v2));
            String code = deptDictMap.get(deptInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(deptInput, code, true);
            } else {
                return new NameConversionResult(deptInput, "未找到部门: " + deptInput);
            }
        } catch (Exception e) {
            return new NameConversionResult(deptInput, "查询部门时发生错误: " + e.getMessage());
        }
    }

    /**
     * 转换职务名称为编码
     * @param positionInput
     * @return
     */
    private NameConversionResult convertPositionNameToCode(String positionInput) {
        if (StringUtils.isBlank(positionInput)) {
            return new NameConversionResult(positionInput, positionInput, false);
        }

        // 查询验证职务是否存在
        try {
            List<DictData> deptDictList = DictUtils.getDictList("swm_position");
            if (deptDictList == null){
                return new NameConversionResult(positionInput, "未找到职务: " + positionInput);
            }
            Map<String, String> deptDictMap = deptDictList.stream()
                    .collect(Collectors.toMap(
                            DictData::getDictLabelRaw,
                            DictData::getDictValue,
                            (v1, v2) -> v2));

            String code = deptDictMap.get(positionInput);
            if (StringUtils.isNotBlank(code)) {
                return new NameConversionResult(positionInput, code, true);
            } else {
                return new NameConversionResult(positionInput, "未找到职务: " + positionInput);
            }
        } catch (Exception e) {
            return new NameConversionResult(positionInput, "查询职务时发生错误: " + e.getMessage());
        }
    }



    /**
     * 标准化人员类型输入
     * 支持多种输入格式的快速转换
     */
    private String normalizePersonTypeInput(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        String value = input.trim().toLowerCase();

        // 工人相关输入 -> 0
        if ("工人".equals(input.trim()) || "0".equals(value) || "worker".equals(value)) {
            return "0";
        }

        // 管理者相关输入 -> 1
        if ("管理者".equals(input.trim()) || "管理员".equals(input.trim()) ||
                "1".equals(value) || "manager".equals(value)) {
            return "1";
        }

        return null;
    }

    /**
     * 层级转换结果
     */
    public static class HierarchyConversionResult {
        private String companyId;
        private String departmentId;
        private String prodLineId;
        private String teamId;
        private String jobTypeId;
        private String personTypeId;
        private boolean success;
        private String errorMessage;
        private String dept;
        private String position;
        private List<String> conversionMessages = new ArrayList<>();

        public HierarchyConversionResult() {
        }

        public HierarchyConversionResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        // Getters and setters
        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(String departmentId) {
            this.departmentId = departmentId;
        }

        public String getProdLineId() {
            return prodLineId;
        }

        public void setProdLineId(String prodLineId) {
            this.prodLineId = prodLineId;
        }

        public String getTeamId() {
            return teamId;
        }

        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }

        public String getJobTypeId() {
            return jobTypeId;
        }

        public void setJobTypeId(String jobTypeId) {
            this.jobTypeId = jobTypeId;
        }

        public String getPersonTypeId() {
            return personTypeId;
        }

        public void setPersonTypeId(String personTypeId) {
            this.personTypeId = personTypeId;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public List<String> getConversionMessages() {
            return conversionMessages;
        }

        public void setConversionMessages(List<String> conversionMessages) {
            this.conversionMessages = conversionMessages;
        }

        public void addConversionMessage(String message) {
            this.conversionMessages.add(message);
        }

        public String getDept() {
            return dept;
        }

        public void setDept(String dept) {
            this.dept = dept;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    /**
     * 按层级顺序转换组织架构名称为编码
     * 
     * @param companyName    单位名称
     * @param departmentName 车间名称
     * @param prodLineName   产线名称
     * @param teamName       班组名称
     * @param jobTypeName    工种名称
     * @param personTypeName 人员类型名称
     * @return 层级转换结果
     */
    public HierarchyConversionResult convertHierarchyNamesToCodes(
            String companyName, String departmentName, String prodLineName, String teamName, String jobTypeName,
            String personTypeName,String dept,String position) {

        HierarchyConversionResult result = new HierarchyConversionResult();

        // 1. 转换单位名称为编码
        NameConversionResult companyResult = convertCompanyNameToCode(companyName);
        if (!companyResult.isConverted() && StringUtils.isNotBlank(companyResult.getErrorMessage())) {
            result.setSuccess(false);
            result.setErrorMessage("单位转换失败: " + companyResult.getErrorMessage());
            return result;
        }
        result.setCompanyId(companyResult.getConvertedValue());
        if (companyResult.isConverted()) {
            result.addConversionMessage("单位名称 '" + companyName + "' 转换为编码: " + companyResult.getConvertedValue());
        }

        // 2. 转换车间名称为编码（基于单位ID）
        if (StringUtils.isNotBlank(departmentName)) {
            NameConversionResult departmentResult = convertDepartmentNameToCode(result.getCompanyId(), departmentName);
            if (!departmentResult.isConverted() && StringUtils.isNotBlank(departmentResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("车间转换失败: " + departmentResult.getErrorMessage());
                return result;
            }
            result.setDepartmentId(departmentResult.getConvertedValue());
            if (departmentResult.isConverted()) {
                result.addConversionMessage(
                        "车间名称 '" + departmentName + "' 转换为编码: " + departmentResult.getConvertedValue());
            }
        }

        // 3. 转换产线名称为编码（基于车间ID）
        if (StringUtils.isNotBlank(prodLineName)) {
            if (StringUtils.isBlank(result.getDepartmentId())) {
                result.setSuccess(false);
                result.setErrorMessage("产线转换失败: 需要车间信息才能转换产线");
                return result;
            }
            NameConversionResult prodLineResult = convertProdLineNameToCode(result.getDepartmentId(), prodLineName);
            if (!prodLineResult.isConverted() && StringUtils.isNotBlank(prodLineResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("产线转换失败: " + prodLineResult.getErrorMessage());
                return result;
            }
            result.setProdLineId(prodLineResult.getConvertedValue());
            if (prodLineResult.isConverted()) {
                result.addConversionMessage("产线名称 '" + prodLineName + "' 转换为编码: " + prodLineResult.getConvertedValue());
            }
        }

        // 4. 转换班组名称为编码（基于产线ID）
        if (StringUtils.isNotBlank(teamName)) {
            if (StringUtils.isBlank(result.getProdLineId())) {
                result.setSuccess(false);
                result.setErrorMessage("班组转换失败: 需要产线信息才能转换班组");
                return result;
            }
            NameConversionResult teamResult = convertTeamNameToCode(result.getProdLineId(), teamName);
            if (!teamResult.isConverted() && StringUtils.isNotBlank(teamResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("班组转换失败: " + teamResult.getErrorMessage());
                return result;
            }
            result.setTeamId(teamResult.getConvertedValue());
            if (teamResult.isConverted()) {
                result.addConversionMessage("班组名称 '" + teamName + "' 转换为编码: " + teamResult.getConvertedValue());
            }
        }

        // 5. 转换工种名称为编码
        if (StringUtils.isNotBlank(jobTypeName)) {
            NameConversionResult jobTypeResult = convertJobTypeNameToCode(jobTypeName);
            if (!jobTypeResult.isConverted() && StringUtils.isNotBlank(jobTypeResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("工种转换失败: " + jobTypeResult.getErrorMessage());
                return result;
            }
            result.setJobTypeId(jobTypeResult.getConvertedValue());
            if (jobTypeResult.isConverted()) {
                result.addConversionMessage("工种名称 '" + jobTypeName + "' 转换为编码: " + jobTypeResult.getConvertedValue());
            }
        }

        // 6. 转换人员类型名称为编码
        if (StringUtils.isNotBlank(personTypeName)) {
            NameConversionResult personTypeResult = convertPersonTypeNameToCode(personTypeName);
            if (!personTypeResult.isConverted() && StringUtils.isNotBlank(personTypeResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("人员类型转换失败: " + personTypeResult.getErrorMessage());
                return result;
            }
            result.setPersonTypeId(personTypeResult.getConvertedValue());
            if (personTypeResult.isConverted()) {
                result.addConversionMessage(
                        "人员类型名称 '" + personTypeName + "' 转换为编码: " + personTypeResult.getConvertedValue());
            }
        }

        // 7. 转换部门名称为编码
        if (StringUtils.isNotBlank(dept)) {
            NameConversionResult deptResult = convertDeptNameToCode(dept);
            if (!deptResult.isConverted() && StringUtils.isNotBlank(deptResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("部门转换失败: " + deptResult.getErrorMessage());
                return result;
            }
            result.setDept(deptResult.getConvertedValue());
            if (deptResult.isConverted()) {
                result.addConversionMessage("部门名称 '" + jobTypeName + "' 转换为编码: " + deptResult.getConvertedValue());
            }
        }

        // 8. 转换职务名称为编码
        if (StringUtils.isNotBlank(position)) {
            NameConversionResult positionResult = convertPositionNameToCode(position);
            if (!positionResult.isConverted() && StringUtils.isNotBlank(positionResult.getErrorMessage())) {
                result.setSuccess(false);
                result.setErrorMessage("职务转换失败: " + positionResult.getErrorMessage());
                return result;
            }
            result.setPosition(positionResult.getConvertedValue());
            if (positionResult.isConverted()) {
                result.addConversionMessage("职务名称 '" + jobTypeName + "' 转换为编码: " + positionResult.getConvertedValue());
            }
        }

        result.setSuccess(true);
        return result;
    }


    /**
     * 生成模板选项
     */
    public Map<String, Object> generateOrgOptionsForTemplate() {
        Map<String, Object> result = new HashMap<>();

        // 获取所有选项
        List<Map<String, Object>> companies = swmCommonOptionsDao.getCompanyOptions();
        List<Map<String, Object>> departments = swmCommonOptionsDao.getDepartmentOptions();
        List<Map<String, Object>> prodLines = swmCommonOptionsDao.getProdLineOptions();
        List<Map<String, Object>> teams = swmCommonOptionsDao.getWorkGroupOptions();
        List<Map<String, Object>> workTypes = swmCommonOptionsDao.getWorkTypeOptions();

        result.put("companies", companies);
        result.put("departments", departments);
        result.put("prodLines", prodLines);
        result.put("teams", teams);
        result.put("workTypes", workTypes);

        return result;
    }

    /**
     * 测试层级转换逻辑
     * 
     * @param companyName    单位名称
     * @param departmentName 车间名称
     * @param prodLineName   产线名称
     * @param teamName       班组名称
     * @param jobTypeName    工种名称
     * @param personTypeName 人员类型名称
     * @return 测试结果描述
     */
    public String testHierarchyConversion(String companyName, String departmentName,
            String prodLineName, String teamName, String jobTypeName, String personTypeName) {
        StringBuilder result = new StringBuilder();
        result.append("=== 层级转换测试 ===\n");
        result.append("输入数据：\n");
        result.append("单位: ").append(companyName).append("\n");
        result.append("车间: ").append(departmentName).append("\n");
        result.append("产线: ").append(prodLineName).append("\n");
        result.append("班组: ").append(teamName).append("\n");
        result.append("工种: ").append(jobTypeName).append("\n");
        result.append("人员类型: ").append(personTypeName).append("\n\n");

        HierarchyConversionResult conversionResult = convertHierarchyNamesToCodes(
                companyName, departmentName, prodLineName, teamName, jobTypeName, personTypeName,null,null);

        if (conversionResult.isSuccess()) {
            result.append("转换成功！\n");
            result.append("转换结果：\n");
            result.append("单位ID: ").append(conversionResult.getCompanyId()).append("\n");
            result.append("车间ID: ").append(conversionResult.getDepartmentId()).append("\n");
            result.append("产线ID: ").append(conversionResult.getProdLineId()).append("\n");
            result.append("班组ID: ").append(conversionResult.getTeamId()).append("\n");
            result.append("工种ID: ").append(conversionResult.getJobTypeId()).append("\n");
            result.append("人员类型ID: ").append(conversionResult.getPersonTypeId()).append("\n\n");

            if (!conversionResult.getConversionMessages().isEmpty()) {
                result.append("转换信息：\n");
                for (String message : conversionResult.getConversionMessages()) {
                    result.append("- ").append(message).append("\n");
                }
            }
        } else {
            result.append("转换失败：").append(conversionResult.getErrorMessage()).append("\n");
        }

        return result.toString();
    }
}