package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonSchedulePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 人员排班 Service 接口
 */
public interface SwmPersonScheduleService {

    String createPersonSchedule(@Valid SwmPersonScheduleSaveReqVO createReqVO);
    void updatePersonSchedule(@Valid SwmPersonScheduleSaveReqVO updateReqVO);
    void deletePersonSchedule(String id);
    SwmPersonScheduleDO getPersonSchedule(String id);
    PageResult<SwmPersonScheduleDO> getPersonSchedulePage(SwmPersonSchedulePageReqVO pageReqVO);

    /**
     * 根据条件查询排班列表
     */
    List<SwmPersonScheduleDO> findList(SwmPersonScheduleDO query);

    /**
     * 根据租户编码查询排班列表
     */
    List<SwmPersonScheduleDO> findListByCorpCode(SwmPersonScheduleDO query);

    /**
     * 根据身份证号和月份查询排班
     */
    List<SwmPersonScheduleDO> findByIdCardAndMonth(String idCard, String month);

    /**
     * 批量插入排班记录
     */
    void insertBatch(List<SwmPersonScheduleDO> list);

}
