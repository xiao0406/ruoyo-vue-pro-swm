package cn.iocoder.yudao.module.iot.job.task;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j

public class WeatherAlertTask {

    @Resource
    private ConfigApi configApi;

    @Value("${sms.platform.url:https://app.cscecsteel.com/uis/a/message/messageSendLog/send}")
    private String smsPlatformUrl;

    @Value("${sms.platform.code:MYSQ_2025_1010_0001}")
    private String smsPlatformCode;

    @Value("${sms.platform.source:工业互联网平台}")
    private String smsPlatformSource;

    @Value("${sms.platform.appid:8fdc76caac10e70bb56a}")
    private String smsPlatformAppid;

    @Value("${sms.platform.token:638f86d8fe485fd284a69a59f90a6c821ed4c13c}")
    private String smsPlatformToken;

    @Value("${weather.amap.api.url:https://restapi.amap.com/v3/weather/weatherInfo}")
    private String amapApiUrl;

    @Value("${weather.amap.api.key:4ae4ba1216bf8e8c2c745c960a8f2750}")
    private String amapApiKey;

    @Value("${weather.amap.city.code:440305}")
    private String cityCode;

    @Value("${weather.extreme.types:强风/劲风,疾风,大风,烈风,风暴,狂爆风,飓风,热带风暴,大雨-暴雨,暴雨,大暴雨,特大暴雨,强阵雨,强雷阵雨,极端降雨,雷阵雨,龙卷风,强沙尘暴,重度霾,严重霾,强浓雾,大雪,特强浓雾}")
    private String extremeWeatherTypes;

    @XxlJob("checkExtremeWeather")
    public void checkExtremeWeather() {
        Date startTime = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        XxlJobHelper.log("========== 开始执行天气预警定时任务 ==========");
        XxlJobHelper.log("任务开始时间: {}", dateTimeFormat.format(startTime));

        try {
            String smsPhones = getSmsPhones();
            if (smsPhones == null || smsPhones.trim().isEmpty()) {
                XxlJobHelper.log("未配置短信接收手机号，跳过发送");
                return;
            }

            String weatherData = fetchWeatherData();
            if (weatherData == null) {
                XxlJobHelper.log("获取天气数据失败，跳过发送");
                return;
            }

            List<SevereWeatherInfo> severeWeatherList = parseSevereWeather(weatherData);
            if (severeWeatherList.isEmpty()) {
                XxlJobHelper.log("未发现极端天气，无需发送预警");
                return;
            }

            String message = buildAlertMessage(severeWeatherList);
            XxlJobHelper.log("预警内容: {}", message);
            sendSmsToAll(message, smsPhones);
            XxlJobHelper.log("========== 极端天气预警任务执行完毕 ==========");
        } catch (Exception e) {
            log.error("天气预警任务执行异常", e);
            XxlJobHelper.log("天气预警任务执行异常: {}", e.getMessage());
        }
    }

    private String getSmsPhones() {
        return configApi.getConfigValueByKey("box.algorithm.sms.push");
    }

    private String fetchWeatherData() {
        try {
            String url = amapApiUrl + "?city=" + cityCode
                    + "&extensions=all"
                    + "&output=JSON"
                    + "&key=" + amapApiKey;

            String response = HttpRequest.get(url).timeout(30000).execute().body();
            XxlJobHelper.log("天气 API 响应: {}", response);

            JSONObject result = JSONUtil.parseObj(response);
            if (!"1".equals(result.getStr("status")) || !"10000".equals(result.getStr("infocode"))) {
                log.error("天气 API 调用失败: status={}, infocode={}", result.getStr("status"), result.getStr("infocode"));
                return null;
            }

            JSONArray forecasts = result.getJSONArray("forecasts");
            if (forecasts == null || forecasts.isEmpty()) {
                log.error("天气 API 返回数据为空");
                return null;
            }
            return response;
        } catch (Exception e) {
            log.error("调用天气 API 异常", e);
            return null;
        }
    }

