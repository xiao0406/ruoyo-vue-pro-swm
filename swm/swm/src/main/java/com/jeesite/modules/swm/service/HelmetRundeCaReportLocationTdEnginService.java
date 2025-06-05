package com.jeesite.modules.swm.service;

import com.jeesite.modules.utils.R;

import java.util.List;
import java.util.Map;

/**
 * 安全帽设备数据服务接口
 * 
 * @author Shawn
 * @date 2025-01-31
 */
public interface HelmetRundeCaReportLocationTdEnginService {

    /**
     * 保存安全帽设备数据
     * 
     * @param deviceId 设备ID
     * @param dataMap  设备数据
     * @return 保存结果
     */
    R<cn.hutool.json.JSONObject> saveHelmetData(String deviceId, Map<String, Object> dataMap);

    /**
     * 根据设备ID获取最新数据
     * 
     * @param deviceId 设备ID
     * @return 最新数据
     */
    R<Map<String, Object>> getLatestData(String deviceId);

    /**
     * 根据设备ID分页查询数据
     * 
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 分页数据
     */
    R<Map<String, Object>> getDataByDeviceId(String deviceId, String startTime, String endTime,
            Integer pageNum, Integer pageSize);

    /**
     * 获取所有安全帽设备列表
     * 
     * @return 设备列表
     */
    R<List<String>> getAllDevices();

    /**
     * 统计安全帽设备数据总数
     * 
     * @param deviceId  设备ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 数据总数
     */
    R<Long> countData(String deviceId, String startTime, String endTime);

    /**
     * 获取安全帽设备统计信息
     * 
     * @return 设备统计信息
     */
    R<Map<String, Object>> getStatistics();

    /**
     * 批量保存安全帽设备数据
     * 
     * @param deviceDataList 设备数据列表
     * @return 保存结果
     */
    R<Map<String, Object>> batchSaveHelmetData(List<Map<String, Object>> deviceDataList);

    /**
     * 获取安全帽超级表名称
     * 
     * @return 超级表名称
     */
    String getHelmetSuperTableName();

    /**
     * 获取设备对应的子表名称
     * 
     * @param deviceId 设备ID
     * @return 子表名称列表（可能存在多个，因为身份证可能变更过）
     */
    R<List<String>> getSubTablesByDeviceId(String deviceId);

    /**
     * 清理设备的旧子表（身份证变更后的历史表）
     * 
     * @param deviceId      设备ID
     * @param currentIdCard 当前身份证ID
     * @return 清理结果
     */
    R<Map<String, Object>> cleanupOldSubTables(String deviceId, String currentIdCard);

    /**
     * 根据身份证列表查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCardList 身份证号列表
     * @return Map<身份证号, Map<坐标信息>>
     * @author Shawn
     * @date 2025-01-31
     */
    R<Map<String, Map<String, Object>>> getLatestLocationsByIdCards(List<String> idCardList);

    /**
     * 根据单个身份证号查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCard 身份证号
     * @return 坐标信息
     * @author Shawn
     * @date 2025-01-31
     */
    R<Map<String, Object>> getLatestLocationByIdCard(String idCard);

    /**
     * 根据身份证号获取当天的所有轨迹坐标
     * 
     * @param idCard 身份证号
     * @return 轨迹坐标列表
     * @author Shawn
     * @date 2025-01-31
     */
    R<List<Map<String, Object>>> getTodayTrajectoryByIdCard(String idCard);
}