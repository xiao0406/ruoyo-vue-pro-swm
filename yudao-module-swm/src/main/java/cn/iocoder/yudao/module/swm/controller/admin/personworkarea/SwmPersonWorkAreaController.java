package cn.iocoder.yudao.module.swm.controller.admin.personworkarea;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonWorkAreaDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonWorkAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员工作区域")
@RestController
@RequestMapping("/swm/person-work-area")
@Validated
public class SwmPersonWorkAreaController {

    @Resource
    private SwmPersonWorkAreaService personWorkAreaService;

    @PostMapping("/create")
    @Operation(summary = "创建人员工作区域")
    @PreAuthorize("@ss.hasPermission('swm:person-work-area:create')")
    public CommonResult<String> createSwmPersonWorkArea(@Valid @RequestBody SwmPersonWorkAreaSaveReqVO createReqVO) {
        String id = personWorkAreaService.createPersonWorkArea(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员工作区域")
    @PreAuthorize("@ss.hasPermission('swm:person-work-area:update')")
    public CommonResult<Boolean> updateSwmPersonWorkArea(@Valid @RequestBody SwmPersonWorkAreaSaveReqVO updateReqVO) {
        personWorkAreaService.updatePersonWorkArea(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员工作区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-work-area:delete')")
    public CommonResult<Boolean> deleteSwmPersonWorkArea(@RequestParam("id") String id) {
        personWorkAreaService.deletePersonWorkArea(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员工作区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-work-area:query')")
    public CommonResult<SwmPersonWorkAreaRespVO> getSwmPersonWorkArea(@RequestParam("id") String id) {
        SwmPersonWorkAreaDO personWorkArea = personWorkAreaService.getPersonWorkArea(id);
        return success(BeanUtils.toBean(personWorkArea, SwmPersonWorkAreaRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员工作区域")
    @PreAuthorize("@ss.hasPermission('swm:person-work-area:query')")
    public CommonResult<PageResult<SwmPersonWorkAreaRespVO>> getSwmPersonWorkAreaPage(@Valid SwmPersonWorkAreaPageReqVO pageReqVO) {
        PageResult<SwmPersonWorkAreaDO> pageResult = personWorkAreaService.getPersonWorkAreaPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonWorkAreaRespVO.class));
    }

}
