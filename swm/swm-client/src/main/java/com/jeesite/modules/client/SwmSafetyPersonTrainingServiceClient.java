package com.jeesite.modules.client;

import com.jeesite.modules.api.SwmSafetyPersonTrainingServiceApi;
import org.springframework.cloud.openfeign.FeignClient;

import com.jeesite.modules.cloud.feign.condition.ConditionalOnNotCurrentApplication;

/**
 * 视频培训记录（人员）API
 * @author wxy
 * @version 2026-01-19
 */
@FeignClient(name="${service.swm.name}", path="${service.swm.path}")
@ConditionalOnNotCurrentApplication(name="${service.swm.name}")
public interface SwmSafetyPersonTrainingServiceClient extends SwmSafetyPersonTrainingServiceApi {
	
}