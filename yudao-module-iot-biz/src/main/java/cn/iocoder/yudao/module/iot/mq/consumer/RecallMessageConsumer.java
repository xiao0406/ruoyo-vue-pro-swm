package cn.iocoder.yudao.module.iot.mq.consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.config.AmqConfig;
import cn.iocoder.yudao.module.iot.mq.handler.RecallMessageHandler;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
@Log4j2
@Conditional(AmqConfig.class)
public class RecallMessageConsumer {
private static final Logger log = LoggerFactory.getLogger(RecallMessageConsumer.class);

    @Resource
    private RecallMessageHandler recallMessageHandler;

    @RabbitHandler
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = "swm.recall.message.push", durable = "true"),
                    exchange = @Exchange(value = "swm.recall.message.push", durable = "true"))})
    public void recallMessage(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        log.info("RecallMessageConsumer recallMessage msg | {}", msg);
        QueueMessage message = JSONUtil.toBean(msg, QueueMessage.class);
        recallMessageHandler.handleMessage(message.getMessage());
    }
}
