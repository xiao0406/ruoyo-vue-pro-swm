package cn.iocoder.yudao.module.swm.controller.admin.thirdapilog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.thirdapilog.vo.SwmThirdApiLogPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.thirdapilog.vo.SwmThirdApiLogRespVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmThirdApiLogDO;
import cn.iocoder.yudao.module.swm.service.SwmThirdApiLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 第三方API日志")
@RestController
@RequestMapping("/swm/third-api-log")
@Validated
public class SwmThirdApiLogController {

    @Resource
    private SwmThirdApiLogService thirdApiLogService;

    @GetMapping("/get")
    @Operation(summary = "获取第三方API日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:third-api-log:query')")
    public CommonResult<SwmThirdApiLogRespVO> getSwmThirdApiLog(@RequestParam("id") String id) {
        SwmThirdApiLogDO thirdApiLog = thirdApiLogService.getThirdApiLog(id);
        return success(BeanUtils.toBean(thirdApiLog, SwmThirdApiLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询第三方API日志")
    @PreAuthorize("@ss.hasPermission('swm:third-api-log:query')")
    public CommonResult<PageResult<SwmThirdApiLogRespVO>> getSwmThirdApiLogPage(@Valid SwmThirdApiLogPageReqVO pageReqVO) {
        PageResult<SwmThirdApiLogDO> pageResult = thirdApiLogService.getThirdApiLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmThirdApiLogRespVO.class));
    }

}
