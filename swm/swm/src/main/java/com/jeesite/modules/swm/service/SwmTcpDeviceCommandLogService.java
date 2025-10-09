/**
 * @author Shawn
 * @date 2025-10-02
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmTcpDeviceCommandLog;
import com.jeesite.modules.swm.entity.vo.SwmTcpDeviceCommandLogVO;

import java.util.List;

/**
 * TDengine 下发指令到设备日志Service接口
 */
public interface SwmTcpDeviceCommandLogService {

    /**
     * 分页查询下发指令到设备日志
     * @param page 分页对象
     * @param entity 查询条件
     * @return 分页结果
     */
    Page<SwmTcpDeviceCommandLogVO> findPage(Page<SwmTcpDeviceCommandLogVO> page, SwmTcpDeviceCommandLog entity);

    /**
     * 查询符合条件的所有数据（用于导出）
     * @param entity 查询条件
     * @param limit 限制条数
     * @return 数据列表
     */
    List<SwmTcpDeviceCommandLogVO> findList(SwmTcpDeviceCommandLog entity, int limit);

    /**
     * 查询记录总数
     * @param entity 查询条件
     * @return 总数
     */
    long count(SwmTcpDeviceCommandLog entity);

    /**
     * 直接分页查询（绕过Page对象限制）
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param entity 查询条件
     * @return 数据列表
     */
    List<SwmTcpDeviceCommandLogVO> findPageData(int pageNo, int pageSize, SwmTcpDeviceCommandLog entity);
}
