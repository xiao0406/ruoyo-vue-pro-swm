package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.thirdapilog.vo.SwmThirdApiLogPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmThirdApiLogDO;

/**
 * 第三方API日志 Service 接口
 */
public interface SwmThirdApiLogService {

    /**
     * 获得第三方API日志
     *
     * @param id 第三方API日志编号
     * @return 第三方API日志
     */
    SwmThirdApiLogDO getThirdApiLog(String id);

    /**
     * 获得第三方API日志分页
     *
     * @param pageReqVO 分页查询
     * @return 第三方API日志分页
     */
    PageResult<SwmThirdApiLogDO> getThirdApiLogPage(SwmThirdApiLogPageReqVO pageReqVO);

    /**
     * 保存第三方接口调用日志
     *
     * @param apiLog 日志对象
     */
    void saveLog(SwmThirdApiLogDO apiLog);

}
