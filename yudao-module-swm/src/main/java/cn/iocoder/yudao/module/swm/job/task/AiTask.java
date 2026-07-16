package cn.iocoder.yudao.module.swm.job.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDifyDO;
import cn.iocoder.yudao.module.swm.service.SwmDifyService;
import cn.iocoder.yudao.module.swm.service.AiServiceImpl;
import cn.iocoder.yudao.module.swm.config.MinioConfiguration;
import cn.iocoder.yudao.module.swm.api.enums.TenantDbEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import io.minio.MinioClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * AI相关任务
 */
@Slf4j
@Component
public class AiTask {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${dify.workflow-url:http://58.251.8.11:17780/v1/workflows/run}")
    private String workflowUrl;
    @Value("${dify.authorization:Bearer app-fZLfOEAP99eXlVX3AuzoK2bn}")
    private String AUTHORIZATION;
    @Value("${dify.workflow-id:0a0b5524-0e96-474b-8b6a-c3ebe7277058}")
    private String workflowId;

    private static final String CONTENT_TYPE = "application/json";
    private static final String responseMode = "blocking";

    @Resource
    private SwmDifyService swmDifyService;

    @Resource
    private AiServiceImpl aiServiceImpl;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioConfiguration minioConfig;

    // ==================== 日报-广东厂 ====================
    // cron: 0 15 1 * * ?  每天凌晨1:15
    @XxlJob("aiDailyReportTaskBYZJGGGD")
    @Transactional(rollbackFor = Exception.class)
    public void aiDailyReportTaskBYZJGGGD() {
        Long tenantId = 1L;
        TenantUtils.execute(tenantId, () -> {
        try {
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
                endDate = DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd");
            }

            XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

            SwmDifyDO swmDify = swmDifyService.getEntity(
                    DateUtil.beginOfDay(yesterday).toLocalDateTime(),
                    DateUtil.endOfDay(yesterday).toLocalDateTime());

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("date", DateUtil.format(yesterday, "yyyy-MM-dd"));
            inputs.put("projectName", "广东厂慧眼安盾项目");
            inputs.put("tenantId", "1");
            inputs.put("city", "惠州市");

            if (swmDify != null) {
                inputs.put("safetyIndex", swmDify.getSafetyIndex());
                inputs.put("workerIndex", swmDify.getWorkerIndex());
            }

            Map<String, Object> requestBody = buildRequestBody(inputs);

            XxlJobHelper.log("请求参数：{}", requestBody);

            // 请求并重试三次
            ObjectNode outputs = retryPostForValidResult(requestBody);

            // 保存数据（广东厂不含四五部分）
            saveAiDifyData(outputs, yesterday, inputs, 1L, "中建钢构广东有限公司");
        } catch (Exception e) {
            XxlJobHelper.log("执行失败：{}", e.getMessage());
            log.error("执行失败", e);
        }
        }); // TenantUtils handles cleanup
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
                .body(MAPPER.writeValueAsString(requestBody))
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
    private ObjectNode retryPostForValidResult(Map<String, Object> requestBody) {
        int maxRetry = 3;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {

            XxlJobHelper.log("AI日报接口请求，第 {} 次尝试", attempt);

            try {
                String body = sendPostRequest(requestBody);
                if (body == null) {
                    continue;
                }

                JsonNode json = MAPPER.readTree(body);
                JsonNode dataNode = json.get("data");
                if (dataNode == null || !dataNode.isObject()) continue;

                JsonNode outputsNode = dataNode.get("outputs");
                if (outputsNode == null || !outputsNode.isObject()) continue;

                ObjectNode outputs = (ObjectNode) outputsNode;
                if (outputs.path("safetyIndex").asLong(0) != 0) {
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
    private void saveAiDifyData(ObjectNode outputs, Date yesterday, Map<String, Object> inputs, Long tenantId, String projectName) {
        if (outputs == null) {
            XxlJobHelper.log("AI日报数据为空，未保存！");
            return;
        }

        SwmDifyDO dify = new SwmDifyDO();
        dify.setReportDate(DateUtil.format(yesterday, "yyyy-MM-dd"));
        dify.setDifyName((String) inputs.get("projectName"));
        dify.setSafetyIndex(outputs.path("safetyIndex").isNull() ? null : BigDecimal.valueOf(outputs.path("safetyIndex").asLong()));
        dify.setWorkerIndex(outputs.path("workerIndex").isNull() ? null : BigDecimal.valueOf(outputs.path("workerIndex").asLong()));
        dify.setReportContent(outputs.path("text").asText(null));

        uploadPdfAndSetRemarks(dify, dify.getReportContent(), tenantId, yesterday, "daily");

        // tenant already set by TenantUtils.execute
        swmDifyService.save(dify);
        // cleanup handled by TenantUtils.execute

        XxlJobHelper.log("AI日报保存成功: {}", outputs.toString());
    }

    private void uploadPdfAndSetRemarks(SwmDifyDO dify, String markdown, Long tenantId, Date reportDate, String reportType) {
        try {
            byte[] pdfBytes = aiServiceImpl.convertMdToPdfBytes(markdown);
            if (pdfBytes == null || pdfBytes.length == 0) {
                XxlJobHelper.log("PDF生成结果为空，跳过上传，tenantId={}，reportType={}", tenantId, reportType);
                return;
            }
            String dateStr = DateUtil.format(reportDate, "yyyy-MM-dd");
            String fileName = dateStr + "_" + sanitizePdfFileName(dify.getDifyName()) + ".pdf";
            String objectName = "ai-report/" + tenantId + "/" + fileName;
            try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(pdfBytes)) {
                minioClient.putObject(io.minio.PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(objectName)
                        .stream(bais, pdfBytes.length, -1)
                        .contentType("application/pdf")
                        .build());
            }
            dify.setRemarks(objectName);
            XxlJobHelper.log("PDF上传MinIO成功，objectName={}", objectName);
        } catch (Exception e) {
            XxlJobHelper.log("PDF生成或上传失败，tenantId={}，reportType={}，error={}", tenantId, reportType, e.getMessage());
            log.error("PDF生成或上传失败，tenantId={}, reportType={}", tenantId, reportType, e);
        }
    }

    private String sanitizePdfFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "AI报告";
        }
        return fileName.trim().replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_");
    }


    // ==================== 日报-江苏厂 ====================
    // cron: 0 15 1 * * ?  每天凌晨1:15
    @XxlJob("aiDailyReportTaskBYZJGGJS")
    @Transactional(rollbackFor = Exception.class)
    public void aiDailyReportTaskBYZJGGJS() {
        Long tenantId = 2L;
        TenantUtils.execute(tenantId, () -> {
        try {
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
                endDate = DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd");
            }

            XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

            SwmDifyDO swmDify = swmDifyService.getEntity(
                    DateUtil.beginOfDay(yesterday).toLocalDateTime(),
                    DateUtil.endOfDay(yesterday).toLocalDateTime());

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("date", DateUtil.format(yesterday, "yyyy-MM-dd"));
            inputs.put("projectName", "江苏厂慧眼安盾项目");
            inputs.put("tenantId", "2");
            inputs.put("city", "靖江市");

            if (swmDify != null) {
                inputs.put("safetyIndex", swmDify.getSafetyIndex());
                inputs.put("workerIndex", swmDify.getWorkerIndex());
            }

            Map<String, Object> requestBody = buildRequestBody(inputs);

            XxlJobHelper.log("请求参数：{}", requestBody);

            // 请求并重试三次
            ObjectNode outputs = retryPostForValidResult(requestBody);

            // 保存数据（拼接四五部分）
            saveAiDifyDataWithPart45(outputs, yesterday, inputs, 2L, "中建钢构江苏有限公司", startDate, endDate, "daily");
        } catch (Exception e) {
            XxlJobHelper.log("执行失败：{}", e.getMessage());
            log.error("执行失败", e);
        }
        }); // TenantUtils handles cleanup
    }

