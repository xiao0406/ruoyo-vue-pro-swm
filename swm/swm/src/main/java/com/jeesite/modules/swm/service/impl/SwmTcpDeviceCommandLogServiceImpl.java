/**
 * @author Shawn
 * @date 2025-10-02
 */
package com.jeesite.modules.swm.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmTcpDeviceCommandLog;
import com.jeesite.modules.swm.entity.vo.SwmTcpDeviceCommandLogVO;
import com.jeesite.modules.swm.service.SwmTcpDeviceCommandLogService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TDengine 下发指令到设备日志Service实现类
 */
@Service
public class SwmTcpDeviceCommandLogServiceImpl implements SwmTcpDeviceCommandLogService {

    private static final Logger logger = LoggerFactory.getLogger(SwmTcpDeviceCommandLogServiceImpl.class);

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String tdengineDbName;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<SwmTcpDeviceCommandLogVO> findPage(Page<SwmTcpDeviceCommandLogVO> page, SwmTcpDeviceCommandLog entity) {
        List<SwmTcpDeviceCommandLogVO> list = findPageData(page.getPageNo(), page.getPageSize(), entity);
        page.setList(list);
        page.setCount(count(entity));
        return page;
    }

    @Override
    public List<SwmTcpDeviceCommandLogVO> findList(SwmTcpDeviceCommandLog entity, int limit) {
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
    public long count(SwmTcpDeviceCommandLog entity) {
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
    private String buildQuerySql(SwmTcpDeviceCommandLog entity, boolean isPaging, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT time, send_message, send_status, error_message, identity_card, person_name, device_id ");
        sql.append("FROM ").append(tdengineDbName).append(".tcp_device_command_log ");

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
    private String buildCountSql(SwmTcpDeviceCommandLog entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tdengineDbName).append(".tcp_device_command_log ");

        List<String> conditions = buildWhereConditions(entity);
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    /**
     * 构建WHERE条件
     */
    private List<String> buildWhereConditions(SwmTcpDeviceCommandLog entity) {
        List<String> conditions = new ArrayList<>();

        // 设备ID条件（支持多设备查询，TAG字段性能好）
        if (StringUtils.isNotBlank(entity.getDeviceId())) {
            String deviceIdParam = entity.getDeviceId().trim();

            if (deviceIdParam.contains(",")) {
                String[] deviceIds = deviceIdParam.split(",");
                List<String> validDeviceIds = new ArrayList<>();

                for (String deviceId : deviceIds) {
                    String trimmedId = deviceId.trim();
                    if (StringUtils.isNotBlank(trimmedId)) {
                        validDeviceIds.add("'" + trimmedId.replace("'", "''") + "'");
                    }
                }

                if (!validDeviceIds.isEmpty()) {
                    conditions.add("device_id IN (" + String.join(",", validDeviceIds) + ")");
                }
            } else {
                conditions.add("device_id = '" + deviceIdParam.replace("'", "''") + "'");
            }
        }

        // 身份证号条件（支持多身份证查询，普通字段）
        if (StringUtils.isNotBlank(entity.getIdentityCard())) {
            String identityCardParam = entity.getIdentityCard().trim();

            if (identityCardParam.contains(",")) {
                String[] identityCards = identityCardParam.split(",");
                List<String> validIdentityCards = new ArrayList<>();

                for (String identityCard : identityCards) {
                    String trimmedCard = identityCard.trim();
                    if (StringUtils.isNotBlank(trimmedCard)) {
                        validIdentityCards.add("'" + trimmedCard.replace("'", "''") + "'");
                    }
                }

                if (!validIdentityCards.isEmpty()) {
                    conditions.add("identity_card IN (" + String.join(",", validIdentityCards) + ")");
                }
            } else {
                conditions.add("identity_card = '" + identityCardParam.replace("'", "''") + "'");
            }
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
    private List<SwmTcpDeviceCommandLogVO> parseQueryResult(JSONArray dataArray) {
        List<SwmTcpDeviceCommandLogVO> list = new ArrayList<>();

        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray row = dataArray.getJSONArray(i);
            if (row.size() >= 7) {
                SwmTcpDeviceCommandLogVO vo = new SwmTcpDeviceCommandLogVO();

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

                vo.setSendMessage(row.getStr(1));
                vo.setSendStatus(row.getStr(2));
                vo.setErrorMessage(row.getStr(3));
                vo.setIdentityCard(row.getStr(4));
                vo.setPersonName(row.getStr(5));
                vo.setDeviceId(row.getStr(6));

                list.add(vo);
            }
        }

        return list;
    }

    @Override
    public List<SwmTcpDeviceCommandLogVO> findPageData(int pageNo, int pageSize, SwmTcpDeviceCommandLog entity) {
        List<SwmTcpDeviceCommandLogVO> allResults = new ArrayList<>();

        // 如果pageSize > 999，使用分批查询绕过TDengine限制
        if (pageSize > 999) {
            int batchSize = 999; // TDengine的限制
            int offset = (pageNo - 1) * pageSize;
            int remaining = pageSize;
            int currentOffset = offset;

            while (remaining > 0) {
                int currentBatchSize = Math.min(batchSize, remaining);

                // 构建当前批次的SQL
                String batchSql = buildQuerySql(entity, true,
                    currentOffset / batchSize + 1, currentBatchSize);

                // 手动调整SQL的OFFSET（因为我们需要精确控制）
                batchSql = batchSql.replaceAll("LIMIT \\d+ OFFSET \\d+",
                    "LIMIT " + currentBatchSize + " OFFSET " + currentOffset);

                R<JSONObject> result = tdengineService.executeTDengineSQL(batchSql);
                if (result.getCode() == R.SUCCESS) {
                    JSONObject data = result.getData();
                    if (data != null && data.containsKey("data")) {
                        JSONArray dataArray = data.getJSONArray("data");
                        List<SwmTcpDeviceCommandLogVO> batchResults = parseQueryResult(dataArray);
                        allResults.addAll(batchResults);

                        // 如果返回的数据少于请求的数量，说明没有更多数据了
                        if (batchResults.size() < currentBatchSize) {
                            break;
                        }
                    }
                } else {
                    logger.warn("TDengine批次查询失败: {}", result.getMsg());
                    break;
                }

                currentOffset += currentBatchSize;
                remaining -= currentBatchSize;
            }
        } else {
            // 正常查询流程
            String sql = buildQuerySql(entity, true, pageNo, pageSize);

            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
            if (result.getCode() == R.SUCCESS) {
                JSONObject data = result.getData();
                if (data != null && data.containsKey("data")) {
                    JSONArray dataArray = data.getJSONArray("data");
                    allResults = parseQueryResult(dataArray);
                }
            } else {
                logger.warn("TDengine查询失败: {}", result.getMsg());
            }
        }

        return allResults;
    }
}
