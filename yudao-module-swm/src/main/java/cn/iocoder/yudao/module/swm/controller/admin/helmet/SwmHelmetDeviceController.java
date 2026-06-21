package cn.iocoder.yudao.module.swm.controller.admin.helmet;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDevicePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDeviceRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDeviceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 安全帽设备")
@RestController
@RequestMapping("/swm/helmet-device")
@Validated
public class SwmHelmetDeviceController {

    @Resource
    private SwmHelmetDeviceService helmetDeviceService;

    @PostMapping("/create")
    @Operation(summary = "创建安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:create')")
    public CommonResult<String> createHelmetDevice(@Valid @RequestBody SwmHelmetDeviceSaveReqVO createReqVO) {
        String id = helmetDeviceService.createHelmetDevice(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> updateHelmetDevice(@Valid @RequestBody SwmHelmetDeviceSaveReqVO updateReqVO) {
        helmetDeviceService.updateHelmetDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安全帽设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:delete')")
    public CommonResult<Boolean> deleteHelmetDevice(@RequestParam("id") String id) {
        helmetDeviceService.deleteHelmetDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取安全帽设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<SwmHelmetDeviceRespVO> getHelmetDevice(@RequestParam("id") String id) {
        SwmHelmetDeviceDO helmetDevice = helmetDeviceService.getHelmetDevice(id);
        return success(BeanUtils.toBean(helmetDevice, SwmHelmetDeviceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<PageResult<SwmHelmetDeviceRespVO>> getHelmetDevicePage(@Valid SwmHelmetDevicePageReqVO pageReqVO) {
        PageResult<SwmHelmetDeviceDO> pageResult = helmetDeviceService.getHelmetDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmHelmetDeviceRespVO.class));
    }

}
