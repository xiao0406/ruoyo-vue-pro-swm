/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmExternalCoordinateData;
import com.jeesite.modules.swm.entity.vo.SwmExternalCoordinateDataVO;
import com.jeesite.modules.swm.service.SwmExternalCoordinateDataService;
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

/**
 * TDengine外部坐标数据Service实现类
 */
@Service
public class SwmExternalCoordinateDataServiceImpl implements SwmExternalCoordinateDataService {

    private static final Logger logger = LoggerFactory.getLogger(SwmExternalCoordinateDataServiceImpl.class);

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String tdengineDbName;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<SwmExternalCoordinateDataVO> findPage(Page<SwmExternalCoordinateDataVO> page, SwmExternalCoordinateData entity) {
        List<SwmExternalCoordinateDataVO> list = findPageData(page.getPageNo(), page.getPageSize(), entity);
        page.setList(list);
        page.setCount(count(entity));
        return page;
    }

    @Override
    public List<SwmExternalCoordinateDataVO> findList(SwmExternalCoordinateData entity, int limit) {
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
    public long count(SwmExternalCoordinateData entity) {
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
    public List<SwmExternalCoordinateDataVO> findPageData(int pageNo, int pageSize, SwmExternalCoordinateData entity) {
        List<SwmExternalCoordinateDataVO> allResults = new ArrayList<>();

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
                        List<SwmExternalCoordinateDataVO> batchResults = parseQueryResult(dataArray);
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

    private String buildQuerySql(SwmExternalCoordinateData entity, boolean isPaging, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT time, address, map_id, x, y, org_cd, time_str, type, app_id, warning_id, original_x, original_y, scale_x, scale_y, nearest_beacon, used_beacons, elder_id, id_card ");
        sql.append("FROM ").append(tdengineDbName).append(".external_coordinate_data ");

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

    private String buildCountSql(SwmExternalCoordinateData entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tdengineDbName).append(".external_coordinate_data ");

        List<String> conditions = buildWhereConditions(entity);
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    private List<String> buildWhereConditions(SwmExternalCoordinateData entity) {
        List<String> conditions = new ArrayList<>();

        if (StringUtils.isNotBlank(entity.getElderId())) {
            String elderIdParam = entity.getElderId().trim();
            if (elderIdParam.contains(",")) {
                String[] elderIds = elderIdParam.split(",");
                List<String> validElderIds = new ArrayList<>();
                for (String elderId : elderIds) {
                    String trimmedId = elderId.trim();
                    if (StringUtils.isNotBlank(trimmedId)) {
                        validElderIds.add("'" + trimmedId.replace("'", "''") + "'");
                    }
                }
                if (!validElderIds.isEmpty()) {
                    conditions.add("elder_id IN (" + String.join(",", validElderIds) + ")");
                }
            } else {
                conditions.add("elder_id = '" + elderIdParam.replace("'", "''") + "'");
            }
        }

        if (StringUtils.isNotBlank(entity.getIdCard())) {
            String idCardParam = entity.getIdCard().trim();
            if (idCardParam.contains(",")) {
                String[] idCards = idCardParam.split(",");
                List<String> validIdCards = new ArrayList<>();
                for (String idCard : idCards) {
                    String trimmed = idCard.trim();
                    if (StringUtils.isNotBlank(trimmed)) {
                        validIdCards.add("'" + trimmed.replace("'", "''") + "'");
                    }
                }
                if (!validIdCards.isEmpty()) {
                    conditions.add("id_card IN (" + String.join(",", validIdCards) + ")");
                }
            } else {
                conditions.add("id_card = '" + idCardParam.replace("'", "''") + "'");
            }
        }

        if (entity.getMapId() != null) {
            conditions.add("map_id = " + entity.getMapId());
        }

        if (entity.getWarningId() != null) {
            conditions.add("warning_id = " + entity.getWarningId());
        }

        if (StringUtils.isNotBlank(entity.getType())) {
            conditions.add("type = '" + entity.getType().trim().replace("'", "''") + "'");
        }

        if (StringUtils.isNotBlank(entity.getOrgCd())) {
            conditions.add("org_cd = '" + entity.getOrgCd().trim().replace("'", "''") + "'");
        }

        if (StringUtils.isNotBlank(entity.getAddress())) {
            conditions.add("address = '" + entity.getAddress().trim().replace("'", "''") + "'");
        }

        if (StringUtils.isNotBlank(entity.getAppId())) {
            conditions.add("app_id = '" + entity.getAppId().trim().replace("'", "''") + "'");
        }

        if (entity.getStartTime() != null) {
            conditions.add("time >= '" + dateFormat.format(entity.getStartTime()) + "'");
        }

        if (entity.getEndTime() != null) {
            conditions.add("time <= '" + dateFormat.format(entity.getEndTime()) + "'");
        }

        return conditions;
    }

    private List<SwmExternalCoordinateDataVO> parseQueryResult(JSONArray dataArray) {
        List<SwmExternalCoordinateDataVO> list = new ArrayList<>();

        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray row = dataArray.getJSONArray(i);
            if (row.size() >= 16) {
                SwmExternalCoordinateDataVO vo = new SwmExternalCoordinateDataVO();

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

                vo.setAddress(row.getStr(1));
                vo.setMapId(row.getInt(2));
                vo.setX(row.getDouble(3));
                vo.setY(row.getDouble(4));
                vo.setOrgCd(row.getStr(5));
                vo.setTimeStr(row.getStr(6));
                vo.setType(row.getStr(7));
                vo.setAppId(row.getStr(8));
                vo.setWarningId(row.getInt(9));
                vo.setOriginalX(row.getDouble(10));
                vo.setOriginalY(row.getDouble(11));
                vo.setScaleX(row.getDouble(12));
                vo.setScaleY(row.getDouble(13));
                vo.setNearestBeacon(row.getStr(14));
                vo.setUsedBeacons(row.getStr(15));
                if (row.size() > 16) {
                    vo.setElderId(row.getStr(16));
                }
                if (row.size() > 17) {
                    vo.setIdCard(row.getStr(17));
                }

                if (vo.getTime() == null && vo.getTimeText() == null && StringUtils.isNotBlank(vo.getTimeStr())) {
                    vo.setTimeText(vo.getTimeStr());
                }

                list.add(vo);
            }
        }

        return list;
    }
}
