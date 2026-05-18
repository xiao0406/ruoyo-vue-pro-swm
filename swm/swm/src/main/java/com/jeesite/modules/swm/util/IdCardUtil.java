package com.jeesite.modules.swm.util;

import com.jeesite.modules.swm.entity.SwmPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * 身份证工具类
 * 从身份证号码解析性别和年龄
 */
public class IdCardUtil {

    private static final Logger logger = LoggerFactory.getLogger(IdCardUtil.class);

    /**
     * 根据身份证号码自动设置人员的性别和年龄
     * 18位身份证：第7-14位为出生日期(YYYYMMDD)，第17位为性别（奇男偶女）
     */
    public static void fillGenderAndAge(SwmPerson person) {
        String idCard = person.getIdentityCard();
        if (idCard == null || idCard.trim().length() != 18) {
            return;
        }
        idCard = idCard.trim();
        try {
            char genderChar = idCard.charAt(16);
            if (Character.isDigit(genderChar)) {
                int genderNum = Character.getNumericValue(genderChar);
                person.setGender(genderNum % 2 == 1 ? "男" : "女");
            }
            String birthStr = idCard.substring(6, 14);
            LocalDate birthDate = LocalDate.parse(birthStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            person.setAge(String.valueOf(age));
        } catch (Exception e) {
            logger.warn("从身份证解析年龄性别失败，idCard={}", idCard, e);
        }
    }
}
