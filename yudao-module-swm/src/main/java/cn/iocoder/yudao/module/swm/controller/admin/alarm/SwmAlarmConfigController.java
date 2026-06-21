package cn.iocoder.yudao.module.swm.controller.admin.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDO;
import cn.iocoder.yudao.module.swm.service.SwmAlarmConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警配置")
@RestController
@RequestMapping("/swm/alarm-config")
@Validated
public class SwmAlarmConfigController {

    @Resource
    private SwmAlarmConfigService alarmConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建告警配置")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config:create')")
    public CommonResult<String> createAlarmConfig(@Valid @RequestBody SwmAlarmConfigSaveReqVO createReqVO) {
        String id = alarmConfigService.createAlarmConfig(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新告警配置")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config:update')")
    public CommonResult<Boolean> updateAlarmConfig(@Valid @RequestBody SwmAlarmConfigSaveReqVO updateReqVO) {
        alarmConfigService.updateAlarmConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:alarm-config:delete')")
    public CommonResult<Boolean> deleteAlarmConfig(@RequestParam("id") String id) {
        alarmConfigService.deleteAlarmConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取告警配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:alarm-config:query')")
    public CommonResult<SwmAlarmConfigRespVO> getAlarmConfig(@RequestParam("id") String id) {
        SwmAlarmConfigDO alarmConfig = alarmConfigService.getAlarmConfig(id);
        return success(BeanUtils.toBean(alarmConfig, SwmAlarmConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询告警配置")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config:query')")
    public CommonResult<PageResult<SwmAlarmConfigRespVO>> getAlarmConfigPage(@Valid SwmAlarmConfigPageReqVO pageReqVO) {
        PageResult<SwmAlarmConfigDO> pageResult = alarmConfigService.getAlarmConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmAlarmConfigRespVO.class));
    }

}
