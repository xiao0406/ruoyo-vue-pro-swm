package com.jeesite.modules.client;

import com.jeesite.modules.api.SwmSafetyFileManageServiceApi;
import org.springframework.cloud.openfeign.FeignClient;

import com.jeesite.modules.cloud.feign.condition.ConditionalOnNotCurrentApplication;

/**
 * 安全教育视频管理API
 * @author wxy
 * @version 2026-01-19
 */
@FeignClient(name="${service.swm.name}", path="${service.swm.path}")
@ConditionalOnNotCurrentApplication(name="${service.swm.name}")
public interface SwmSafetyFileManageServiceClient extends SwmSafetyFileManageServiceApi {
	
}