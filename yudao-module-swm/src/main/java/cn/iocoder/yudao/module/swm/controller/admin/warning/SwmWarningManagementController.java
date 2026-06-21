package cn.iocoder.yudao.module.swm.controller.admin.warning;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import cn.iocoder.yudao.module.swm.service.SwmWarningManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预警管理")
@RestController
@RequestMapping("/swm/warning")
@Validated
public class SwmWarningManagementController {

    @Resource
    private SwmWarningManagementService warningManagementService;

    @PostMapping("/create")
    @Operation(summary = "创建预警管理")
    @PreAuthorize("@ss.hasPermission('swm:warning:create')")
    public CommonResult<String> createWarningManagement(@Valid @RequestBody SwmWarningManagementSaveReqVO createReqVO) {
        String id = warningManagementService.createWarningManagement(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新预警管理")
    @PreAuthorize("@ss.hasPermission('swm:warning:update')")
    public CommonResult<Boolean> updateWarningManagement(@Valid @RequestBody SwmWarningManagementSaveReqVO updateReqVO) {
        warningManagementService.updateWarningManagement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除预警管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:warning:delete')")
    public CommonResult<Boolean> deleteWarningManagement(@RequestParam("id") String id) {
        warningManagementService.deleteWarningManagement(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取预警管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:warning:query')")
    public CommonResult<SwmWarningManagementRespVO> getWarningManagement(@RequestParam("id") String id) {
        SwmWarningManagementDO warningManagement = warningManagementService.getWarningManagement(id);
        return success(BeanUtils.toBean(warningManagement, SwmWarningManagementRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询预警管理")
    @PreAuthorize("@ss.hasPermission('swm:warning:query')")
    public CommonResult<PageResult<SwmWarningManagementRespVO>> getWarningManagementPage(@Valid SwmWarningManagementPageReqVO pageReqVO) {
        PageResult<SwmWarningManagementDO> pageResult = warningManagementService.getWarningManagementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmWarningManagementRespVO.class));
    }

}
