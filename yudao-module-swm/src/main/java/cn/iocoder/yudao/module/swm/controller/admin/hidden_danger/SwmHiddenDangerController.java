package cn.iocoder.yudao.module.swm.controller.admin.hidden_danger;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHiddenDangerDO;
import cn.iocoder.yudao.module.swm.service.SwmHiddenDangerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 隐患排查")
@RestController
@RequestMapping("/swm/hidden-danger")
@Validated
public class SwmHiddenDangerController {

    @Resource
    private SwmHiddenDangerService hiddenDangerService;

    @PostMapping("/create")
    @Operation(summary = "创建隐患排查")
    @PreAuthorize("@ss.hasPermission('swm:hidden-danger:create')")
    public CommonResult<String> createSwmHiddenDanger(@Valid @RequestBody SwmHiddenDangerSaveReqVO createReqVO) {
        String id = hiddenDangerService.createHiddenDanger(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新隐患排查")
    @PreAuthorize("@ss.hasPermission('swm:hidden-danger:update')")
    public CommonResult<Boolean> updateSwmHiddenDanger(@Valid @RequestBody SwmHiddenDangerSaveReqVO updateReqVO) {
        hiddenDangerService.updateHiddenDanger(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除隐患排查")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:hidden-danger:delete')")
    public CommonResult<Boolean> deleteSwmHiddenDanger(@RequestParam("id") String id) {
        hiddenDangerService.deleteHiddenDanger(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取隐患排查")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:hidden-danger:query')")
    public CommonResult<SwmHiddenDangerRespVO> getSwmHiddenDanger(@RequestParam("id") String id) {
        SwmHiddenDangerDO hiddenDanger = hiddenDangerService.getHiddenDanger(id);
        return success(BeanUtils.toBean(hiddenDanger, SwmHiddenDangerRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询隐患排查")
    @PreAuthorize("@ss.hasPermission('swm:hidden-danger:query')")
    public CommonResult<PageResult<SwmHiddenDangerRespVO>> getSwmHiddenDangerPage(@Valid SwmHiddenDangerPageReqVO pageReqVO) {
        PageResult<SwmHiddenDangerDO> pageResult = hiddenDangerService.getHiddenDangerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmHiddenDangerRespVO.class));
    }

}
