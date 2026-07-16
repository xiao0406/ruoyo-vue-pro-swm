package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.controller.admin.dashboard.vo.AttendanceAnalysisVO;
import cn.iocoder.yudao.module.swm.controller.admin.dashboard.vo.DashboardPersonVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDailyAttendanceDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.SwmDashboardDto;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.SwmMonthlyAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 姣忔棩鑰冨嫟 Mapper
 */
@Mapper
public interface SwmDailyAttendanceMapper extends BaseMapperX<SwmDailyAttendanceDO> {

    /** 同步班次调整到当天日考勤，保留旧系统导入后的联动行为。 */
    @Update("UPDATE swm_daily_attendance SET classes = #{classes}, update_date = NOW() " +
            "WHERE identity_card = #{idCard} AND attendance_date = CURRENT_DATE AND status = '0'")
    int updateTodayClasses(@Param("idCard") String idCard, @Param("classes") String classes);

    List<SwmDailyAttendanceDO> findList(@Param("employeeId") String employeeId,
            @Param("employeeName") String employeeName, @Param("attendanceDate") Date attendanceDate,
            @Param("beginAttendanceDate") Date beginAttendanceDate,
            @Param("endAttendanceDate") Date endAttendanceDate,
            @Param("workTimeRange") String workTimeRange, @Param("company") String company,
            @Param("department") String department, @Param("prodLine") String prodLine,
            @Param("team") String team, @Param("random") Integer random, @Param("page") PageParam page);

    SwmDailyAttendanceDO findByEmployeeIdAndDate(@Param("employeeId") String employeeId,
            @Param("attendanceDate") Date attendanceDate);

    SwmDailyAttendanceDO findByEmployeeAndDate(@Param("employeeName") String employeeName,
            @Param("attendanceDate") Date attendanceDate);

    List<SwmDailyAttendanceDO> findByEmployeeIdAndDateRange(@Param("employeeId") String employeeId,
            @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    List<SwmDailyAttendanceDO> findByEmployeeAndDateRange(@Param("employeeName") String employeeName,
            @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    List<SwmDailyAttendanceDO> findByDate(@Param("attendanceDate") Date attendanceDate);

    List<SwmDailyAttendanceDO> findByEmployeeIdAndMonth(@Param("employeeId") String employeeId,
            @Param("year") Integer year, @Param("month") Integer month);

    List<SwmDailyAttendanceDO> findByEmployeeAndMonth(@Param("employeeName") String employeeName,
            @Param("year") Integer year, @Param("month") Integer month);

    List<SwmDailyAttendanceDO> findByIdentityCardAndDate(@Param("identityCard") String identityCard,
            @Param("attendanceDate") Date attendanceDate);

    List<SwmDailyAttendanceDO> findByIdentityCardAndMonth(@Param("identityCard") String identityCard,
            @Param("month") String month);

    List<SwmMonthlyAttendance> findStatisticsByMonth(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmMonthlyAttendance> findStatisticsByMonthWithPage(@Param("startDate") String startDate,
            @Param("endDate") String endDate, @Param("team") String team,
            @Param("employeeName") String employeeName, @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize);

    Long findStatisticsTotalByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("team") String team, @Param("employeeName") String employeeName);

    List<Map<String, Object>> statisticsPersonJobType(@Param("date") String date);

    Long countAttendanceByWorkshop(@Param("date") String date, @Param("companyName") String companyName,
            @Param("workshopName") String workshopName);

    Long countAttendanceByTeam(@Param("date") String date, @Param("companyName") String companyName,
            @Param("workshopName") String workshopName, @Param("lineName") String lineName,
            @Param("teamName") String teamName);

    List<DashboardPersonVO> attendanceList(@Param("date") String date);

    List<SwmDailyAttendanceDO> findByDateRange(@Param("beginDate") Date beginDate,
            @Param("endDate") Date endDate);

    List<DashboardPersonVO> attendanceListPage(@Param("date") String date,
            @Param("personTypeList") List<String> personTypeList);

    Long attendanceListCount(@Param("date") String date,
            @Param("personTypeList") List<String> personTypeList);

    List<SwmDailyAttendanceDO> findByEmployeeIdAndDateBatch(@Param("employeeIds") List<String> employeeIds,
            @Param("attendanceDate") Date attendanceDate);

    List<SwmDailyAttendanceDO> findAllList(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDailyAttendanceDO> findClockInCardList(@Param("date") String date,
            @Param("random") Integer random);

    List<SwmDailyAttendanceDO> findClockOutCardList(@Param("yestDay") String yestDay,
            @Param("nowDate") String nowDate, @Param("random") Integer random);

    List<AttendanceAnalysisVO> getAllTeamNumber(@Param("companyCode") String companyCode);

    List<AttendanceAnalysisVO> getTeamAttendance(@Param("date") String date,
            @Param("companyCode") String companyCode);

    List<SwmDashboardDto.IdleHoursRankingDto> idleHoursRankingList(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDashboardDto.ManagementOnDutyDto> managementOnDuty(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDashboardDto.TeamAttendanceAnalysis> teamAttendanceAnalysis(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDashboardDto.TeamAttendanceAnalysis> departmentAttendanceAnalysis(
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<SwmDashboardDto.NoAttendancePerson> noAttendancePerson();

    List<SwmDashboardDto.NoAttendancePerson> beLatePerson(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDashboardDto.NoAttendancePerson> leaveEarlyPerson(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDashboardDto.NoAttendancePerson> beLatePersonWithDept(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    List<SwmDashboardDto.NoAttendancePerson> leaveEarlyPersonWithDept(@Param("startDate") String startDate,
            @Param("endDate") String endDate);

    SwmDailyAttendanceDO findClasses(@Param("classes") String classes);

    List<SwmDailyAttendanceDO> findDateByIdCards(@Param("idCard") List<String> idCard,
            @Param("nowDate") String nowDate);

    List<SwmDashboardDto.NoAttendancePerson> noMonthAttendancePerson(@Param("now") String now,
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    void updateWithClockTime(@Param("employeeId") String employeeId,
            @Param("employeeName") String employeeName, @Param("identityCard") String identityCard,
            @Param("personType") String personType, @Param("attendanceDate") Date attendanceDate,
            @Param("workTimeRange") String workTimeRange, @Param("clockInTime") String clockInTime,
            @Param("clockInDate") Date clockInDate, @Param("noonEndTime") String noonEndTime,
            @Param("noonEndDate") Date noonEndDate, @Param("afterStartTime") String afterStartTime,
            @Param("afterStartDate") Date afterStartDate, @Param("clockOutTime") String clockOutTime,
            @Param("clockOutDate") Date clockOutDate, @Param("scheduledHours") Double scheduledHours,
            @Param("actualHours") Double actualHours, @Param("idleHours") Double idleHours,
            @Param("effectiveWorkHours") Double effectiveWorkHours,
            @Param("dailyEfficiency") Double dailyEfficiency,
            @Param("dailyAchievementRate") Double dailyAchievementRate,
            @Param("attendanceNormal") String attendanceNormal,
            @Param("currentPosition") String currentPosition, @Param("updateBy") String updateBy,
            @Param("updateDate") Date updateDate, @Param("remarks") String remarks, @Param("id") Long id);

    void updateBatch(@Param("records") List<SwmDailyAttendanceDO> records);

    void updateAttendanceRecord(@Param("item") SwmDailyAttendanceDO item);
}

