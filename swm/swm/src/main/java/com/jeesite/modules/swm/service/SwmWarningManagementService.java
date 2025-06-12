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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
                            entity.setWarningTime(new Date(row.getLong(i)));
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    entity.setWarningTime(sdf.parse(timeStr));
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
                            entity.setAlarmTime(new Date(row.getLong(i)));
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    entity.setAlarmTime(sdf.parse(timeStr));
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
                                entity.setHandleTime(new Date(row.getLong(i)));
                            } catch (Exception e) {
                                // 如果转换失败，尝试作为字符串解析
                                try {
                                    String timeStr = row.getStr(i);
                                    if (timeStr != null && !timeStr.isEmpty()) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        entity.setHandleTime(sdf.parse(timeStr));
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
                            entity.setCreateDate(new Date(row.getLong(i)));
                        } catch (Exception e) {
                            // 如果转换失败，尝试作为字符串解析
                            try {
                                String timeStr = row.getStr(i);
                                if (timeStr != null && !timeStr.isEmpty()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    entity.setCreateDate(sdf.parse(timeStr));
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
                                entity.setUpdateDate(new Date(row.getLong(i)));
                            } catch (Exception e) {
                                // 如果转换失败，尝试作为字符串解析
                                try {
                                    String timeStr = row.getStr(i);
                                    if (timeStr != null && !timeStr.isEmpty()) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        entity.setUpdateDate(sdf.parse(timeStr));
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
}