package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmMonthlyAttendance;
import com.jeesite.modules.swm.entity.dto.SwmDashboardDto;
import com.jeesite.modules.swm.web.SwmDashboardNewController;
import com.jeesite.modules.entity.AiDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 日考勤统计表DAO接口
 * 
 * @author zwf
 * @version 2025-05-20
 */
@MyBatisDao
public interface SwmDailyAttendanceDao extends CrudDao<SwmDailyAttendance> {

    /**
     * 自定义更新方法，确保包含打卡时间字段
     * 
     * @param swmDailyAttendance 日考勤记录
     * @return 影响的行数
     */
    int updateWithClockTime(SwmDailyAttendance swmDailyAttendance);

    /**
     * 根据员工ID和日期查询考勤记录
     * 
     * @param employeeId     员工ID
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    SwmDailyAttendance findByEmployeeIdAndDate(String employeeId, Date attendanceDate);

    /**
     * 根据员工姓名和日期查询考勤记录
     * 
     * @param employeeName   员工姓名
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    SwmDailyAttendance findByEmployeeAndDate(String employeeName, Date attendanceDate);

    /**
     * 根据员工ID和日期范围查询考勤记录
     * 
     * @param employeeId 员工ID
     * @param beginDate  开始日期
     * @param endDate    结束日期
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeIdAndDateRange(String employeeId, Date beginDate, Date endDate);

    /**
     * 根据员工姓名和日期范围查询考勤记录
     * 
     * @param employeeName 员工姓名
     * @param beginDate    开始日期
     * @param endDate      结束日期
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeAndDateRange(String employeeName, Date beginDate, Date endDate);

    /**
     * 根据日期查询所有考勤记录
     * 
     * @param attendanceDate 考勤日期
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByDate(Date attendanceDate);

    /**
     * 根据员工ID和月份查询考勤记录
     * 
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份 (1-12)
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeIdAndMonth(String employeeId, int year, int month);

    /**
     * 根据员工姓名和月份查询考勤记录
     * 
     * @param employeeName 员工姓名
     * @param year         年份
     * @param month        月份 (1-12)
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeAndMonth(String employeeName, int year, int month);

    /**
     * 查询指定月份的日考勤记录
     * 
     * @param employeeIds 员工ID(可选)
     * @param month       月份(格式: yyyy-MM)
     * @return 日考勤记录列表
     */
    List<SwmDailyAttendance> findByMonth(@Param("employeeIds") List<String> employeeIds, @Param("month") String month);

    /**
     * 根据身份证和日期查询考勤记录
     * 
     * @param identityCard   身份证号
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     * @author Shawn
     * @date 2025-08-12
     */
    SwmDailyAttendance findByIdentityCardAndDate(@Param("identityCard") String identityCard,
            @Param("attendanceDate") Date attendanceDate);

    /**
     * 根据身份证号和月份查询考勤记录
     * 
     * @param identityCard 身份证号
     * @param month        月份(格式: yyyy-MM)
     * @return 考勤记录列表
     * @author Shawn
     * @date 2025-08-21
     */
    List<SwmDailyAttendance> findByIdentityCardAndMonth(@Param("identityCard") String identityCard,
            @Param("month") String month);

    /**
     * 查询指定月份的月考勤记录
     *
     * @param month       月份(格式: yyyy-MM)
     * @return 月考勤记录列表
     */
    List<SwmMonthlyAttendance> findStatisticsByMonth(@Param("month") String month);

    /**
     * 查询指定月份的月考勤记录 --分页查询
     * @param team  班组
     * @param employeeName 员工姓名
     * @param month   当前月
     * @param pageNum  页码
     * @param pageSize  条数
     * @return
     */
    List<SwmMonthlyAttendance> findStatisticsByMonthWithPage(@Param("team") String team,@Param("employeeName") String employeeName,
                                                             @Param("month") String month,@Param("pageNum") int pageNum,@Param("pageSize") int pageSize);

    /**
     * 查询指定月份的月考勤记录总数
     * @param team
     * @param employeeName
     * @param month
     * @return
     */
    Long findStatisticsTotalByMonth(@Param("team") String team,@Param("employeeName") String employeeName,
                                                             @Param("month") String month);

    List<SwmDashboardNewController.JobTypeCount> statisticsPersonJobType(@Param("date") String date);

    Long countAttendanceByWorkshop(@Param("companyName") String companyName, @Param("workshopName") String workshopName, @Param("date") String date);

    Long countAttendanceByTeam(@Param("companyName")String companyName,
                               @Param("workshopName")String workshopName,
                               @Param("lineName")String lineName,
                               @Param("teamName")String teamName,
                               @Param("date")String date);

    List<SwmDashboardNewController.Person> attendanceList(@Param("date")String date);

    List<SwmDailyAttendance> findByDateRange(@Param("beginDate")Date beginDate, @Param("endDate")Date endDate);

    List<SwmDashboardNewController.Person> attendanceListPage(SwmDashboardNewController.Person person);

    Long attendanceListCount(SwmDashboardNewController.Person person);

    List<SwmDailyAttendance> findByEmployeeIdAndDateBatch(@Param("employeeIds") List<String> employeeIds, @Param("attendanceDate") Date attendanceDate);

    void updateBatch(List<SwmDailyAttendance> records);

    List<SwmDailyAttendance> findAllList(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<AiDto.WorkerFatigue> workerFatigue(AiDto.WorkerFatigue vo);

    Long countWorkerFatigue(AiDto.WorkerFatigue vo);

    List<SwmDailyAttendance> findClockInCardList(String date);

    List<SwmDailyAttendance> findClockOutCardList(String yestDay, String nowDate);

    List<SwmDashboardNewController.AttendanceAnalysis> getAllTeamNumber(String companyCode);

    List<SwmDashboardNewController.AttendanceAnalysis> getTeamAttendance(String companyCode, String date);

    List<SwmDashboardDto.IdleHoursRankingDto> idleHoursRankingList(SwmDashboardDto.IdleHoursRankingDto vo);

    List<SwmDashboardDto.ManagementOnDutyDto> managementOnDuty(SwmDashboardDto.ManagementOnDutyDto vo);

    List<SwmDashboardDto.TeamAttendanceAnalysis> teamAttendanceAnalysis(SwmDashboardDto.TeamAttendanceAnalysis vo);

    List<SwmDashboardDto.TeamAttendanceAnalysis> departmentAttendanceAnalysis(SwmDashboardDto.TeamAttendanceAnalysis vo);

    List<SwmDashboardDto.NoAttendancePerson> noAttendancePerson(SwmDashboardDto.NoAttendancePerson vo);

    List<SwmDashboardDto.NoAttendancePerson> beLatePerson(SwmDashboardDto.NoAttendancePerson vo);

    List<SwmDashboardDto.NoAttendancePerson> leaveEarlyPerson(SwmDashboardDto.NoAttendancePerson vo);
}
