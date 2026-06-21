package cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummaryPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummaryRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummarySaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAttendanceSummaryDO;
import cn.iocoder.yudao.module.swm.service.SwmAttendanceSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 考勤汇总")
@RestController
@RequestMapping("/swm/attendance-summary")
@Validated
public class SwmAttendanceSummaryController {

    @Resource
    private SwmAttendanceSummaryService attendanceSummaryService;

    @PostMapping("/create")
    @Operation(summary = "创建考勤汇总")
    @PreAuthorize("@ss.hasPermission('swm:attendance-summary:create')")
    public CommonResult<String> createSwmAttendanceSummary(@Valid @RequestBody SwmAttendanceSummarySaveReqVO createReqVO) {
        String id = attendanceSummaryService.createAttendanceSummary(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新考勤汇总")
    @PreAuthorize("@ss.hasPermission('swm:attendance-summary:update')")
    public CommonResult<Boolean> updateSwmAttendanceSummary(@Valid @RequestBody SwmAttendanceSummarySaveReqVO updateReqVO) {
        attendanceSummaryService.updateAttendanceSummary(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除考勤汇总")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:attendance-summary:delete')")
    public CommonResult<Boolean> deleteSwmAttendanceSummary(@RequestParam("id") String id) {
        attendanceSummaryService.deleteAttendanceSummary(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取考勤汇总")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:attendance-summary:query')")
    public CommonResult<SwmAttendanceSummaryRespVO> getSwmAttendanceSummary(@RequestParam("id") String id) {
        SwmAttendanceSummaryDO attendanceSummary = attendanceSummaryService.getAttendanceSummary(id);
        return success(BeanUtils.toBean(attendanceSummary, SwmAttendanceSummaryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询考勤汇总")
    @PreAuthorize("@ss.hasPermission('swm:attendance-summary:query')")
    public CommonResult<PageResult<SwmAttendanceSummaryRespVO>> getSwmAttendanceSummaryPage(@Valid SwmAttendanceSummaryPageReqVO pageReqVO) {
        PageResult<SwmAttendanceSummaryDO> pageResult = attendanceSummaryService.getAttendanceSummaryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmAttendanceSummaryRespVO.class));
    }

}
