package cn.iocoder.yudao.module.swm.controller.admin.monitordevice;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmMonitorDeviceInfoDO;
import cn.iocoder.yudao.module.swm.service.SwmMonitorDeviceInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 监控设备")
@RestController
@RequestMapping("/swm/monitor-device")
@Validated
public class SwmMonitorDeviceInfoController {

    @Resource
    private SwmMonitorDeviceInfoService monitorDeviceInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建监控设备")
    @PreAuthorize("@ss.hasPermission('swm:monitor-device:create')")
    public CommonResult<String> createSwmMonitorDeviceInfo(@Valid @RequestBody SwmMonitorDeviceInfoSaveReqVO createReqVO) {
        String id = monitorDeviceInfoService.createMonitorDeviceInfo(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新监控设备")
    @PreAuthorize("@ss.hasPermission('swm:monitor-device:update')")
    public CommonResult<Boolean> updateSwmMonitorDeviceInfo(@Valid @RequestBody SwmMonitorDeviceInfoSaveReqVO updateReqVO) {
        monitorDeviceInfoService.updateMonitorDeviceInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除监控设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:monitor-device:delete')")
    public CommonResult<Boolean> deleteSwmMonitorDeviceInfo(@RequestParam("id") String id) {
        monitorDeviceInfoService.deleteMonitorDeviceInfo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取监控设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:monitor-device:query')")
    public CommonResult<SwmMonitorDeviceInfoRespVO> getSwmMonitorDeviceInfo(@RequestParam("id") String id) {
        SwmMonitorDeviceInfoDO monitorDeviceInfo = monitorDeviceInfoService.getMonitorDeviceInfo(id);
        return success(BeanUtils.toBean(monitorDeviceInfo, SwmMonitorDeviceInfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询监控设备")
    @PreAuthorize("@ss.hasPermission('swm:monitor-device:query')")
    public CommonResult<PageResult<SwmMonitorDeviceInfoRespVO>> getSwmMonitorDeviceInfoPage(@Valid SwmMonitorDeviceInfoPageReqVO pageReqVO) {
        PageResult<SwmMonitorDeviceInfoDO> pageResult = monitorDeviceInfoService.getMonitorDeviceInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmMonitorDeviceInfoRespVO.class));
    }

}
