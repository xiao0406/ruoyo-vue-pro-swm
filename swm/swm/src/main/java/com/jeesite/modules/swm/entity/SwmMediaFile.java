package com.jeesite.modules.swm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 媒体文件管理实体类
 * @author zwf
 * @version 2023-07-01
 */
@Table(name = "swm_media_file", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "文件ID", isPK = true),
        @Column(name = "file_name", attrName = "fileName", label = "文件名称", queryType = QueryType.LIKE),
        @Column(name = "file_type", attrName = "fileType", label = "文件类型"),
        @Column(name = "file_size", attrName = "fileSize", label = "文件大小"),
        @Column(name = "file_url", attrName = "fileUrl", label = "文件访问URL"),
        @Column(name = "thumbnail_url", attrName = "thumbnailUrl", label = "缩略图URL"),
        @Column(name = "upload_by", attrName = "uploadBy", label = "上传人ID"),
        @Column(name = "upload_by_name", attrName = "uploadByName", label = "上传人姓名"),
        @Column(name = "upload_time", attrName = "uploadTime", label = "上传时间"),
        @Column(name = "tags", attrName = "tags", label = "文件标签", queryType = QueryType.LIKE),
        @Column(name = "business_type", attrName = "businessType", label = "业务类型"),
        @Column(name = "business_id", attrName = "businessId", label = "关联业务ID"),
        @Column(name = "status", attrName = "status", label = "文件状态"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.upload_time DESC")
public class SwmMediaFile extends DataEntity<SwmMediaFile> {

    private static final long serialVersionUID = 1L;

    private String fileName;        // 文件名称
    private String fileType;        // 文件类型
    private Long fileSize;          // 文件大小
    private String fileUrl;         // 文件访问URL
    private String thumbnailUrl;    // 缩略图URL
    private String uploadBy;        // 上传人ID
    private String uploadByName;    // 上传人姓名
    private Date uploadTime;        // 上传时间
    private String tags;            // 文件标签
    private String businessType;    // 业务类型
    private String businessId;      // 关联业务ID
    private String status;          // 文件状态

    // 查询条件：上传时间范围
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginUploadTime;   // 开始上传时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endUploadTime;     // 结束上传时间

    public SwmMediaFile() {
        this(null);
    }

    public SwmMediaFile(String id) {
        super(id);
    }

    @NotBlank(message = "文件名称不能为空")
    @Size(min = 0, max = 255, message = "文件名称长度不能超过255个字符")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @NotBlank(message = "文件类型不能为空")
    @Size(min = 0, max = 50, message = "文件类型长度不能超过50个字符")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @NotNull(message = "文件大小不能为空")
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @NotBlank(message = "文件访问URL不能为空")
    @Size(min = 0, max = 500, message = "文件访问URL长度不能超过500个字符")
    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Size(min = 0, max = 500, message = "缩略图URL长度不能超过500个字符")
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @NotBlank(message = "上传人ID不能为空")
    @Size(min = 0, max = 64, message = "上传人ID长度不能超过64个字符")
    public String getUploadBy() {
        return uploadBy;
    }

    public void setUploadBy(String uploadBy) {
        this.uploadBy = uploadBy;
    }

    @NotBlank(message = "上传人姓名不能为空")
    @Size(min = 0, max = 100, message = "上传人姓名长度不能超过100个字符")
    public String getUploadByName() {
        return uploadByName;
    }

    public void setUploadByName(String uploadByName) {
        this.uploadByName = uploadByName;
    }

    @NotNull(message = "上传时间不能为空")
    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Size(min = 0, max = 255, message = "文件标签长度不能超过255个字符")
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Size(min = 0, max = 100, message = "业务类型长度不能超过100个字符")
    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @Size(min = 0, max = 64, message = "关联业务ID长度不能超过64个字符")
    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getBeginUploadTime() {
        return beginUploadTime;
    }

    public void setBeginUploadTime(Date beginUploadTime) {
        this.beginUploadTime = beginUploadTime;
    }

    public Date getEndUploadTime() {
        return endUploadTime;
    }

    public void setEndUploadTime(Date endUploadTime) {
        this.endUploadTime = endUploadTime;
    }
}
