package com.jeesite.modules.job.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.modules.swm.entity.SwmDify;
import com.jeesite.modules.swm.service.SwmDifyService;
import com.jeesite.modules.service.AiServiceImpl;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * AI相关任务
 */
@Slf4j
@Component
public class AiTask {

    private static final String workflowUrl = "http://10.50.103.189:7780/v1/workflows/run";
    private static final String AUTHORIZATION = "Bearer app-fZLfOEAP99eXlVX3AuzoK2bn";
    private static final String CONTENT_TYPE = "application/json";

    private static final String workflowId = "0a0b5524-0e96-474b-8b6a-c3ebe7277058";
    private static final String responseMode = "blocking";

    @Autowired
    private SwmDifyService swmDifyService;

    @Autowired
    private AiServiceImpl aiServiceImpl;

    // ==================== 日报-广东厂 ====================
    // cron: 0 15 1 * * ?  每天凌晨1:15
    @XxlJob("aiDailyReportTaskBYZJGGGD")
    @Transactional(rollbackFor = Exception.class)
    public void aiDailyReportTaskBYZJGGGD() {
        XxlJobHelper.log("=====================定时生成工效任务=================");

        //city 城市

        Date yesterday = DateUtil.yesterday();
        String jobParam = XxlJobHelper.getJobParam();
        String startDate = "";
        String endDate = "";
        XxlJobHelper.log("参数：{}", jobParam);

        if (jobParam != null && jobParam.contains(",")) {
            String[] split = jobParam.split(",");
            startDate = split[0];
            endDate = split[1];
        }

        if (startDate.isEmpty()) {
            startDate = DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd");
        }
        if (endDate.isEmpty()) {
            endDate = DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd");
        }

        XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

        SwmDify swmDify = swmDifyService.getEntity(DateUtil.beginOfDay(yesterday), DateUtil.endOfDay(yesterday));

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("date", DateUtil.format(yesterday, "yyyy-MM-dd"));
        inputs.put("projectName", "广东厂慧眼安盾项目");
        inputs.put("corpCode", "ZJGGGD");

        if (swmDify != null) {
            inputs.put("safetyIndex", swmDify.getSafetyIndex());
            inputs.put("workerIndex", swmDify.getWorkerIndex());
        }

        Map<String, Object> requestBody = buildRequestBody(inputs);

        XxlJobHelper.log("请求参数：{}", requestBody);

        // 请求并重试三次
        JSONObject outputs = retryPostForValidResult(requestBody);

        // 保存数据
        saveAiDifyDataWithPart45(outputs, yesterday, inputs, "ZJGGGD", "中建钢构广东有限公司", startDate, endDate, "daily");

    }

    /**
     * 构建请求体参数
     */
    private Map<String, Object> buildRequestBody(Map<String, Object> inputs) {
        Map<String, Object> body = new HashMap<>();
        body.put("workflow_id", workflowId);
        body.put("response_mode", responseMode);
        body.put("user", workflowId);
        body.put("files", new HashMap[] {});
        body.put("inputs", inputs);
        return body;
    }

