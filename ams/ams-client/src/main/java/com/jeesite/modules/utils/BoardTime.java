package com.jeesite.modules.utils;

import com.jeesite.modules.wms.common.zjgg.ZJGGTime;
import com.jeesite.modules.wms.common.zjgg.ZJGGTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
public class BoardTime extends ZJGGTime{

    /**
     * 季度开始时间
     */
    private LocalDate quarterStartDate;

    /**
     * 季度结束时间
     */
    private LocalDate quarterEndDate;


    /**
     * 季度开始时间
     */
    private LocalDateTime quarterStartTime;

    /**
     * 季度结束时间
     */
    private LocalDateTime quarterEndTime;

    public BoardTime(ZJGGTime zjggTime) {
        super.setDate(zjggTime.getDate());
        super.setDateStartTime(zjggTime.getDateStartTime());
        super.setDateEndTime(zjggTime.getDateEndTime());
        super.setMonthStartDate(zjggTime.getMonthStartDate());
        super.setMonthEndDate(zjggTime.getMonthEndDate());
        super.setMonth(zjggTime.getMonth());
        super.setMonthStartTime(zjggTime.getMonthStartTime());
        super.setMonthEndTime(zjggTime.getMonthEndTime());
        super.setYearStartDate(zjggTime.getYearStartDate());
        super.setYearEndDate(zjggTime.getYearEndDate());
        super.setYear(zjggTime.getYear());
        super.setYearStartTime(zjggTime.getYearStartTime());
        super.setYearEndTime(zjggTime.getYearEndTime());
        super.setYearStartMonth(zjggTime.getYearStartMonth());
        super.setYearEndMonth(zjggTime.getYearEndMonth());

        LocalDate now = LocalDate.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime startTime = LocalTime.of(ZJGGTimeUtil.START_HOUR, ZJGGTimeUtil.START_MINUTE);
        LocalTime endTime = LocalTime.of(ZJGGTimeUtil.END_HOUR, ZJGGTimeUtil.END_MINUTE,ZJGGTimeUtil.END_SECOND);
        int year = now.getYear();

        if (now.compareTo(LocalDate.of(year, 3, ZJGGTimeUtil.END_DATE)) < 0) {
            quarterStartDate = LocalDate.of(year, 12, ZJGGTimeUtil.START_DATE).minusYears(1);
            quarterEndDate = LocalDate.of(year, 3, ZJGGTimeUtil.END_DATE);
        }

        if ((now.compareTo(LocalDate.of(year, 3, ZJGGTimeUtil.START_DATE)) >= 0) &&
                (now.compareTo(LocalDate.of(year, 6, ZJGGTimeUtil.END_DATE))) < 0) {
            quarterStartDate = LocalDate.of(year, 3, ZJGGTimeUtil.START_DATE);
            quarterEndDate = LocalDate.of(year, 6, ZJGGTimeUtil.END_DATE);
        }

        if ((now.compareTo(LocalDate.of(year, 6, ZJGGTimeUtil.START_DATE)) >= 0) &&
                (now.compareTo(LocalDate.of(year, 9, ZJGGTimeUtil.END_DATE))) < 0) {
            quarterStartDate = LocalDate.of(year, 6, ZJGGTimeUtil.START_DATE);
            quarterEndDate = LocalDate.of(year, 9, ZJGGTimeUtil.END_DATE);
        }

        if ((now.compareTo(LocalDate.of(year, 9, ZJGGTimeUtil.END_DATE)) > 0) &&
                (now.compareTo(LocalDate.of(year, 12, ZJGGTimeUtil.END_DATE)) < 0)) {
            quarterStartDate = LocalDate.of(year, 9, ZJGGTimeUtil.START_DATE);
            quarterEndDate = LocalDate.of(year, 12, ZJGGTimeUtil.END_DATE);
        }

        if (now.compareTo(LocalDate.of(year, 12, ZJGGTimeUtil.END_DATE)) > 0) {
            quarterStartDate = LocalDate.of(year, 12, ZJGGTimeUtil.START_DATE);
            quarterEndDate = LocalDate.of(year, 3, ZJGGTimeUtil.END_DATE).plusYears(1);
        }

        quarterStartTime = LocalDateTime.of(quarterStartDate, startTime);
        quarterEndTime = LocalDateTime.of(quarterEndDate.plusDays(1), endTime);

    }

}
