package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmAreaFenceData;
import com.jeesite.modules.swm.entity.vo.SwmAreaFenceDataVO;

import java.util.List;

public interface SwmAreaFenceDataService {

    Page<SwmAreaFenceDataVO> findPage(Page<SwmAreaFenceDataVO> page, SwmAreaFenceData entity);

    List<SwmAreaFenceDataVO> findList(SwmAreaFenceData entity, int limit);

    long count(SwmAreaFenceData entity);

    List<SwmAreaFenceDataVO> findPageData(int pageNo, int pageSize, SwmAreaFenceData entity);
}
