/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.web;

import com.jeesite.common.web.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/test")
@Api(value = "智慧劳动力管理测试接口")
public class SwmTestController extends BaseController {

    /**
     * 测试接口，验证SWM微服务是否可访问
     */
    @GetMapping(value = "hello")
    @ResponseBody
    @ApiOperation(value = "SWM测试接口")
    public Map<String, Object> hello() {
        log.info("调试：访问SWM成功");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "SWM微服务访问成功");
        result.put("data", "Hello from SWM Service!");

        return result;
    }

    /**
     * 返回服务器时间
     */
    @GetMapping(value = "serverTime")
    @ResponseBody
    @ApiOperation(value = "获取服务器时间")
    public Map<String, Object> serverTime() {
        log.info("获取SWM服务器时间");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取服务器时间成功");
        result.put("data", System.currentTimeMillis());
        result.put("formatTime", new java.util.Date().toString());

        return result;
    }
}