package com.jeesite.modules;


import com.jeesite.modules.swm.mq.SwmQueueKey;
import com.jeesite.modules.swm.mq.producer.RabbitMqSender;
import com.jeesite.modules.swm.param.SwmOneClickRecallSaveParam;
import com.jeesite.modules.swm.service.impl.SwmOneClickRecallServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.util.UUID;

@ComponentScan(basePackages="com.jeesite.*")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {SwmApplication.class})
@Slf4j
public class ControllerTest {

    @Autowired
    private SwmOneClickRecallServiceImpl swmOneClickRecallService;

    @Test
    public void send(){

        System.out.println(123);
        SwmOneClickRecallSaveParam param = new SwmOneClickRecallSaveParam();
        swmOneClickRecallService.addRecallRecord(param);
    }

}
