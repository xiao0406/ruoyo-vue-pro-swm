package cn.iocoder.yudao.module.swm.controller.admin.joblog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.joblog.vo.SwmJobLogPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.joblog.vo.SwmJobLogRespVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmJobLogDO;
import cn.iocoder.yudao.module.swm.service.SwmJobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 任务日志")
@RestController
@RequestMapping("/swm/job-log")
@Validated
public class SwmJobLogController {

    @Resource
    private SwmJobLogService jobLogService;

    @GetMapping("/get")
    @Operation(summary = "获取任务日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:job-log:query')")
    public CommonResult<SwmJobLogRespVO> getSwmJobLog(@RequestParam("id") String id) {
        SwmJobLogDO jobLog = jobLogService.getJobLog(id);
        return success(BeanUtils.toBean(jobLog, SwmJobLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询任务日志")
    @PreAuthorize("@ss.hasPermission('swm:job-log:query')")
    public CommonResult<PageResult<SwmJobLogRespVO>> getSwmJobLogPage(@Valid SwmJobLogPageReqVO pageReqVO) {
        PageResult<SwmJobLogDO> pageResult = jobLogService.getJobLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmJobLogRespVO.class));
    }

}
