package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmAlarmConfigDao;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.sys.utils.DictUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.utils.R;
import org.springframework.beans.BeanUtils;
import javax.annotation.PreDestroy;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            String sql = String.format("SELECT * FROM %s.swm_warning_management WHERE id='%s' LIMIT 1", 
                dbname, swmWarningManagement.getId());
            
            try {
                R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
                if (result.getCode() == R.SUCCESS && result.getData() != null) {
                    JSONObject data = result.getData();
                    JSONArray rows = data.getJSONArray("data");
                    
                    if (rows != null && rows.size() > 0) {
                        return convertToEntity(rows.getJSONArray(0), data.getJSONArray("column_meta"));
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
               .append("create_by, create_date, update_by, update_date, remarks, status, device_id, id_card ")
               .append("FROM ").append(dbname).append(".swm_warning_management");
        
        // 添加查询条件
        List<String> conditions = new ArrayList<>();
        
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
                            // 保持原始时间，不做时区调整，统一由Controller处理
                            Date warningTime = new Date(row.getLong(i));
                            entity.setWarningTime(warningTime);
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
                            // 保持原始时间，不做时区调整，统一由Controller处理
                            Date alarmTime = new Date(row.getLong(i));
                            entity.setAlarmTime(alarmTime);
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
                }
            }
            
            // 设置显示文本值
            if (entity.getWarningType() != null) {
                // 修复warningType=1时显示为"主动报警"的问题
                if ("1".equals(entity.getWarningType())) {
                    entity.setWarningTypeText("主动报警");
                } else {
                entity.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", entity.getWarningType(), entity.getWarningType()));
                }
            }
            
            if (entity.getHandleStatus() != null) {
                entity.setHandleStatusText(DictUtils.getDictLabel("handle_status_enum", entity.getHandleStatus(), entity.getHandleStatus()));
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
     * @param id 预警ID
     * @param handler 处置人
     * @param handleTime 处置时间
     * @param handleProcess 处置过程
     * @param handleStatus 处置状态
     * @param attachment 附件路径
     * @return 处理结果
     */
    @Transactional(readOnly = false)
    public boolean processWarningToMySql(String id, String handler, Date handleTime, String handleProcess, String handleStatus, String attachment) {
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
        
        // 设置前端传入的处置信息
        mysqlWarning.setHandler(handler);
        mysqlWarning.setHandleTime(handleTime);
        mysqlWarning.setHandleProcess(handleProcess);
        mysqlWarning.setHandleStatus(handleStatus);
        mysqlWarning.setAttachment(attachment);
        
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
     * @param page 分页对象
     * @param swmWarningManagement 查询条件
     * @return 混合数据的分页结果
     */
    public Page<SwmWarningManagement> hybridFindPage(Page<SwmWarningManagement> page, SwmWarningManagement swmWarningManagement) {
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
                        item.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), item.getWarningType()));
                        }
                    }
                    if (item.getHandleStatus() != null) {
                        item.setHandleStatusText(DictUtils.getDictLabel("handle_status_enum", item.getHandleStatus(), item.getHandleStatus()));
                    }
                }
            }
            
            // 手动计算总数和分页
            long total = mysqlList != null ? mysqlList.size() : 0;
            int fromIndex = (page.getPageNo() - 1) * page.getPageSize();
            int toIndex = Math.min(fromIndex + page.getPageSize(), mysqlList.size());
            
            // 得到当前页的数据子集
            List<SwmWarningManagement> pageList = fromIndex < toIndex ? 
                    mysqlList.subList(fromIndex, toIndex) : new ArrayList<>();
                    
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
            resultMap.put(item.getId(), item);
        }
        
        // 再添加MySQL中已处置的数据
        for (SwmWarningManagement item : mysqlList) {
            // 设置显示文本值
            if (item.getWarningType() != null) {
                // 修复warningType=1时显示为"主动报警"的问题
                if ("1".equals(item.getWarningType())) {
                    item.setWarningTypeText("主动报警");
                } else {
                item.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), item.getWarningType()));
                }
            }
            if (item.getHandleStatus() != null) {
                item.setHandleStatusText(DictUtils.getDictLabel("handle_status_enum", item.getHandleStatus(), item.getHandleStatus()));
            }
            // 覆盖原有记录（如果有的话）
            resultMap.put(item.getId(), item);
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
     * 获取近七天预警数据
     * @return
     */
    public List<SwmWarningManagement> listPast7DaysWarning() {
        return dao.listPast7DaysWarning();
    }

    /**
     * 获取今日预警数据
     * @return
     */
    public List<SwmWarningManagement> listTodayWarning() {
        return dao.listTodayWarning();
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
            String sql = String.format("SELECT * FROM %s.swm_warning_management WHERE front_alarm='1' ORDER BY warning_time DESC LIMIT 20", dbname);
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
                                if (confirmList.size() < 5) {  // 最多5条需要确认的告警
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
                                if (notificationList.size() < 10) {  // 最多10条通知
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
                super.save(mysqlEntity);
                logger.info("告警确认：更新MySQL记录front_alarm=0, ID: {}", id);
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
               .append("create_by, create_date, update_by, update_date, remarks, status, device_id, id_card ")
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
