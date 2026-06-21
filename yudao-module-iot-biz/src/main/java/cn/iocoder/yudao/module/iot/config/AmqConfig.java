package cn.iocoder.yudao.module.iot.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * RabbitMQ 条件配置
 * 检查 RabbitMQ 是否启用
 */
public class AmqConfig implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 检查 RabbitMQ 配置是否存在
        return context.getEnvironment().containsProperty("spring.rabbitmq.host");
    }

}
