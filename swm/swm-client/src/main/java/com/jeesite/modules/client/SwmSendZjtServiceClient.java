package com.jeesite.modules.client;

import com.jeesite.modules.api.SwmSendZjtServiceApi;
import com.jeesite.modules.cloud.feign.condition.ConditionalOnNotCurrentApplication;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="${service.swm.name}", path="${service.swm.path}")
@ConditionalOnNotCurrentApplication(name="${service.swm.name}")
public interface SwmSendZjtServiceClient extends SwmSendZjtServiceApi {
}
