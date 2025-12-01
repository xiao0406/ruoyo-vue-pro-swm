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
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmSafetyHelmetOrder;
import com.jeesite.modules.swm.service.OrgValidationService;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmSafetyHelmetOrderService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
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

    private int helmetBindSuccessCount = 0;
    private int helmetRebindCount = 0;
    private int helmetBindFailCount = 0;

    private List<Integer> helmetBindSuccessRows = new ArrayList<>();
    private List<Integer> helmetRebindRows = new ArrayList<>();
    private List<Integer> helmetBindFailRows = new ArrayList<>();

    private SwmPersonService swmPersonService;
    private OrgValidationService orgValidationService;
    private SwmHelmetDeviceService swmHelmetDeviceService;
    private SwmSafetyHelmetOrderService swmSafetyHelmetOrderService;

    public SwmPersonImportEnhancedListener() {
        this.swmPersonService = SpringUtils.getBean(SwmPersonService.class);
        this.orgValidationService = SpringUtils.getBean(OrgValidationService.class);
        this.swmHelmetDeviceService = SpringUtils.getBean(SwmHelmetDeviceService.class);
        this.swmSafetyHelmetOrderService = SpringUtils.getBean(SwmSafetyHelmetOrderService.class);
    }

    @Override
    public void invoke(SwmPersonExcelEnhancedModel data, AnalysisContext context) {
        totalCount++;
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        data.trimAll();
        data.setRowIndex(rowIndex);
        data.setSafetyHelmetCode(data.getTrimmedSafetyHelmetCode());

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
            errors.add("第" + rowIndex + "行，字段[数据处理]：发生异常 - " + e.getMessage());
            errorCount++;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // Step 1: 批次内排重检查
        List<String> batchDuplicateErrors = checkBatchDuplicates(list);
        if (!batchDuplicateErrors.isEmpty()) {
            errors.addAll(batchDuplicateErrors);
            errorCount += batchDuplicateErrors.size();
            logger.warn("批次内发现重复数据，停止导入：{}", batchDuplicateErrors);
            return; // 立即停止，不执行后续处理
        }

        // Step 2: 全局排重检查
        List<String> globalDuplicateErrors = checkGlobalDuplicates(list);
        if (!globalDuplicateErrors.isEmpty()) {
            errors.addAll(globalDuplicateErrors);
            errorCount += globalDuplicateErrors.size();
            logger.warn("与系统数据发现重复，停止导入：{}", globalDuplicateErrors);
            return; // 立即停止，不执行后续处理
        }

        // Step 3: 排重检查通过，继续原有的数据处理逻辑
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
                        data.getJobType(),
                        data.getPersonType(),
                        data.getDept(),
                        data.getPosition());

        if (!hierarchyResult.isSuccess()) {
            errors.add("第" + rowIndex + "行，字段[组织架构]：转换失败 - " + hierarchyResult.getErrorMessage());
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
        if (StringUtils.isNotBlank(hierarchyResult.getPersonTypeId())) {
            data.setPersonType(hierarchyResult.getPersonTypeId());
        }
        if (StringUtils.isNotBlank(hierarchyResult.getDept())) {
            data.setDept(hierarchyResult.getDept());
        }
        if (StringUtils.isNotBlank(hierarchyResult.getPosition())) {
            data.setPosition(hierarchyResult.getPosition());
        }

        // 记录转换信息
        for (String conversionMessage : hierarchyResult.getConversionMessages()) {
            warnings.add("第" + rowIndex + "行，字段[组织架构]：" + conversionMessage);
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
            errors.add("第" + rowIndex + "行，字段[姓名]：不能为空");
            valid = false;
        }

        if (StringUtils.isBlank(data.getGender())) {
            errors.add("第" + rowIndex + "行，字段[性别]：不能为空");
            valid = false;
        }

        if (StringUtils.isBlank(data.getPersonType())) {
            errors.add("第" + rowIndex + "行，字段[人员类型]：不能为空");
            valid = false;
        }

        if (StringUtils.isBlank(data.getIsExternalPersonnel())) {
            errors.add("第" + rowIndex + "行，字段[是否场内员工]：不能为空");
            valid = false;
        }

        // 身份证号码必填验证
        if (StringUtils.isBlank(data.getIdentityCard())) {
            errors.add("第" + rowIndex + "行，字段[身份证号码]：不能为空");
            valid = false;
        }

        // 手机号码必填验证
        if (StringUtils.isBlank(data.getPhoneNumber())) {
            errors.add("第" + rowIndex + "行，字段[手机号码]：不能为空");
            valid = false;
        }

        // 格式验证
        if (StringUtils.isNotBlank(data.getIdentityCard())) {
            if (!data.getIdentityCard().matches("^\\d{15}$|^\\d{18}$|^\\d{17}[Xx]$")) {
                errors.add("第" + rowIndex + "行，字段[身份证号码]：格式不正确");
                valid = false;
            }
        }

        if (StringUtils.isNotBlank(data.getPhoneNumber())) {
            if (!data.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
                errors.add("第" + rowIndex + "行，字段[手机号码]：格式不正确");
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
                errors.add("第" + rowIndex + "行，字段[所属单位]：厂内员工此字段不能为空");
                valid = false;
            }
//            if (StringUtils.isBlank(data.getDepartment())) {
//                errors.add("第" + rowIndex + "行，字段[所属车间]：厂内员工此字段不能为空");
//                valid = false;
//            }
//            if (StringUtils.isBlank(data.getProdLine())) {
//                errors.add("第" + rowIndex + "行，字段[所属产线]：厂内员工此字段不能为空");
//                valid = false;
//            }
//            if (StringUtils.isBlank(data.getTeam())) {
//                errors.add("第" + rowIndex + "行，字段[所属班组]：厂内员工此字段不能为空");
//                valid = false;
//            }
//            if (StringUtils.isBlank(data.getJobType())) {
//                errors.add("第" + rowIndex + "行，字段[所属工种]：厂内员工此字段不能为空");
//                valid = false;
//            }
        }

        // 人员类型验证
        if (StringUtils.isNotBlank(data.getPersonType())) {
            if (!SwmPerson.PersonTypeEnum.WORKER.equals(data.getPersonType()) &&
                    !SwmPerson.PersonTypeEnum.MANAGER.equals(data.getPersonType())
                    && !SwmPerson.PersonTypeEnum.TEAMLEADER.equals(data.getPersonType())
                    && !SwmPerson.PersonTypeEnum.SPECIALTRADES.equals(data.getPersonType())) {
                errors.add("第" + rowIndex + "行，字段[人员类型]：只能是0（工人）或1（管理者）");
                valid = false;
            }
        }

        // 性别验证
        if (StringUtils.isNotBlank(data.getGender())) {
            if (!"男".equals(data.getGender()) && !"女".equals(data.getGender())) {
                errors.add("第" + rowIndex + "行，字段[性别]：只能是男或女");
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
                errors.add("第" + rowIndex + "行，字段[组织架构]：层级关系验证失败 - " + result.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("第{}行层级关系验证发生异常", rowIndex, e);
            errors.add("第" + rowIndex + "行，字段[组织架构]：层级关系验证发生异常 - " + e.getMessage());
            return false;
        }
    }

    /**
     * 批次内排重检查
     * 
     * @param data 数据列表
     * @return 错误信息列表
     */
    private List<String> checkBatchDuplicates(List<SwmPersonExcelEnhancedModel> data) {
        List<String> errors = new ArrayList<>();

        // 身份证号码排重
        Map<String, List<Integer>> identityCardMap = new HashMap<>();
        // 手机号码排重
        Map<String, List<Integer>> phoneNumberMap = new HashMap<>();

        // 遍历数据收集重复信息
        for (int i = 0; i < data.size(); i++) {
            SwmPersonExcelEnhancedModel rowData = data.get(i);
            String identityCard = rowData.getIdentityCard();
            String phoneNumber = rowData.getPhoneNumber();

            // 收集身份证重复位置
            if (StringUtils.isNotBlank(identityCard)) {
                identityCardMap.computeIfAbsent(identityCard, k -> new ArrayList<>()).add(i + 2); // +2因为Excel从第2行开始
            }

            // 收集手机号重复位置
            if (StringUtils.isNotBlank(phoneNumber)) {
                phoneNumberMap.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(i + 2);
            }
        }

        // 生成身份证重复错误信息
        for (Map.Entry<String, List<Integer>> entry : identityCardMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String rowsStr = entry.getValue().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("行、"));
                errors.add(String.format("第%s行，字段[身份证号码]：存在重复 (%s)", rowsStr, entry.getKey()));
            }
        }

        // 生成手机号重复错误信息
        for (Map.Entry<String, List<Integer>> entry : phoneNumberMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String rowsStr = entry.getValue().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("行、"));
                errors.add(String.format("第%s行，字段[手机号码]：存在重复 (%s)", rowsStr, entry.getKey()));
            }
        }

        return errors;
    }

    /**
     * 全局排重检查
     * 
     * @param data 数据列表
     * @return 错误信息列表
     */
    private List<String> checkGlobalDuplicates(List<SwmPersonExcelEnhancedModel> data) {
        List<String> errors = new ArrayList<>();

        // 收集所有身份证号码和手机号码
        List<String> identityCards = data.stream()
                .map(SwmPersonExcelEnhancedModel::getIdentityCard)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        List<String> phoneNumbers = data.stream()
                .map(SwmPersonExcelEnhancedModel::getPhoneNumber)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        // 批量查询数据库中的在职人员
        List<SwmPerson> existingPersonsByIdentity = swmPersonService.findActivePersonsByIdentityCards(identityCards);
        List<SwmPerson> existingPersonsByPhone = swmPersonService.findActivePersonsByPhoneNumbers(phoneNumbers);

        // 构建查询结果的Map
        Map<String, SwmPerson> identityCardPersonMap = existingPersonsByIdentity.stream()
                .collect(Collectors.toMap(SwmPerson::getIdentityCard, p -> p));
        Map<String, SwmPerson> phoneNumberPersonMap = existingPersonsByPhone.stream()
                .collect(Collectors.toMap(SwmPerson::getPhoneNumber, p -> p));

        // 检查每行数据是否与数据库重复
        for (int i = 0; i < data.size(); i++) {
            SwmPersonExcelEnhancedModel rowData = data.get(i);
            int rowIndex = i + 2; // Excel从第2行开始

            // 检查身份证号码重复 - 允许更新同一人员
            if (StringUtils.isNotBlank(rowData.getIdentityCard())) {
                SwmPerson existingPerson = identityCardPersonMap.get(rowData.getIdentityCard());
                if (existingPerson != null) {
                    continue;
                }
            }

            // 检查手机号码重复 - 若与不同身份证重复则报错
            if (StringUtils.isNotBlank(rowData.getPhoneNumber())) {
                SwmPerson existingPerson = phoneNumberPersonMap.get(rowData.getPhoneNumber());
                if (existingPerson != null) {
                    String existingIdentity = existingPerson.getIdentityCard();
                    if (StringUtils.isBlank(rowData.getIdentityCard())
                            || !rowData.getIdentityCard().equals(existingIdentity)) {
                        errors.add(String.format("第%d行，字段[手机号码]：与在职人员[%s]重复 (%s)",
                                rowIndex, existingPerson.getName(), rowData.getPhoneNumber()));
                    }
                }
            }
        }

        return errors;
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (ListUtils.isEmpty(list)) {
            return;
        }

        for (SwmPersonExcelEnhancedModel excelModel : list) {
            int rowIndex = excelModel.getRowIndex() != null ? excelModel.getRowIndex() : (successCount + errorCount + 1);
            try {
                EnhancedRowContext context = buildRowContext(excelModel);
                validateHelmetBinding(context, rowIndex);

                SwmPerson person = context.getPerson();
                person.setSafetyEducation("1");
                swmPersonService.save(person);
                successCount++;

                processHelmetBinding(context, rowIndex);
            } catch (Exception e) {
                logger.error("保存第{}行数据时发生异常: {}", rowIndex, e.getMessage(), e);
                errors.add("第" + rowIndex + "行，字段[数据处理]：发生异常 - " + e.getMessage());
                errorCount++;
                if (StringUtils.isNotBlank(excelModel.getSafetyHelmetCode())) {
                    helmetBindFailCount++;
                    helmetBindFailRows.add(rowIndex);
                }
            }
        }

        list.clear();
    }

    private EnhancedRowContext buildRowContext(SwmPersonExcelEnhancedModel excelModel) {
        String identityCard = trimToNull(excelModel.getIdentityCard());
        SwmPerson existingPerson = identityCard != null ? swmPersonService.getByIdentityCard(identityCard) : null;
        boolean newRecord = existingPerson == null;

        SwmPerson person = newRecord ? new SwmPerson() : existingPerson;
        if (newRecord) {
            person.setIsNewRecord(true);
            person.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
            person.setSafetyEducation(SwmPerson.SafetyEducationEnum.NOT_STARTED);
            person.setHelmetReturned(SwmPerson.HelmetReturnedEnum.NO);
            person.setStatus("0");
        } else {
            person.setIsNewRecord(false);
        }

        applyExcelValues(person, excelModel);

        String previousHelmetId = existingPerson != null ? existingPerson.getSafetyHelmetId() : null;
        return new EnhancedRowContext(person, identityCard, excelModel.getSafetyHelmetCode(), previousHelmetId);
    }

    private void applyExcelValues(SwmPerson person, SwmPersonExcelEnhancedModel excelModel) {
        person.setName(trimToNull(excelModel.getName()));
        person.setPersonType(trimToNull(excelModel.getPersonType()));
        person.setGender(trimToNull(excelModel.getGender()));

        String identityCard = trimToNull(excelModel.getIdentityCard());
        if (identityCard != null) {
            person.setIdentityCard(identityCard);
        }

        person.setPhoneNumber(trimToNull(excelModel.getPhoneNumber()));
        person.setIsExternalPersonnel(excelModel.getStandardizedExternalPersonnel());

        if (excelModel.isInternalPersonnel()) {
            person.setCompany(trimToNull(excelModel.getCompany()));
            person.setDepartment(trimToNull(excelModel.getDepartment()));
            person.setProdLine(trimToNull(excelModel.getProdLine()));
            person.setTeam(trimToNull(excelModel.getTeam()));
            person.setJobType(trimToNull(excelModel.getJobType()));
            person.setDept(trimToNull(excelModel.getDept()));
            person.setPosition(trimToNull(excelModel.getPosition()));
        } else {
            person.setCompany(null);
            person.setDepartment(null);
            person.setProdLine(null);
            person.setTeam(null);
            person.setJobType(null);
            person.setDept(null);
            person.setPosition(null);
        }

        person.setRemarks(trimToNull(excelModel.getRemarks()));
    }

    private void validateHelmetBinding(EnhancedRowContext context, int rowIndex) {
        if (!context.hasHelmetCode()) {
            return;
        }

        if (StringUtils.isBlank(context.getIdentityCard())) {
            throw new RuntimeException("第" + rowIndex + "行，字段[身份证号码]：提供安全帽编码时必须填写身份证号码");
        }

        SwmHelmetDevice device = swmHelmetDeviceService.getByDeviceId(context.getHelmetCode());
        if (device == null) {
            throw new RuntimeException("第" + rowIndex + "行，字段[安全帽编码]：设备不存在 (" + context.getHelmetCode() + ")");
        }

        String assignedPerson = trimToNull(device.getAssignedPerson());
        if (assignedPerson != null && !assignedPerson.equals(context.getIdentityCard())) {
            throw new RuntimeException("第" + rowIndex + "行，字段[安全帽编码]：设备已绑定其他人员 (" + assignedPerson + ")");
        }

        context.setTargetHelmet(device);
        context.getPerson().setSafetyHelmetId(device.getDeviceId());
    }

    private void processHelmetBinding(EnhancedRowContext context, int rowIndex) {
        if (context.getTargetHelmet() == null) {
            return;
        }

        SwmHelmetDevice device = context.getTargetHelmet();
        SwmPerson person = context.getPerson();
        String newHelmetId = device.getDeviceId();
        String previousHelmetId = trimToNull(context.getPreviousHelmetId());

        if (previousHelmetId != null && !previousHelmetId.equals(newHelmetId)) {
            unbindPreviousHelmet(previousHelmetId, rowIndex);
            helmetRebindCount++;
            helmetRebindRows.add(rowIndex);
        } else {
            helmetBindSuccessCount++;
            helmetBindSuccessRows.add(rowIndex);
        }

        updateDeviceAssignment(device, person);
        ensureHelmetOrderExists(newHelmetId, person);
    }

    private void unbindPreviousHelmet(String helmetId, int rowIndex) {
        swmHelmetDeviceService.clearDeviceAssignment(helmetId);
        SwmSafetyHelmetOrder activeOrder = swmSafetyHelmetOrderService.findActiveOrderByDeviceId(helmetId);
        if (activeOrder != null) {
            swmSafetyHelmetOrderService.unbindHelmet(activeOrder.getId());
        }
    }

    private void updateDeviceAssignment(SwmHelmetDevice device, SwmPerson person) {
        device.setAssignedPerson(person.getIdentityCard());
        device.setPersonName(person.getName());
        device.setPersonPhone(person.getPhoneNumber());
        device.setAssignedWorkshop(person.getDepartment());
        device.setAssignedProcess(person.getWorkProcess());
        device.setAssignedTeam(person.getTeam());
        swmHelmetDeviceService.updateDevice(device);
    }

    private void ensureHelmetOrderExists(String helmetId, SwmPerson person) {
        SwmSafetyHelmetOrder activeOrder = swmSafetyHelmetOrderService.findActiveOrderByDeviceId(helmetId);
        if (activeOrder != null) {
            return;
        }

        swmSafetyHelmetOrderService.createBindingOrder(
                person.getId(),
                person.getName(),
                helmetId,
                resolveCurrentUser(),
                person.getIdentityCard());
    }

    private String resolveCurrentUser() {
        User user = UserUtils.getUser();
        if (user != null && StringUtils.isNotBlank(user.getLoginCode())) {
            return user.getLoginCode();
        }
        return "system";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
        result.setHelmetBindSuccessCount(helmetBindSuccessCount);
        result.setHelmetRebindCount(helmetRebindCount);
        result.setHelmetBindFailCount(helmetBindFailCount);
        result.setHelmetBindSuccessRows(new ArrayList<>(helmetBindSuccessRows));
        result.setHelmetRebindRows(new ArrayList<>(helmetRebindRows));
        result.setHelmetBindFailRows(new ArrayList<>(helmetBindFailRows));
        return result;
    }

    private static class EnhancedRowContext {
        private final SwmPerson person;
        private final String identityCard;
        private final String helmetCode;
        private final String previousHelmetId;
        private SwmHelmetDevice targetHelmet;

        EnhancedRowContext(SwmPerson person, String identityCard, String helmetCode, String previousHelmetId) {
            this.person = person;
            this.identityCard = identityCard;
            this.helmetCode = helmetCode;
            this.previousHelmetId = previousHelmetId;
        }

        public SwmPerson getPerson() {
            return person;
        }

        public String getIdentityCard() {
            return identityCard;
        }

        public String getHelmetCode() {
            return helmetCode;
        }

        public String getPreviousHelmetId() {
            return previousHelmetId;
        }

        public boolean hasHelmetCode() {
            return StringUtils.isNotBlank(helmetCode);
        }

        public SwmHelmetDevice getTargetHelmet() {
            return targetHelmet;
        }

        public void setTargetHelmet(SwmHelmetDevice targetHelmet) {
            this.targetHelmet = targetHelmet;
        }
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
        private int helmetBindSuccessCount;
        private int helmetRebindCount;
        private int helmetBindFailCount;
        private List<Integer> helmetBindSuccessRows;
        private List<Integer> helmetRebindRows;
        private List<Integer> helmetBindFailRows;

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

        public int getHelmetBindSuccessCount() {
            return helmetBindSuccessCount;
        }

        public void setHelmetBindSuccessCount(int helmetBindSuccessCount) {
            this.helmetBindSuccessCount = helmetBindSuccessCount;
        }

        public int getHelmetRebindCount() {
            return helmetRebindCount;
        }

        public void setHelmetRebindCount(int helmetRebindCount) {
            this.helmetRebindCount = helmetRebindCount;
        }

        public int getHelmetBindFailCount() {
            return helmetBindFailCount;
        }

        public void setHelmetBindFailCount(int helmetBindFailCount) {
            this.helmetBindFailCount = helmetBindFailCount;
        }

        public List<Integer> getHelmetBindSuccessRows() {
            return helmetBindSuccessRows;
        }

        public void setHelmetBindSuccessRows(List<Integer> helmetBindSuccessRows) {
            this.helmetBindSuccessRows = helmetBindSuccessRows;
        }

        public List<Integer> getHelmetRebindRows() {
            return helmetRebindRows;
        }

        public void setHelmetRebindRows(List<Integer> helmetRebindRows) {
            this.helmetRebindRows = helmetRebindRows;
        }

        public List<Integer> getHelmetBindFailRows() {
            return helmetBindFailRows;
        }

        public void setHelmetBindFailRows(List<Integer> helmetBindFailRows) {
            this.helmetBindFailRows = helmetBindFailRows;
        }
    }
}
