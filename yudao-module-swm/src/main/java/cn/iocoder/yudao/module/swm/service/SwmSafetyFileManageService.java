package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyFileManageDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SwmSafetyFileManageService extends IService<SwmSafetyFileManageDO> {

    SwmSafetyFileManageDO getSwmSafetyFileManage(String id);

    /**
     * 查询列表（兼容 JeeSite findList）
     * @param pushDateStart 推送日期起始（含）
     * @param pushDateEnd   推送日期截止（含）
     * @return 满足条件的列表
     */
    List<SwmSafetyFileManageDO> findList(java.time.LocalDateTime pushDateStart, java.time.LocalDateTime pushDateEnd);
}
