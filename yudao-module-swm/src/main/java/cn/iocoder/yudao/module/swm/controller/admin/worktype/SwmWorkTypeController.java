package cn.iocoder.yudao.module.swm.controller.admin.worktype;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypeRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWorkTypeDO;
import cn.iocoder.yudao.module.swm.service.SwmWorkTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 工种管理")
@RestController
@RequestMapping("/swm/work-type")
@Validated
public class SwmWorkTypeController {

    @Resource
    private SwmWorkTypeService workTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建工种")
    @PreAuthorize("@ss.hasPermission('swm:work-type:create')")
    public CommonResult<String> createSwmWorkType(@Valid @RequestBody SwmWorkTypeSaveReqVO createReqVO) {
        String id = workTypeService.createWorkType(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新工种")
    @PreAuthorize("@ss.hasPermission('swm:work-type:update')")
    public CommonResult<Boolean> updateSwmWorkType(@Valid @RequestBody SwmWorkTypeSaveReqVO updateReqVO) {
        workTypeService.updateWorkType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工种")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:work-type:delete')")
    public CommonResult<Boolean> deleteSwmWorkType(@RequestParam("id") String id) {
        workTypeService.deleteWorkType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取工种")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:work-type:query')")
    public CommonResult<SwmWorkTypeRespVO> getSwmWorkType(@RequestParam("id") String id) {
        SwmWorkTypeDO workType = workTypeService.getWorkType(id);
        return success(BeanUtils.toBean(workType, SwmWorkTypeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询工种")
    @PreAuthorize("@ss.hasPermission('swm:work-type:query')")
    public CommonResult<PageResult<SwmWorkTypeRespVO>> getSwmWorkTypePage(@Valid SwmWorkTypePageReqVO pageReqVO) {
        PageResult<SwmWorkTypeDO> pageResult = workTypeService.getWorkTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmWorkTypeRespVO.class));
    }

    @GetMapping("/active-list")
    @Operation(summary = "获取有效工种列表")
    public CommonResult<List<SwmWorkTypeRespVO>> getActiveWorkTypes() {
        return success(BeanUtils.toBean(workTypeService.getActiveWorkTypes(), SwmWorkTypeRespVO.class));
    }

}
