package cn.iocoder.yudao.module.swm.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 基础消息消费者
 */
@Service
@Slf4j
public class BaseMessageConsumer {


    @RabbitHandler
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = "swm.base.message.push", durable = "true"),
                    exchange = @Exchange(value = "swm.base.message.push", durable = "true"))})
    public void baseDataConsumer(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        log.info("baseDataConsumer接收到的消息：{}", msg);
    }


}
