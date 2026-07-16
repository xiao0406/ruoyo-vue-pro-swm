package cn.iocoder.yudao.module.iot.tcp.processor;

import lombok.Data;

@Data
public class SwmHazardSource {
    private String id;
    private Long tenantId;
    private String hazardName;
    private String hazardCategory;
    private String location;
    private String beaconIdentifier;
    private String voiceTemplateId;
    private String beaconTag;
}
