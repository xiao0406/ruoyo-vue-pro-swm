package com.jeesite.modules.swm.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmAlarmConfigDao;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 预警管理Service
 *
 * @author zwf
 * @version 2025-05-16
 */
@Service
@Transactional(readOnly = true)
public class SwmWarningManagementService extends CrudService<SwmWarningManagementDao, SwmWarningManagement> {

    @Autowired
    private TDengineService tdengineService;

    // 直接使用固定的数据库名称
    private String dbname = "plb";

    /**
     * 获取单条数据
     */
    @Override
    public SwmWarningManagement get(SwmWarningManagement swmWarningManagement) {
        // 使用TDengine查询单条数据
        if (swmWarningManagement != null && swmWarningManagement.getId() != null) {
            // 在SQL中使用TIMEDIFF函数添加8小时(28800000ms)到时间字段
            String sql = String.format("SELECT id, person_name, warning_type, warning_content, " +
                    "CAST(warning_time + 28800000 AS TIMESTAMP) as warning_time, " +
                    "alarm_record, CAST(alarm_time + 28800000 AS TIMESTAMP) as alarm_time, " +
                    "trigger_reason, handler, handle_time, handle_process, handle_status, attachment, " +
                    "create_by, create_date, update_by, update_date, remarks, status, device_id, id_card, " +
                    "front_alarm, type, x, y, hazard_category, location, area " +
                    "FROM %s.swm_warning_management WHERE id='%s' LIMIT 1",
                    dbname, swmWarningManagement.getId());

            try {
                logger.info("执行单条查询SQL: {}", sql);
                R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
                if (result.getCode() == R.SUCCESS && result.getData() != null) {
                    JSONObject data = result.getData();
                    JSONArray rows = data.getJSONArray("data");

                    if (rows != null && rows.size() > 0) {
                        SwmWarningManagement entity = convertToEntity(rows.getJSONArray(0),
                                data.getJSONArray("column_meta"));
                        logger.info("从时序数据库查询到警告记录，ID: {}, 警告时间: {}, 报警时间: {}",
                                entity.getId(),
                                entity.getWarningTime() != null
                                        ? DateUtils.formatDate(entity.getWarningTime(), "yyyy-MM-dd HH:mm:ss")
                                        : "null",
                                entity.getAlarmTime() != null
                                        ? DateUtils.formatDate(entity.getAlarmTime(), "yyyy-MM-dd HH:mm:ss")
                                        : "null");
                        return entity;
                    }
                }
            } catch (Exception e) {
                logger.error("从TDengine获取预警数据失败", e);
            }
        }

        // 如果TDengine查询失败，回退到原始查询
        return super.get(swmWarningManagement);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmWarningManagement> findPage(SwmWarningManagement swmWarningManagement) {
        // 创建分页对象
        Page<SwmWarningManagement> page = swmWarningManagement.getPage();
        if (page == null) {
            page = new Page<>();
        }

        // 构建TDengine查询SQL
        StringBuilder sqlBuilder = new StringBuilder();
        // 在SQL中使用TIMEDIFF函数添加8小时(28800000ms)到时间字段
        sqlBuilder.append("SELECT id, person_name, warning_type, warning_content, ")
                .append("CAST(warning_time + 28800000 AS TIMESTAMP) as warning_time, ")
                .append("alarm_record, CAST(alarm_time + 28800000 AS TIMESTAMP) as alarm_time, ")
                .append("trigger_reason, handler, handle_time, handle_process, handle_status, attachment, ")
                .append("create_by, create_date, update_by, update_date, remarks, status, device_id, id_card, ")
                .append("front_alarm, type, x, y, hazard_category, location, area ")
                .append("FROM ").append(dbname).append(".swm_warning_management");

        // 添加查询条件
        List<String> conditions = new ArrayList<>();

        // 如果需要排除一键SOS
        if (swmWarningManagement.isExcludeSOS()) {
            conditions.add("warning_content != '一键SOS'");
            logger.info("添加排除一键SOS的查询条件");
        }

        // 如果需要排除考勤打卡
        if (swmWarningManagement.isExcludeAttendance()) {
            conditions.add("warning_content != '考勤打卡'");
            logger.info("添加排除考勤打卡的查询条件");
        }

        // 如果需要排除进入大门
        if (swmWarningManagement.isExcludeGateEntry()) {
            conditions.add("warning_content != '进入大门'");
            logger.info("添加排除进入大门的查询条件");
        }

        // 添加预警单号(ID)查询条件
        if (swmWarningManagement.getId() != null && !swmWarningManagement.getId().isEmpty()) {
            conditions.add("id LIKE '%" + swmWarningManagement.getId() + "%'");
            logger.info("添加预警单号查询条件: {}", swmWarningManagement.getId());
        }

        if (swmWarningManagement.getPersonName() != null && !swmWarningManagement.getPersonName().isEmpty()) {
            conditions.add("person_name LIKE '%" + swmWarningManagement.getPersonName() + "%'");
        }

        if (swmWarningManagement.getWarningType() != null && !swmWarningManagement.getWarningType().isEmpty()) {
            conditions.add("warning_type = '" + swmWarningManagement.getWarningType() + "'");
        }

        if (swmWarningManagement.getWarningContent() != null && !swmWarningManagement.getWarningContent().isEmpty()) {
            conditions.add("warning_content = '" + swmWarningManagement.getWarningContent() + "'");
        }

        if (swmWarningManagement.getHandleStatus() != null && !swmWarningManagement.getHandleStatus().isEmpty()) {
            conditions.add("handle_status = '" + swmWarningManagement.getHandleStatus() + "'");
        }

        // 添加位置条件
        if (swmWarningManagement.getLocation() != null && !swmWarningManagement.getLocation().isEmpty()) {
            conditions.add("location LIKE '%" + swmWarningManagement.getLocation() + "%'");
            logger.info("添加位置查询条件: {}", swmWarningManagement.getLocation());
        }

        // 添加区域条件
        if (swmWarningManagement.getArea() != null && !swmWarningManagement.getArea().isEmpty()) {
            conditions.add("area LIKE '%" + swmWarningManagement.getArea() + "%'");
            logger.info("添加区域查询条件: {}", swmWarningManagement.getArea());
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                sqlBuilder.append(conditions.get(i));
                if (i < conditions.size() - 1) {
                    sqlBuilder.append(" AND ");
                }
            }
        }

        // 计算总记录数
        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";
        R<JSONObject> countResult = tdengineService.executeTDengineSQL(countSql);
        long total = 0;

        if (countResult.getCode() == R.SUCCESS && countResult.getData() != null) {
            JSONObject data = countResult.getData();
            JSONArray rows = data.getJSONArray("data");

            if (rows != null && rows.size() > 0) {
                total = rows.getJSONArray(0).getLong(0);
            }
        }

        // 添加排序和分页
        sqlBuilder.append(" ORDER BY warning_time DESC");
        sqlBuilder.append(" LIMIT ").append(page.getPageSize());
        sqlBuilder.append(" OFFSET ").append((page.getPageNo() - 1) * page.getPageSize());

        logger.info("执行SQL: {}", sqlBuilder.toString());

        // 执行查询
        R<JSONObject> result = tdengineService.executeTDengineSQL(sqlBuilder.toString());
        List<SwmWarningManagement> list = new ArrayList<>();

        if (result.getCode() == R.SUCCESS && result.getData() != null) {
            JSONObject data = result.getData();
            JSONArray rows = data.getJSONArray("data");
            JSONArray columnMeta = data.getJSONArray("column_meta");

            if (rows != null) {
                for (int i = 0; i < rows.size(); i++) {
                    try {
                        JSONArray row = rows.getJSONArray(i);
                        SwmWarningManagement entity = convertToEntity(row, columnMeta);
                        if (entity != null) {
                            list.add(entity);
                        }
                    } catch (Exception e) {
                        logger.error("转换行数据异常: {}", e.getMessage());
                    }
                }
            }
        }

        // 设置分页结果
        page.setCount(total);
        page.setList(list);
        return page;
    }

