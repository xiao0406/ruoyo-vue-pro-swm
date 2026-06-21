package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.IotDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT设备")
@RestController
@RequestMapping("/iot/device")
@Validated
public class IotDeviceController {

    @Resource
    private IotDeviceService iotDeviceService;

    @PostMapping("/create")
    @Operation(summary = "创建IoT设备")
    @PreAuthorize("@ss.hasPermission('iot:device:create')")
    public CommonResult<String> createIotDevice(@Valid @RequestBody IotDeviceSaveReqVO createReqVO) {
        String id = iotDeviceService.createIotDevice(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新IoT设备")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateIotDevice(@Valid @RequestBody IotDeviceSaveReqVO updateReqVO) {
        iotDeviceService.updateIotDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除IoT设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteIotDevice(@RequestParam("id") String id) {
        iotDeviceService.deleteIotDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取IoT设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceRespVO> getIotDevice(@RequestParam("id") String id) {
        IotDeviceDO device = iotDeviceService.getIotDevice(id);
        return success(BeanUtils.toBean(device, IotDeviceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询IoT设备")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceRespVO>> getIotDevicePage(@Valid IotDevicePageReqVO pageReqVO) {
        PageResult<IotDeviceDO> pageResult = iotDeviceService.getIotDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceRespVO.class));
    }
}
