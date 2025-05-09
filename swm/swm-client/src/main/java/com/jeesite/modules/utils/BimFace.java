package com.jeesite.modules.utils;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.InputStream;
import java.util.List;

/**
 * @author 清欢
 * @ClassName: BimFace
 * @time 2023-07-13 14:15:14
 */
@Data
public class BimFace {

    /**
     * 上传的文件
     **/
    private InputStream file;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件Id，即调用上传文件API返回的fileId
     **/
    private Long fileId;

     /**
      * 生成分页查询的ContextId
      **/
    private String paginationContextId;

    /**
     * 分页大小(默认不传)
     **/
    private Integer paginationSize;
    /**
     * 分页大小(默认不传)
     **/
    private Integer paginationNo;

    /**
     * 构件ids
     **/
    private List<String> elementIds;

    /**
     * 是否为压缩文件，默认为false
     **/
    private Boolean compressed;

    /**
     * 如果是压缩文件，必须指定压缩包中哪一个是主文件
     **/
    private String rootName;

    /**
     * 转换引擎自定义参数，config参数跟转换引擎相关，不同的转换引擎支持不同的config格式。例如转换时添加内置材质，则添加参数值{"texture":true}
     **/
    private JSON config;

    private String status;

    private String thumbnail;

    private String reason;

    private String signature;

    private String nonce;
    /**归属配置*/
    private String configurationId;
}