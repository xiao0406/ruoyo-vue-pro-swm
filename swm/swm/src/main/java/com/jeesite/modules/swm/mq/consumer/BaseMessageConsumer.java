package com.jeesite.modules.swm.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.jeesite.modules.base.QueueMessage;
import com.jeesite.modules.config.AmqConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 基础消息消费者
 */
@Service
@Log4j2
@Conditional(AmqConfig.class)
public class BaseMessageConsumer {


    @RabbitHandler
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = "swm.base.message.push", durable = "true"),
                    exchange = @Exchange(value = "swm.base.message.push", durable = "true"))})
    public void baseDataConsumer(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        QueueMessage message = JSONUtil.toBean(msg, QueueMessage.class);
        System.out.println("baseDataConsumer接收到的消息：" + message.getMessage());
        System.out.println("baseDataConsumer接收到的消息：" + message.getMessage());
        System.out.println("baseDataConsumer接收到的消息：" + message.getMessage());
    }


}
