package cn.iocoder.yudao.module.swm.util;

import lombok.Data;

import java.io.InputStream;
import java.util.List;

/**
 * BimFace 模型查看器参数
 *
 * 迁移自 JeeSite: com.jeesite.modules.utils.BimFace
 */
@Data
public class BimFaceUtil {

    private InputStream file;
    private String fileType;
    private String fileSize;
    private String fileName;
    private Long fileId;
    private String paginationContextId;
    private Integer paginationSize;
    private Integer paginationNo;
    private List<String> elementIds;
    private Boolean compressed;
    private String rootName;
    private String config; // JSON string
    private String status;
    private String thumbnail;
    private String reason;
    private String signature;
    private String nonce;
    private String configurationId;

}
