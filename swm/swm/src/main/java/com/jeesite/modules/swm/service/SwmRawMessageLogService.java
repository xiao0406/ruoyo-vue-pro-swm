/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmRawMessageLog;
import com.jeesite.modules.swm.entity.vo.SwmRawMessageLogVO;

import java.util.List;

/**
 * TDengine原始消息日志Service接口
 */
public interface SwmRawMessageLogService {

    /**
     * 分页查询原始消息日志
     * @param page 分页对象
     * @param entity 查询条件
     * @return 分页结果
     */
    Page<SwmRawMessageLogVO> findPage(Page<SwmRawMessageLogVO> page, SwmRawMessageLog entity);

    /**
     * 查询符合条件的所有数据（用于导出）
     * @param entity 查询条件
     * @param limit 限制条数
     * @return 数据列表
     */
    List<SwmRawMessageLogVO> findList(SwmRawMessageLog entity, int limit);

    /**
     * 查询记录总数
     * @param entity 查询条件
     * @return 总数
     */
    long count(SwmRawMessageLog entity);
}