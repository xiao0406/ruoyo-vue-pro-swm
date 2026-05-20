package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.cache.SwmAlarmConfigCache;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.service.SwmAlarmConfigService;
import com.jeesite.modules.sys.utils.CorpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 报警配置Controller
 * 
 * @author zwf
 * @version 2025-06-18
 */
@Controller
@RequestMapping(value = "${adminPath}/alarmConfig")
@Api(value = "报警配置接口", tags = "报警配置接口")
public class SwmAlarmConfigController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SwmAlarmConfigController.class);

    @Autowired
    private SwmAlarmConfigService swmAlarmConfigService;
    @Autowired
    @Lazy
    private SwmAlarmConfigCache swmAlarmConfigCache;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmAlarmConfig get(String id, boolean isNewRecord) {
        return swmAlarmConfigService.get(id, isNewRecord);
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "list")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmAlarmConfig> list(SwmAlarmConfig swmAlarmConfig, HttpServletRequest request,
            HttpServletResponse response) {
        Page<SwmAlarmConfig> page = new Page<>(request, response);
        swmAlarmConfig.setPage(page);
        return swmAlarmConfigService.findPage(swmAlarmConfig);
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public SwmAlarmConfig form(SwmAlarmConfig swmAlarmConfig) {
        if (swmAlarmConfig.getIsNewRecord()) {
            // 新增时设置默认值
            swmAlarmConfig.setEnableAlarm(1); // 默认启用
            swmAlarmConfig.setNeedConfirm(0); // 默认不需要弹窗确认
        }
        return swmAlarmConfig;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmAlarmConfig swmAlarmConfig) {
        try {
            swmAlarmConfigService.save(swmAlarmConfig);

            //更新缓存
            swmAlarmConfig.setCorpCode(CorpUtils.getCurrentCorpCode());
            swmAlarmConfigCache.update(swmAlarmConfig);
            return renderResult(Global.TRUE, text("保存报警配置成功！"));
        } catch (Exception e) {
            logger.error("保存报警配置失败", e);
            return renderResult(Global.FALSE, text("保存报警配置失败：" + e.getMessage()));
        }
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmAlarmConfig swmAlarmConfig) {
        swmAlarmConfigService.delete(swmAlarmConfig);
        //更新缓存
        swmAlarmConfig.setCorpCode(CorpUtils.getCurrentCorpCode());
        swmAlarmConfigCache.update(swmAlarmConfig);
        return renderResult(Global.TRUE, text("删除报警配置成功！"));
    }

} 