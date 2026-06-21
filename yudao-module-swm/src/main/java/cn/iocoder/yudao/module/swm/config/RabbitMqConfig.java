package cn.iocoder.yudao.module.swm.config;

import cn.iocoder.yudao.module.swm.mq.SwmMqConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * RabbitMQ配置：人员+设备+信标 模块
 */
@Configuration
public class RabbitMqConfig {

    // ===================== 核心优化：全局JSON消息转换器 =====================
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.printf("消息发送到交换机失败！correlationId: %s, 原因: %s%n",
                        correlationData != null ? correlationData.getId() : "null", cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            System.err.printf("消息发送到队列失败！exchange: %s, routingKey: %s, 响应码: %d, 原因: %s%n",
                    returnedMessage.getExchange(), returnedMessage.getRoutingKey(),
                    returnedMessage.getReplyCode(), returnedMessage.getReplyText());
        });
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(5000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate,
                SwmMqConstants.PERSON_CHANGE_DLQ_EXCHANGE, SwmMqConstants.PERSON_CHANGE_DLQ_ROUTING_KEY);
    }

    // ===================== 死信队列配置 =====================
    // 人员死信
    @Bean
    public Queue personChangeDlqQueue() {
        return QueueBuilder.durable(SwmMqConstants.PERSON_CHANGE_DLQ_QUEUE).build();
    }

    @Bean
    public DirectExchange personChangeDlqExchange() {
        return ExchangeBuilder.directExchange(SwmMqConstants.PERSON_CHANGE_DLQ_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding personDlqBinding() {
        return BindingBuilder.bind(personChangeDlqQueue()).to(personChangeDlqExchange())
                .with(SwmMqConstants.PERSON_CHANGE_DLQ_ROUTING_KEY);
    }

    // 设备死信
    @Bean
    public Queue deviceChangeDlqQueue() {
        return QueueBuilder.durable(SwmMqConstants.DEVICE_CHANGE_DLQ_QUEUE).build();
    }

    @Bean
    public DirectExchange deviceChangeDlqExchange() {
        return ExchangeBuilder.directExchange(SwmMqConstants.DEVICE_CHANGE_DLQ_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding deviceDlqBinding() {
        return BindingBuilder.bind(deviceChangeDlqQueue()).to(deviceChangeDlqExchange())
                .with(SwmMqConstants.DEVICE_CHANGE_DLQ_ROUTING_KEY);
    }

    // 信标死信
    @Bean
    public Queue beaconChangeDlqQueue() {
        return QueueBuilder.durable(SwmMqConstants.BEACON_CHANGE_DLQ_QUEUE).build();
    }

    @Bean
    public DirectExchange beaconChangeDlqExchange() {
        return ExchangeBuilder.directExchange(SwmMqConstants.BEACON_CHANGE_DLQ_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding beaconDlqBinding() {
        return BindingBuilder.bind(beaconChangeDlqQueue()).to(beaconChangeDlqExchange())
                .with(SwmMqConstants.BEACON_CHANGE_DLQ_ROUTING_KEY);
    }

    // ===================== 业务队列配置 =====================
    // 人员队列
    @Bean
    public Queue personChangeQueue() {
        return QueueBuilder.durable(SwmMqConstants.PERSON_CHANGE_QUEUE)
                .deadLetterExchange(SwmMqConstants.PERSON_CHANGE_DLQ_EXCHANGE)
                .deadLetterRoutingKey(SwmMqConstants.PERSON_CHANGE_DLQ_ROUTING_KEY)
                .maxLength(10000)
                .build();
    }

    @Bean
    public DirectExchange personChangeExchange() {
        return ExchangeBuilder.directExchange(SwmMqConstants.PERSON_CHANGE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding personChangeBinding() {
        return BindingBuilder.bind(personChangeQueue()).to(personChangeExchange())
                .with(SwmMqConstants.PERSON_CHANGE_ROUTING_KEY);
    }

    // 设备队列
    @Bean
    public Queue deviceChangeQueue() {
        return QueueBuilder.durable(SwmMqConstants.DEVICE_CHANGE_QUEUE)
                .deadLetterExchange(SwmMqConstants.DEVICE_CHANGE_DLQ_EXCHANGE)
                .deadLetterRoutingKey(SwmMqConstants.DEVICE_CHANGE_DLQ_ROUTING_KEY)
                .maxLength(10000)
                .build();
    }

    @Bean
    public DirectExchange deviceChangeExchange() {
        return ExchangeBuilder.directExchange(SwmMqConstants.DEVICE_CHANGE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding deviceChangeBinding() {
        return BindingBuilder.bind(deviceChangeQueue()).to(deviceChangeExchange())
                .with(SwmMqConstants.DEVICE_CHANGE_ROUTING_KEY);
    }

    // 信标队列
    @Bean
    public Queue beaconChangeQueue() {
        return QueueBuilder.durable(SwmMqConstants.BEACON_CHANGE_QUEUE)
                .deadLetterExchange(SwmMqConstants.BEACON_CHANGE_DLQ_EXCHANGE)
                .deadLetterRoutingKey(SwmMqConstants.BEACON_CHANGE_DLQ_ROUTING_KEY)
                .maxLength(10000)
                .build();
    }

    @Bean
    public DirectExchange beaconChangeExchange() {
        return ExchangeBuilder.directExchange(SwmMqConstants.BEACON_CHANGE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding beaconChangeBinding() {
        return BindingBuilder.bind(beaconChangeQueue()).to(beaconChangeExchange())
                .with(SwmMqConstants.BEACON_CHANGE_ROUTING_KEY);
    }
}
