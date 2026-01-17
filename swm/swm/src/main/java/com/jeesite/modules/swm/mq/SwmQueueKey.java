package com.jeesite.modules.swm.mq;

import com.jeesite.common.constant.RabbitMQConstant;

public enum SwmQueueKey implements RabbitMQConstant {

    SWM_RECALL_MESSAGE_PUSH,

    // 主要用于人员信息、设备信息以及信标基础信息变更推送
    SWM_BASE_MESSAGE_PUSH;

}
