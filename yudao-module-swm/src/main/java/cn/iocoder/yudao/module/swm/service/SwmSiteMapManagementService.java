package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSiteMapManagementDO;
import jakarta.validation.Valid;

/**
 * 站点地图 Service 接口
 */
public interface SwmSiteMapManagementService {

    /**
     * 创建站点地图
     *
     * @param createReqVO 创建信息
     * @return 站点地图编号
     */
    String createSiteMapManagement(@Valid SwmSiteMapManagementSaveReqVO createReqVO);

    /**
     * 更新站点地图
     *
     * @param updateReqVO 更新信息
     */
    void updateSiteMapManagement(@Valid SwmSiteMapManagementSaveReqVO updateReqVO);

    /**
     * 删除站点地图
     *
     * @param id 站点地图编号
     */
    void deleteSiteMapManagement(String id);

    /**
     * 获得站点地图
     *
     * @param id 站点地图编号
     * @return 站点地图
     */
    SwmSiteMapManagementDO getSiteMapManagement(String id);

    /**
     * 获得站点地图分页
     *
     * @param pageReqVO 分页查询
     * @return 站点地图分页
     */
    PageResult<SwmSiteMapManagementDO> getSiteMapManagementPage(SwmSiteMapManagementPageReqVO pageReqVO);

}
