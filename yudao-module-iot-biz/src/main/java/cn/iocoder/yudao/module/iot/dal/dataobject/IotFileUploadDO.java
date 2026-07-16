package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.Date;

/**
 * IoT 文件上传 DO
 * 表: iot_file_upload
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_file_upload")
public class IotFileUploadDO extends IotBaseDO {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String deviceId;
    private String personId;
    private BigDecimal xPoint;
    private BigDecimal yPoint;
    private String updateBy;
    private Date updateDate;
    private String response;
}
