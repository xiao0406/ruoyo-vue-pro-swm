package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.api.enums.CorpDbEnum;
import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.session.DeviceSession;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 头盔设备注册服务
 * 专门处理TCP设备注册逻辑，基于Session级别的检查策略
 *
 * @author Shawn
 * @date 2025-01-22
 */
@Service
public class HelmetDeviceRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(HelmetDeviceRegistrationService.class);

    @Resource
    private SwmHelmetDeviceService swmHelmetDeviceService;

    /**
     * 检查并注册新设备（基于Session级别的检查）
     *
     * @param tcpData TCP消息数据
     * @param session 设备会话
     */
    public void registerDeviceIfNeeded(TcpMessageData tcpData, DeviceSession session) {
        String deviceId = tcpData.getDeviceId();

        if (deviceId == null || deviceId.trim().isEmpty()) {
            logger.warn("设备ID为空，跳过设备注册检查");
            return;
        }

        if (session == null) {
            logger.warn("Session为空，跳过设备注册检查: deviceId={}", deviceId);
            return;
        }

        // 检查Session是否已经检查过设备注册
        if (session.isDeviceRegistrationChecked()) {
            logger.info("Session已检查过设备注册，跳过: sessionId={}, deviceId={}",
                    session.getSessionId(), deviceId);
            return;
        }

        try {
            // 检查数据库中是否存在该设备
            SwmHelmetDevice existingDevice = swmHelmetDeviceService.getByDeviceId(deviceId);
            if (existingDevice != null) {
                // 设备已存在
                logger.info("设备已存在于数据库: {}", deviceId);
            } else {
                // 设备不存在，注册新设备
                registerNewDevice(tcpData);
                logger.info("新设备注册成功: {}", deviceId);
            }

            // 标记Session已检查过设备注册
            session.setDeviceRegistrationChecked(true);
            logger.debug("标记Session已检查设备注册: sessionId={}, deviceId={}",
                    session.getSessionId(), deviceId);

        } catch (Exception e) {
            logger.error("设备注册检查失败，设备ID: {}, 错误: {}", deviceId, e.getMessage(), e);
        }
    }

    /**
     * 注册新设备到数据库
     *
     * @param tcpData TCP消息数据
     */
    private void registerNewDevice(TcpMessageData tcpData) {
        String deviceId = tcpData.getDeviceId();

        try {
            // 创建新的设备记录
            SwmHelmetDevice newDevice = new SwmHelmetDevice();

            // 不设置ID，让JeeSite框架自动生成
            // newDevice.setId(IdGen.uuid()); // 删除手动设置ID

            // 设置基本信息
            newDevice.setDeviceId(deviceId); // 设备编号
            newDevice.setHelmetType(SwmHelmetDevice.HelmetTypeEnum.INTEGRATED); // 头盔类型：3-一体式
            newDevice.setStatus("0"); // 状态：0-正常（有效数据）
            newDevice.setUsageStatus(0); // 使用状态：0-已解绑

            // 设置电池信息（如果有）
            if (tcpData.getBatteryLevel() != null) {
                newDevice.setBatteryLevel(tcpData.getBatteryLevel());
            }

            // 设置时间信息
            Date now = new Date();
            newDevice.setCreateDate(now);
            newDevice.setUpdateDate(now);

            // 其他字段设为null（按需求）
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
            newDevice.setRemarks("TCP自动注册");
            newDevice.setCorpCode(CorpDbEnum.ZJZK.getCorpCode());
            newDevice.setCorpName(CorpDbEnum.ZJZK.getCorpCode());

            // 保存到数据库
            swmHelmetDeviceService.save(newDevice);

            logger.info("新设备注册成功: deviceId={}, batteryLevel={}",
                    deviceId, tcpData.getBatteryLevel());

        } catch (Exception e) {
            logger.error("新设备注册失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new RuntimeException("设备注册失败: " + e.getMessage(), e);
        }
    }

}
