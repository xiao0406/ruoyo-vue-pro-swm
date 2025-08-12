package com.jeesite.modules.api;

import com.jeesite.modules.vo.SwmAlarmConfigDetailVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/inner/api/swm/swmSendZjt")
public interface SwmSendZjtServiceApi {

    @PostMapping("send")
    void send(SwmAlarmConfigDetailVO detail);
}
