package cn.iocoder.yudao.module.swm.mq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
@Slf4j
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
        log.info("agencyDataConsumer接收到的消息：{}", msg);
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
