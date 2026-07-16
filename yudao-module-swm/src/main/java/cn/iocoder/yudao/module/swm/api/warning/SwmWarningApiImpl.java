package cn.iocoder.yudao.module.swm.api.warning;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.swm.api.warning.dto.SwmWarningCreateReqDTO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmWarningManagementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * Public SWM warning API implementation.
 */
@Slf4j
@Service
@Validated
public class SwmWarningApiImpl implements SwmWarningApi {

    @Resource
    private SwmWarningManagementMapper swmWarningManagementMapper;

    @Override
    public String createWarning(SwmWarningCreateReqDTO reqDTO) {
        SwmWarningManagementDO warning = new SwmWarningManagementDO();
        warning.setDeviceId(reqDTO.getDeviceId());
        warning.setIdCard(StrUtil.blankToDefault(reqDTO.getIdCard(), ""));
        warning.setPersonName(reqDTO.getPersonName());
        warning.setWarningType(reqDTO.getWarningType());
        warning.setWarningContent(reqDTO.getWarningContent());
        warning.setWarningTime(reqDTO.getWarningTime() == null ? LocalDateTime.now() : reqDTO.getWarningTime());
        warning.setAlarmRecord(reqDTO.getAlarmRecord());
        warning.setAlarmTime(reqDTO.getAlarmTime() == null ? warning.getWarningTime() : reqDTO.getAlarmTime());
        warning.setTriggerReason(reqDTO.getTriggerReason());
        warning.setHandleStatus(StrUtil.blankToDefault(reqDTO.getHandleStatus(), "0"));
        warning.setFrontAlarm(reqDTO.getFrontAlarm());
        warning.setType(reqDTO.getType());
        warning.setX(reqDTO.getX());
        warning.setY(reqDTO.getY());
        warning.setHazardCategory(reqDTO.getHazardCategory());
        warning.setLocation(reqDTO.getLocation());
        warning.setArea(reqDTO.getArea());
        warning.setExtraData(reqDTO.getExtraData());
        swmWarningManagementMapper.insert(warning);
        log.info("Created SWM warning through public API, id={}, deviceId={}, type={}",
                warning.getId(), warning.getDeviceId(), warning.getType());
        return warning.getId();
    }
}
