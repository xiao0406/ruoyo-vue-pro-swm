package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHelmetDeviceConfig;
import com.jeesite.modules.swm.service.SwmHelmetDeviceConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 头盔设备配置Controller
 * @author Shawn
 * @date 2025-01-08
 */
@Api(tags = "头盔设备配置管理")
@RestController
@RequestMapping(value = "${adminPath}/swmHelmetDeviceConfig")
public class SwmHelmetDeviceConfigController extends BaseController {
    
    @Autowired
    private SwmHelmetDeviceConfigService swmHelmetDeviceConfigService;
    
    /**
     * 保存或更新配置
     */
    @ApiOperation(value = "保存或更新配置")
    @PostMapping(value = "saveConfig")
    public String saveConfig(@RequestBody SwmHelmetDeviceConfig config) {
        swmHelmetDeviceConfigService.saveOrUpdateConfig(config);
        return renderResult(Global.TRUE, "保存成功");
    }
}