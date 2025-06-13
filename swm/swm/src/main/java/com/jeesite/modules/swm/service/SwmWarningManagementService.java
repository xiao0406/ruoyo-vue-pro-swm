package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.sys.utils.DictUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.modules.utils.R;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
        sqlBuilder.append("SELECT * FROM ").append(dbname).append(".swm_warning_management");
        
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
                            // 移除8小时时区调整
                            Date warningTime = new Date(row.getLong(i));
                            entity.setWarningTime(warningTime);
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date parsedTime = sdf.parse(timeStr);
                                    // 移除8小时时区调整
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
                            // 移除8小时时区调整
                            Date alarmTime = new Date(row.getLong(i));
                            entity.setAlarmTime(alarmTime);
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date parsedTime = sdf.parse(timeStr);
                                    // 移除8小时时区调整
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
                }
            }
            
            // 设置显示文本值
            if (entity.getWarningType() != null) {
                entity.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", entity.getWarningType(), entity.getWarningType()));
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
     * 处理预警并向MySQL插入完整记录（仅查询时序数据库，不修改）
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
        logger.info("处理预警并向MySQL插入完整记录，预警ID：{}", id);
        
        // 只查询时序数据库中的预警记录，不进行修改
        SwmWarningManagement swmWarningManagement = this.get(id);
        if (swmWarningManagement == null) {
            logger.error("预警记录不存在，ID：{}", id);
            return false;
        }
        
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
        
        try {
            // 使用自定义方法直接向MySQL插入数据
            dao.insertToMySql(mysqlWarning);
            logger.info("成功向MySQL数据库插入预警处置记录，ID：{}", id);
            return true;
        } catch (Exception e) {
            logger.error("向MySQL数据库插入预警处置记录失败", e);
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
                        item.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), item.getWarningType()));
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
                item.setWarningTypeText(DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), item.getWarningType()));
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
}
