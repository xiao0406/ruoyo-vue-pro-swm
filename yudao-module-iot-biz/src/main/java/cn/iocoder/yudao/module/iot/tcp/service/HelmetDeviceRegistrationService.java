package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.session.DeviceSession;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class HelmetDeviceRegistrationService {

    @Resource
    private IotHelmetDeviceService iotHelmetDeviceService;

    public void registerDeviceIfNeeded(TcpMessageData tcpData, DeviceSession session) {
        if (tcpData == null || tcpData.getDeviceId() == null || tcpData.getDeviceId().trim().isEmpty()) {
            log.warn("Skip helmet device registration because deviceId is empty");
            return;
        }
        if (session == null) {
            log.warn("Skip helmet device registration because session is null, deviceId={}", tcpData.getDeviceId());
            return;
        }
        if (session.isDeviceRegistrationChecked()) {
            log.debug("Helmet device registration already checked, sessionId={}, deviceId={}",
                    session.getSessionId(), tcpData.getDeviceId());
            return;
        }

        String deviceId = tcpData.getDeviceId();
        try {
            SwmHelmetDevice existingDevice = iotHelmetDeviceService.getByDeviceId(deviceId);
            if (existingDevice == null) {
                registerNewDevice(tcpData);
            }
            session.setDeviceRegistrationChecked(true);
        } catch (Exception ex) {
            log.error("Failed to check or register helmet device, deviceId={}", deviceId, ex);
        }
    }

    private void registerNewDevice(TcpMessageData tcpData) {
        String deviceId = tcpData.getDeviceId();

        SwmHelmetDevice newDevice = new SwmHelmetDevice();
        newDevice.setDeviceId(deviceId);
        newDevice.setHelmetType(SwmHelmetDevice.HelmetTypeEnum.INTEGRATED);
        newDevice.setTenantId(TenantContextHolder.getTenantId());
        newDevice.setStatus("0");
        newDevice.setUsageStatus(0);
        newDevice.setBatteryLevel(tcpData.getBatteryLevel());

        Date now = new Date();
        newDevice.setCreateDate(now);
        newDevice.setUpdateDate(now);
        newDevice.setIp(null);
        newDevice.setMacAddress(null);
        newDevice.setAssignedPerson(null);
        newDevice.setPersonName(null);
        newDevice.setPersonPhone(null);
        newDevice.setAssignedWorkshop(null);
        newDevice.setAssignedProcess(null);
        newDevice.setAssignedTeam(null);
        newDevice.setMotionStatus(null);
        newDevice.setBindTime(null);
        newDevice.setUnbindTime(null);
        newDevice.setBindDurationDays(null);
        newDevice.setRemarks("TCP auto registration");

        iotHelmetDeviceService.save(newDevice);
        log.info("Registered new helmet device, deviceId={}, tenantId={}", deviceId, newDevice.getTenantId());
    }
}
