package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 坐标请求 DO
 * 表: iot_coordinate_request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_coordinate_request")
public class IotCoordinateRequestDO extends SwmBaseDO {
}
