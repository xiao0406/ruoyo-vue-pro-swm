package com.jeesite.modules.swm.mq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.entity.DataEntity;
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

import javax.annotation.Resource;


@Service
@Log4j2
@Conditional(AmqConfig.class)
public class AgencyMessageConsumer {

//    @Resource
//    private MessageAgency messageAgency;



    /**
     * 代办消息推送（OA、中建通）
     * @param msg         消息
     * @param deliveryTag 标识
     * @param channel     信道
     */

    @RabbitHandler
    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(value = "core.agency.message.push", durable = "true"),
                    exchange = @Exchange(value = "core.agency.message.push", durable = "true"))})
    public void projectEngineeringTasks(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        QueueMessage message = JSONUtil.toBean(msg, QueueMessage.class);
//        OARequestVO vo = JSONObject.parseObject(message.getMessage(), OARequestVO.class);
//        log.info("MQ开始消费,准备往oa推送代办");
//        String flag = vo.getFlag();
//        if (flag.equals(DataEntity.STATUS_DELETE) ) {
//            messageAgency.pushAgencyMessage(vo);
//        } else if (flag.equals(DataEntity.STATUS_DISABLE) ) {
//            messageAgency.handlAgencyMessage(vo);
//        } else if (flag.equals(DataEntity.STATUS_FREEZE) ) {
//            messageAgency.endAgencyMessage(vo);
//        }else {
//            log.info(StrUtil.format("参数错误：{}，没有该类型的消息！"), flag);
//        }
    }


}
