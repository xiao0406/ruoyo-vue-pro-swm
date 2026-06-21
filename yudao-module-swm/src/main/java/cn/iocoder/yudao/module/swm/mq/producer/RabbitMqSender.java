package cn.iocoder.yudao.module.swm.mq.producer;

import cn.iocoder.yudao.module.swm.mq.SwmMqConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMqSender {

    @Resource
    private RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendMessage(String exchange, String routingKey, Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            rabbitTemplate.convertAndSend(exchange, routingKey, json);
            log.info("发送MQ消息，exchange:{}, routingKey:{}", exchange, routingKey);
        } catch (Exception e) {
            log.error("发送MQ消息失败", e);
        }
    }

    public void sendRecallMessage(Object obj) {
        sendMessage(SwmMqConstants.SWM_RECALL_MESSAGE_PUSH, "", obj);
    }
}
