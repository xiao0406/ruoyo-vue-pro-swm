package com.jeesite.modules.swm.service;

import com.jeesite.modules.utils.R;

import java.util.List;
import java.util.Map;

/**
 * 外部坐标数据服务接口
 * 
 * @author Shawn
 */
public interface ExternalCoordinateDataService {

    /**
     * 根据身份证列表查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCardList 身份证号列表
     * @return Map<身份证号, Map<坐标信息>>
     */
    R<Map<String, Map<String, Object>>> getLatestLocationsByIdCards(List<String> idCardList);

    /**
     * 根据单个身份证号查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCard 身份证号
     * @return 坐标信息
     */
    R<Map<String, Object>> getLatestLocationByIdCard(String idCard);

    /**
     * 根据身份证号获取当天的所有轨迹坐标
     * 
     * @param idCard 身份证号
     * @return 轨迹坐标列表
     */
    R<List<Map<String, Object>>> getTodayTrajectoryByIdCard(String idCard);

    /**
     * 根据身份证号和时间范围获取轨迹坐标
     * 
     * @param idCard    身份证号
     * @param startDate 开始日期 (格式: YYYY-MM-DD)
     * @param endDate   结束日期 (格式: YYYY-MM-DD)
     * @param startTime 开始时间（当日的秒数，可选）
     * @param endTime   结束时间（当日的秒数，可选）
     * @return 轨迹坐标列表
     */
    R<List<Map<String, Object>>> getTrajectoryByIdCardAndTimeRange(String idCard, String startDate, String endDate,
            Integer startTime, Integer endTime);

    /**
     * 保存外部坐标数据
     * 
     * @param dataMap 坐标数据
     * @return 保存结果
     */
    R<Map<String, Object>> saveCoordinateData(Map<String, Object> dataMap);

    /**
     * 获取外部坐标数据统计信息
     * 
     * @return 统计信息
     */
    R<Map<String, Object>> getStatistics();
}