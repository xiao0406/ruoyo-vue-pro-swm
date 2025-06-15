/**
 * 危险源信息DAO接口
 * @author Shawn
 * @version 2025-05-21
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 危险源信息DAO接口
 */
@MyBatisDao
public interface SwmHazardSourceDao extends CrudDao<SwmHazardSource> {

    /**
     * 根据状态统计数量
     * @param status 状态
     * @param year 年
     * @param month 月
     * @return
     */
    int countByStatusAndMonth(@Param("status") String status, @Param("year") String year, @Param("month") String month);

    /**
     * 按危险源类别统计数量
     */
    List<Map<String, Object>> countByCategoryAndMonth(@Param("year") String year, @Param("month") String month);

    /**
     * 获取危险源类别排名前10
     */
    List<Map<String, Object>> getTop10Categories(@Param("year") String year, @Param("month") String month);
}
