package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 人员管理 Service 接口
 */
public interface SwmPersonService {

    /**
     * 创建人员
     *
     * @param createReqVO 创建信息
     * @return 人员编号
     */
    String createPerson(@Valid SwmPersonSaveReqVO createReqVO);

    /**
     * 更新人员
     *
     * @param updateReqVO 更新信息
     */
    void updatePerson(@Valid SwmPersonSaveReqVO updateReqVO);

    /**
     * 删除人员
     *
     * @param id 人员编号
     */
    void deletePerson(String id);

    /**
     * 获得人员
     *
     * @param id 人员编号
     * @return 人员
     */
    SwmPersonDO getPerson(String id);

    /**
     * 获得人员分页
     *
     * @param pageReqVO 分页查询
     * @return 人员分页
     */
    PageResult<SwmPersonDO> getPersonPage(SwmPersonPageReqVO pageReqVO);

    /**
     * 根据条件查询人员列表
     *
     * @param query 查询条件（personnelStatus/status/corpCode/random 等）
     * @return 人员列表
     */
    List<SwmPersonDO> findList(SwmPersonDO query);

    /**
     * 根据身份证号查询人员
     *
     * @param identityCard 身份证号
     * @return 人员
     */
    SwmPersonDO getByIdentityCard(String identityCard);

    /**
     * 根据工种列表查询人员
     *
     * @param jobTypeList 工种列表
     * @return 人员列表
     */
    List<SwmPersonDO> findListByJobTypeList(List<String> jobTypeList);

}
