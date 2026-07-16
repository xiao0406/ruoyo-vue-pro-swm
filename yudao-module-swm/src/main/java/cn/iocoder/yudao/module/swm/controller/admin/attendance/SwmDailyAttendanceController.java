package cn.iocoder.yudao.module.swm.controller.admin.attendance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendancePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendanceRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendanceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDailyAttendanceDO;
import cn.iocoder.yudao.module.swm.service.SwmDailyAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 每日考勤")
@RestController
@RequestMapping("/swm/daily-attendance")
@Validated
public class SwmDailyAttendanceController {

    @Resource
    private SwmDailyAttendanceService dailyAttendanceService;

    @PostMapping("/create")
    @Operation(summary = "创建每日考勤")
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:create')")
    public CommonResult<String> createSwmDailyAttendance(@Valid @RequestBody SwmDailyAttendanceSaveReqVO createReqVO) {
        String id = dailyAttendanceService.createDailyAttendance(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新每日考勤")
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:update')")
    public CommonResult<Boolean> updateSwmDailyAttendance(@Valid @RequestBody SwmDailyAttendanceSaveReqVO updateReqVO) {
        dailyAttendanceService.updateDailyAttendance(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除每日考勤")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:delete')")
    public CommonResult<Boolean> deleteSwmDailyAttendance(@RequestParam("id") String id) {
        dailyAttendanceService.deleteDailyAttendance(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取每日考勤")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:query')")
    public CommonResult<SwmDailyAttendanceRespVO> getSwmDailyAttendance(@RequestParam("id") String id) {
        SwmDailyAttendanceDO attendance = dailyAttendanceService.getDailyAttendance(id);
        return success(BeanUtils.toBean(attendance, SwmDailyAttendanceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询每日考勤")
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:query')")
    public CommonResult<PageResult<SwmDailyAttendanceRespVO>> getSwmDailyAttendancePage(@Valid SwmDailyAttendancePageReqVO pageReqVO) {
        PageResult<SwmDailyAttendanceDO> pageResult = dailyAttendanceService.getDailyAttendancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmDailyAttendanceRespVO.class));
    }

    @GetMapping("/weekly-list")
    @Operation(summary = "鍛ㄨ€冨嫟鍒楄〃")
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:query')")
    public CommonResult<PageResult<SwmDailyAttendanceRespVO>> getWeeklyAttendancePage(
            @Valid SwmDailyAttendancePageReqVO pageReqVO) {
        PageResult<SwmDailyAttendanceDO> pageResult = dailyAttendanceService.getDailyAttendancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmDailyAttendanceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出日考勤记录")
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:export')")
    public void exportDaily(SwmDailyAttendancePageReqVO reqVO, HttpServletResponse response) throws IOException {
        export(reqVO, response, "日考勤记录.xls", "日考勤");
    }

    @GetMapping("/export-weekly")
    @Operation(summary = "导出周考勤记录")
    @PreAuthorize("@ss.hasPermission('swm:daily-attendance:export')")
    public void exportWeekly(SwmDailyAttendancePageReqVO reqVO, HttpServletResponse response) throws IOException {
        export(reqVO, response, "周考勤记录.xls", "周考勤");
    }

    private void export(SwmDailyAttendancePageReqVO reqVO, HttpServletResponse response,
                        String fileName, String sheetName) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SwmDailyAttendanceRespVO> list = BeanUtils.toBean(
                dailyAttendanceService.getDailyAttendancePage(reqVO).getList(), SwmDailyAttendanceRespVO.class);
        ExcelUtils.write(response, fileName, sheetName, SwmDailyAttendanceRespVO.class, list);
    }

}
