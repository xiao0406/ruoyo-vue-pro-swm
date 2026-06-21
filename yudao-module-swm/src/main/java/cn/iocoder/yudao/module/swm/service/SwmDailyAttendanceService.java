package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendancePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendanceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDailyAttendanceDO;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日考勤 Service 接口
 */
public interface SwmDailyAttendanceService {

    String createDailyAttendance(@Valid SwmDailyAttendanceSaveReqVO createReqVO);
    void updateDailyAttendance(@Valid SwmDailyAttendanceSaveReqVO updateReqVO);
    void deleteDailyAttendance(String id);
    SwmDailyAttendanceDO getDailyAttendance(String id);
    PageResult<SwmDailyAttendanceDO> getDailyAttendancePage(SwmDailyAttendancePageReqVO pageReqVO);

    /**
     * 根据条件查询考勤列表
     */
    List<SwmDailyAttendanceDO> findList(SwmDailyAttendanceDO query);

    /**
     * 保存考勤记录（新增）
     */
    void save(SwmDailyAttendanceDO attendance);

    /**
     * 更新考勤记录（直接DO更新）
     */
    void update(SwmDailyAttendanceDO attendance);

    /**
     * 批量保存考勤记录
     */
    void saveBatch(List<SwmDailyAttendanceDO> list);

    /**
     * 批量更新考勤记录
     */
    void updateBatch(List<SwmDailyAttendanceDO> list);

    /**
     * 根据员工ID和日期查询考勤
     */
    SwmDailyAttendanceDO findByEmployeeIdAndDate(String employeeId, LocalDate attendanceDate);

    /**
     * 根据身份证号和日期查询考勤
     */
    SwmDailyAttendanceDO findByIdentityCardAndDate(String identityCard, LocalDate attendanceDate);

    /**
     * 查询需要补上班卡的考勤列表
     */
    List<SwmDailyAttendanceDO> findClockInCardList(String date, Integer random);

    /**
     * 查询需要补下班卡的考勤列表
     */
    List<SwmDailyAttendanceDO> findClockOutCardList(String yestDay, String nowDate, Integer random);

    /**
     * 根据员工ID获取身份证号
     */
    String getIdCardByEmployeeId(String employeeId);

    /**
     * 根据身份证号计算怠工时长
     */
    double calculateIdleTimeByIdCard(String idCard, String dateStr, String workTimeRange);

    /**
     * 根据身份证号计算有效工作时长
     */
    double calculateEffectiveWorkHoursByIdCard(String idCard, String dateStr, String workTimeRange);

    /**
     * 根据月份查询考勤列表
     */
    List<SwmDailyAttendanceDO> findByMonth(String month);

}
