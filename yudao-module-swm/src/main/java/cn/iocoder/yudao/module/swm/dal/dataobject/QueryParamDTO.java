package cn.iocoder.yudao.module.swm.dal.dataobject;

import lombok.Data;

@Data
public class QueryParamDTO {

    private Long tenantId;
    private String deviceId;
    private String deviceCode;
    private String column;
    private String startTime;
    private String endTime;
    private String sortOrder;
    private Integer pageNum;
    private Integer pageSize;
}
