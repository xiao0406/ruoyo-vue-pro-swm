package cn.iocoder.yudao.module.swm.api.warning;

import cn.iocoder.yudao.module.swm.api.warning.dto.SwmWarningCreateReqDTO;

/**
 * Public SWM warning API used by other modules such as IOT.
 */
public interface SwmWarningApi {

    /**
     * Create a warning record and return the warning id.
     */
    String createWarning(SwmWarningCreateReqDTO reqDTO);
}
