package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWorkTypeDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 工种管理 Service 接口
 */
public interface SwmWorkTypeService {

    /**
     * 创建工种
     *
     * @param createReqVO 创建信息
     * @return 工种编号
     */
    String createWorkType(@Valid SwmWorkTypeSaveReqVO createReqVO);

    /**
     * 更新工种
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkType(@Valid SwmWorkTypeSaveReqVO updateReqVO);

    /**
     * 删除工种
     *
     * @param id 工种编号
     */
    void deleteWorkType(String id);

    /**
     * 获得工种
     *
     * @param id 工种编号
     * @return 工种
     */
    SwmWorkTypeDO getWorkType(String id);

    /**
     * 获得工种分页
     *
     * @param pageReqVO 分页查询
     * @return 工种分页
     */
    PageResult<SwmWorkTypeDO> getWorkTypePage(SwmWorkTypePageReqVO pageReqVO);

    List<SwmWorkTypeDO> getActiveWorkTypes();

}
