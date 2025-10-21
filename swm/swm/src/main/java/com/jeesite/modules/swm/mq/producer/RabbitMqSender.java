package com.jeesite.modules.swm.mq.producer;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.constant.RabbitMQConstant;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.service.ServiceException;
import com.jeesite.modules.base.producer.IMqProducerService;
import com.jeesite.modules.config.properties.RabbitmqConfigProperties;
import com.jeesite.modules.swm.mq.SwmQueueKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

/**
 * 类说明
 *
 * @author 李鹏
 * @date 2022/7/6
 */
@Slf4j
@Component
public class RabbitMqSender {

    @Autowired
    private IMqProducerService mqProducer;
    @Autowired
    private RabbitmqConfigProperties rabbitmqConfigProperties;

    /**
     * 发送消息
     * @param queueKey yml中配置的队列key
     * @param bizKey 业务key，防止重复放入队列
     * @param obj 消息体
     */
    public void sendMessage(SwmQueueKey queueKey, String bizKey, Object obj) {
        String queueKeys = queueKey.name();
        String queueName = rabbitmqConfigProperties.getBizQueusMap().get(queueKeys);
        if(StringUtils.isEmpty(queueName)){
            throw new ServiceException(MessageFormat.format("RabbitMQ消息通道key：{}未配置", queueKeys));
        }
        mqProducer.sendMq(queueKeys + bizKey, queueName, JSONObject.toJSONString(obj));
    }
}
