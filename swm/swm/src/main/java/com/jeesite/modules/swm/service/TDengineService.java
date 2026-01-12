package com.jeesite.modules.swm.service;

import cn.hutool.json.JSONObject;
import com.jeesite.modules.utils.R;
import com.jeesite.modules.vo.DeviceDataDTO;
import com.jeesite.modules.vo.QueryParamDTO;

import java.util.Map;

/**
 * tdengine服务接口
 * @author: cjie
 * @date: 2025/5/27
 */
public interface TDengineService {

    /**
     * 执行任意sql
     * @param sql 操作语句
     */
    R<JSONObject> execute(String sql);

    /**
     * 设备数据存储
     */
    R<JSONObject> insertTsData(DeviceDataDTO deviceDataDTO);

    /**
     * 查询对应表中的最后一条记录
     * @param deviceNum 设备编号
     * @return
     */
    Map<String,Object> getLastRow(String deviceNum);

    /**
     * 分页查询
     * @param param 查询参数
     * @return
     */
    R<Map<String, Object>> listPage(QueryParamDTO param);

    /**
     * 查询总数
     * @param param 查询参数
     * @return
     */
    Long count(QueryParamDTO param);

    R<JSONObject> executeTDengineSQL(String sql);

    R<JSONObject> executeTDengineSQLByXXJOB(String sql,String corpCode);
}