    /**
     * 查询分页数据（带分页参数）
     */
    public Page<SwmWarningManagement> findPage(Page<SwmWarningManagement> page,
            SwmWarningManagement swmWarningManagement) {
        swmWarningManagement.setPage(page);
        return this.findPage(swmWarningManagement);
    }

    /**
     * 将TDengine结果转换为实体对象
     */
    private SwmWarningManagement convertToEntity(JSONArray row, JSONArray columnMeta) {
        if (row == null || columnMeta == null) {
            logger.error("行数据或列元数据为空");
            return null;
        }

        SwmWarningManagement entity = new SwmWarningManagement();

        try {
            for (int i = 0; i < columnMeta.size(); i++) {
                if (i >= row.size()) {
                    continue; // 防止数组越界
                }

                JSONArray column = columnMeta.getJSONArray(i);
                if (column == null || column.size() == 0) {
                    continue;
                }

                String columnName = column.getStr(0);
                if (columnName == null) {
                    continue;
                }

                Object value = row.get(i);
                if (value == null) {
                    continue; // 跳过空值
                }

                switch (columnName) {
                    case "id":
                        entity.setId(row.getStr(i));
                        break;
                    case "person_name":
                        entity.setPersonName(row.getStr(i));
                        break;
                    case "warning_type":
                        entity.setWarningType(row.getStr(i));
                        break;
                    case "warning_content":
                        entity.setWarningContent(row.getStr(i));
                        break;
                    case "warning_time":
                        try {
                            // 时间已在SQL中+8小时，可以直接使用
                            Date warningTime = new Date(row.getLong(i));
                            entity.setWarningTime(warningTime);
                            logger.debug("设置预警时间: {} (已在SQL中添加8小时时区调整)",
                                    DateUtils.formatDate(warningTime, "yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date parsedTime = sdf.parse(timeStr);
                                    entity.setWarningTime(parsedTime);
                                }
                            } catch (ParseException pe) {
                                logger.warn("解析warning_time失败: {}", pe.getMessage());
                            }
                        }
                        break;
                    case "alarm_record":
                        entity.setAlarmRecord(row.getStr(i));
                        break;
                    case "alarm_time":
                        try {
                            // 时间已在SQL中+8小时，可以直接使用
                            Date alarmTime = new Date(row.getLong(i));
                            entity.setAlarmTime(alarmTime);
                            logger.debug("设置报警时间: {} (已在SQL中添加8小时时区调整)",
                                    DateUtils.formatDate(alarmTime, "yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date parsedTime = sdf.parse(timeStr);
                                    entity.setAlarmTime(parsedTime);
                                }
                            } catch (ParseException pe) {
                                logger.warn("解析alarm_time失败: {}", pe.getMessage());
                            }
                        }
                        break;
                    case "trigger_reason":
                        entity.setTriggerReason(row.getStr(i));
                        break;
                    case "handler":
                        entity.setHandler(row.getStr(i));
                        break;
                    case "handle_time":
                        if (row.get(i) != null) {
                            try {
                                // 移除8小时时区调整
                                Date handleTime = new Date(row.getLong(i));
                                entity.setHandleTime(handleTime);
                            } catch (Exception e) {
                                // 如果转换失败，尝试作为字符串解析
                                try {
                                    String timeStr = row.getStr(i);
                                    if (timeStr != null && !timeStr.isEmpty()) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        Date parsedTime = sdf.parse(timeStr);
                                        // 移除8小时时区调整
                                        entity.setHandleTime(parsedTime);
                                    }
                                } catch (ParseException pe) {
                                    logger.warn("解析handle_time失败: {}", pe.getMessage());
                                }
                            }
                        }
                        break;
                    case "handle_process":
                        entity.setHandleProcess(row.getStr(i));
                        break;
                    case "handle_status":
                        entity.setHandleStatus(row.getStr(i));
                        break;
                    case "attachment":
                        entity.setAttachment(row.getStr(i));
                        break;
                    case "create_by":
                        entity.setCreateBy(row.getStr(i));
                        break;
                    case "create_date":
                        try {
                            // 移除8小时时区调整
                            Date createDate = new Date(row.getLong(i));
                            entity.setCreateDate(createDate);
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date parsedTime = sdf.parse(timeStr);
                                    // 移除8小时时区调整
                                    entity.setCreateDate(parsedTime);
                                }
                            } catch (ParseException pe) {
                                logger.warn("解析create_date失败: {}", pe.getMessage());
                            }
                        }
                        break;
                    case "update_by":
                        entity.setUpdateBy(row.getStr(i));
                        break;
                    case "update_date":
                        if (row.get(i) != null) {
                            try {
                                // 移除8小时时区调整
                                Date updateDate = new Date(row.getLong(i));
                                entity.setUpdateDate(updateDate);
                            } catch (Exception e) {
                                // 如果转换失败，尝试作为字符串解析
                                try {
                                    String timeStr = row.getStr(i);
                                    if (timeStr != null && !timeStr.isEmpty()) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        Date parsedTime = sdf.parse(timeStr);
                                        // 移除8小时时区调整
                                        entity.setUpdateDate(parsedTime);
                                    }
                                } catch (ParseException pe) {
                                    logger.warn("解析update_date失败: {}", pe.getMessage());
                                }
                            }
                        }
                        break;
                    case "remarks":
                        entity.setRemarks(row.getStr(i));
                        break;
                    case "status":
                        entity.setStatus(row.getStr(i));
                        break;
                    case "device_id":
                        entity.setDeviceId(row.getStr(i));
                        break;
                    case "id_card":
                        entity.setIdCard(row.getStr(i));
                        break;
                    case "type":
                        entity.setType(row.getStr(i));
                        break;
                    case "front_alarm":
                        entity.setFrontAlarm(row.getStr(i));
                        break;
                    case "x":
                        entity.setX(row.getStr(i));
                        break;
                    case "y":
                        entity.setY(row.getStr(i));
                        break;
                    case "hazard_category":
                        entity.setHazardCategory(row.getStr(i));
                        logger.debug("设置危险源类别: {}", row.getStr(i));
                        break;
                    case "location":
                        entity.setLocation(row.getStr(i));
                        logger.debug("设置位置: {}", row.getStr(i));
                        break;
                    case "area":
                        entity.setArea(row.getStr(i));
                        logger.debug("设置区域: {}", row.getStr(i));
                        break;
                }
            }

            // 设置显示文本值
            if (entity.getWarningType() != null) {
                // 修复warningType=1时显示为"主动报警"的问题
                if ("1".equals(entity.getWarningType())) {
                    entity.setWarningTypeText("主动报警");
                } else {
                    entity.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", entity.getWarningType(),
                            entity.getWarningType()));
                }
            }

            if (entity.getHandleStatus() != null) {
                entity.setHandleStatusText(DictUtils.getDictLabel("handle_status_enum", entity.getHandleStatus(),
                        entity.getHandleStatus()));
            }

        } catch (Exception e) {
            logger.error("转换TDengine数据异常", e);
            return null;
        }

        return entity;
    }

    /**
     * 查询分页数据并带文本值
     */
    public Page<SwmWarningManagement> findPageWithTextValues(Page<SwmWarningManagement> page,
            SwmWarningManagement swmWarningManagement) {
        // 使用TDengine查询方法
        return findPage(page, swmWarningManagement);
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmWarningManagement swmWarningManagement) {
        // 如果是新记录
        if (swmWarningManagement.getIsNewRecord()) {
            // 创建SQL插入语句
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(dbname).append(".swm_warning_management")
                    .append(" (");

            // 字段列表
            List<String> columns = new ArrayList<>();
            List<String> values = new ArrayList<>();

            // 添加必要的字段
            addField(columns, values, "id", swmWarningManagement.getId(), true);
            addField(columns, values, "create_date", new Date(), false);
            addField(columns, values, "create_by", swmWarningManagement.getCreateBy(), true);
            addField(columns, values, "update_date", new Date(), false);
            addField(columns, values, "update_by", swmWarningManagement.getUpdateBy(), true);
            addField(columns, values, "status", swmWarningManagement.getStatus(), true);
            addField(columns, values, "remarks", swmWarningManagement.getRemarks(), true);
            addField(columns, values, "person_name", swmWarningManagement.getPersonName(), true);
            addField(columns, values, "warning_type", swmWarningManagement.getWarningType(), true);
            addField(columns, values, "warning_content", swmWarningManagement.getWarningContent(), true);
            addField(columns, values, "warning_time", swmWarningManagement.getWarningTime(), false);
            addField(columns, values, "alarm_record", swmWarningManagement.getAlarmRecord(), true);
            addField(columns, values, "alarm_time", swmWarningManagement.getAlarmTime(), false);
            addField(columns, values, "trigger_reason", swmWarningManagement.getTriggerReason(), true);
            addField(columns, values, "handler", swmWarningManagement.getHandler(), true);
            addField(columns, values, "handle_time", swmWarningManagement.getHandleTime(), false);
            addField(columns, values, "handle_process", swmWarningManagement.getHandleProcess(), true);
            addField(columns, values, "handle_status", swmWarningManagement.getHandleStatus(), true);
            addField(columns, values, "attachment", swmWarningManagement.getAttachment(), true);
            addField(columns, values, "device_id", swmWarningManagement.getDeviceId(), true);
            addField(columns, values, "id_card", swmWarningManagement.getIdCard(), true);
            addField(columns, values, "front_alarm", swmWarningManagement.getFrontAlarm(), true);
            addField(columns, values, "type", swmWarningManagement.getType(), true);
            addField(columns, values, "x", swmWarningManagement.getX(), true);
            addField(columns, values, "y", swmWarningManagement.getY(), true);

            // 构建SQL语句
            sql.append(String.join(",", columns))
                    .append(") VALUES (")
                    .append(String.join(",", values))
                    .append(")");

            // 执行插入
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql.toString());
            if (result.getCode() != R.SUCCESS) {
                logger.error("TDengine插入失败: {}", result.getMsg());
                // 如果TDengine插入失败，回退到原始保存
                super.save(swmWarningManagement);
            }
        } else {
            // 如果是更新，构建更新SQL
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(dbname).append(".swm_warning_management SET ");

            // 更新字段列表
            List<String> updates = new ArrayList<>();

            // 只更新非空字段
            if (swmWarningManagement.getUpdateBy() != null) {
                updates.add("update_by = '" + swmWarningManagement.getUpdateBy() + "'");
            }
            updates.add("update_date = " + System.currentTimeMillis());

            if (swmWarningManagement.getStatus() != null) {
                updates.add("status = '" + swmWarningManagement.getStatus() + "'");
            }

            if (swmWarningManagement.getRemarks() != null) {
                updates.add("remarks = '" + swmWarningManagement.getRemarks() + "'");
            }

            if (swmWarningManagement.getPersonName() != null) {
                updates.add("person_name = '" + swmWarningManagement.getPersonName() + "'");
            }

            if (swmWarningManagement.getWarningType() != null) {
                updates.add("warning_type = '" + swmWarningManagement.getWarningType() + "'");
            }

            if (swmWarningManagement.getWarningContent() != null) {
                updates.add("warning_content = '" + swmWarningManagement.getWarningContent() + "'");
            }

            if (swmWarningManagement.getWarningTime() != null) {
                updates.add("warning_time = " + swmWarningManagement.getWarningTime().getTime());
            }

            if (swmWarningManagement.getAlarmRecord() != null) {
                updates.add("alarm_record = '" + swmWarningManagement.getAlarmRecord() + "'");
            }

            if (swmWarningManagement.getAlarmTime() != null) {
                updates.add("alarm_time = " + swmWarningManagement.getAlarmTime().getTime());
            }

            if (swmWarningManagement.getTriggerReason() != null) {
                updates.add("trigger_reason = '" + swmWarningManagement.getTriggerReason() + "'");
            }

            if (swmWarningManagement.getHandler() != null) {
                updates.add("handler = '" + swmWarningManagement.getHandler() + "'");
            }

            if (swmWarningManagement.getHandleTime() != null) {
                updates.add("handle_time = " + swmWarningManagement.getHandleTime().getTime());
            }

            if (swmWarningManagement.getHandleProcess() != null) {
                updates.add("handle_process = '" + swmWarningManagement.getHandleProcess() + "'");
            }

            if (swmWarningManagement.getHandleStatus() != null) {
                updates.add("handle_status = '" + swmWarningManagement.getHandleStatus() + "'");
            }

            if (swmWarningManagement.getAttachment() != null) {
                updates.add("attachment = '" + swmWarningManagement.getAttachment() + "'");
            }

            if (swmWarningManagement.getDeviceId() != null) {
                updates.add("device_id = '" + swmWarningManagement.getDeviceId() + "'");
            }

            if (swmWarningManagement.getIdCard() != null) {
                updates.add("id_card = '" + swmWarningManagement.getIdCard() + "'");
            }

            if (swmWarningManagement.getFrontAlarm() != null) {
                updates.add("front_alarm = '" + swmWarningManagement.getFrontAlarm() + "'");
            }

            if (swmWarningManagement.getType() != null) {
                updates.add("type = '" + swmWarningManagement.getType() + "'");
            }

            if (swmWarningManagement.getX() != null) {
                updates.add("x = '" + swmWarningManagement.getX() + "'");
            }

            if (swmWarningManagement.getY() != null) {
                updates.add("y = '" + swmWarningManagement.getY() + "'");
            }

            // 完成SQL语句
            sql.append(String.join(",", updates))
                    .append(" WHERE id = '").append(swmWarningManagement.getId()).append("'");

            // 执行更新
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql.toString());
            if (result.getCode() != R.SUCCESS) {
                logger.error("TDengine更新失败: {}", result.getMsg());
                // 如果TDengine更新失败，回退到原始保存
                super.save(swmWarningManagement);
            }
        }
    }

    /**
     * 添加字段到SQL语句中
     */
    private void addField(List<String> columns, List<String> values, String column, Object value, boolean isString) {
        if (value != null) {
            columns.add(column);
            if (isString) {
                values.add("'" + value.toString() + "'");
            } else if (value instanceof Date) {
                values.add(String.valueOf(((Date) value).getTime()));
            } else {
                values.add(value.toString());
            }
        }
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmWarningManagement swmWarningManagement) {
        // 构建删除SQL
        String sql = String.format("DELETE FROM %s.swm_warning_management WHERE id='%s'",
                dbname, swmWarningManagement.getId());

        // 执行删除
        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() != R.SUCCESS) {
            logger.error("TDengine删除失败: {}", result.getMsg());
            // 如果TDengine删除失败，回退到原始删除
            super.delete(swmWarningManagement);
        }
    }

    /**
     * 处理预警并向MySQL插入或更新完整记录
     *
     * @param id            预警ID
     * @param handler       处置人
     * @param handleTime    处置时间
     * @param handleProcess 处置过程
     * @param handleStatus  处置状态
     * @param attachment    附件路径
     * @return 处理结果
     */
    @Transactional(readOnly = false)
    public boolean processWarningToMySql(String id, String handler, Date handleTime, String handleProcess,
            String handleStatus, String attachment) {
        logger.info("处理预警并向MySQL插入或更新完整记录，预警ID：{}", id);

        // 只查询时序数据库中的预警记录，不进行修改
        SwmWarningManagement swmWarningManagement = this.get(id);
        if (swmWarningManagement == null) {
            logger.error("预警记录不存在，ID：{}", id);
            return false;
        }

        try {
            // 先查询MySQL中是否已存在该记录
            SwmWarningManagement query = new SwmWarningManagement();
            query.setId(id);
            SwmWarningManagement existingRecord = super.get(query);

            if (existingRecord == null) {
                // MySQL中不存在，需要插入
                logger.info("MySQL中不存在该预警记录，准备插入新记录，ID：{}", id);

                // 创建一个新对象用于MySQL插入
                SwmWarningManagement mysqlWarning = new SwmWarningManagement();

                // 从时序数据库查询的记录中复制基础属性
                mysqlWarning.setId(swmWarningManagement.getId());
                mysqlWarning.setCreateBy(swmWarningManagement.getCreateBy());
                mysqlWarning.setCreateDate(swmWarningManagement.getCreateDate());
                mysqlWarning.setUpdateBy(swmWarningManagement.getUpdateBy());
                mysqlWarning.setUpdateDate(new Date()); // 使用当前时间作为更新时间
                mysqlWarning.setRemarks(swmWarningManagement.getRemarks());
                mysqlWarning.setStatus("0"); // 正常状态
                mysqlWarning.setPersonName(swmWarningManagement.getPersonName());
                mysqlWarning.setWarningType(swmWarningManagement.getWarningType());
                mysqlWarning.setWarningContent(swmWarningManagement.getWarningContent());
                mysqlWarning.setWarningTime(swmWarningManagement.getWarningTime());
                mysqlWarning.setAlarmRecord(swmWarningManagement.getAlarmRecord());
                mysqlWarning.setAlarmTime(swmWarningManagement.getAlarmTime());
                mysqlWarning.setTriggerReason(swmWarningManagement.getTriggerReason());
                mysqlWarning.setDeviceId(swmWarningManagement.getDeviceId());
                mysqlWarning.setIdCard(swmWarningManagement.getIdCard());
                mysqlWarning.setFrontAlarm(swmWarningManagement.getFrontAlarm());
                mysqlWarning.setType(swmWarningManagement.getType());
                mysqlWarning.setX(swmWarningManagement.getX());
                mysqlWarning.setY(swmWarningManagement.getY());
                mysqlWarning.setLocation(swmWarningManagement.getLocation());
                mysqlWarning.setArea(swmWarningManagement.getArea());

                // 设置前端传入的处置信息
                mysqlWarning.setHandler(handler);
                mysqlWarning.setHandleTime(handleTime);
                mysqlWarning.setHandleProcess(handleProcess);
                mysqlWarning.setHandleStatus(handleStatus);
                mysqlWarning.setAttachment(attachment);

                // 计算处置时长（处置时间减去报警时间，单位：分钟）
                if (handleStatus != null && handleStatus.equals("1") && handleTime != null
                        && mysqlWarning.getAlarmTime() != null) {
                    // 报警时间需要加上8小时再计算，因为存储的是UTC时间
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mysqlWarning.getAlarmTime());
                    calendar.add(Calendar.HOUR_OF_DAY, 8); // 直接加上8小时
                    Date adjustedAlarmTime = calendar.getTime();

                    long durationMillis = handleTime.getTime() - adjustedAlarmTime.getTime();
                    long durationMinutes = durationMillis / (60 * 1000);
                    mysqlWarning.setDisposalDuration(durationMinutes > 0 ? durationMinutes : 0);
                    logger.info("计算处置时长：报警时间 {} + 8小时调整为 {}，处置时间 {}，处置时长 {} 分钟",
                            mysqlWarning.getAlarmTime(), adjustedAlarmTime, handleTime, durationMinutes);
                } else {
                    mysqlWarning.setDisposalDuration(0L);
                }

                // 使用自定义方法直接向MySQL插入数据
                dao.insertToMySql(mysqlWarning);
                logger.info("成功向MySQL数据库插入预警处置记录，ID：{}", id);
            } else {
                // MySQL中已存在该记录，进行更新
                logger.info("MySQL中已存在该预警记录，准备更新记录，ID：{}", id);

                // 更新处置信息
                existingRecord.setHandler(handler);
                existingRecord.setHandleTime(handleTime);
                existingRecord.setHandleProcess(handleProcess);
                existingRecord.setHandleStatus(handleStatus);
                existingRecord.setUpdateDate(new Date()); // 更新时间

                // 计算处置时长（处置时间减去报警时间，单位：分钟）
                if (handleStatus != null && handleStatus.equals("1") && handleTime != null
                        && existingRecord.getAlarmTime() != null) {
                    // 报警时间需要加上8小时再计算，因为存储的是UTC时间
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(existingRecord.getAlarmTime());
                    calendar.add(Calendar.HOUR_OF_DAY, 8); // 直接加上8小时
                    Date adjustedAlarmTime = calendar.getTime();

                    long durationMillis = handleTime.getTime() - adjustedAlarmTime.getTime();
                    long durationMinutes = durationMillis / (60 * 1000);
                    existingRecord.setDisposalDuration(durationMinutes > 0 ? durationMinutes : 0);
                    logger.info("计算处置时长：报警时间 {} + 8小时调整为 {}，处置时间 {}，处置时长 {} 分钟",
                            existingRecord.getAlarmTime(), adjustedAlarmTime, handleTime, durationMinutes);
                } else {
                    existingRecord.setDisposalDuration(0L);
                }

                // 更新坐标信息
                if (swmWarningManagement.getX() != null) {
                    existingRecord.setX(swmWarningManagement.getX());
                }
                if (swmWarningManagement.getY() != null) {
                    existingRecord.setY(swmWarningManagement.getY());
                }

                // 更新位置和区域信息
                if (swmWarningManagement.getLocation() != null) {
                    existingRecord.setLocation(swmWarningManagement.getLocation());
                }
                if (swmWarningManagement.getArea() != null) {
                    existingRecord.setArea(swmWarningManagement.getArea());
                }

                // 如果附件不为空，则更新
                if (attachment != null && !attachment.isEmpty()) {
                    existingRecord.setAttachment(attachment);
                }

                // 使用父类的save方法更新记录
                super.save(existingRecord);
                logger.info("成功更新MySQL数据库中的预警处置记录，ID：{}", id);
            }

            return true;
        } catch (Exception e) {
            logger.error("向MySQL数据库插入或更新预警处置记录失败", e);
            return false;
        }
    }

    /**
     * 混合查询分页数据（从时序数据库和MySQL混合查询）
     *
     * @param page                 分页对象
     * @param swmWarningManagement 查询条件
     * @return 混合数据的分页结果
     */
    public Page<SwmWarningManagement> hybridFindPage(Page<SwmWarningManagement> page,
            SwmWarningManagement swmWarningManagement) {
        logger.info("开始混合查询分页数据...");

        // 检查是否是查询已处置数据(handleStatus=1)
        if (swmWarningManagement != null && "1".equals(swmWarningManagement.getHandleStatus())) {
            logger.info("查询已处置数据，直接从MySQL数据库查询");

            // 创建一个简单的分页查询
            Page<SwmWarningManagement> mysqlPage = new Page<>(page.getPageNo(), page.getPageSize());

            // 直接使用新增的DAO方法查询已处置数据
            List<SwmWarningManagement> mysqlList = dao.findProcessedInMySql(swmWarningManagement);

            // 设置显示文本值
            if (mysqlList != null) {
                for (SwmWarningManagement item : mysqlList) {
                    if (item.getWarningType() != null) {
                        // 修复warningType=1时显示为"主动报警"的问题
                        if ("1".equals(item.getWarningType())) {
                            item.setWarningTypeText("主动报警");
                        } else {
                            item.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", item.getWarningType(),
                                    item.getWarningType()));
                        }
                    }
                    if (item.getHandleStatus() != null) {
                        item.setHandleStatusText(DictUtils.getDictLabel("handle_status_enum", item.getHandleStatus(),
                                item.getHandleStatus()));
                    }
                }
            }

            // 手动计算总数和分页
            long total = mysqlList != null ? mysqlList.size() : 0;
            int fromIndex = (page.getPageNo() - 1) * page.getPageSize();
            int toIndex = Math.min(fromIndex + page.getPageSize(), mysqlList.size());

            // 得到当前页的数据子集
            List<SwmWarningManagement> pageList = fromIndex < toIndex ? mysqlList.subList(fromIndex, toIndex)
                    : new ArrayList<>();

            // 设置结果页和返回
            mysqlPage.setCount(total);
            mysqlPage.setList(pageList);
            logger.info("从MySQL查询到 {} 条已处置数据", total);

            return mysqlPage;
        }

        // 1. 首先从MySQL获取所有已处置的记录ID（无论是否符合查询条件）
        List<String> processedIds = dao.findAllProcessedIds();
        Set<String> processedIdSet = new HashSet<>(processedIds);
        logger.info("从MySQL获取到 {} 条已处置记录ID", processedIdSet.size());

        // 2. 从时序数据库获取数据（非已处置数据的查询）
        Page<SwmWarningManagement> tdEnginePage = this.findPage(page, swmWarningManagement);
        List<SwmWarningManagement> tdEngineList = tdEnginePage.getList();

        if (tdEngineList == null || tdEngineList.isEmpty()) {
            logger.info("时序数据库查询结果为空，直接返回空结果");
            return tdEnginePage;
        }

        // 3. 过滤掉时序数据库中已经在MySQL中标记为已处置的记录，防止重复
        List<SwmWarningManagement> filteredTdEngineList = new ArrayList<>();
        List<String> needMySqlIds = new ArrayList<>();

        for (SwmWarningManagement item : tdEngineList) {
            if (processedIdSet.contains(item.getId())) {
                // 这条记录在MySQL中已标记为处置过，我们需要查询MySQL获取处置后的数据
                needMySqlIds.add(item.getId());
            } else {
                // 这条记录在MySQL中没有，保留时序数据库的数据
                filteredTdEngineList.add(item);
            }
        }

        // 4. 从MySQL中查询已处置的记录
        List<SwmWarningManagement> mysqlList = new ArrayList<>();
        if (!needMySqlIds.isEmpty()) {
            mysqlList = dao.findInMySqlByIds(needMySqlIds);
        }

        // 5. 合并结果，去重并确保正确排序
        Map<String, SwmWarningManagement> resultMap = new HashMap<>();

        // 先添加时序数据库中未处置的数据
        for (SwmWarningManagement item : filteredTdEngineList) {
            // 检查是否符合handleStatus查询条件
            if (isMatchHandleStatus(item, swmWarningManagement.getHandleStatus())) {
                resultMap.put(item.getId(), item);
            }
        }

        // 再添加MySQL中已处置的数据
        for (SwmWarningManagement item : mysqlList) {
            // 检查是否符合handleStatus查询条件
            if (isMatchHandleStatus(item, swmWarningManagement.getHandleStatus())) {
                // 设置显示文本值
                if (item.getWarningType() != null) {
                    // 修复warningType=1时显示为"主动报警"的问题
                    if ("1".equals(item.getWarningType())) {
                        item.setWarningTypeText("主动报警");
                    } else {
                        item.setWarningTypeText(
                                DictUtils.getDictLabel("warning_type_enum", item.getWarningType(),
                                        item.getWarningType()));
                    }
                }
                if (item.getHandleStatus() != null) {
                    item.setHandleStatusText(
                            DictUtils.getDictLabel("handle_status_enum", item.getHandleStatus(),
                                    item.getHandleStatus()));
                }
                // 覆盖原有记录（如果有的话）
                resultMap.put(item.getId(), item);
            }
        }

        // 6. 恢复原始排序
        List<SwmWarningManagement> resultList = new ArrayList<>();
        for (SwmWarningManagement item : tdEngineList) {
            SwmWarningManagement resultItem = resultMap.get(item.getId());
            if (resultItem != null) {
                resultList.add(resultItem);
            }
        }

        // 7. 设置结果列表并返回
        tdEnginePage.setList(resultList);
        logger.info("混合查询完成，返回 {} 条记录", resultList.size());
        return tdEnginePage;
    }

    /**
     * 检查记录的处置状态是否匹配查询条件
     *
     * @param item              预警记录
     * @param queryHandleStatus 查询的处置状态条件
     * @return 是否匹配
     */
    private boolean isMatchHandleStatus(SwmWarningManagement item, String queryHandleStatus) {
        // 如果没有查询条件，则匹配所有记录
        if (queryHandleStatus == null || queryHandleStatus.isEmpty()) {
            return true;
        }

        // 获取记录的处置状态
        String itemHandleStatus = item.getHandleStatus();

        // 如果记录没有处置状态，默认为未处置(0)
        if (itemHandleStatus == null || itemHandleStatus.isEmpty()) {
            itemHandleStatus = "0";
        }

        // 比较处置状态
        return queryHandleStatus.equals(itemHandleStatus);
    }

    /**
     * 获取近七天预警数据
     * 
     * @return
     */
    public List<SwmWarningManagement> listPast7DaysWarning() {
        return dao.listPast7DaysWarning();
    }

    /**
     * 获取近七天预警数据（分页）
     * 
     * @param swmWarningManagement 查询条件
     * @param page                 分页参数
     * @return 分页结果
     */
    public Page<SwmWarningManagement> findPast7DaysWarningPage(SwmWarningManagement swmWarningManagement,
            Page<SwmWarningManagement> page) {
        // 设置基础查询条件
        if (swmWarningManagement == null) {
            swmWarningManagement = new SwmWarningManagement();
        }

        // 设置查询近7天的条件
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6); // 7天前（包含今天）
        Date startDate = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // 明天
        Date endDate = calendar.getTime();

        // 使用sqlMap设置日期范围
        swmWarningManagement.getSqlMap().getWhere()
                .and("warning_time", QueryType.GTE, startDate)
                .and("warning_time", QueryType.LT, endDate);

        swmWarningManagement.setStatus("0"); // 状态为0的记录

        // 调用混合分页查询方法
        return hybridFindPage(page, swmWarningManagement);
    }

    /**
     * 获取今日的预警记录
     */
    public List<SwmWarningManagement> listTodayWarning() {
        return dao.listTodayWarning();
    }

    /**
     * 获取今日已处置的预警记录
     */
    public List<SwmWarningManagement> listTodayHandledWarning() {
        // 查询所有今日预警
        List<SwmWarningManagement> allTodayWarnings = listTodayWarning();

        // 过滤已处置的预警（handleStatus为1表示已处置）
        return allTodayWarnings.stream()
                .filter(warning -> "1".equals(warning.getHandleStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 获取当月的预警记录
     */
    public List<SwmWarningManagement> listCurrentMonthWarning() {
        return dao.listCurrentMonthWarning();
    }

    /**
     * 获取当月已处置的预警记录
     */
    public List<SwmWarningManagement> listCurrentMonthHandledWarning() {
        return dao.listCurrentMonthHandledWarning();
    }

    /**
     * 为预警记录列表填充班组信息
     * 通过身份证号从fms_worker和fms_work_group表中获取班组名称
     *
     * @param warningList 预警记录列表
     */
    public void fillWorkGroupInfo(List<SwmWarningManagement> warningList) {
        if (warningList == null || warningList.isEmpty()) {
            return;
        }

        // 收集所有不为空的身份证号
        List<String> idCards = warningList.stream()
                .filter(w -> w.getIdCard() != null && !w.getIdCard().isEmpty())
                .map(SwmWarningManagement::getIdCard)
                .distinct()
                .collect(Collectors.toList());

        if (idCards.isEmpty()) {
            return;
        }

        try {
            // 批量查询身份证号对应的班组信息
            Map<String, String> idCardToWorkGroupMap = new HashMap<>();
            List<Map<String, String>> workGroupInfoList = dao.findWorkGroupNamesByIdCards(idCards);

            // 将查询结果转换为idCard -> workGroupName的映射
            for (Map<String, String> map : workGroupInfoList) {
                if (map.containsKey("idCard") && map.containsKey("value")) {
                    idCardToWorkGroupMap.put(map.get("idCard"), map.get("value"));
                }
            }

            // 将班组信息填充到预警记录中
            for (SwmWarningManagement warning : warningList) {
                if (warning.getIdCard() != null && idCardToWorkGroupMap.containsKey(warning.getIdCard())) {
                    warning.setWorkGroupName(idCardToWorkGroupMap.get(warning.getIdCard()));
                }
            }
        } catch (Exception e) {
            logger.error("填充班组信息失败", e);
        }
    }

    /**
     * 为预警记录列表填充位置信息
     * 从TDengine的area_fence_data表中查询位置信息
     *
     * @param warningList 预警记录列表
     */
    public void fillLocationInfo(List<SwmWarningManagement> warningList) {
        if (warningList == null || warningList.isEmpty()) {
            return;
        }

        try {
            // 收集所有有效的设备ID和身份证号
            List<Map<String, Object>> queryParams = new ArrayList<>();
            for (SwmWarningManagement warning : warningList) {
                String deviceId = warning.getDeviceId();
                String idCard = warning.getIdCard();
                Date warningTime = warning.getWarningTime();

                if (deviceId != null && !deviceId.isEmpty() && warningTime != null) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("deviceId", deviceId);
                    param.put("idCard", idCard);
                    param.put("warningTime", warningTime);
                    param.put("tenMinutesBefore", warningTime.getTime() - 10 * 60 * 1000);
                    param.put("tenMinutesAfter", warningTime.getTime() + 10 * 60 * 1000);
                    queryParams.add(param);
                }
            }

            if (queryParams.isEmpty()) {
                return;
            }

            // 构建批量查询SQL
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT device_id, id_card, area_name, time FROM ")
                    .append(dbname).append(".area_fence_data")
                    .append(" WHERE (");

            // 添加每个设备ID和时间范围的条件
            for (int i = 0; i < queryParams.size(); i++) {
                Map<String, Object> param = queryParams.get(i);
                String deviceId = (String) param.get("deviceId");
                String idCard = (String) param.get("idCard");
                long tenMinutesBefore = (long) param.get("tenMinutesBefore");
                long tenMinutesAfter = (long) param.get("tenMinutesAfter");

                if (i > 0) {
                    sqlBuilder.append(" OR ");
                }

                sqlBuilder.append("(device_id = '").append(deviceId).append("'");

                // 如果有身份证号，也加入条件
                if (idCard != null && !idCard.isEmpty()) {
                    sqlBuilder.append(" AND id_card = '").append(idCard).append("'");
                }

                sqlBuilder.append(" AND time >= ").append(tenMinutesBefore)
                        .append(" AND time <= ").append(tenMinutesAfter).append(")");
            }

            sqlBuilder.append(") ORDER BY device_id, id_card, time DESC");

            // 执行批量查询
            R<JSONObject> result = tdengineService.executeTDengineSQL(sqlBuilder.toString());

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONObject data = result.getData();
                JSONArray rows = data.getJSONArray("data");
                JSONArray columnMeta = data.getJSONArray("column_meta");

                if (rows != null && rows.size() > 0) {
                    // 提取列索引
                    int deviceIdIdx = -1;
                    int idCardIdx = -1;
                    int areaNameIdx = -1;
                    int timeIdx = -1;

                    for (int i = 0; i < columnMeta.size(); i++) {
                        JSONArray column = columnMeta.getJSONArray(i);
                        if (column != null && column.size() > 0) {
                            String columnName = column.getStr(0);
                            if ("device_id".equals(columnName)) {
                                deviceIdIdx = i;
                            } else if ("id_card".equals(columnName)) {
                                idCardIdx = i;
                            } else if ("area_name".equals(columnName)) {
                                areaNameIdx = i;
                            } else if ("time".equals(columnName)) {
                                timeIdx = i;
                            }
                        }
                    }

                    // 创建设备ID和身份证号到位置信息的映射
                    Map<String, Map<String, String>> deviceLocationMap = new HashMap<>();

                    // 处理查询结果
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() > 0 && deviceIdIdx >= 0 && areaNameIdx >= 0) {
                            String deviceId = row.getStr(deviceIdIdx);
                            String idCard = idCardIdx >= 0 ? row.getStr(idCardIdx) : null;
                            String areaName = row.getStr(areaNameIdx);

                            if (deviceId != null && areaName != null) {
                                String key = deviceId + ":" + (idCard != null ? idCard : "");

                                // 只保存每个设备ID和身份证号组合的第一条记录（最新的）
                                if (!deviceLocationMap.containsKey(key)) {
                                    Map<String, String> locationInfo = new HashMap<>();
                                    locationInfo.put("areaName", areaName);
                                    deviceLocationMap.put(key, locationInfo);
                                }
                            }
                        }
                    }

                    // 填充位置信息到预警记录
                    for (SwmWarningManagement warning : warningList) {
                        String deviceId = warning.getDeviceId();
                        String idCard = warning.getIdCard();

                        if (deviceId != null) {
                            String key = deviceId + ":" + (idCard != null ? idCard : "");
                            Map<String, String> locationInfo = deviceLocationMap.get(key);

                            if (locationInfo != null) {
                                String areaName = locationInfo.get("areaName");
                                if (areaName != null && !areaName.isEmpty()) {
                                    warning.setAreaName(areaName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("填充位置信息失败", e);
        }
    }

    /**
     * 获取今日最新的20条预警数据，使用混合查询逻辑（TDengine + MySQL）
     * 
     * @return 今日最新的20条预警数据
     */
    public List<SwmWarningManagement> findTodayWarningWithHybrid() {
        logger.info("开始混合查询今日最新20条预警数据...");

        // 创建一个简单的分页对象，设置为第1页，每页20条
        Page<SwmWarningManagement> page = new Page<>(1, 20);

        // 创建查询条件，设置为今天的日期范围
        SwmWarningManagement swmWarningManagement = new SwmWarningManagement();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date endDate = calendar.getTime();

        // 设置查询今日的条件
        swmWarningManagement.getSqlMap().getWhere()
                .and("warning_time", QueryType.GTE, startDate)
                .and("warning_time", QueryType.LT, endDate);

        swmWarningManagement.setStatus("0"); // 状态为0的记录
        
        // 设置排除考勤打卡和进入大门的记录
        swmWarningManagement.setExcludeAttendance(true);
        swmWarningManagement.setExcludeGateEntry(true);

        // 调用混合查询方法
        Page<SwmWarningManagement> resultPage = hybridFindPage(page, swmWarningManagement);

        // 返回结果列表
        List<SwmWarningManagement> resultList = resultPage.getList();
        logger.info("混合查询今日预警数据完成，返回 {} 条记录", resultList.size());

        return resultList;
    }

    /**
     * 获取需要前端弹框显示的告警数据
     * 条件：
     * 1. 时序数据库中front_alarm=1，且在MySQL中不存在相同id和id_card的记录
     * 2. 如果MySQL中存在记录且front_alarm=0，则不需要告警
     * 3. 根据时序数据库中的type字段与alarm_config表的alarm_key匹配，判断是否需要告警和弹框确认
     *
     * 告警分类规则：
     * - 如果是否报警(enableAlarm)为0，则不显示任何通知
     * - 如果是否报警为1，是否弹窗确认(needConfirm)为0，则只在消息弹窗显示，自动确认
     * - 如果是否报警为1，是否弹窗确认为1，则在告警通知和消息弹窗都显示
     *
     * @return 包含两个列表：confirmList(需要确认的告警)和notificationList(只需通知的告警)
     */
    @Autowired
    private SwmAlarmConfigDao alarmConfigDao;

    // 添加线程池用于异步确认告警
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Map<String, Object> getPopupWarnings() {
        List<SwmWarningManagement> confirmList = new ArrayList<>(); // 需要弹框确认的告警
        List<SwmWarningManagement> notificationList = new ArrayList<>(); // 需要消息提示但不需要确认的告警
        Map<String, Object> result = new HashMap<>();

        try {
            // 0. 获取所有启用的告警配置
            List<SwmAlarmConfig> allConfigs = alarmConfigDao.findAllEnabled();
            Map<String, SwmAlarmConfig> configMap = new HashMap<>();
            for (SwmAlarmConfig config : allConfigs) {
                configMap.put(config.getAlarmKey(), config);
            }

            // 获取所有需要弹窗确认的告警配置
            List<SwmAlarmConfig> needConfirmConfigs = alarmConfigDao.findAllNeedConfirm();
            Set<String> needConfirmKeys = new HashSet<>();
            for (SwmAlarmConfig config : needConfirmConfigs) {
                needConfirmKeys.add(config.getAlarmKey());
            }

            logger.info("获取到{}个启用的告警配置，其中{}个需要弹窗确认",
                    allConfigs.size(), needConfirmKeys.size());

            // 1. 查询时序数据库中front_alarm=1的记录
            String sql = String.format(
                    "SELECT * FROM %s.swm_warning_management WHERE front_alarm='1' ORDER BY warning_time DESC LIMIT 20",
                    dbname);
            R<JSONObject> tdResult = tdengineService.executeTDengineSQL(sql);

            if (tdResult.getCode() == R.SUCCESS && tdResult.getData() != null) {
                JSONObject data = tdResult.getData();
                JSONArray rows = data.getJSONArray("data");
                JSONArray columnMeta = data.getJSONArray("column_meta");

                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        try {
                            JSONArray row = rows.getJSONArray(i);
                            SwmWarningManagement tdEntity = convertToEntity(row, columnMeta);
                            if (tdEntity == null || tdEntity.getId() == null) {
                                continue;
                            }

                            // 手动对预警时间加8小时
                            if (tdEntity.getWarningTime() != null) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(tdEntity.getWarningTime());
                                calendar.add(Calendar.HOUR_OF_DAY, 8);
                                tdEntity.setWarningTime(calendar.getTime());
                            }

                            // 2. 在MySQL中检查是否存在相同ID和身份证号的记录
                            SwmWarningManagement query = new SwmWarningManagement();
                            query.setId(tdEntity.getId());
                            if (tdEntity.getIdCard() != null) {
                                query.setIdCard(tdEntity.getIdCard());
                            }
                            SwmWarningManagement mysqlEntity = super.get(query);

                            // 如果MySQL中存在且front_alarm=0，则不需要告警
                            if (mysqlEntity != null && "0".equals(mysqlEntity.getFrontAlarm())) {
                                continue;
                            }

                            // 3. 根据预警类型检查告警配置
                            // 时序数据库swm_warning_management的type字段与mysql的swm_alarm_config表的alarm_key匹配
                            String warningType = tdEntity.getType();
                            SwmAlarmConfig alarmConfig = configMap.get(warningType);

                            // 如果没有对应的配置或配置为不告警，则跳过
                            if (alarmConfig == null || alarmConfig.getEnableAlarm() == 0) {
                                logger.info("警告类型 {} 未配置或配置为不告警，跳过", warningType);
                                continue;
                            }

                            // 添加配置信息到实体
                            tdEntity.setExtraData("alarmConfig", alarmConfig);

                            // 4. 根据配置分类处理
                            // 如果是否报警为1，是否弹窗确认为1，则添加到confirmList和notificationList
                            if (alarmConfig.getEnableAlarm() == 1 && alarmConfig.getNeedConfirm() == 1) {
                                // 添加到需要确认的列表
                                if (confirmList.size() < 5) { // 最多5条需要确认的告警
                                    confirmList.add(tdEntity);
                                }

                                // 同时也添加到通知列表，通知列表不自动确认
                                if (notificationList.size() < 10) {
                                    // 深拷贝一份，避免共享引用
                                    SwmWarningManagement notificationEntity = new SwmWarningManagement();
                                    BeanUtils.copyProperties(tdEntity, notificationEntity);
                                    notificationEntity.setExtraData("alarmConfig", alarmConfig);
                                    notificationEntity.setExtraData("needConfirm", true);
                                    notificationList.add(notificationEntity);
                                }
                            }
                            // 如果是否报警为1，是否弹窗确认为0，则只添加到notificationList
                            else if (alarmConfig.getEnableAlarm() == 1 && alarmConfig.getNeedConfirm() == 0) {
                                // 只添加到通知列表，并自动确认
                                if (notificationList.size() < 10) { // 最多10条通知
                                    tdEntity.setExtraData("needConfirm", false);
                                    notificationList.add(tdEntity);

                                    // 异步自动确认，使用线程池
                                    final String warningId = tdEntity.getId();
                                    executorService.submit(() -> {
                                        try {
                                            // 在新的线程中调用确认方法
                                            confirmWarning(warningId);
                                            logger.info("异步确认告警成功: {}", warningId);
                                        } catch (Exception e) {
                                            logger.error("异步确认告警失败: {}, 错误: {}", warningId, e.getMessage(), e);
                                        }
                                    });
                                }
                            }
                            // 如果是否报警为0，则两个列表都不添加
                        } catch (Exception e) {
                            logger.error("处理告警数据异常: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取告警数据失败", e);
        }

        result.put("confirmList", confirmList);
        result.put("notificationList", notificationList);

        logger.info("获取告警数据完成，需要确认的告警: {}条，只需通知的告警: {}条",
                confirmList.size(), notificationList.size());

        return result;
    }

    /**
     * 处理告警确认
     * 如果MySQL中不存在该记录，则插入；如果存在，则更新front_alarm为0
     * 
     * @param id 告警ID
     * @return 处理结果
     */
    @Transactional(readOnly = false)
    public boolean confirmWarning(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("告警ID为空，无法处理确认");
            return false;
        }

        try {
            // 1. 从时序数据库获取完整记录
            String sql = String.format("SELECT * FROM %s.swm_warning_management WHERE id='%s' LIMIT 1", dbname, id);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() != R.SUCCESS || result.getData() == null) {
                logger.error("从时序数据库获取告警数据失败，ID: {}", id);
                return false;
            }

            JSONObject data = result.getData();
            JSONArray rows = data.getJSONArray("data");
            if (rows == null || rows.size() == 0) {
                logger.error("告警记录不存在，ID: {}", id);
                return false;
            }

            // 2. 转换为实体
            SwmWarningManagement entity = convertToEntity(rows.getJSONArray(0), data.getJSONArray("column_meta"));
            if (entity == null) {
                logger.error("告警数据转换失败，ID: {}", id);
                return false;
            }

            // 3. 查询MySQL中是否存在
            SwmWarningManagement query = new SwmWarningManagement();
            query.setId(id);
            SwmWarningManagement mysqlEntity = super.get(query);

            // 4. 根据查询结果决定插入或更新
            if (mysqlEntity == null) {
                // MySQL中不存在，需要插入
                entity.setFrontAlarm("0"); // 设置为已确认
                // 使用MyBatis的insert方法而不是save方法
                dao.insert(entity);
                logger.info("告警确认：向MySQL插入新记录, ID: {}", id);
            } else {
                // MySQL中已存在，更新front_alarm字段
                mysqlEntity.setFrontAlarm("0");
                // 更新x和y坐标字段
                mysqlEntity.setX(entity.getX());
                mysqlEntity.setY(entity.getY());
                // 更新location和area字段
                mysqlEntity.setLocation(entity.getLocation());
                mysqlEntity.setArea(entity.getArea());
                super.save(mysqlEntity);
                logger.info("告警确认：更新MySQL记录front_alarm=0, x={}, y={}, location={}, area={}, ID: {}",
                        entity.getX(), entity.getY(), entity.getLocation(), entity.getArea(), id);
            }

            return true;
        } catch (Exception e) {
            logger.error("处理告警确认失败", e);
            return false;
        }
    }

    /**
     * 查询未处置的预警记录
     * 逻辑：
     * 1. 从时序数据库查询所有数据
     * 2. 从MySQL筛选相同id和id_card且handle_status为1的记录（已处置的记录）
     * 3. 将时序数据库查到的所有数据减去MySQL中已处置的记录，得到未处置的数据
     * 4. 排除危险源报警类型的数据
     *
     * @param keyword 搜索关键词（可选）
     * @return 未处置的预警记录列表
     */
    public List<SwmWarningManagement> findUnhandledWarnings(String keyword) {
        logger.info("开始查询未处置预警记录，关键词: {}", keyword);

        // 1. 从时序数据库查询所有数据
        StringBuilder sqlBuilder = new StringBuilder();
        // 在SQL中使用TIMEDIFF函数添加8小时(28800000ms)到时间字段，保证时区正确
        sqlBuilder.append("SELECT id, person_name, warning_type, warning_content, ")
                .append("CAST(warning_time + 28800000 AS TIMESTAMP) as warning_time, ")
                .append("alarm_record, CAST(alarm_time + 28800000 AS TIMESTAMP) as alarm_time, ")
                .append("trigger_reason, handler, handle_time, handle_process, handle_status, attachment, ")
                .append("create_by, create_date, update_by, update_date, remarks, status, device_id, id_card, ")
                .append("front_alarm, type, x, y, hazard_category, location, area ")
                .append("FROM ").append(dbname).append(".swm_warning_management");

        // 添加排序条件
        sqlBuilder.append(" ORDER BY warning_time DESC");

        // 执行查询，获取时序数据库所有记录
        List<SwmWarningManagement> tdEngineList = new ArrayList<>();
        try {
            R<JSONObject> result = tdengineService.executeTDengineSQL(sqlBuilder.toString());
            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONObject data = result.getData();
                JSONArray rows = data.getJSONArray("data");
                JSONArray columnMeta = data.getJSONArray("column_meta");

                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        try {
                            JSONArray row = rows.getJSONArray(i);
                            SwmWarningManagement entity = convertToEntity(row, columnMeta);
                            if (entity != null) {
                                tdEngineList.add(entity);
                            }
                        } catch (Exception e) {
                            logger.error("转换行数据异常: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("从时序数据库查询数据失败: {}", e.getMessage());
        }

        logger.info("从时序数据库获取到 {} 条记录", tdEngineList.size());

        // 2. 从MySQL获取所有已处置的记录（handle_status为1）的ID和idCard
        List<SwmWarningManagement> processedList = dao.findAllProcessedWarnings();
        logger.info("从MySQL获取到 {} 条已处置记录", processedList.size());

        // 创建已处置记录的映射，用于快速查找
        Map<String, Set<String>> processedMap = new HashMap<>();
        for (SwmWarningManagement processed : processedList) {
            String id = processed.getId();
            String idCard = processed.getIdCard();

            if (id != null) {
                if (!processedMap.containsKey(id)) {
                    processedMap.put(id, new HashSet<>());
                }

                if (idCard != null) {
                    processedMap.get(id).add(idCard);
                } else {
                    // 如果idCard为空，使用特殊标记
                    processedMap.get(id).add("NULL");
                }
            }
        }

        // 3. 过滤时序数据库记录，只保留未处置的记录并排除危险源报警
        List<SwmWarningManagement> unhandledList = new ArrayList<>();
        for (SwmWarningManagement tdEntity : tdEngineList) {
            String id = tdEntity.getId();
            String idCard = tdEntity.getIdCard();
            String warningContent = tdEntity.getWarningContent();

            // 排除危险源报警
            if ("危险源报警".equals(warningContent)) {
                continue;
            }

            boolean isProcessed = false;
            if (processedMap.containsKey(id)) {
                Set<String> processedIdCards = processedMap.get(id);
                if (idCard != null) {
                    if (processedIdCards.contains(idCard)) {
                        isProcessed = true;
                    }
                } else if (processedIdCards.contains("NULL")) {
                    isProcessed = true;
                }
            }

            // 只添加未处置的记录
            if (!isProcessed) {
                // 设置未处置状态
                tdEntity.setHandleStatus(SwmWarningManagement.HandleStatusEnum.UNHANDLED);
                unhandledList.add(tdEntity);
            }
        }

        // 4. 根据关键词过滤结果（如果提供了关键词）
        List<SwmWarningManagement> filteredList = unhandledList;
        if (StringUtils.isNotBlank(keyword)) {
            filteredList = new ArrayList<>();
            String lowerKeyword = keyword.toLowerCase();

            for (SwmWarningManagement warning : unhandledList) {
                boolean matches = false;

                // 检查人员姓名
                if (warning.getPersonName() != null &&
                        warning.getPersonName().toLowerCase().contains(lowerKeyword)) {
                    matches = true;
                }

                // 检查身份证号
                if (!matches && warning.getIdCard() != null &&
                        warning.getIdCard().toLowerCase().contains(lowerKeyword)) {
                    matches = true;
                }

                // 检查预警内容
                String warningContent = warning.getWarningContent();
                if (!matches && warningContent != null) {
                    // 如果预警内容是数字，尝试转换为文本形式
                    if (warningContent.matches("\\d+")) {
                        warningContent = DictUtils.getDictLabel("warning_content_enum",
                                warningContent, warningContent);
                    }

                    if (warningContent.toLowerCase().contains(lowerKeyword)) {
                        matches = true;
                    }
                }

                if (matches) {
                    filteredList.add(warning);
                }
            }
        }

        logger.info("过滤后得到 {} 条未处置预警记录", filteredList.size());

        return filteredList;
    }

    /**
     * 在Spring容器销毁时关闭线程池
     */
    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
