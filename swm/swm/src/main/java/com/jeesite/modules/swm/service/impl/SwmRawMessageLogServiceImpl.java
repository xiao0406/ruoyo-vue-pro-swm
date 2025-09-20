/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmRawMessageLog;
import com.jeesite.modules.swm.entity.vo.SwmRawMessageLogVO;
import com.jeesite.modules.swm.service.SwmRawMessageLogService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TDengine原始消息日志Service实现类
 */
@Service
public class SwmRawMessageLogServiceImpl implements SwmRawMessageLogService {

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String tdengineDbName;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<SwmRawMessageLogVO> findPage(Page<SwmRawMessageLogVO> page, SwmRawMessageLog entity) {
        String sql = buildQuerySql(entity, true, page.getPageNo(), page.getPageSize());

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() == R.SUCCESS) {
            JSONObject data = result.getData();
            if (data != null && data.containsKey("data")) {
                JSONArray dataArray = data.getJSONArray("data");
                List<SwmRawMessageLogVO> list = parseQueryResult(dataArray);
                page.setList(list);

                // 设置总记录数
                long totalCount = count(entity);
                page.setCount(totalCount);
            }
        }

        return page;
    }

    @Override
    public List<SwmRawMessageLogVO> findList(SwmRawMessageLog entity, int limit) {
        String sql = buildQuerySql(entity, false, 1, limit);

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() == R.SUCCESS) {
            JSONObject data = result.getData();
            if (data != null && data.containsKey("data")) {
                JSONArray dataArray = data.getJSONArray("data");
                return parseQueryResult(dataArray);
            }
        }

        return new ArrayList<>();
    }

    @Override
    public long count(SwmRawMessageLog entity) {
        String sql = buildCountSql(entity);

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() == R.SUCCESS) {
            JSONObject data = result.getData();
            if (data != null && data.containsKey("data")) {
                JSONArray dataArray = data.getJSONArray("data");
                if (dataArray.size() > 0) {
                    JSONArray row = dataArray.getJSONArray(0);
                    if (row.size() > 0) {
                        return row.getLong(0);
                    }
                }
            }
        }

        return 0L;
    }

    /**
     * 构建查询SQL
     */
    private String buildQuerySql(SwmRawMessageLog entity, boolean isPaging, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT time, session_id, message_time, message_content, original_length, is_truncated, device_id ");
        sql.append("FROM ").append(tdengineDbName).append(".raw_message_log ");

        // 构建WHERE条件
        List<String> conditions = buildWhereConditions(entity);
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        // 排序（根据sortOrder参数决定升序或降序）
        String order = "DESC"; // 默认降序
        if (entity.getSortOrder() != null && "ASC".equalsIgnoreCase(entity.getSortOrder())) {
            order = "ASC";
        }
        sql.append("ORDER BY time ").append(order).append(" ");

        // 分页
        if (isPaging) {
            int offset = (pageNo - 1) * pageSize;
            sql.append("LIMIT ").append(pageSize).append(" OFFSET ").append(offset);
        } else {
            sql.append("LIMIT ").append(pageSize);
        }

        return sql.toString();
    }

    /**
     * 构建计数SQL
     */
    private String buildCountSql(SwmRawMessageLog entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tdengineDbName).append(".raw_message_log ");

        List<String> conditions = buildWhereConditions(entity);
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    /**
     * 构建WHERE条件
     */
    private List<String> buildWhereConditions(SwmRawMessageLog entity) {
        List<String> conditions = new ArrayList<>();

        // 设备ID条件
        if (StringUtils.isNotBlank(entity.getDeviceId())) {
            conditions.add("device_id = '" + entity.getDeviceId() + "'");
        }

        // 时间范围条件
        if (entity.getStartTime() != null) {
            conditions.add("time >= '" + dateFormat.format(entity.getStartTime()) + "'");
        }

        if (entity.getEndTime() != null) {
            conditions.add("time <= '" + dateFormat.format(entity.getEndTime()) + "'");
        }

        return conditions;
    }

    /**
     * 解析查询结果
     */
    private List<SwmRawMessageLogVO> parseQueryResult(JSONArray dataArray) {
        List<SwmRawMessageLogVO> list = new ArrayList<>();

        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray row = dataArray.getJSONArray(i);
            if (row.size() >= 7) {
                SwmRawMessageLogVO vo = new SwmRawMessageLogVO();

                // 解析时间戳
                String timeStr = row.getStr(0);
                if (StringUtils.isNotBlank(timeStr)) {
                    try {
                        // 首先尝试解析为数字时间戳
                        Date time = new Date(Long.parseLong(timeStr));
                        vo.setTime(time);
                        vo.setTimeText(dateFormat.format(time));
                    } catch (NumberFormatException e) {
                        try {
                            // 如果是ISO 8601格式时间字符串，解析为Date
                            Instant instant = Instant.parse(timeStr);
                            Date time = Date.from(instant);
                            vo.setTime(time);
                            vo.setTimeText(dateFormat.format(time));
                        } catch (Exception ex) {
                            // 如果都解析失败，直接设置原始字符串
                            vo.setTimeText(timeStr);
                        }
                    }
                }

                vo.setSessionId(row.getStr(1));
                vo.setMessageTime(row.getLong(2));
                vo.setMessageContent(row.getStr(3));
                vo.setOriginalLength(row.getInt(4));
                vo.setIsTruncated(row.getInt(5));
                vo.setDeviceId(row.getStr(6));

                // 设置是否截断的文本
                Integer isTruncated = vo.getIsTruncated();
                if (isTruncated != null) {
                    vo.setIsTruncatedText(isTruncated == 1 ? "是" : "否");
                }

                list.add(vo);
            }
        }

        return list;
    }
}