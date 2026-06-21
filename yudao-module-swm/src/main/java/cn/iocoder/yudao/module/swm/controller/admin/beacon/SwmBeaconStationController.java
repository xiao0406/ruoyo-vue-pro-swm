package cn.iocoder.yudao.module.swm.controller.admin.beacon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.service.SwmBeaconStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 信标站点")
@RestController
@RequestMapping("/swm/beacon-station")
@Validated
public class SwmBeaconStationController {

    @Resource
    private SwmBeaconStationService beaconStationService;

    @PostMapping("/create")
    @Operation(summary = "创建信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:create')")
    public CommonResult<String> createBeaconStation(@Valid @RequestBody SwmBeaconStationSaveReqVO createReqVO) {
        String id = beaconStationService.createBeaconStation(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:update')")
    public CommonResult<Boolean> updateBeaconStation(@Valid @RequestBody SwmBeaconStationSaveReqVO updateReqVO) {
        beaconStationService.updateBeaconStation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除信标站点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:delete')")
    public CommonResult<Boolean> deleteBeaconStation(@RequestParam("id") String id) {
        beaconStationService.deleteBeaconStation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取信标站点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:query')")
    public CommonResult<SwmBeaconStationRespVO> getBeaconStation(@RequestParam("id") String id) {
        SwmBeaconStationDO beaconStation = beaconStationService.getBeaconStation(id);
        return success(BeanUtils.toBean(beaconStation, SwmBeaconStationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:query')")
    public CommonResult<PageResult<SwmBeaconStationRespVO>> getBeaconStationPage(@Valid SwmBeaconStationPageReqVO pageReqVO) {
        PageResult<SwmBeaconStationDO> pageResult = beaconStationService.getBeaconStationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmBeaconStationRespVO.class));
    }

}
