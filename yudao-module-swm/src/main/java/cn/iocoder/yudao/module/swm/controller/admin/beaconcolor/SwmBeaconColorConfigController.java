package cn.iocoder.yudao.module.swm.controller.admin.beaconcolor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconColorConfigDO;
import cn.iocoder.yudao.module.swm.service.SwmBeaconColorConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 信标颜色配置")
@RestController
@RequestMapping("/swm/beacon-color-config")
@Validated
public class SwmBeaconColorConfigController {

    @Resource
    private SwmBeaconColorConfigService beaconColorConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建信标颜色配置")
    @PreAuthorize("@ss.hasPermission('swm:beacon-color-config:create')")
    public CommonResult<String> createSwmBeaconColorConfig(@Valid @RequestBody SwmBeaconColorConfigSaveReqVO createReqVO) {
        String id = beaconColorConfigService.createBeaconColorConfig(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新信标颜色配置")
    @PreAuthorize("@ss.hasPermission('swm:beacon-color-config:update')")
    public CommonResult<Boolean> updateSwmBeaconColorConfig(@Valid @RequestBody SwmBeaconColorConfigSaveReqVO updateReqVO) {
        beaconColorConfigService.updateBeaconColorConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除信标颜色配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:beacon-color-config:delete')")
    public CommonResult<Boolean> deleteSwmBeaconColorConfig(@RequestParam("id") String id) {
        beaconColorConfigService.deleteBeaconColorConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取信标颜色配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:beacon-color-config:query')")
    public CommonResult<SwmBeaconColorConfigRespVO> getSwmBeaconColorConfig(@RequestParam("id") String id) {
        SwmBeaconColorConfigDO beaconColorConfig = beaconColorConfigService.getBeaconColorConfig(id);
        return success(BeanUtils.toBean(beaconColorConfig, SwmBeaconColorConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询信标颜色配置")
    @PreAuthorize("@ss.hasPermission('swm:beacon-color-config:query')")
    public CommonResult<PageResult<SwmBeaconColorConfigRespVO>> getSwmBeaconColorConfigPage(@Valid SwmBeaconColorConfigPageReqVO pageReqVO) {
        PageResult<SwmBeaconColorConfigDO> pageResult = beaconColorConfigService.getBeaconColorConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmBeaconColorConfigRespVO.class));
    }

}
