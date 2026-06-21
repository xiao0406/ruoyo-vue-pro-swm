package cn.iocoder.yudao.module.swm.controller.admin.alarm_light;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmLightDO;
import cn.iocoder.yudao.module.swm.service.SwmAlarmLightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 报警灯")
@RestController
@RequestMapping("/swm/alarm-light")
@Validated
public class SwmAlarmLightController {

    @Resource
    private SwmAlarmLightService alarmLightService;

    @PostMapping("/create")
    @Operation(summary = "创建报警灯")
    @PreAuthorize("@ss.hasPermission('swm:alarm-light:create')")
    public CommonResult<String> createSwmAlarmLight(@Valid @RequestBody SwmAlarmLightSaveReqVO createReqVO) {
        String id = alarmLightService.createAlarmLight(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新报警灯")
    @PreAuthorize("@ss.hasPermission('swm:alarm-light:update')")
    public CommonResult<Boolean> updateSwmAlarmLight(@Valid @RequestBody SwmAlarmLightSaveReqVO updateReqVO) {
        alarmLightService.updateAlarmLight(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报警灯")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:alarm-light:delete')")
    public CommonResult<Boolean> deleteSwmAlarmLight(@RequestParam("id") String id) {
        alarmLightService.deleteAlarmLight(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取报警灯")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:alarm-light:query')")
    public CommonResult<SwmAlarmLightRespVO> getSwmAlarmLight(@RequestParam("id") String id) {
        SwmAlarmLightDO alarmLight = alarmLightService.getAlarmLight(id);
        return success(BeanUtils.toBean(alarmLight, SwmAlarmLightRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询报警灯")
    @PreAuthorize("@ss.hasPermission('swm:alarm-light:query')")
    public CommonResult<PageResult<SwmAlarmLightRespVO>> getSwmAlarmLightPage(@Valid SwmAlarmLightPageReqVO pageReqVO) {
        PageResult<SwmAlarmLightDO> pageResult = alarmLightService.getAlarmLightPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmAlarmLightRespVO.class));
    }

}
