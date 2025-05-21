package com.jeesite.modules.swm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmMediaFileDao;
import com.jeesite.modules.swm.entity.SwmMediaFile;
import com.jeesite.common.idgen.IdGen;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 媒体文件管理Service
 * @author zwf
 * @version 2023-07-01
 */
@Service
@Transactional(readOnly = true)
public class SwmMediaFileService extends CrudService<SwmMediaFileDao, SwmMediaFile> {
    
    private static final Logger logger = LoggerFactory.getLogger(SwmMediaFileService.class);
    
    /**
     * 获取单条数据
     * @param swmMediaFile
     * @return
     */
    @Override
    public SwmMediaFile get(SwmMediaFile swmMediaFile) {
        return super.get(swmMediaFile);
    }
    
    /**
     * 查询分页数据
     * @param swmMediaFile 查询条件
     * @param swmMediaFile.page 分页对象
     * @return
     */
    @Override
    public Page<SwmMediaFile> findPage(SwmMediaFile swmMediaFile) {
        return super.findPage(swmMediaFile);
    }
    
    /**
     * 保存数据（插入或更新）
     * @param swmMediaFile
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmMediaFile swmMediaFile) {
        super.save(swmMediaFile);
    }
    
    /**
     * 更新状态
     * @param swmMediaFile
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmMediaFile swmMediaFile) {
        super.updateStatus(swmMediaFile);
    }
    
    /**
     * 删除数据
     * @param swmMediaFile
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmMediaFile swmMediaFile) {
        super.delete(swmMediaFile);
    }
    
    /**
     * 上传媒体文件
     * @param file 上传的文件
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param tags 标签
     * @param request HTTP请求对象
     * @return 媒体文件信息
     */
    @Transactional(readOnly = false)
    public SwmMediaFile uploadMediaFile(MultipartFile file, String businessType, String businessId, 
                                        String tags, String uploadBy, String uploadByName,
                                        HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        String fileType = file.getContentType();
        Long fileSize = file.getSize();
        
        // 构建存储路径
        String baseDir = request.getServletContext().getRealPath("/upload");
        String relativePath = "/mediafile";
        if (StringUtils.isNotBlank(businessType)) {
            relativePath += "/" + businessType;
        }
        if (StringUtils.isNotBlank(businessId)) {
            relativePath += "/" + businessId;
        }
        
        // 确保目录存在
        File targetDir = new File(baseDir + relativePath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        // 生成唯一文件名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String saveFileName = IdGen.uuid() + fileExtension;
        
        // 保存文件
        File targetFile = new File(targetDir, saveFileName);
        file.transferTo(targetFile);
        
        // 文件访问URL
        String fileUrl = request.getContextPath() + "/upload" + relativePath + "/" + saveFileName;
        
        // 缩略图URL，如果是图片则与原图相同，否则为null
        String thumbnailUrl = null;
        if (fileType != null && fileType.startsWith("image/")) {
            thumbnailUrl = fileUrl;
        }
        
        // 保存文件信息
        SwmMediaFile mediaFile = new SwmMediaFile();
        mediaFile.setFileName(originalFilename);
        mediaFile.setFileType(fileType);
        mediaFile.setFileSize(fileSize);
        mediaFile.setFileUrl(fileUrl);
        mediaFile.setThumbnailUrl(thumbnailUrl);
        mediaFile.setUploadBy(uploadBy);
        mediaFile.setUploadByName(uploadByName);
        mediaFile.setUploadTime(new Date());
        mediaFile.setBusinessType(businessType);
        mediaFile.setBusinessId(businessId);
        mediaFile.setTags(tags);
        mediaFile.setStatus("0"); // 正常状态
        
        save(mediaFile);
        return mediaFile;
    }
} 