    // ==================== 周报-江苏厂 ====================
    // cron: 0 15 1 ? * 1  每周一凌晨1:15
    @XxlJob("aiWeeklyReportTaskJS")
    @Transactional(rollbackFor = Exception.class)
    public void aiWeeklyReportTaskJS() {
        Long tenantId = 2L;
        TenantUtils.execute(tenantId, () -> {
        try {
            XxlJobHelper.log("=====================周报任务-江苏厂=================");

            // 上周一 ~ 上周日
            Date today = DateUtil.date();
            Date lastMonday = DateUtil.beginOfWeek(DateUtil.offsetWeek(today, -1));
            Date lastSunday = DateUtil.endOfWeek(DateUtil.offsetWeek(today, -1));
            String startDate = DateUtil.format(lastMonday, "yyyy-MM-dd");
            String endDate = DateUtil.format(lastSunday, "yyyy-MM-dd");

            XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("date", DateUtil.format(DateUtil.yesterday(), "yyyy-MM-dd"));
            inputs.put("projectName", "江苏厂慧眼安盾项目");
            inputs.put("tenantId", "2");
            inputs.put("city", "靖江市");

            Map<String, Object> requestBody = buildRequestBody(inputs);
            XxlJobHelper.log("请求参数：{}", requestBody);

            ObjectNode outputs = retryPostForValidResult(requestBody);
            saveAiDifyDataWithPart45(outputs, lastMonday, inputs, 2L, "中建钢构江苏有限公司", startDate, endDate, "weekly");
        } catch (Exception e) {
            XxlJobHelper.log("执行失败：{}", e.getMessage());
            log.error("执行失败", e);
        }
        }); // TenantUtils handles cleanup
    }

