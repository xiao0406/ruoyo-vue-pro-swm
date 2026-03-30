package com.jeesite.modules.config;

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
 * 完善点：1. 全局JSON消息转换器 2. 生产端重试配置 3. 死信恢复策略 4. 消息确认配置
 */
@Configuration
public class RabbitMqConfig {
    // ===================== 常量定义（保留原有） =====================
    // 人员模块
    public static final String PERSON_CHANGE_QUEUE = "person_change_queue";
    public static final String PERSON_CHANGE_EXCHANGE = "person_change_exchange";
    public static final String PERSON_CHANGE_ROUTING_KEY = "person.change";
    public static final String PERSON_CHANGE_DLQ_QUEUE = "person_change_dlq_queue";
    public static final String PERSON_CHANGE_DLQ_EXCHANGE = "person_change_dlq_exchange";
    public static final String PERSON_CHANGE_DLQ_ROUTING_KEY = "person.change.dlq";

    // 设备模块
    public static final String DEVICE_CHANGE_QUEUE = "device_change_queue";
    public static final String DEVICE_CHANGE_EXCHANGE = "device_change_exchange";
    public static final String DEVICE_CHANGE_ROUTING_KEY = "device.change";
    public static final String DEVICE_CHANGE_DLQ_QUEUE = "device_change_dlq_queue";
    public static final String DEVICE_CHANGE_DLQ_EXCHANGE = "device_change_dlq_exchange";
    public static final String DEVICE_CHANGE_DLQ_ROUTING_KEY = "device.change.dlq";

    // 信标模块
    public static final String BEACON_CHANGE_QUEUE = "beacon_change_queue";
    public static final String BEACON_CHANGE_EXCHANGE = "beacon_change_exchange";
    public static final String BEACON_CHANGE_ROUTING_KEY = "beacon.change";
    public static final String BEACON_CHANGE_DLQ_QUEUE = "beacon_change_dlq_queue";
    public static final String BEACON_CHANGE_DLQ_EXCHANGE = "beacon_change_dlq_exchange";
    public static final String BEACON_CHANGE_DLQ_ROUTING_KEY = "beacon.change.dlq";

