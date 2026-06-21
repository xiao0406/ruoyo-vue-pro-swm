package cn.iocoder.yudao.module.swm.controller.admin.schedulelog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo.SwmPersonScheduleLogPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo.SwmPersonScheduleLogRespVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleLogDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonScheduleLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 排班日志")
@RestController
@RequestMapping("/swm/schedule-log")
@Validated
public class SwmPersonScheduleLogController {

    @Resource
    private SwmPersonScheduleLogService personScheduleLogService;

    @GetMapping("/get")
    @Operation(summary = "获取排班日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:schedule-log:query')")
    public CommonResult<SwmPersonScheduleLogRespVO> getSwmPersonScheduleLog(@RequestParam("id") String id) {
        SwmPersonScheduleLogDO personScheduleLog = personScheduleLogService.getPersonScheduleLog(id);
        return success(BeanUtils.toBean(personScheduleLog, SwmPersonScheduleLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询排班日志")
    @PreAuthorize("@ss.hasPermission('swm:schedule-log:query')")
    public CommonResult<PageResult<SwmPersonScheduleLogRespVO>> getSwmPersonScheduleLogPage(@Valid SwmPersonScheduleLogPageReqVO pageReqVO) {
        PageResult<SwmPersonScheduleLogDO> pageResult = personScheduleLogService.getPersonScheduleLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonScheduleLogRespVO.class));
    }

}
