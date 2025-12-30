package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPersonScheduleLog;
import com.jeesite.modules.swm.service.SwmPersonScheduleLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 人员班次修改日志 Controller
 * 提供日志查询、单个保存、批量保存等接口
 *
 * @author fangxiaolong
 * @since 2025-12-30
 */
@Controller
@RequestMapping(value = "${adminPath}/personScheduleLog")
@Api(tags = "人员班次修改日志管理")
public class SwmPersonScheduleLogController extends BaseController {

    @Autowired
    private SwmPersonScheduleLogService swmPersonScheduleLogService;

    /**
     * 根据ID查询单条日志详情
     */
    @GetMapping(value = "get/{id}")
    @ApiOperation(value = "查询日志详情", notes = "根据日志ID查询单条人员班次修改日志详情")
    @ResponseBody
    public Object getClassesRecord(
            @ApiParam(name = "id", value = "日志ID", required = true)
            @PathVariable String id) {
        List<SwmPersonScheduleLog> swmPersonScheduleLogList = swmPersonScheduleLogService.getClassesRecord(id);
        return swmPersonScheduleLogList;
    }
}
