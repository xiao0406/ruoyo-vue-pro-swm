package com.jeesite.modules.swm.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.entity.Page;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.swm.entity.SwmAreaFenceData;
import com.jeesite.modules.swm.entity.vo.SwmAreaFenceDataVO;
import com.jeesite.modules.swm.service.SwmAreaFenceDataService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SwmAreaFenceDataServiceImpl implements SwmAreaFenceDataService {

    private static final Logger logger = LoggerFactory.getLogger(SwmAreaFenceDataServiceImpl.class);

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String tdengineDbName;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<SwmAreaFenceDataVO> findPage(Page<SwmAreaFenceDataVO> page, SwmAreaFenceData entity) {
        List<SwmAreaFenceDataVO> list = findPageData(page.getPageNo(), page.getPageSize(), entity);
        page.setList(list);
        page.setCount(count(entity));
        return page;
    }

    @Override
    public List<SwmAreaFenceDataVO> findList(SwmAreaFenceData entity, int limit) {
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
    public long count(SwmAreaFenceData entity) {
        String sql = buildCountSql(entity);
        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() == R.SUCCESS) {
            JSONObject data = result.getData();
            if (data != null && data.containsKey("data")) {
                JSONArray dataArray = data.getJSONArray("data");
                if (!dataArray.isEmpty()) {
                    JSONArray row = dataArray.getJSONArray(0);
                    if (!row.isEmpty()) {
                        return row.getLong(0);
                    }
                }
            }
        }
        return 0L;
    }

    @Override
    public List<SwmAreaFenceDataVO> findPageData(int pageNo, int pageSize, SwmAreaFenceData entity) {
        List<SwmAreaFenceDataVO> allResults = new ArrayList<>();

        if (pageSize > 999) {
            int batchSize = 999;
            int offset = (pageNo - 1) * pageSize;
            int remaining = pageSize;
            int currentOffset = offset;

            while (remaining > 0) {
                int currentBatchSize = Math.min(batchSize, remaining);
                String batchSql = buildQuerySql(entity, true, currentOffset / batchSize + 1, currentBatchSize);
                batchSql = batchSql.replaceAll("LIMIT \\d+ OFFSET \\d+", "LIMIT " + currentBatchSize + " OFFSET " + currentOffset);

                R<JSONObject> result = tdengineService.executeTDengineSQL(batchSql);
                if (result.getCode() == R.SUCCESS) {
                    JSONObject data = result.getData();
                    if (data != null && data.containsKey("data")) {
                        JSONArray dataArray = data.getJSONArray("data");
                        List<SwmAreaFenceDataVO> batchResults = parseQueryResult(dataArray);
                        allResults.addAll(batchResults);
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

    private String buildQuerySql(SwmAreaFenceData entity, boolean isPaging, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT time, x, y, area_name, area_id, remarks, area_type, device_id, id_card ");
        sql.append("FROM ").append(tdengineDbName).append(".").append(TdengineSuperTableConstant.AREA_FENCE_DATA).append(" ");

        List<String> conditions = buildWhereConditions(entity);
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        String order = "DESC";
        if (StringUtils.isNotBlank(entity.getSortOrder()) && "ASC".equalsIgnoreCase(entity.getSortOrder())) {
            order = "ASC";
        }
        sql.append("ORDER BY time ").append(order).append(" ");

        if (isPaging) {
            int offset = (pageNo - 1) * pageSize;
            sql.append("LIMIT ").append(pageSize).append(" OFFSET ").append(offset);
        } else {
            sql.append("LIMIT ").append(pageSize);
        }

        return sql.toString();
    }

    private String buildCountSql(SwmAreaFenceData entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tdengineDbName).append(".").append(TdengineSuperTableConstant.AREA_FENCE_DATA).append(" ");
        List<String> conditions = buildWhereConditions(entity);
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    private List<String> buildWhereConditions(SwmAreaFenceData entity) {
        List<String> conditions = new ArrayList<>();

        if (StringUtils.isNotBlank(entity.getDeviceId())) {
            String deviceParam = entity.getDeviceId().trim();
            if (deviceParam.contains(",")) {
                String[] devices = deviceParam.split(",");
                List<String> valid = new ArrayList<>();
                for (String device : devices) {
                    String trimmed = device.trim();
                    if (StringUtils.isNotBlank(trimmed)) {
                        valid.add("'" + trimmed.replace("'", "''") + "'");
                    }
                }
                if (!valid.isEmpty()) {
                    conditions.add("device_id IN (" + String.join(",", valid) + ")");
                }
            } else {
                conditions.add("device_id = '" + deviceParam.replace("'", "''") + "'");
            }
        }

        if (StringUtils.isNotBlank(entity.getIdCard())) {
            String idCardParam = entity.getIdCard().trim();
            if (idCardParam.contains(",")) {
                String[] idCards = idCardParam.split(",");
                List<String> valid = new ArrayList<>();
                for (String card : idCards) {
                    String trimmed = card.trim();
                    if (StringUtils.isNotBlank(trimmed)) {
                        valid.add("'" + trimmed.replace("'", "''") + "'");
                    }
                }
                if (!valid.isEmpty()) {
                    conditions.add("id_card IN (" + String.join(",", valid) + ")");
                }
            } else {
                conditions.add("id_card = '" + idCardParam.replace("'", "''") + "'");
            }
        }

        if (entity.getStartTime() != null) {
            conditions.add("time >= '" + dateFormat.format(entity.getStartTime()) + "'");
        }

        if (entity.getEndTime() != null) {
            conditions.add("time <= '" + dateFormat.format(entity.getEndTime()) + "'");
        }

        return conditions;
    }

    private List<SwmAreaFenceDataVO> parseQueryResult(JSONArray dataArray) {
        List<SwmAreaFenceDataVO> list = new ArrayList<>();

        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray row = dataArray.getJSONArray(i);
            if (row.size() >= 9) {
                SwmAreaFenceDataVO vo = new SwmAreaFenceDataVO();

                String timeStr = row.getStr(0);
                if (StringUtils.isNotBlank(timeStr)) {
                    try {
                        Date time = new Date(Long.parseLong(timeStr));
                        vo.setTime(time);
                        vo.setTimeText(dateFormat.format(time));
                    } catch (NumberFormatException e) {
                        try {
                            Instant instant = Instant.parse(timeStr);
                            Date time = Date.from(instant);
                            vo.setTime(time);
                            vo.setTimeText(dateFormat.format(time));
                        } catch (Exception ex) {
                            vo.setTimeText(timeStr);
                        }
                    }
                }

                vo.setX(row.getDouble(1));
                vo.setY(row.getDouble(2));
                vo.setAreaName(row.getStr(3));
                vo.setAreaId(row.getStr(4));
                vo.setRemarks(row.getStr(5));
                vo.setAreaType(row.getStr(6));
                vo.setDeviceId(row.getStr(7));
                vo.setIdCard(row.getStr(8));

                list.add(vo);
            }
        }

        return list;
    }
}
