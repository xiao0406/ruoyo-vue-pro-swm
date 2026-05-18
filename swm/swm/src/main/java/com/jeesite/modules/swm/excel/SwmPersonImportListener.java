/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.entity.SwmSafetyHelmetOrder;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmSafetyHelmetOrderService;
import com.jeesite.modules.swm.util.IdCardUtil;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final SwmHelmetDeviceService swmHelmetDeviceService;
    private final SwmSafetyHelmetOrderService swmSafetyHelmetOrderService;

    private int helmetBindSuccessCount;
    private int helmetRebindCount;
    private int helmetBindFailCount;

    private final List<Integer> helmetBindSuccessRows = new ArrayList<>();
    private final List<Integer> helmetRebindRows = new ArrayList<>();
    private final List<Integer> helmetBindFailRows = new ArrayList<>();

    public SwmPersonImportListener(SwmPersonService swmPersonService,
            SwmHelmetDeviceService swmHelmetDeviceService,
            SwmSafetyHelmetOrderService swmSafetyHelmetOrderService) {
        this.swmPersonService = swmPersonService;
        this.swmHelmetDeviceService = swmHelmetDeviceService;
        this.swmSafetyHelmetOrderService = swmSafetyHelmetOrderService;
    }

    @Override
    public void invoke(SwmPersonExcelModel excelModel, AnalysisContext analysisContext) {
        total++;
        int rowIndex = analysisContext.readRowHolder().getRowIndex() + 1;
        try {
            excelModel.trimAll();
            validateData(excelModel);

            PersonRowContext rowContext = buildRowContext(excelModel);
            validateHelmetBindingPreconditions(rowContext, rowIndex);

            swmPersonService.save(rowContext.getPerson());
            successList.add(rowContext.getPerson());

            processHelmetBinding(rowContext, rowIndex);
        } catch (Exception e) {
            logger.error("导入第{}行人员数据失败: {}", rowIndex, e.getMessage(), e);
            excelModel.setErrorMsg(e.getMessage());
            errorList.add(excelModel);
            if (StringUtils.isNotBlank(trimToNull(excelModel.getSafetyHelmetCode()))) {
                helmetBindFailCount++;
                helmetBindFailRows.add(rowIndex);
            }
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

        if (StringUtils.isNotBlank(model.getSafetyHelmetCode())
                && StringUtils.isBlank(model.getIdentityCard())) {
            errorMsg.append("提供安全帽编码时必须填写身份证号码; ");
        }

        if (errorMsg.length() > 0) {
            throw new RuntimeException(errorMsg.toString());
        }
    }

    private PersonRowContext buildRowContext(SwmPersonExcelModel model) {
        String identityCard = trimToNull(model.getIdentityCard());
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

        applyExcelValues(person, model, newRecord);

        String helmetCode = trimToNull(model.getSafetyHelmetCode());
        String previousHelmetId = existingPerson != null ? existingPerson.getSafetyHelmetId() : null;

        return new PersonRowContext(person, identityCard, helmetCode, previousHelmetId);
    }

    private void applyExcelValues(SwmPerson person, SwmPersonExcelModel model, boolean newRecord) {
        String name = trimToNull(model.getName());
        if (name != null) {
            person.setName(name);
        }

        String personType = trimToNull(model.getPersonType());
        if (personType != null) {
            person.setPersonType(personType);
        }

        String gender = trimToNull(model.getGender());
        if (gender != null) {
            person.setGender(gender);
        }

        String identityCard = trimToNull(model.getIdentityCard());
        if (identityCard != null) {
            person.setIdentityCard(identityCard);
            IdCardUtil.fillGenderAndAge(person);
        }

        String phoneNumber = trimToNull(model.getPhoneNumber());
        if (phoneNumber != null || newRecord) {
            person.setPhoneNumber(phoneNumber);
        }

        String remarks = trimToNull(model.getRemarks());
        if (remarks != null || newRecord) {
            person.setRemarks(remarks);
        }
    }

    private void validateHelmetBindingPreconditions(PersonRowContext context, int rowIndex) {
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

    private void processHelmetBinding(PersonRowContext context, int rowIndex) {
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

    public List<SwmPerson> getSuccessList() {
        return successList;
    }

    public List<SwmPersonExcelModel> getErrorList() {
        return errorList;
    }

    public int getTotal() {
        return total;
    }

    public int getHelmetBindSuccessCount() {
        return helmetBindSuccessCount;
    }

    public int getHelmetRebindCount() {
        return helmetRebindCount;
    }

    public int getHelmetBindFailCount() {
        return helmetBindFailCount;
    }

    public List<Integer> getHelmetBindSuccessRows() {
        return helmetBindSuccessRows;
    }

    public List<Integer> getHelmetRebindRows() {
        return helmetRebindRows;
    }

    public List<Integer> getHelmetBindFailRows() {
        return helmetBindFailRows;
    }

    private static class PersonRowContext {
        private final SwmPerson person;
        private final String identityCard;
        private final String helmetCode;
        private final String previousHelmetId;
        private SwmHelmetDevice targetHelmet;

        PersonRowContext(SwmPerson person, String identityCard, String helmetCode,
                String previousHelmetId) {
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

        public boolean hasHelmetCode() {
            return StringUtils.isNotBlank(helmetCode);
        }

        public String getHelmetCode() {
            return helmetCode;
        }

        public String getPreviousHelmetId() {
            return previousHelmetId;
        }

        public SwmHelmetDevice getTargetHelmet() {
            return targetHelmet;
        }

        public void setTargetHelmet(SwmHelmetDevice targetHelmet) {
            this.targetHelmet = targetHelmet;
        }
    }
}
