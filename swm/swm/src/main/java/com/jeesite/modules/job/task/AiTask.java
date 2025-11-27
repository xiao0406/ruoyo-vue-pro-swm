package com.jeesite.modules.job.task;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmDify;
import com.jeesite.modules.swm.service.SwmDailyAttendanceService;
import com.jeesite.modules.swm.service.SwmDifyService;
import com.jeesite.modules.vo.AiDto;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Ai相关任务
 */
@Component
@Slf4j
public class AiTask {

    private static final String workflowUrl = "http://10.50.103.189:7780/v1/workflows/run";
    // 请求头配置
    private static final String AUTHORIZATION = "Bearer app-fZLfOEAP99eXlVX3AuzoK2bn";
    private static final String CONTENT_TYPE = "application/json";

    //Boyd配置
    private static final String workflowId = "0a0b5524-0e96-474b-8b6a-c3ebe7277058";
    private static final String responseMode = "blocking";

    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;
    @Autowired
    private SwmDifyService swmDifyService;

    /**
     * 工效任务
     * 自动统计并输出昨天的相关信息（例：24日自动输出23日白班、夜班及全天的统计数据），具体如下：
     * 根据白班/夜班/全天、按班组、车间、全体工人等维度去统计以下数据：
     统计数据	白班	  夜班	全天
     班组		xx	   xx   xx
     车间		xx	   xx   xx
     全体工人	xx     xx   xx

     * 1.应到人数：根据排班计算应到人数
     * 2.实到人数：根据实际出勤时间统计实到人数（只要实际出勤时间不为0就算出勤）
     * 3.出勤率：应到人数/实到人数
     * 4.有效作业时长：有效出勤时间
     * 输出全体人员的上班时间、下班时间、有效出勤时长
     * 根据全体人员的有效出勤时长进行排序，超过某一阈值判定为疲劳，输出疲劳人员信息并统计疲劳人员人数。（姓名、绑定安全帽ID、手机号、车间、班组）
     *
     * 传参：startDate,endDate
     */
    @XxlJob("workEfficiencyTask")
    @Transactional(rollbackFor = Exception.class)
    public void workEfficiencyTask() {
        XxlJobHelper.log("=====================定时生成工效任务=================");
        Date yesterday = DateUtil.yesterday();
        String jobParam = XxlJobHelper.getJobParam();
        String[] split = jobParam.split(",");
        String startDate = split[0];
        String endDate = split[1];


        if (ObjectUtils.isEmpty(startDate)){
            startDate = DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd");
        }
        if (ObjectUtils.isEmpty(endDate)){
            endDate = DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd");
        }
        log.info("日报开始时间：{},结束时间{}", startDate, endDate);

//        //1.获取所有的人员打卡信息
//        List<SwmDailyAttendance> teamList =swmDailyAttendanceService.findAllList(startDate, endDate);
//        //2.获取前一天班组的有效时长
//        Map<String, List<SwmDailyAttendance>> teamMap = teamList.stream().collect(Collectors.groupingBy(SwmDailyAttendance::getTeamName));
//        for (Map.Entry<String, List<SwmDailyAttendance>> entry : teamMap.entrySet()) {
//            String teamName = entry.getKey();
//            List<SwmDailyAttendance> value = entry.getValue();
//            BigDecimal totalActualHours = value.stream()
//                    .map(SwmDailyAttendance::getActualHours)
//                    .filter(Objects::nonNull)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            realutMap.put(teamName, totalActualHours);
//        }
        //3.从ai日报表数据库查询前一天的数据
        SwmDify swmDify = swmDifyService.getEntity(DateUtil.beginOfDay(yesterday), DateUtil.endOfDay(yesterday));
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("date", DateUtil.format(yesterday, "yyyy-MM-dd"));
        inputs.put("projectName", "广东厂慧眼安盾项目");
        if (swmDify != null){
            inputs.put("safetyIndex", swmDify.getSafetyIndex());
            inputs.put("workerIndex", swmDify.getWorkerIndex());
        }
        //构建请求参数
        Map<String, Object> requestBody = buildRequestBody(inputs);
        log.info("请求参数：{}", requestBody);
        String body = sendPostRequest(requestBody);
        if (body != null){
            JSONObject jsonObject = JSONObject.parseObject(body);
            Object object = jsonObject.get("data");
            if (object instanceof JSONObject){
                JSONObject data = (JSONObject) object;
                Object object1 = data.get("outputs");
                if (object1 instanceof JSONObject){
                    SwmDify dify = new SwmDify();
                    JSONObject object2 = (JSONObject) object1;
                    dify.setDate(yesterday);
                    dify.setProjectName((String) inputs.get("projectName"));
                    dify.setSafetyIndex(object2.getLong("safetyIndex"));
                    dify.setWorkerIndex(object2.getLong("workerIndex"));
                    dify.setText(object2.getString("text"));
                    swmDifyService.save(dify);
                }
            }
        }
    }


    /**
     * 构建请求体参数
     */
    private Map<String, Object> buildRequestBody(Map<String, Object> inputs) {
        Map<String, Object> body = new HashMap<>();

        // 基础参数
        body.put("workflow_id", workflowId);
        body.put("response_mode", responseMode);
        body.put("user", workflowId);
        body.put("files", new HashMap[]{}); // 空数组
        body.put("inputs", inputs);
        return body;
    }

    /**
     * 发送 Post 请求（Hutool 实现）
     * @param requestBody 请求体参数
     * @return 接口响应字符串
     */
    private String sendPostRequest(Map<String, Object> requestBody) {
        try (HttpResponse response = HttpRequest.post(workflowUrl)
                // 设置请求头
                .header("Authorization", AUTHORIZATION)
                .header("Content-Type", CONTENT_TYPE)
                // 设置请求体（Hutool 自动序列化 Map 为 JSON）
                .body(String.valueOf(new JSONObject(requestBody)))
                // 执行请求
                .execute()) {

            // 检查响应状态码（200 表示成功）
            if (response.isOk()) {
                return response.body(); // 返回响应体字符串
            } else {
                System.err.println("接口请求失败，状态码：" + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            System.err.println("接口请求异常：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
