package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmMediaFile;
import com.jeesite.modules.swm.service.SwmMediaFileService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 媒体文件管理Controller
 * @author auto create
 * @version 2023-07-01
 */
@Controller
@RequestMapping(value = "${adminPath}/swmMediaFile")
public class SwmMediaFileController extends BaseController {

    @Autowired
    private SwmMediaFileService swmMediaFileService;
    
    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmMediaFile get(String id, boolean isNewRecord) {
        return swmMediaFileService.get(id, isNewRecord);
    }
    
    /**
     * 查询列表
     */
    @RequestMapping(value = {"list", ""})
    public String list(SwmMediaFile swmMediaFile, Model model) {
        model.addAttribute("swmMediaFile", swmMediaFile);
        return "modules/swm/swmMediaFileList";
    }
    
    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmMediaFile> listData(SwmMediaFile swmMediaFile, HttpServletRequest request, HttpServletResponse response) {
        swmMediaFile.setPage(new Page<>(request, response));
        Page<SwmMediaFile> page = swmMediaFileService.findPage(swmMediaFile);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmMediaFile swmMediaFile, Model model) {
        model.addAttribute("swmMediaFile", swmMediaFile);
        return "modules/swm/swmMediaFileForm";
    }

    /**
     * 保存媒体文件
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmMediaFile swmMediaFile) {
        swmMediaFileService.save(swmMediaFile);
        return renderResult(Global.TRUE, text("保存媒体文件成功！"));
    }
    
    /**
     * 删除媒体文件
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmMediaFile swmMediaFile) {
        swmMediaFileService.delete(swmMediaFile);
        return renderResult(Global.TRUE, text("删除媒体文件成功！"));
    }
    
    /**
     * API：上传媒体文件
     */
    @PostMapping(value = "upload")
    @ResponseBody
    public SwmMediaFile upload(@RequestParam("file") MultipartFile file,
                              @RequestParam(required = false) String businessType,
                              @RequestParam(required = false) String businessId,
                              @RequestParam(required = false) String tags,
                              HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // 用简单方式创建媒体文件对象
            SwmMediaFile mediaFile = new SwmMediaFile();
            mediaFile.setFileName(file.getOriginalFilename());
            mediaFile.setFileType(file.getContentType());
            mediaFile.setFileSize(file.getSize());
            
            // 保存文件到临时目录并构建URL
            String contextPath = request.getContextPath();
            String tempDir = request.getServletContext().getRealPath("/upload/mediafile");
            
            // 此处只构建演示对象，具体文件存储逻辑需要根据实际情况实现
            mediaFile.setFileUrl(contextPath + "/upload/mediafile/" + file.getOriginalFilename());
            
            if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
                mediaFile.setThumbnailUrl(mediaFile.getFileUrl());
            }
            
            mediaFile.setUploadTime(new Date());
            mediaFile.setBusinessType(businessType);
            mediaFile.setBusinessId(businessId);
            mediaFile.setTags(tags);
            mediaFile.setStatus("0");
            
            // 假设上传人是系统用户
            mediaFile.setUploadBy("system");
            mediaFile.setUploadByName("系统管理员");
            
            // 保存记录
            swmMediaFileService.save(mediaFile);
            return mediaFile;
        } catch (Exception e) {
            logger.error("上传媒体文件失败", e);
            return null;
        }
    }
    
    /**
     * API：获取文件详情
     */
    @RequestMapping(value = "get")
    @ResponseBody
    public SwmMediaFile getInfo(String id) {
        return swmMediaFileService.get(id);
    }
    
    /**
     * API：删除媒体文件
     */
    @RequestMapping(value = "deleteFile")
    @ResponseBody
    public String deleteFile(String id) {
        SwmMediaFile mediaFile = new SwmMediaFile(id);
        swmMediaFileService.delete(mediaFile);
        return renderResult(Global.TRUE, text("删除文件成功"));
    }
    
    /**
     * API：获取所有媒体文件数据（不分页）
     */
    @RequestMapping(value = "listAll")
    @ResponseBody
    public List<SwmMediaFile> listAll(SwmMediaFile swmMediaFile) {
        return swmMediaFileService.findList(swmMediaFile);
    }
} 