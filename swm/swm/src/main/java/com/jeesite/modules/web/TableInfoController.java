package com.jeesite.modules.web;

import com.jeesite.common.web.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表结构查询控制器
 */
@Slf4j
@RestController
@RequestMapping("${adminPath}/tableInfo")
@Api(value = "表结构查询", tags = "表结构查询")
public class TableInfoController extends BaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取swm_safety_education表的字段信息
     */
    @GetMapping("/safetyEducation")
    @ResponseBody
    @ApiOperation("获取安全教育表字段信息")
    public Map<String, Object> getSafetyEducationTableInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // MySQL查询
            String mysqlSql = "SELECT COLUMN_NAME as columnName, DATA_TYPE as dataType, " +
                    "COLUMN_COMMENT as columnComment, IS_NULLABLE as isNullable, " +
                    "CHARACTER_MAXIMUM_LENGTH as maxLength " +
                    "FROM information_schema.COLUMNS " +
                    "WHERE TABLE_NAME = 'swm_safety_education'";
                
            // Oracle查询
            String oracleSql = "SELECT COLUMN_NAME as columnName, DATA_TYPE as dataType, " +
                    "'' as columnComment, NULLABLE as isNullable, " +
                    "DATA_LENGTH as maxLength " +
                    "FROM USER_TAB_COLUMNS " +
                    "WHERE TABLE_NAME = 'SWM_SAFETY_EDUCATION'";
            
            // 先尝试MySQL查询
            List<Map<String, Object>> columns = new ArrayList<>();
            try {
                columns = jdbcTemplate.queryForList(mysqlSql);
            } catch (Exception e) {
                // 如果失败，尝试Oracle查询
                try {
                    columns = jdbcTemplate.queryForList(oracleSql);
                } catch (Exception e2) {
                    log.error("查询表结构失败: {}", e2.getMessage());
                }
            }
            
            if (!columns.isEmpty()) {
                log.info("成功获取到swm_safety_education表的字段信息，共{}个字段", columns.size());
                result.put("status", "success");
                result.put("message", "查询成功");
                result.put("columns", columns);
            } else {
                // 通用方法：直接查询表数据，推断字段
                try {
                    String sql = "SELECT * FROM swm_safety_education WHERE ROWNUM <= 1";
                    Map<String, Object> row = jdbcTemplate.queryForMap(sql);
                    
                    List<Map<String, Object>> inferredColumns = new ArrayList<>();
                    for (String key : row.keySet()) {
                        Map<String, Object> column = new HashMap<>();
                        column.put("columnName", key);
                        column.put("dataType", row.get(key) != null ? row.get(key).getClass().getSimpleName() : "Unknown");
                        inferredColumns.add(column);
                    }
                    
                    result.put("status", "success");
                    result.put("message", "通过数据推断的字段");
                    result.put("columns", inferredColumns);
                } catch (Exception e) {
                    log.error("推断表结构失败: {}", e.getMessage());
                    result.put("status", "error");
                    result.put("message", "查询失败：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("查询表字段信息失败", e);
            result.put("status", "error");
            result.put("message", "查询失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取表的数据示例
     */
    @GetMapping("/safetyEducationData")
    @ResponseBody
    @ApiOperation("获取表数据示例")
    public Map<String, Object> getSampleData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String sql = "SELECT * FROM swm_safety_education WHERE ROWNUM <= 5";
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            
            result.put("status", "success");
            result.put("message", "查询成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("查询表数据失败", e);
            result.put("status", "error");
            result.put("message", "查询失败：" + e.getMessage());
            
            // 尝试MySQL语法
            try {
                String sql = "SELECT * FROM swm_safety_education LIMIT 5";
                List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
                
                result.put("status", "success");
                result.put("message", "查询成功");
                result.put("data", data);
            } catch (Exception e2) {
                log.error("MySQL语法查询也失败", e2);
            }
        }
        
        return result;
    }
} 