package cn.iocoder.yudao.module.iot.controller.admin.tcp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.tcp.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.TcpSendMessageLogDO;
import cn.iocoder.yudao.module.iot.service.TcpSendMessageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - TCP消息日志")
@RestController
@RequestMapping("/iot/tcp-message-log")
@Validated
public class TcpSendMessageLogController {

    @Resource
    private TcpSendMessageLogService tcpSendMessageLogService;

    @GetMapping("/get")
    @Operation(summary = "获取TCP消息日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:tcp-message-log:query')")
    public CommonResult<TcpSendMessageLogRespVO> get(@RequestParam("id") String id) {
        TcpSendMessageLogDO log = tcpSendMessageLogService.getTcpSendMessageLog(id);
        return success(BeanUtils.toBean(log, TcpSendMessageLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询TCP消息日志")
    @PreAuthorize("@ss.hasPermission('iot:tcp-message-log:query')")
    public CommonResult<PageResult<TcpSendMessageLogRespVO>> getPage(@Valid TcpSendMessageLogPageReqVO pageReqVO) {
        PageResult<TcpSendMessageLogDO> pageResult = tcpSendMessageLogService.getTcpSendMessageLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TcpSendMessageLogRespVO.class));
    }
}
