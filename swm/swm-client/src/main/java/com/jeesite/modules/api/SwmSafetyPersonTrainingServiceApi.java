package com.jeesite.modules.api;

import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeesite.common.service.rest.CrudServiceRest;

/**
 * 视频培训记录（人员）API
 * @author wxy
 * @version 2026-01-19
 */
@RequestMapping(value = "/inner/api/swm/swmSafetyPersonTraining")
public interface SwmSafetyPersonTrainingServiceApi extends CrudServiceRest<SwmSafetyPersonTraining> {

	
}