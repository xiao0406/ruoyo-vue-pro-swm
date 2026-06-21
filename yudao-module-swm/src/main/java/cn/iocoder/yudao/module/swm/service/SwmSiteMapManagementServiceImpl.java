package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSiteMapManagementDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSiteMapManagementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.AREA_NOT_EXISTS;

/**
 * 站点地图 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmSiteMapManagementServiceImpl implements SwmSiteMapManagementService {

    @Resource
    private SwmSiteMapManagementMapper swmSiteMapManagementMapper;

    @Override
    public String createSiteMapManagement(SwmSiteMapManagementSaveReqVO createReqVO) {
        // 插入站点地图
        SwmSiteMapManagementDO siteMapManagement = BeanUtils.toBean(createReqVO, SwmSiteMapManagementDO.class);
        swmSiteMapManagementMapper.insert(siteMapManagement);
        return siteMapManagement.getId();
    }

    @Override
    public void updateSiteMapManagement(SwmSiteMapManagementSaveReqVO updateReqVO) {
        // 校验存在
        validateSiteMapManagementExists(updateReqVO.getId());

        // 更新站点地图
        SwmSiteMapManagementDO updateObj = BeanUtils.toBean(updateReqVO, SwmSiteMapManagementDO.class);
        swmSiteMapManagementMapper.updateById(updateObj);
    }

    @Override
    public void deleteSiteMapManagement(String id) {
        // 校验存在
        validateSiteMapManagementExists(id);

        // 删除站点地图
        swmSiteMapManagementMapper.deleteById(id);
    }

    @Override
    public SwmSiteMapManagementDO getSiteMapManagement(String id) {
        return swmSiteMapManagementMapper.selectById(id);
    }

    @Override
    public PageResult<SwmSiteMapManagementDO> getSiteMapManagementPage(SwmSiteMapManagementPageReqVO pageReqVO) {
        return swmSiteMapManagementMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateSiteMapManagementExists(String id) {
        if (swmSiteMapManagementMapper.selectById(id) == null) {
            throw exception(AREA_NOT_EXISTS);
        }
    }

}
