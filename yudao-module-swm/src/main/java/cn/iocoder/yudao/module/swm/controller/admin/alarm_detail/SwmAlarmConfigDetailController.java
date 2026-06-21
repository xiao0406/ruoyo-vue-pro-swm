package cn.iocoder.yudao.module.swm.controller.admin.alarm_detail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDetailDO;
import cn.iocoder.yudao.module.swm.service.SwmAlarmConfigDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警配置详情")
@RestController
@RequestMapping("/swm/alarm-config-detail")
@Validated
public class SwmAlarmConfigDetailController {

    @Resource
    private SwmAlarmConfigDetailService alarmConfigDetailService;

    @PostMapping("/create")
    @Operation(summary = "创建告警配置详情")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config-detail:create')")
    public CommonResult<String> createSwmAlarmConfigDetail(@Valid @RequestBody SwmAlarmConfigDetailSaveReqVO createReqVO) {
        return success(alarmConfigDetailService.createAlarmConfigDetail(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新告警配置详情")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config-detail:update')")
    public CommonResult<Boolean> updateSwmAlarmConfigDetail(@Valid @RequestBody SwmAlarmConfigDetailSaveReqVO updateReqVO) {
        alarmConfigDetailService.updateAlarmConfigDetail(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警配置详情")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config-detail:delete')")
    public CommonResult<Boolean> deleteSwmAlarmConfigDetail(@Parameter(name = "id", description = "告警配置详情编号", required = true)
                                                            @RequestParam("id") String id) {
        alarmConfigDetailService.deleteAlarmConfigDetail(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得告警配置详情")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config-detail:query')")
    public CommonResult<SwmAlarmConfigDetailRespVO> getSwmAlarmConfigDetail(@Parameter(name = "id", description = "告警配置详情编号", required = true)
                                                                            @RequestParam("id") String id) {
        SwmAlarmConfigDetailDO alarmConfigDetail = alarmConfigDetailService.getAlarmConfigDetail(id);
        return success(BeanUtils.toBean(alarmConfigDetail, SwmAlarmConfigDetailRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警配置详情分页")
    @PreAuthorize("@ss.hasPermission('swm:alarm-config-detail:query')")
    public CommonResult<PageResult<SwmAlarmConfigDetailRespVO>> getSwmAlarmConfigDetailPage(@Valid SwmAlarmConfigDetailPageReqVO pageReqVO) {
        PageResult<SwmAlarmConfigDetailDO> pageResult = alarmConfigDetailService.getAlarmConfigDetailPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmAlarmConfigDetailRespVO.class));
    }

}