    // ===================== 核心优化：全局JSON消息转换器 =====================
    /**
     * 替换默认的JDK序列化，全局使用JSON序列化
     * 解决消费端instanceof判断失效、contentType为java-serialized-object的问题
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate：全局JSON转换器 + 消息确认 + 重试机制
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 1. 设置全局JSON消息转换器（核心修复）
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());

        // 2. 消息发送确认（确认是否到达交换机）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 可接入告警系统，比如钉钉/邮件
                System.err.printf("消息发送到交换机失败！correlationId: %s, 原因: %s%n",
                        correlationData != null ? correlationData.getId() : "null", cause);
            }
        });

        // 3. 消息返回确认（确认是否到达队列）
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            System.err.printf("消息发送到队列失败！exchange: %s, routingKey: %s, 响应码: %d, 原因: %s%n",
                    returnedMessage.getExchange(), returnedMessage.getRoutingKey(),
                    returnedMessage.getReplyCode(), returnedMessage.getReplyText());
        });
        rabbitTemplate.setMandatory(true); // 开启返回回调

        // 4. 生产端重试配置（网络抖动时自动重试）
        rabbitTemplate.setRetryTemplate(retryTemplate());

        return rabbitTemplate;
    }

    /**
     * 生产端重试策略：指数退避重试（避免高频重试压垮MQ）
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 重试次数：3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 指数退避：第一次重试间隔1s，第二次2s，第三次4s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 初始间隔1秒
        backOffPolicy.setMultiplier(2);         // 倍数
        backOffPolicy.setMaxInterval(5000);     // 最大间隔5秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    /**
     * 消费端重试失败后，将消息发布到死信队列（替代basicNack重回队列）
     */
    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, PERSON_CHANGE_DLQ_EXCHANGE, PERSON_CHANGE_DLQ_ROUTING_KEY);
    }

    // ===================== 死信队列配置（保留原有，格式优化） =====================
    // 人员死信
    @Bean
    public Queue personChangeDlqQueue() {
        return QueueBuilder.durable(PERSON_CHANGE_DLQ_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange personChangeDlqExchange() {
        return ExchangeBuilder.directExchange(PERSON_CHANGE_DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding personDlqBinding() {
        return BindingBuilder.bind(personChangeDlqQueue())
                .to(personChangeDlqExchange())
                .with(PERSON_CHANGE_DLQ_ROUTING_KEY);
    }

    // 设备死信
    @Bean
    public Queue deviceChangeDlqQueue() {
        return QueueBuilder.durable(DEVICE_CHANGE_DLQ_QUEUE).build();
    }

    @Bean
    public DirectExchange deviceChangeDlqExchange() {
        return ExchangeBuilder.directExchange(DEVICE_CHANGE_DLQ_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding deviceDlqBinding() {
        return BindingBuilder.bind(deviceChangeDlqQueue()).to(deviceChangeDlqExchange()).with(DEVICE_CHANGE_DLQ_ROUTING_KEY);
    }

    // 信标死信
    @Bean
    public Queue beaconChangeDlqQueue() {
        return QueueBuilder.durable(BEACON_CHANGE_DLQ_QUEUE).build();
    }

    @Bean
    public DirectExchange beaconChangeDlqExchange() {
        return ExchangeBuilder.directExchange(BEACON_CHANGE_DLQ_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding beaconDlqBinding() {
        return BindingBuilder.bind(beaconChangeDlqQueue()).to(beaconChangeDlqExchange()).with(BEACON_CHANGE_DLQ_ROUTING_KEY);
    }

    // ===================== 业务队列配置（保留原有，补充注释） =====================
    // 人员队列（绑定死信）
    @Bean
    public Queue personChangeQueue() {
        return QueueBuilder.durable(PERSON_CHANGE_QUEUE)
                .deadLetterExchange(PERSON_CHANGE_DLQ_EXCHANGE)    // 死信交换机
                .deadLetterRoutingKey(PERSON_CHANGE_DLQ_ROUTING_KEY)// 死信路由键
                .maxLength(10000)                                  // 可选：队列最大长度（防止消息堆积）
                .build();
    }

    @Bean
    public DirectExchange personChangeExchange() {
        return ExchangeBuilder.directExchange(PERSON_CHANGE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding personChangeBinding() {
        return BindingBuilder.bind(personChangeQueue()).to(personChangeExchange()).with(PERSON_CHANGE_ROUTING_KEY);
    }

    // 设备队列
    @Bean
    public Queue deviceChangeQueue() {
        return QueueBuilder.durable(DEVICE_CHANGE_QUEUE)
                .deadLetterExchange(DEVICE_CHANGE_DLQ_EXCHANGE)
                .deadLetterRoutingKey(DEVICE_CHANGE_DLQ_ROUTING_KEY)
                .maxLength(10000)
                .build();
    }

    @Bean
    public DirectExchange deviceChangeExchange() {
        return ExchangeBuilder.directExchange(DEVICE_CHANGE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding deviceChangeBinding() {
        return BindingBuilder.bind(deviceChangeQueue()).to(deviceChangeExchange()).with(DEVICE_CHANGE_ROUTING_KEY);
    }

    // 信标队列
    @Bean
    public Queue beaconChangeQueue() {
        return QueueBuilder.durable(BEACON_CHANGE_QUEUE)
                .deadLetterExchange(BEACON_CHANGE_DLQ_EXCHANGE)
                .deadLetterRoutingKey(BEACON_CHANGE_DLQ_ROUTING_KEY)
                .maxLength(10000)
                .build();
    }

    @Bean
    public DirectExchange beaconChangeExchange() {
        return ExchangeBuilder.directExchange(BEACON_CHANGE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding beaconChangeBinding() {
        return BindingBuilder.bind(beaconChangeQueue()).to(beaconChangeExchange()).with(BEACON_CHANGE_ROUTING_KEY);
    }
}
