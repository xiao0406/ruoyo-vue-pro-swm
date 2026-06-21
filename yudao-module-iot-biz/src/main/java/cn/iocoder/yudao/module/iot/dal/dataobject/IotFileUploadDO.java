package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * IoT 文件上传 DO
 * 表: iot_file_upload
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_file_upload")
public class IotFileUploadDO extends SwmBaseDO {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String deviceId;
    private String personId;
}