    // ==================== 月报-江苏厂 ====================
    // cron: 0 15 1 1 * ?  每月1号凌晨1:15
    @XxlJob("aiMonthlyReportTaskJS")
    @Transactional(rollbackFor = Exception.class)
    public void aiMonthlyReportTaskJS() {
        Long tenantId = 2L;
        TenantUtils.execute(tenantId, () -> {
        try {
            XxlJobHelper.log("=====================月报任务-江苏厂=================");

            // 上月
            Date today = DateUtil.date();
            Date lastMonthFirst = DateUtil.beginOfMonth(DateUtil.offsetMonth(today, -1));
            Date lastMonthLast = DateUtil.endOfMonth(DateUtil.offsetMonth(today, -1));
            String startDate = DateUtil.format(lastMonthFirst, "yyyy-MM-dd");
            String endDate = DateUtil.format(lastMonthLast, "yyyy-MM-dd");

            XxlJobHelper.log("开始时间：{}, 结束时间：{}", startDate, endDate);

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("date", DateUtil.format(DateUtil.yesterday(), "yyyy-MM-dd"));
            inputs.put("projectName", "江苏厂慧眼安盾项目");
            inputs.put("tenantId", "2");
            inputs.put("city", "靖江市");

            Map<String, Object> requestBody = buildRequestBody(inputs);
            XxlJobHelper.log("请求参数：{}", requestBody);

            ObjectNode outputs = retryPostForValidResult(requestBody);
            saveAiDifyDataWithPart45(outputs, lastMonthFirst, inputs, 2L, "中建钢构江苏有限公司", startDate, endDate, "monthly");
        } catch (Exception e) {
            XxlJobHelper.log("执行失败：{}", e.getMessage());
            log.error("执行失败", e);
        }
        }); // TenantUtils handles cleanup
    }

    /**
     * 保存 AI 日报数据，拼接 Java 生成的第四、五部分，并生成 PDF 上传 MinIO
     */
    private void saveAiDifyDataWithPart45(ObjectNode outputs, Date yesterday, Map<String, Object> inputs,
                                          Long tenantId, String projectName, String startDate, String endDate, String reportType) {
        if (outputs == null) {
            XxlJobHelper.log("AI日报数据为空，未保存！");
            return;
        }

        // 拼接四五部分
        String part45 = aiServiceImpl.generatePart45Markdown(startDate, endDate, reportType);
        String difyText = outputs.path("text").asText("");
        String fullText = difyText + "\n\n" + part45;

        // 根据报告类型加后缀
        String reportSuffix = "daily".equals(reportType) ? "_日报" : "weekly".equals(reportType) ? "_周报" : "_月报";
        String finalProjectName = (String) inputs.get("projectName") + reportSuffix;

        SwmDifyDO dify = new SwmDifyDO();
        dify.setReportDate(DateUtil.format(yesterday, "yyyy-MM-dd"));
        dify.setDifyName(finalProjectName);
        dify.setSafetyIndex(outputs.path("safetyIndex").isNull() ? null : BigDecimal.valueOf(outputs.path("safetyIndex").asLong()));
        dify.setWorkerIndex(outputs.path("workerIndex").isNull() ? null : BigDecimal.valueOf(outputs.path("workerIndex").asLong()));
        dify.setReportContent(fullText);

        // 生成 PDF 并上传 MinIO
        try {
            byte[] pdfBytes = aiServiceImpl.convertMdToPdfBytes(fullText);
            if (pdfBytes != null && pdfBytes.length > 0) {
                String dateStr = DateUtil.format(yesterday, "yyyy-MM-dd");
                String fileName = dateStr + "_" + sanitizePdfFileName(dify.getDifyName()) + ".pdf";
                String objectName = "ai-report/" + tenantId + "/" + fileName;
                // 直接用 MinioClient 上传
                try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(pdfBytes)) {
                    minioClient.putObject(io.minio.PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(bais, pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build());
                }
                dify.setRemarks(objectName);
                XxlJobHelper.log("PDF上传MinIO成功，objectName={}", objectName);
            } else {
                XxlJobHelper.log("PDF生成结果为空，跳过上传");
            }
        } catch (Exception e) {
            XxlJobHelper.log("PDF生成或上传失败：{}", e.getMessage());
        }

        // tenant already set by TenantUtils.execute
        swmDifyService.save(dify);
        // cleanup handled by TenantUtils.execute
        XxlJobHelper.log("AI日报（含四五部分）保存成功，reportType={}", reportType);
    }

}
