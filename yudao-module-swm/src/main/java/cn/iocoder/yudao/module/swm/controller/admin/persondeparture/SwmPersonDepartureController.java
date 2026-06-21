package cn.iocoder.yudao.module.swm.controller.admin.persondeparture;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDeparturePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDepartureDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonDepartureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员退场")
@RestController
@RequestMapping("/swm/person-departure")
@Validated
public class SwmPersonDepartureController {

    @Resource
    private SwmPersonDepartureService personDepartureService;

    @PostMapping("/create")
    @Operation(summary = "创建人员退场")
    @PreAuthorize("@ss.hasPermission('swm:person-departure:create')")
    public CommonResult<String> createSwmPersonDeparture(@Valid @RequestBody SwmPersonDepartureSaveReqVO createReqVO) {
        String id = personDepartureService.createPersonDeparture(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员退场")
    @PreAuthorize("@ss.hasPermission('swm:person-departure:update')")
    public CommonResult<Boolean> updateSwmPersonDeparture(@Valid @RequestBody SwmPersonDepartureSaveReqVO updateReqVO) {
        personDepartureService.updatePersonDeparture(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员退场")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-departure:delete')")
    public CommonResult<Boolean> deleteSwmPersonDeparture(@RequestParam("id") String id) {
        personDepartureService.deletePersonDeparture(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员退场")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-departure:query')")
    public CommonResult<SwmPersonDepartureRespVO> getSwmPersonDeparture(@RequestParam("id") String id) {
        SwmPersonDepartureDO personDeparture = personDepartureService.getPersonDeparture(id);
        return success(BeanUtils.toBean(personDeparture, SwmPersonDepartureRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员退场")
    @PreAuthorize("@ss.hasPermission('swm:person-departure:query')")
    public CommonResult<PageResult<SwmPersonDepartureRespVO>> getSwmPersonDeparturePage(@Valid SwmPersonDeparturePageReqVO pageReqVO) {
        PageResult<SwmPersonDepartureDO> pageResult = personDepartureService.getPersonDeparturePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonDepartureRespVO.class));
    }

}
