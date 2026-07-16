package cn.iocoder.yudao.module.iot.entity;

import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;

public class IotDevice extends IotDeviceDO {
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
