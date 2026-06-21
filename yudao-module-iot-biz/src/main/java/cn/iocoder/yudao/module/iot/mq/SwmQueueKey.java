package cn.iocoder.yudao.module.iot.mq;

/**
 * SWM 队列常量
 */
public enum SwmQueueKey {

    SWM_RECALL_MESSAGE_PUSH("swm.recall.message.push");

    private final String queueName;

    SwmQueueKey(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

}
