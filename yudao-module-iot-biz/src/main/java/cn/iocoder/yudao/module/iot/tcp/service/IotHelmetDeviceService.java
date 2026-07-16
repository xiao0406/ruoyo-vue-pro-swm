package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.IotDeviceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class IotHelmetDeviceService {

    static final String HELMET_DEVICE_PARAM_KEY = "iot:helmet_device:param";

    @Resource
    private IotDeviceMapper iotDeviceMapper;
    @Resource
    private RedisService redisService;

    @SuppressWarnings("unchecked")
    public <T> T getByDeviceId(String deviceId) {
        IotDeviceDO device = selectDevice(deviceId);
        if (device == null) {
            return null;
        }
        return (T) toHelmetDevice(device);
    }

    public void save(Object device) {
        if (!(device instanceof SwmHelmetDevice helmetDevice)) {
            log.warn("Unsupported helmet device object: {}", device == null ? null : device.getClass().getName());
            return;
        }

        IotDeviceDO existing = selectDevice(helmetDevice.getDeviceId());
        IotDeviceDO entity = toIotDevice(helmetDevice);
        if (existing == null) {
            iotDeviceMapper.insert(entity);
            return;
        }
        entity.setId(existing.getId());
        iotDeviceMapper.updateById(entity);
    }

    public void update(Object device) {
        save(device);
    }

    public boolean updateServerIp(String deviceId, String value) {
        return updateParam(deviceId, "serverIp", value);
    }

    public boolean updateServerPort(String deviceId, String value) {
        return updateParam(deviceId, "serverPort", value);
    }

    public boolean updateGroupDuration(String deviceId, String value) {
        return updateParam(deviceId, "groupDuration", value);
    }

    public boolean updateNormalBeaconCs(String deviceId, String value) {
        return updateParam(deviceId, "normalBeaconCs", value);
    }

    public boolean updateSpecialBeaconCs(String deviceId, String value) {
        return updateParam(deviceId, "specialBeaconCs", value);
    }

    public boolean updateDeepSleepDuration(String deviceId, String value) {
        return updateParam(deviceId, "deepSleepDuration", value);
    }

    public boolean updateLocationMode(String deviceId, String value) {
        return updateParam(deviceId, "locationMode", value);
    }

    public boolean updateBluetoothScanWindow(String deviceId, String value) {
        return updateParam(deviceId, "bluetoothScanWindow", value);
    }

    public boolean updateBluetoothScanDuration(String deviceId, String value) {
        return updateParam(deviceId, "bluetoothScanDuration", value);
    }

    public boolean updateSendInterval(String deviceId, String value) {
        return updateParam(deviceId, "sendInterval", value);
    }

    public boolean updateHazardRetriggerInterval(String deviceId, String value) {
        return updateParam(deviceId, "hazardRetriggerInterval", value);
    }

    public boolean updateSleepWakeupTime(String deviceId, String value) {
        return updateParam(deviceId, "sleepWakeupTime", value);
    }

    public boolean updateBeaconFilterName(String deviceId, String value) {
        return updateParam(deviceId, "beaconFilterName", value);
    }

    public boolean updatehatOffAlarmInterval(String deviceId, String value) {
        return updateParam(deviceId, "hatOffAlarmInterval", value);
    }

    private boolean updateParam(String deviceId, String field, String value) {
        if (deviceId == null || deviceId.isBlank()) {
            return false;
        }
        redisService.hset(HELMET_DEVICE_PARAM_KEY + ":" + deviceId, field, value);
        return true;
    }

    private IotDeviceDO selectDevice(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }
        return iotDeviceMapper.selectOne(new LambdaQueryWrapper<IotDeviceDO>()
                .eq(IotDeviceDO::getDeviceId, deviceId)
                .last("LIMIT 1"));
    }

    private IotDeviceDO toIotDevice(SwmHelmetDevice helmetDevice) {
        IotDeviceDO entity = new IotDeviceDO();
        entity.setDeviceId(helmetDevice.getDeviceId());
        entity.setDeviceName(helmetDevice.getDeviceId());
        entity.setDeviceType(helmetDevice.getHelmetType());
        entity.setMacAddress(helmetDevice.getMacAddress());
        entity.setBindPersonId(helmetDevice.getAssignedPerson());
        entity.setBindPersonName(helmetDevice.getPersonName());
        entity.setStatus(helmetDevice.getStatus() == null ? "0" : helmetDevice.getStatus());
        entity.setRemarks(helmetDevice.getRemarks());
        entity.setTenantId(helmetDevice.getTenantId() == null ? TenantContextHolder.getTenantId() : helmetDevice.getTenantId());
        return entity;
    }

    private SwmHelmetDevice toHelmetDevice(IotDeviceDO device) {
        SwmHelmetDevice helmetDevice = new SwmHelmetDevice();
        helmetDevice.setId(device.getId());
        helmetDevice.setTenantId(device.getTenantId());
        helmetDevice.setDeviceId(device.getDeviceId());
        helmetDevice.setHelmetType(SwmHelmetDevice.HelmetTypeEnum.INTEGRATED);
        helmetDevice.setMacAddress(device.getMacAddress());
        helmetDevice.setAssignedPerson(device.getBindPersonId());
        helmetDevice.setPersonName(device.getBindPersonName());
        helmetDevice.setStatus(device.getStatus());
        helmetDevice.setRemarks(device.getRemarks());
        Date now = new Date();
        helmetDevice.setCreateDate(now);
        helmetDevice.setUpdateDate(now);
        return helmetDevice;
    }
}
