package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummaryPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummarySaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAttendanceSummaryDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 考勤汇总 Service 接口
 */
public interface SwmAttendanceSummaryService {

    String createAttendanceSummary(@Valid SwmAttendanceSummarySaveReqVO createReqVO);
    void updateAttendanceSummary(@Valid SwmAttendanceSummarySaveReqVO updateReqVO);
    void deleteAttendanceSummary(String id);
    SwmAttendanceSummaryDO getAttendanceSummary(String id);
    PageResult<SwmAttendanceSummaryDO> getAttendanceSummaryPage(SwmAttendanceSummaryPageReqVO pageReqVO);

    /**
     * 根据员工ID和月份查询考勤汇总
     */
    SwmAttendanceSummaryDO findByEmployeeIdAndMonth(String employeeId, String month);

    /**
     * 根据身份证号和月份查询考勤汇总列表
     */
    List<SwmAttendanceSummaryDO> findByIdentityCardAndMonth(String identityCard, String month);

    /**
     * 保存考勤汇总（新增）
     */
    void save(SwmAttendanceSummaryDO summary);

    /**
     * 更新考勤汇总（直接DO更新）
     */
    void update(SwmAttendanceSummaryDO summary);

}
