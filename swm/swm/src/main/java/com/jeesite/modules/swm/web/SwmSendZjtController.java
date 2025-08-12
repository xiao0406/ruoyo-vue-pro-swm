package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.service.SwmSendZjtService;
import com.jeesite.modules.vo.SwmAlarmConfigDetailVO;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * 消息推送zjt contaoller
 */
@Controller
@RequestMapping(value = "${adminPath}/sendZjt")
@Api(value = "报警配置接口", tags = "报警配置接口")
public class SwmSendZjtController  extends BaseController {
    @Resource
    private SwmSendZjtService swmSendZjtService;
    /**
     * 推送中建通
     */
    @PostMapping("send")
    public String send(SwmAlarmConfigDetailVO detail){
        swmSendZjtService.send(detail);
        return renderResult(Global.TRUE, text("推送成功！"));
    }
}