    /**
     * 发起请求
     */
    private String sendPostRequest(Map<String, Object> requestBody) {
        try (HttpResponse response = HttpRequest.post(workflowUrl)
                .header("Authorization", AUTHORIZATION)
                .header("Content-Type", CONTENT_TYPE)
                .body(String.valueOf(new JSONObject(requestBody)))
                .execute()) {

            if (response.isOk()) {
                XxlJobHelper.log("接口请求成功，响应结果：{}", response.body());
                return response.body();
            } else {
                XxlJobHelper.log("接口请求失败，状态码={}", response.getStatus());
                return null;
            }
        } catch (Exception e) {
            XxlJobHelper.log("接口请求异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 带 3 次重试的接口调用
     */
    private JSONObject retryPostForValidResult(Map<String, Object> requestBody) {
        int maxRetry = 3;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {

            XxlJobHelper.log("AI日报接口请求，第 {} 次尝试", attempt);

            try {
                String body = sendPostRequest(requestBody);
                if (body == null) {
                    continue;
                }

                JSONObject json = JSONObject.parseObject(body);
                JSONObject data = json.getJSONObject("data");
                if (data == null) continue;

                JSONObject outputs = data.getJSONObject("outputs");
                if (outputs == null) continue;

                if (outputs.getLong("safetyIndex") != null) {
                    return outputs;
                }

            } catch (Exception e) {
                XxlJobHelper.log("AI日报接口异常：{}", e.getMessage());
            }

            // 避免短时间内疯狂重试
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        }

        XxlJobHelper.log("AI日报接口重试三次均失败！");
        return null;
    }

    /**
     * 保存数据
     */
    private void saveAiDifyData(JSONObject outputs, Date yesterday, Map<String, Object> inputs,String corpCode,String projectName) {
        if (outputs == null) {
            XxlJobHelper.log("AI日报数据为空，未保存！");
            return;
        }

        SwmDify dify = new SwmDify();
        dify.setDate(yesterday);
        dify.setProjectName((String) inputs.get("projectName"));
        dify.setSafetyIndex(outputs.getLong("safetyIndex"));
        dify.setWorkerIndex(outputs.getLong("workerIndex"));
        dify.setText(outputs.getString("text"));
        CorpUtils.setCurrentCorpCode(corpCode, projectName);

        swmDifyService.save(dify);

        XxlJobHelper.log("AI日报保存成功: {}", outputs.toJSONString());
    }




    // ==================== 日报-江苏厂 ====================
    // cron: 0 15 1 * * ?  每天凌晨1:15
    @XxlJob("aiDailyReportTaskBYZJGGJS")
    @Transactional(rollbackFor = Exception.class)
    public void aiDailyReportTaskBYZJGGJS() {
        XxlJobHelper.log("=====================定时生成工效任务=================");

        //city 城市

        Date yesterday = DateUtil.yesterday();
        String jobParam = XxlJobHelper.getJobParam();
        String startDate = "";
        String endDate = "";
        XxlJobHelper.log("参数：{}", jobParam);

        if (jobParam != null && jobParam.contains(",")) {
            String[] split = jobParam.split(",");
            startDate = split[0];
            endDate = split[1];
        }

        if (startDate.isEmpty()) {
            startDate = DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd");
        }
        if (endDate.isEmpty()) {
            endDate = DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd");
        }

        XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

        SwmDify swmDify = swmDifyService.getEntity(DateUtil.beginOfDay(yesterday), DateUtil.endOfDay(yesterday));

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("date", DateUtil.format(yesterday, "yyyy-MM-dd"));
        inputs.put("projectName", "江苏厂慧眼安盾项目");
        inputs.put("corpCode", "ZJGGJS");

        if (swmDify != null) {
            inputs.put("safetyIndex", swmDify.getSafetyIndex());
            inputs.put("workerIndex", swmDify.getWorkerIndex());
        }

        Map<String, Object> requestBody = buildRequestBody(inputs);

        XxlJobHelper.log("请求参数：{}", requestBody);

        // 请求并重试三次
        JSONObject outputs = retryPostForValidResult(requestBody);

        // 保存数据（拼接四五部分）
        saveAiDifyDataWithPart45(outputs, yesterday, inputs, "ZJGGJS", "中建钢构江苏有限公司", startDate, endDate, "daily");

    }

    // ==================== 周报-江苏厂 ====================
    // cron: 0 15 1 ? * 1  每周一凌晨1:15
    @XxlJob("aiWeeklyReportTaskJS")
    @Transactional(rollbackFor = Exception.class)
    public void aiWeeklyReportTaskJS() {
        XxlJobHelper.log("=====================周报任务-江苏厂=================");

        // 上周一 ~ 上周日
        Date today = DateUtil.date();
        Date lastMonday = DateUtil.beginOfWeek(DateUtil.offsetWeek(today, -1));
        Date lastSunday = DateUtil.endOfWeek(DateUtil.offsetWeek(today, -1));
        String startDate = DateUtil.format(lastMonday, "yyyy-MM-dd");
        String endDate = DateUtil.format(lastSunday, "yyyy-MM-dd");

        XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("date", DateUtil.format(lastMonday, "yyyy-MM-dd") + "~" + DateUtil.format(lastSunday, "yyyy-MM-dd"));
        inputs.put("projectName", "江苏厂慧眼安盾项目");
        inputs.put("corpCode", "ZJGGJS");

        Map<String, Object> requestBody = buildRequestBody(inputs);
        XxlJobHelper.log("请求参数：{}", requestBody);

        JSONObject outputs = retryPostForValidResult(requestBody);
        saveAiDifyDataWithPart45(outputs, lastMonday, inputs, "ZJGGJS", "中建钢构江苏有限公司", startDate, endDate, "weekly");
    }

    // ==================== 月报-江苏厂 ====================
    // cron: 0 15 1 1 * ?  每月1号凌晨1:15
    @XxlJob("aiMonthlyReportTaskJS")
    @Transactional(rollbackFor = Exception.class)
    public void aiMonthlyReportTaskJS() {
        XxlJobHelper.log("=====================月报任务-江苏厂=================");

        // 上月
        Date today = DateUtil.date();
        Date lastMonthFirst = DateUtil.beginOfMonth(DateUtil.offsetMonth(today, -1));
        Date lastMonthLast = DateUtil.endOfMonth(DateUtil.offsetMonth(today, -1));
        String startDate = DateUtil.format(lastMonthFirst, "yyyy-MM-dd");
        String endDate = DateUtil.format(lastMonthLast, "yyyy-MM-dd");

        XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("date", DateUtil.format(lastMonthFirst, "yyyy-MM-dd") + "~" + DateUtil.format(lastMonthLast, "yyyy-MM-dd"));
        inputs.put("projectName", "江苏厂慧眼安盾项目");
        inputs.put("corpCode", "ZJGGJS");

        Map<String, Object> requestBody = buildRequestBody(inputs);
        XxlJobHelper.log("请求参数：{}", requestBody);

        JSONObject outputs = retryPostForValidResult(requestBody);
        saveAiDifyDataWithPart45(outputs, lastMonthFirst, inputs, "ZJGGJS", "中建钢构江苏有限公司", startDate, endDate, "monthly");
    }

    /**
     * 保存 AI 日报数据，拼接 Java 生成的第四、五部分
     */
    private void saveAiDifyDataWithPart45(JSONObject outputs, Date yesterday, Map<String, Object> inputs,
                                          String corpCode, String projectName, String startDate, String endDate, String reportType) {
        if (outputs == null) {
            XxlJobHelper.log("AI日报数据为空，未保存！");
            return;
        }

        // 拼接四五部分
        String part45 = aiServiceImpl.generatePart45Markdown(startDate, endDate, reportType);
        String fullText = (outputs.getString("text") != null ? outputs.getString("text") : "") + "\n\n" + part45;

        SwmDify dify = new SwmDify();
        dify.setDate(yesterday);
        dify.setProjectName((String) inputs.get("projectName"));
        dify.setSafetyIndex(outputs.getLong("safetyIndex"));
        dify.setWorkerIndex(outputs.getLong("workerIndex"));
        dify.setText(fullText);
        CorpUtils.setCurrentCorpCode(corpCode, projectName);

        swmDifyService.save(dify);

        XxlJobHelper.log("AI日报（含四五部分）保存成功，reportType={}", reportType);
    }

}

