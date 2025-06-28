package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHandleRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 处置记录DAO接口
 * @author zwf
 * @version 2025-05-16
 */
@MyBatisDao
public interface SwmHandleRecordDao extends CrudDao<SwmHandleRecord> {

    /**
     * 获取最新处置记录
     * @param limit 获取数量
     * @return
     */
    List<SwmHandleRecord> latestHandleRecord(@Param("limit") int limit);
}