    private List<SevereWeatherInfo> parseSevereWeather(String weatherData) {
        List<SevereWeatherInfo> severeWeatherList = new ArrayList<>();
        try {
            JSONArray forecasts = JSONUtil.parseObj(weatherData).getJSONArray("forecasts");
            for (int i = 0; i < forecasts.size(); i++) {
                JSONArray casts = forecasts.getJSONObject(i).getJSONArray("casts");
                for (int j = 0; j < casts.size(); j++) {
                    JSONObject cast = casts.getJSONObject(j);
                    String dayWeather = cast.getStr("dayweather");
                    String nightWeather = cast.getStr("nightweather");
                    String dayPower = cast.getStr("daypower");
                    String nightPower = cast.getStr("nightpower");
                    if (isExtremeWeather(dayWeather, dayPower) || isExtremeWeather(nightWeather, nightPower)) {
                        SevereWeatherInfo info = new SevereWeatherInfo();
                        info.setDate(cast.getStr("date"));
                        info.setDayWeather(dayWeather);
                        info.setNightWeather(nightWeather);
                        info.setDayPower(dayPower);
                        info.setNightPower(nightPower);
                        severeWeatherList.add(info);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析天气数据异常", e);
        }
        return severeWeatherList;
    }

    private String buildAlertMessage(List<SevereWeatherInfo> severeWeatherList) {
        String forecastTime = new SimpleDateFormat("yyyy年MM月dd日HH时mm分").format(new Date());
        String startDate = severeWeatherList.get(0).getDate();
        String endDate = severeWeatherList.get(severeWeatherList.size() - 1).getDate();

        StringBuilder message = new StringBuilder();
        message.append("【工地安全预报预警】\n");
        message.append("预报时间：").append(forecastTime).append("\n");
        message.append("未来").append(severeWeatherList.size()).append("天（")
                .append(formatDate(startDate)).append("至").append(formatDate(endDate))
                .append("）天气提示：\n");

        for (SevereWeatherInfo info : severeWeatherList) {
            String formattedDate = formatDate(info.getDate());
            if (isExtremeWeather(info.getDayWeather(), info.getDayPower())) {
                message.append("关键天气：").append(formattedDate).append(" 白天：")
                        .append(info.getDayWeather()).append("，风力")
                        .append(getMaxWindLevel(info.getDayPower())).append("级\n");
            }
            if (isExtremeWeather(info.getNightWeather(), info.getNightPower())) {
                message.append("关键天气：").append(formattedDate).append(" 夜间：")
                        .append(info.getNightWeather()).append("，风力")
                        .append(getMaxWindLevel(info.getNightPower())).append("级\n");
            }
        }

        message.append("风险提示：该天气易造成工地围挡倾斜倒伏、水马移位冲毁。\n");
        message.append("管控要求：请提前完成围挡加固、水马注满配重并串联固定，雨后及时排查地基及设施稳定性。");
        return message.toString();
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || !dateStr.contains("-")) {
            return dateStr;
        }
        String[] parts = dateStr.split("-");
        return Integer.parseInt(parts[0]) + "年" + Integer.parseInt(parts[1]) + "月" + Integer.parseInt(parts[2]) + "日";
    }

    private void sendSmsToAll(String message, String phones) {
        JSONArray messageList = new JSONArray();
        for (String phone : phones.split(",")) {
            String trimmedPhone = phone.trim();
            if (!trimmedPhone.isEmpty()) {
                JSONObject messageInfo = new JSONObject();
                messageInfo.set("content", message);
                messageInfo.set("phone", trimmedPhone);
                messageList.add(messageInfo);
            }
        }

        if (messageList.isEmpty()) {
            log.warn("短信手机号列表为空，不发送短信");
            return;
        }

        JSONObject smsRequest = new JSONObject();
        smsRequest.set("code", smsPlatformCode);
        smsRequest.set("msgSource", smsPlatformSource);
        smsRequest.set("messageSendLogInfoList", messageList);

        String fullUrl = smsPlatformUrl + "?appid=" + smsPlatformAppid
                + "&timestamp=" + System.currentTimeMillis()
                + "&token=" + smsPlatformToken;

        String response = HttpRequest.post(fullUrl).body(smsRequest.toString()).timeout(60000).execute().body();
        log.info("短信发送结果: {}", response);
        XxlJobHelper.log("短信发送结果: {}", response);
    }

    private int getMaxWindLevel(String power) {
        if (power == null || power.isEmpty()) {
            return 0;
        }
        try {
            if (power.contains("-")) {
                String[] parts = power.split("-");
                return Math.max(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            }
            return Integer.parseInt(power.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析风力等级: {}", power);
            return 0;
        }
    }

    private boolean isExtremeWeather(String weather, String power) {
        if (weather != null) {
            for (String type : extremeWeatherTypes.split(",")) {
                if (type.trim().equals(weather)) {
                    return true;
                }
            }
        }
        return power != null && getMaxWindLevel(power) >= 6;
    }

    @Data
    private static class SevereWeatherInfo {
        private String date;
        private String dayWeather;
        private String nightWeather;
        private String dayPower;
        private String nightPower;
    }
}
