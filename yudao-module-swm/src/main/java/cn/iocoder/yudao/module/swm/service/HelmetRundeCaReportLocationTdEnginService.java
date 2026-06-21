package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import java.util.Map;

/**
 * 安全帽定位数据 TDengine 服务接口
 */
public interface HelmetRundeCaReportLocationTdEnginService {

    /**
     * 根据身份证号和日期查询第一条和最后一条时间记录
     *
     * @param idCard        身份证号
     * @param dateStr       日期字符串
     * @param workTimeRange 工作时间范围
     * @return 包含 firstTime 和 lastTime 的结果
     */
    CommonResult<Map<String, Object>> getFirstAndLastTimeByIdCardAndDate(String idCard, String dateStr, String workTimeRange);

}
