package cn.iocoder.yudao.module.swm.controller.admin.danger_disposal;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDangerDisposalDO;
import cn.iocoder.yudao.module.swm.service.SwmDangerDisposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 隐患处置")
@RestController
@RequestMapping("/swm/danger-disposal")
@Validated
public class SwmDangerDisposalController {

    @Resource
    private SwmDangerDisposalService dangerDisposalService;

    @PostMapping("/create")
    @Operation(summary = "创建隐患处置")
    @PreAuthorize("@ss.hasPermission('swm:danger-disposal:create')")
    public CommonResult<String> createSwmDangerDisposal(@Valid @RequestBody SwmDangerDisposalSaveReqVO createReqVO) {
        String id = dangerDisposalService.createDangerDisposal(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新隐患处置")
    @PreAuthorize("@ss.hasPermission('swm:danger-disposal:update')")
    public CommonResult<Boolean> updateSwmDangerDisposal(@Valid @RequestBody SwmDangerDisposalSaveReqVO updateReqVO) {
        dangerDisposalService.updateDangerDisposal(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除隐患处置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:danger-disposal:delete')")
    public CommonResult<Boolean> deleteSwmDangerDisposal(@RequestParam("id") String id) {
        dangerDisposalService.deleteDangerDisposal(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取隐患处置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:danger-disposal:query')")
    public CommonResult<SwmDangerDisposalRespVO> getSwmDangerDisposal(@RequestParam("id") String id) {
        SwmDangerDisposalDO dangerDisposal = dangerDisposalService.getDangerDisposal(id);
        return success(BeanUtils.toBean(dangerDisposal, SwmDangerDisposalRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询隐患处置")
    @PreAuthorize("@ss.hasPermission('swm:danger-disposal:query')")
    public CommonResult<PageResult<SwmDangerDisposalRespVO>> getSwmDangerDisposalPage(@Valid SwmDangerDisposalPageReqVO pageReqVO) {
        PageResult<SwmDangerDisposalDO> pageResult = dangerDisposalService.getDangerDisposalPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmDangerDisposalRespVO.class));
    }

}
