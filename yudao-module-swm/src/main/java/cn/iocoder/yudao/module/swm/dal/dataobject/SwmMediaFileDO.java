package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 媒体文件 DO
 * 表: swm_media_file
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_media_file")
public class SwmMediaFileDO extends SwmBaseDO {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String thumbnailUrl;
    private String uploadBy;
    private String uploadByName;
    private LocalDateTime uploadTime;
    private String tags;
    private String businessType;
    private String businessId;
}
