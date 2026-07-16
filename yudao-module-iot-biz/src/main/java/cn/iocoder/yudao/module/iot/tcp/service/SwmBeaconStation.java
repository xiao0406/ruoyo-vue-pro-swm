package cn.iocoder.yudao.module.iot.tcp.service;

import lombok.Data;

@Data
public class SwmBeaconStation {
    private String id;
    private Long tenantId;
    private String beaconId;
    private String deviceName;
    private String beaconType;
    private String location;
    private String area;
}
