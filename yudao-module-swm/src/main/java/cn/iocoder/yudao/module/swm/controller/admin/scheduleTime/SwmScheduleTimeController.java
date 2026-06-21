package cn.iocoder.yudao.module.swm.controller.admin.scheduleTime;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimeRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmScheduleTimeDO;
import cn.iocoder.yudao.module.swm.service.SwmScheduleTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 排班时间")
@RestController
@RequestMapping("/swm/schedule-time")
@Validated
public class SwmScheduleTimeController {

    @Resource
    private SwmScheduleTimeService scheduleTimeService;

    @PostMapping("/create")
    @Operation(summary = "创建排班时间")
    @PreAuthorize("@ss.hasPermission('swm:schedule-time:create')")
    public CommonResult<String> createSwmScheduleTime(@Valid @RequestBody SwmScheduleTimeSaveReqVO createReqVO) {
        String id = scheduleTimeService.createScheduleTime(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新排班时间")
    @PreAuthorize("@ss.hasPermission('swm:schedule-time:update')")
    public CommonResult<Boolean> updateSwmScheduleTime(@Valid @RequestBody SwmScheduleTimeSaveReqVO updateReqVO) {
        scheduleTimeService.updateScheduleTime(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排班时间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:schedule-time:delete')")
    public CommonResult<Boolean> deleteSwmScheduleTime(@RequestParam("id") String id) {
        scheduleTimeService.deleteScheduleTime(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取排班时间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:schedule-time:query')")
    public CommonResult<SwmScheduleTimeRespVO> getSwmScheduleTime(@RequestParam("id") String id) {
        SwmScheduleTimeDO scheduleTime = scheduleTimeService.getScheduleTime(id);
        return success(BeanUtils.toBean(scheduleTime, SwmScheduleTimeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询排班时间")
    @PreAuthorize("@ss.hasPermission('swm:schedule-time:query')")
    public CommonResult<PageResult<SwmScheduleTimeRespVO>> getSwmScheduleTimePage(@Valid SwmScheduleTimePageReqVO pageReqVO) {
        PageResult<SwmScheduleTimeDO> pageResult = scheduleTimeService.getScheduleTimePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmScheduleTimeRespVO.class));
    }

}
