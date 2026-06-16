package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPersonWorkArea;
import com.jeesite.modules.swm.service.SwmPersonWorkAreaService;
import com.jeesite.modules.swm.service.SwmPersonWorkAreaService.BatchBindResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 人员工作区域绑定 Controller。
 */
@Controller
@RequestMapping(value = "${adminPath}/swmPersonWorkArea")
@Api(value = "人员工作区域绑定接口", tags = "人员工作区域绑定接口")
public class SwmPersonWorkAreaController extends BaseController {

    @Autowired
    private SwmPersonWorkAreaService swmPersonWorkAreaService;

    @ModelAttribute
    public SwmPersonWorkArea get(String id, boolean isNewRecord) {
        return swmPersonWorkAreaService.get(id, isNewRecord);
    }

    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmPersonWorkArea> listData(SwmPersonWorkArea swmPersonWorkArea,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        swmPersonWorkArea.setPage(new Page<>(request, response));
        return swmPersonWorkAreaService.findPage(swmPersonWorkArea);
    }

    @RequestMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存绑定")
    public String save(SwmPersonWorkArea swmPersonWorkArea,
                       @RequestParam(value = "identityCards", required = false) String[] identityCards,
                       @RequestParam(value = "areaIds", required = false) String[] areaIds) {
        try {
            swmPersonWorkArea.setIdentityCards(joinValues(identityCards, swmPersonWorkArea.getIdentityCards()));
            swmPersonWorkArea.setAreaIds(joinValues(areaIds, swmPersonWorkArea.getAreaIds()));
            if (swmPersonWorkArea.getIsNewRecord()) {
                BatchBindResult bindResult = swmPersonWorkAreaService.saveBatch(swmPersonWorkArea);
                return renderResult(Global.TRUE, text("保存人员工作区域绑定成功！新增 "
                        + bindResult.getSuccessCount() + " 条，跳过已存在 "
                        + bindResult.getSkippedCount() + " 条。"));
            }
            swmPersonWorkAreaService.save(swmPersonWorkArea);
            return renderResult(Global.TRUE, text("保存人员工作区域绑定成功！"));
        } catch (Exception e) {
            logger.error("保存人员工作区域绑定失败", e);
            return renderResult(Global.FALSE, e.getMessage());
        }
    }

    @RequestMapping(value = "batchBind")
    @ResponseBody
    @ApiOperation("多人追加绑定")
    public String batchBind(@RequestParam(value = "identityCards", required = false) String[] identityCards,
                            @RequestParam(value = "areaIds", required = false) String[] areaIds,
                            String remarks) {
        try {
            BatchBindResult bindResult = swmPersonWorkAreaService.batchBind(
                    swmPersonWorkAreaService.parseInputValues(joinValues(identityCards, null)),
                    swmPersonWorkAreaService.resolveInputAreaIds(joinValues(areaIds, null)),
                    remarks);
            return renderResult(Global.TRUE, text("追加绑定成功！新增 "
                    + bindResult.getSuccessCount() + " 条，跳过已存在 "
                    + bindResult.getSkippedCount() + " 条。"));
        } catch (Exception e) {
            logger.error("多人追加绑定失败", e);
            return renderResult(Global.FALSE, e.getMessage());
        }
    }

    @RequestMapping(value = "replaceBind")
    @ResponseBody
    @ApiOperation("单人覆盖绑定")
    public String replaceBind(String identityCard,
                              @RequestParam(value = "areaIds", required = false) String[] areaIds,
                              String remarks) {
        try {
            BatchBindResult bindResult = swmPersonWorkAreaService.replaceBind(
                    identityCard,
                    swmPersonWorkAreaService.resolveInputAreaIds(joinValues(areaIds, null)),
                    remarks);
            return renderResult(Global.TRUE, text("覆盖绑定成功！新增 "
                    + bindResult.getSuccessCount() + " 条，保留已存在 "
                    + bindResult.getSkippedCount() + " 条，删除 "
                    + bindResult.getDeletedCount() + " 条。"));
        } catch (Exception e) {
            logger.error("单人覆盖绑定失败", e);
            return renderResult(Global.FALSE, e.getMessage());
        }
    }

    private String joinValues(String[] values, String fallback) {
        if (values == null || values.length == 0) {
            return fallback;
        }
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(value.trim());
        }
        return builder.length() > 0 ? builder.toString() : fallback;
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除绑定")
    public String delete(SwmPersonWorkArea swmPersonWorkArea) {
        swmPersonWorkAreaService.delete(swmPersonWorkArea);
        return renderResult(Global.TRUE, text("删除人员工作区域绑定成功！"));
    }
}
