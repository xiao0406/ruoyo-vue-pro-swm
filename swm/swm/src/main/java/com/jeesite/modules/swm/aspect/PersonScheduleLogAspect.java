package com.jeesite.modules.swm.aspect;

import com.jeesite.common.idgen.IdGen;
import com.jeesite.modules.annotation.SavePersonScheduleLog;
import com.jeesite.modules.swm.dao.SwmPersonScheduleDao;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.entity.SwmPersonScheduleLog;
import com.jeesite.modules.swm.entity.dto.SwmPersonScheduleDto;
import com.jeesite.modules.swm.service.SwmPersonScheduleLogService;
import com.jeesite.modules.sys.utils.UserUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Aspect
@Component
public class PersonScheduleLogAspect {

    @Autowired
    private SwmPersonScheduleLogService swmPersonScheduleLogService;

    @Autowired
    private SwmPersonScheduleDao swmPersonScheduleDao; // 注入人员DAO，用于通过personId查询姓名

    // 时间格式化器（线程安全，避免重复创建）
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy年MM月dd日HH点mm分");

    @Pointcut("@annotation(com.jeesite.modules.annotation.SavePersonScheduleLog)")
    public void scheduleLogPointcut() {
    }

    @Around("scheduleLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 执行目标方法（单/批量修改）
        Object result = joinPoint.proceed();

        // 2. 提取人员ID列表和目标班次（原有逻辑，保持不变）
        Object[] args = joinPoint.getArgs();
        List<String> personIdList = new ArrayList<>();
        String targetClasses = null;
        for (Object arg : args) {
            if (arg instanceof SwmPersonScheduleDto) {
                SwmPersonScheduleDto dto = (SwmPersonScheduleDto) arg;
                if (dto.getIds() != null && !dto.getIds().isEmpty()) {
                    personIdList = dto.getIds();
                }
                targetClasses = dto.getClasses();
                break;
            } else if (arg instanceof SwmPersonSchedule) {
                SwmPersonSchedule schedule = (SwmPersonSchedule) arg;
                String singleId = schedule.getId();
                if (singleId != null && !singleId.trim().isEmpty()) {
                    personIdList.add(singleId);
                }
                targetClasses = schedule.getClasses();
                break;
            }
        }

        // 3. 空值判断（原有逻辑，保持不变）
        if (personIdList.isEmpty() || targetClasses == null || targetClasses.trim().isEmpty()) {
            return result;
        }

        // 4. 获取操作人信息（原有逻辑，保持不变）
        String userCode = "system";
        if (UserUtils.getUser() != null) {
            String tempUserCode = UserUtils.getUser().getUserCode();
            if (tempUserCode != null && !tempUserCode.trim().isEmpty()) {
                userCode = tempUserCode.trim();
            }
        }

        // 5. 获取注解属性和操作时间（原有逻辑，保持不变）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SavePersonScheduleLog annotation = method.getAnnotation(SavePersonScheduleLog.class);
        String remarkPrefix = annotation.remarkPrefix();
        String remark = remarkPrefix + targetClasses;
        Date operateTime = new Date();
        // 格式化操作时间：转为指定字符串格式
        String formatOperateTime = DATE_FORMATTER.format(operateTime);

        // 6. 遍历人员ID，构建日志（核心扩展：填充operateDesc）
        for (String singlePersonId : personIdList) {
            if (singlePersonId == null || singlePersonId.trim().isEmpty()) {
                continue;
            }

            // 扩展1：通过personId查询人员姓名（需实现SwmPersonDao.getPersonNameById方法）
            String personName = "未知人员"; // 兜底值，避免姓名为空
            SwmPersonSchedule query = new SwmPersonSchedule(singlePersonId);
            SwmPersonSchedule swmPersonSchedule = swmPersonScheduleDao.get(query); // 自定义DAO方法
            if (swmPersonSchedule != null && swmPersonSchedule.getPersonName() != null && !swmPersonSchedule.getPersonName().trim().isEmpty()) {
                personName = swmPersonSchedule.getPersonName().trim();
            }

            // 扩展2：转换班次名称（可选，如："3" → "白班"，根据你的业务字典调整）
            String classesName = getClassesName(targetClasses); // 自定义方法，映射班次编码与名称

            // 扩展3：拼接格式化操作描述
            String operateDesc = String.format("%s于%s，班次调整为‘%s’", personName, formatOperateTime, classesName);

            // 构建日志对象（原有逻辑 + 新增operateDesc赋值）
            SwmPersonScheduleLog scheduleLog = new SwmPersonScheduleLog();
            scheduleLog.setId(IdGen.uuid());
            scheduleLog.setOperateUser(userCode);
            scheduleLog.setOperateTime(operateTime);
            scheduleLog.setTargetClasses(targetClasses);
            scheduleLog.setPersonId(singlePersonId);
            scheduleLog.setRemark(remark);
            scheduleLog.setOperateDesc(operateDesc); // 赋值新增字段

            // 7. 保存日志（原有逻辑，保持不变）
            swmPersonScheduleLogService.saveScheduleLog(scheduleLog);
        }

        return result;
    }

    /**
     *
     * @param classesCode 班次编码（如："1"、"3"）
     * @return 班次名称（如："早班"、"晚班"）
     */
    private String getClassesName(String classesCode) {
        switch (classesCode) {
            case "1":
                return "早班";
            case "3":
                return "晚班";
            default:
                return classesCode; // 无匹配时，返回原始编码
        }
    }
}
