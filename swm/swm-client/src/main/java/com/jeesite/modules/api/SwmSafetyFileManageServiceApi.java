package com.jeesite.modules.api;

import com.jeesite.modules.entity.SwmSafetyFileManage;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeesite.common.service.rest.CrudServiceRest;


/**
 * 安全教育视频管理API
 * @author wxy
 * @version 2026-01-19
 */
@RequestMapping(value = "/inner/api/swm/swmSafetyFileManage")
public interface SwmSafetyFileManageServiceApi extends CrudServiceRest<SwmSafetyFileManage> {
	
}