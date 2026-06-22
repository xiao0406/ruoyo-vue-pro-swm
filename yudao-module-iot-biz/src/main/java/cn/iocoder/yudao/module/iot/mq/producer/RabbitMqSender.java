package cn.iocoder.yudao.module.iot.mq.producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.mq.SwmQueueKey;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息发送器
 *
 * 迁移自 JeeSite: com.jeesite.modules.mq.producer.RabbitMqSender
 */
@Component
public class RabbitMqSender {
private static final Logger log = LoggerFactory.getLogger(RabbitMqSender.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param queueKey 队列key
     * @param bizKey 业务key，防止重复放入队列
     * @param obj 消息体
     */
    public void sendMessage(SwmQueueKey queueKey, String bizKey, Object obj) {
        String queueName = queueKey.name();
        if (StrUtil.isBlank(queueName)) {
            log.error("RabbitMQ消息通道key：{}未配置", queueKey);
            return;
        }

        try {
            rabbitTemplate.convertAndSend(queueName, obj);
            log.info("发送RabbitMQ消息成功，队列：{}，业务key：{}", queueName, bizKey);
        } catch (Exception e) {
            log.error("发送RabbitMQ消息失败，队列：{}，业务key：{}", queueName, bizKey, e);
        }
    }

}
