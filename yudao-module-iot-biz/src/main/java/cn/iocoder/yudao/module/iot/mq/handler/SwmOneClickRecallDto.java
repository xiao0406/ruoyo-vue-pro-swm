package cn.iocoder.yudao.module.iot.mq.handler;

import lombok.Data;

@Data
public class SwmOneClickRecallDto {
    private String id;
    private String voiceText;
    private String deviceList;
    private Integer recallFrequency;
    private Integer recallCount;
    private OneKeyRecall.EvacuationPlanEnum evacuationPlan;
}
