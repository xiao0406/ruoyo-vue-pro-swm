package cn.iocoder.yudao.module.swm.controller.admin.sitemap;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSiteMapManagementDO;
import cn.iocoder.yudao.module.swm.service.SwmSiteMapManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 站点地图")
@RestController
@RequestMapping("/swm/site-map")
@Validated
public class SwmSiteMapManagementController {

    @Resource
    private SwmSiteMapManagementService siteMapManagementService;

    @PostMapping("/create")
    @Operation(summary = "创建站点地图")
    @PreAuthorize("@ss.hasPermission('swm:site-map:create')")
    public CommonResult<String> createSwmSiteMapManagement(@Valid @RequestBody SwmSiteMapManagementSaveReqVO createReqVO) {
        String id = siteMapManagementService.createSiteMapManagement(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新站点地图")
    @PreAuthorize("@ss.hasPermission('swm:site-map:update')")
    public CommonResult<Boolean> updateSwmSiteMapManagement(@Valid @RequestBody SwmSiteMapManagementSaveReqVO updateReqVO) {
        siteMapManagementService.updateSiteMapManagement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除站点地图")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:site-map:delete')")
    public CommonResult<Boolean> deleteSwmSiteMapManagement(@RequestParam("id") String id) {
        siteMapManagementService.deleteSiteMapManagement(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取站点地图")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:site-map:query')")
    public CommonResult<SwmSiteMapManagementRespVO> getSwmSiteMapManagement(@RequestParam("id") String id) {
        SwmSiteMapManagementDO siteMapManagement = siteMapManagementService.getSiteMapManagement(id);
        return success(BeanUtils.toBean(siteMapManagement, SwmSiteMapManagementRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询站点地图")
    @PreAuthorize("@ss.hasPermission('swm:site-map:query')")
    public CommonResult<PageResult<SwmSiteMapManagementRespVO>> getSwmSiteMapManagementPage(@Valid SwmSiteMapManagementPageReqVO pageReqVO) {
        PageResult<SwmSiteMapManagementDO> pageResult = siteMapManagementService.getSiteMapManagementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmSiteMapManagementRespVO.class));
    }

}
