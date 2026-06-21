package cn.iocoder.yudao.module.swm.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * 身份证工具类
 * 从身份证号码解析性别和年龄
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.util.IdCardUtil
 * 已解耦对 SwmPerson 实体的依赖，改为纯值计算
 */
public class IdCardUtil {

    /**
     * 从身份证号解析性别
     *
     * @param idCard 18位身份证号码
     * @return "男" 或 "女"，解析失败返回 null
     */
    public static String parseGender(String idCard) {
        if (idCard == null || idCard.trim().length() != 18) {
            return null;
        }
        try {
            char genderChar = idCard.trim().charAt(16);
            if (Character.isDigit(genderChar)) {
                int genderNum = Character.getNumericValue(genderChar);
                return genderNum % 2 == 1 ? "男" : "女";
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 从身份证号解析年龄
     *
     * @param idCard 18位身份证号码
     * @return 年龄，解析失败返回 -1
     */
    public static int parseAge(String idCard) {
        if (idCard == null || idCard.trim().length() != 18) {
            return -1;
        }
        try {
            String birthStr = idCard.trim().substring(6, 14);
            LocalDate birthDate = LocalDate.parse(birthStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return -1;
        }
    }

}
