package cn.iocoder.yudao.module.iot.mq.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmOneClickRecallDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.OneKeyRecallDO;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.DeviceSoSAlarmHandler;
import cn.iocoder.yudao.module.iot.service.OneKeyRecallService;
import cn.iocoder.yudao.module.iot.service.VoiceAlarmService;
import cn.iocoder.yudao.module.iot.service.scheduler.MethodScheduler;
import cn.iocoder.yudao.module.iot.tcp.server.NettyTcpServer;
import cn.iocoder.yudao.module.iot.tcp.session.DeviceSession;
import cn.iocoder.yudao.module.iot.tcp.session.SessionManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RecallMessageHandler {

    @Resource
    private NettyTcpServer nettyTcpServer;
    @Resource
    private VoiceAlarmService voiceAlarmService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private OneKeyRecallService oneKeyRecallService;
    @Resource
    private DeviceSoSAlarmHandler soSAlarmHandler;

    public void handleMessage(String message) {
        SwmOneClickRecallDto swmOneClickRecallDto = JSON.parseObject(message, SwmOneClickRecallDto.class);
        log.info("RecallMessageHandler handleMessage swmOneClickRecallDto | {}", swmOneClickRecallDto);
        String voiceText = swmOneClickRecallDto.getVoiceText();
//        String templateContent = oneClickRecallDto.getTemplateContent();
        String deviceListStr = swmOneClickRecallDto.getDeviceList();
        List<Map<String, Object>> deviceList;
        try {
            deviceList = objectMapper.readValue(deviceListStr, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("RecallMessageHandler handleMessage deviceList parse error", e);
            return;
        }

        Integer recallFrequency = swmOneClickRecallDto.getRecallFrequency();
        if(Objects.nonNull(recallFrequency) && recallFrequency == 0){
            // 推送一次
            if(swmOneClickRecallDto.getEvacuationPlan().equals(OneKeyRecall.EvacuationPlanEnum.ALL)){
                // 全体撤离  科利特
                sendVoiceAlarmToAllTcpDevices(swmOneClickRecallDto.getId(), voiceText.trim(), deviceList);
                //全体撤离 中泰
                sendVoiceAlarmToAllTcpDevicesByZT(swmOneClickRecallDto.getId(), voiceText.trim(), deviceList);
            }else{
                // 非全体撤离 科利特
                sendVoiceAlarmToDevices(swmOneClickRecallDto.getId(), deviceList, voiceText.trim());
                // 非全体撤离 中泰
                sendVoiceAlarmToDevicesByZT(swmOneClickRecallDto.getId(), deviceList, voiceText.trim());
            }
        }else {
            Integer recallCount = swmOneClickRecallDto.getRecallCount();
            log.info("开始调度: 每{}秒调用一次，总共调用{}次", recallFrequency, recallCount);
            MethodScheduler scheduler = new MethodScheduler(recallFrequency, recallCount);
            scheduler.scheduleMethod(() -> {
                // 这里是要执行的方法
                if(swmOneClickRecallDto.getEvacuationPlan().equals(OneKeyRecall.EvacuationPlanEnum.ALL)){
                    // 全体撤离
                    sendVoiceAlarmToAllTcpDevices(swmOneClickRecallDto.getId(), voiceText.trim(), deviceList);
                    //全体撤离 中泰
                    sendVoiceAlarmToAllTcpDevicesByZT(swmOneClickRecallDto.getId(), voiceText.trim(), deviceList);
                }else{
                    // 非全体撤离
                    sendVoiceAlarmToDevices(swmOneClickRecallDto.getId(), deviceList, voiceText.trim());
                    // 非全体撤离 中泰
                    sendVoiceAlarmToDevicesByZT(swmOneClickRecallDto.getId(), deviceList, voiceText.trim());
                }
            });
        }
    }

    /**
     * 向所有TCP连接的设备发送语音播报（科利特设备）
     *
     * @param voiceText 语音文字内容
     * @author Shawn
     * @date 2025-07-30
     */
    private void sendVoiceAlarmToAllTcpDevices(String recallId, String voiceText, List<Map<String, Object>> deviceList) {
        // 提取所有目标人员姓名
        List<String> allTargetPersonNames = new ArrayList<>();
        Map<String, String> deviceToPersonMap = new HashMap<>();
        for (Map<String, Object> personnel : deviceList) {
            String deviceId = (String) personnel.get("deviceId");
            String personName = (String) personnel.get("personName");
            if (personName != null && !personName.trim().isEmpty()) {
                allTargetPersonNames.add(personName);
                if (deviceId != null) {
                    deviceToPersonMap.put(deviceId, personName);
                }
            }
        }
        log.info("查询到{}名应该接收推送的目标人员", allTargetPersonNames.size());

        // 2. 获取SessionManager和活跃连接
        SessionManager sessionManager = nettyTcpServer.getSessionManager();
        if (sessionManager == null) {
            log.error("无法获取SessionManager，TCP语音播报失败");
            return;
        }

        List<DeviceSession> activeSessions = sessionManager.getActiveSessions();
        log.info("开始向{}个TCP连接设备发送语音播报，目标人员{}名，内容：{}", activeSessions.size(), allTargetPersonNames.size(), voiceText);
        // 3. 向在线设备发送语音播报
        int successCount = 0;
        int failCount = 0;
        List<String> successPersons = new ArrayList<>();

        for (DeviceSession session : activeSessions) {
            String deviceId = session.getDeviceId();
            if (deviceId == null || deviceId.trim().isEmpty()) {
                log.warn("跳过设备ID为空的会话：{}", session.getSessionId());
                continue;
            }

            String personName = deviceToPersonMap.get(deviceId);
            if (personName == null) {
                // 设备在线但不在目标人员列表中，跳过
                log.debug("设备{}不在目标人员列表中，跳过", deviceId);
                continue;
            }

            try {
                boolean result = voiceAlarmService.sendVoiceAlarm(deviceId, voiceText);
                if (result) {
                    successCount++;
                    successPersons.add(personName);
                    log.debug("成功发送语音播报到设备：{}({})", deviceId, personName);
                } else {
                    failCount++;
                    log.warn("发送语音播报失败，设备：{}({})", deviceId, personName);
                }
            } catch (Exception e) {
                failCount++;
                log.error("发送语音播报异常，设备：{}({})", deviceId, personName, e);
            }
        }

        // 4. 计算失败人员（所有目标人员 - 成功人员）
        List<String> failedPersons = new ArrayList<>();
        for (String targetPersonName : allTargetPersonNames) {
            if (!successPersons.contains(targetPersonName)) {
                failedPersons.add(targetPersonName);
            }
        }
        log.info("TCP语音播报完成，目标人员：{}，成功：{}，失败：{}（含设备离线）", allTargetPersonNames.size(), successCount, failedPersons.size());

        // 更新召回结果
        oneKeyRecallService.updateResult(recallId, OneKeyRecall.RecallResultEnum.SUCCESS, allTargetPersonNames.size(), successCount, failedPersons.size());

    }

    /**
     * 向所有TCP连接的设备发送语音播报（中泰设备）
     *
     * @param voiceText 语音文字内容
     * @author Shawn
     * @date 2025-07-30
     */
    private void sendVoiceAlarmToAllTcpDevicesByZT(String recallId, String voiceText, List<Map<String, Object>> deviceList) {
        if (soSAlarmHandler == null) {
            log.debug("MQTT未启用，跳过中泰设备语音播报");
            return;
        }

        // 提取所有目标人员姓名
        List<String> allTargetPersonNames = new ArrayList<>();
        Map<String, String> deviceToPersonMap = new HashMap<>();
        for (Map<String, Object> personnel : deviceList) {
            String deviceId = (String) personnel.get("deviceId");
            String personName = (String) personnel.get("personName");
            if (personName != null && !personName.trim().isEmpty()) {
                allTargetPersonNames.add(personName);
                if (deviceId != null) {
                    deviceToPersonMap.put(deviceId, personName);
                }
            }
        }
        log.info("查询到{}名应该接收推送的目标人员", allTargetPersonNames.size());

        // 2. 获取SessionManager和活跃连接
        SessionManager sessionManager = nettyTcpServer.getSessionManager();
        if (sessionManager == null) {
            log.error("无法获取SessionManager，TCP语音播报失败");
            return;
        }

        List<DeviceSession> activeSessions = sessionManager.getActiveSessions();
        log.info("开始向{}个TCP连接设备发送语音播报，目标人员{}名，内容：{}", activeSessions.size(), allTargetPersonNames.size(), voiceText);
        // 3. 向在线设备发送语音播报
        int successCount = 0;
        int failCount = 0;
        List<String> successPersons = new ArrayList<>();


        for (Map.Entry<String, String> entry : deviceToPersonMap.entrySet()) {
            String deviceId = entry.getKey();
            String personName = entry.getValue();

            try {
                JSONObject jsonObject = soSAlarmHandler.sendVoiceCommand(deviceId, voiceText);
                Boolean result = jsonObject.getBool("success");
                if (result) {
                    successCount++;
                    successPersons.add(personName);
                    log.debug("成功发送语音播报到设备：{}({})", deviceId, personName);
                } else {
                    failCount++;
                    log.warn("发送语音播报失败，设备：{}({})", deviceId, personName);
                }
            } catch (Exception e) {
                failCount++;
                log.error("发送语音播报异常，设备：{}({})", deviceId, personName, e);
            }
        }

        // 4. 计算失败人员（所有目标人员 - 成功人员）
        List<String> failedPersons = new ArrayList<>();
        for (String targetPersonName : allTargetPersonNames) {
            if (!successPersons.contains(targetPersonName)) {
                failedPersons.add(targetPersonName);
            }
        }
        log.info("TCP语音播报完成，目标人员：{}，成功：{}，失败：{}（含设备离线）", allTargetPersonNames.size(), successCount, failedPersons.size());

        // 更新召回结果
        oneKeyRecallService.updateResult(recallId, OneKeyRecall.RecallResultEnum.SUCCESS, allTargetPersonNames.size(), successCount, failedPersons.size());

    }

    /**
     * 向指定设备列表发送TCP语音播报（科利特设备）
     *
     * @param deviceList 设备列表
     * @param voiceText  语音文字内容
     * @author Shawn
     * @date 2025-07-30
     */
    private void sendVoiceAlarmToDevices(String recallId, List<Map<String, Object>> deviceList, String voiceText) {
        log.info("开始向{}个设备发送TCP语音播报，内容：{}", deviceList.size(), voiceText);
        int successCount = 0;
        int failCount = 0;
        List<String> failedDevices = new ArrayList<>();
        List<String> successPersons = new ArrayList<>();
        List<String> failedPersons = new ArrayList<>();
        // 逐个发送语音播报
        for (Map<String, Object> device : deviceList) {
            String deviceId = (String) device.get("deviceId");
            String personName = (String) device.get("personName");

            if (deviceId == null || deviceId.trim().isEmpty()) {
                log.warn("跳过设备ID为空的记录");
                failCount++;
                if (personName != null && !personName.trim().isEmpty()) {
                    failedPersons.add(personName);
                }
                continue;
            }

            try {
                boolean result = voiceAlarmService.sendVoiceAlarm(deviceId, voiceText);
                if (result) {
                    successCount++;
                    if (personName != null && !personName.trim().isEmpty()) {
                        successPersons.add(personName);
                    }
                    log.debug("TCP语音播报成功，设备：{}({})", deviceId, personName);
                } else {
                    failCount++;
                    failedDevices.add(deviceId + "(" + personName + ")");
                    if (personName != null && !personName.trim().isEmpty()) {
                        failedPersons.add(personName);
                    }
                    log.warn("TCP语音播报失败，设备：{}({})", deviceId, personName);
                }
            } catch (Exception e) {
                failCount++;
                failedDevices.add(deviceId + "(" + personName + ")");
                if (personName != null && !personName.trim().isEmpty()) {
                    failedPersons.add(personName);
                }
                log.error("TCP语音播报异常，设备：{}({})", deviceId, personName, e);
            }
        }
        log.info("TCP语音播报完成，总设备数：{}，成功：{}，失败：{}", deviceList.size(), successCount, failCount);

        // 更新召回结果
        oneKeyRecallService.updateResult(recallId, OneKeyRecall.RecallResultEnum.SUCCESS, deviceList.size(), successCount, failCount);
    }



    /**
     * 向指定设备列表发送TCP语音播报(中泰设备)
     *
     * @param deviceList 设备列表
     * @param voiceText  语音文字内容
     * @author Shawn
     * @date 2025-07-30
     */
    private void sendVoiceAlarmToDevicesByZT(String recallId, List<Map<String, Object>> deviceList, String voiceText) {
        if (soSAlarmHandler == null) {
            log.debug("MQTT未启用，跳过中泰设备语音播报");
            return;
        }

        log.info("开始向{}个设备发送TCP语音播报，内容：{}", deviceList.size(), voiceText);
        int successCount = 0;
        int failCount = 0;
        List<String> failedDevices = new ArrayList<>();
        List<String> successPersons = new ArrayList<>();
        List<String> failedPersons = new ArrayList<>();
        // 逐个发送语音播报
        for (Map<String, Object> device : deviceList) {
            String deviceId = (String) device.get("deviceId");
            String personName = (String) device.get("personName");

            if (deviceId == null || deviceId.trim().isEmpty()) {
                log.warn("跳过设备ID为空的记录");
                failCount++;
                if (personName != null && !personName.trim().isEmpty()) {
                    failedPersons.add(personName);
                }
                continue;
            }

            try {
                JSONObject jsonObject = soSAlarmHandler.sendVoiceCommand(deviceId, voiceText);
                Boolean result = jsonObject.getBool("success");
                if (result) {
                    successCount++;
                    if (personName != null && !personName.trim().isEmpty()) {
                        successPersons.add(personName);
                    }
                    log.debug("TCP语音播报成功，设备：{}({})", deviceId, personName);
                } else {
                    failCount++;
                    failedDevices.add(deviceId + "(" + personName + ")");
                    if (personName != null && !personName.trim().isEmpty()) {
                        failedPersons.add(personName);
                    }
                    log.warn("TCP语音播报失败，设备：{}({})", deviceId, personName);
                }
            } catch (Exception e) {
                failCount++;
                failedDevices.add(deviceId + "(" + personName + ")");
                if (personName != null && !personName.trim().isEmpty()) {
                    failedPersons.add(personName);
                }
                log.error("TCP语音播报异常，设备：{}({})", deviceId, personName, e);
            }
        }
        log.info("TCP语音播报完成，总设备数：{}，成功：{}，失败：{}", deviceList.size(), successCount, failCount);

        // 更新召回结果
        oneKeyRecallService.updateResult(recallId, OneKeyRecall.RecallResultEnum.SUCCESS, deviceList.size(), successCount, failCount);
    }

}
