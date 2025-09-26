/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmExternalCoordinateData;
import com.jeesite.modules.swm.entity.vo.SwmExternalCoordinateDataVO;

import java.util.List;

/**
 * TDengine外部坐标数据Service接口
 */
public interface SwmExternalCoordinateDataService {

    Page<SwmExternalCoordinateDataVO> findPage(Page<SwmExternalCoordinateDataVO> page, SwmExternalCoordinateData entity);

    List<SwmExternalCoordinateDataVO> findList(SwmExternalCoordinateData entity, int limit);

    long count(SwmExternalCoordinateData entity);

    List<SwmExternalCoordinateDataVO> findPageData(int pageNo, int pageSize, SwmExternalCoordinateData entity);
}
