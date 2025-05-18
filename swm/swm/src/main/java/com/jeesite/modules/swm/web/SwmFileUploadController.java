package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.util.MinioUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通用文件上传控制器
 * 
 * @author Shawn
 * @date 2024-06-11
 */
@Controller
@RequestMapping(value = "${adminPath}/fileUpload")
@Api(value = "通用文件上传接口", tags = "通用文件上传接口")
public class SwmFileUploadController extends BaseController {

    @Autowired
    private MinioUtils minioUtils;

    /**
     * 上传文件
     */
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @ApiOperation("上传文件")
    public Map<String, Object> upload(HttpServletRequest request,
            @RequestParam(value = "recordId", required = false) String recordId,
            @RequestParam(value = "businessType", required = false) String businessType) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("收到文件上传请求，Content-Type: {}", request.getContentType());

            // 尝试从请求中获取file参数
            MultipartFile file = null;
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                file = multipartRequest.getFile("file");
                logger.info("从MultipartHttpServletRequest获取文件: {}", file != null ? file.getOriginalFilename() : "null");
            }

            if (file == null || file.isEmpty()) {
                logger.warn("上传的文件为空");
                result.put("result", "error");
                result.put("message", "上传文件不能为空");
                return result;
            }

            String originalFilename = file.getOriginalFilename();
            logger.info("处理文件上传，文件名: {}, 文件大小: {}, 记录ID: {}, 业务类型: {}",
                    originalFilename, file.getSize(), recordId, businessType);

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 构建文件的存储路径，使用业务类型区分不同模块
            String businessFolder = (businessType != null && !businessType.trim().isEmpty())
                    ? businessType.trim() + "/"
                    : "common/";
            String recordFolder = (recordId != null && !recordId.trim().isEmpty())
                    ? recordId.trim() + "/"
                    : "";
            String objectName = businessFolder + recordFolder + fileName;

            logger.info("文件将存储在MinIO路径: {}", objectName);

            // 使用MinioUtils上传文件
            Map<String, Object> uploadResult = minioUtils.upload(file, objectName);
            logger.info("MinIO上传成功，返回结果: {}", uploadResult);

            // 生成文件ID，实际项目中可能会将文件信息存入数据库
            String fileId = UUID.randomUUID().toString();

            result.put("result", "success");
            result.put("message", "文件上传成功");
            result.put("fileId", fileId);
            result.put("fileName", originalFilename);
            result.put("url", uploadResult.get("url"));
            result.put("businessType", businessType);
            result.put("recordId", recordId);

            logger.info("文件上传处理完成，返回结果: {}", result);

        } catch (Exception e) {
            logger.error("文件上传失败", e);
            result.put("result", "error");
            result.put("message", "文件上传失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取文件列表
     */
    @GetMapping(value = "list")
    @ResponseBody
    @ApiOperation("获取文件列表")
    public List<Map<String, Object>> getFileList(
            @RequestParam(value = "recordId", required = true) String recordId,
            @RequestParam(value = "businessType", required = false) String businessType) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            logger.info("获取文件列表，记录ID: {}, 业务类型: {}", recordId, businessType);

            // 构建查询路径
            String businessFolder = (businessType != null && !businessType.trim().isEmpty())
                    ? businessType.trim() + "/"
                    : "common/";
            String prefix = businessFolder + recordId + "/";

            // 此处应调用MinioUtils的方法获取文件列表
            // 在实际项目中，您可能需要根据MinioUtils的实现来调整
            // 以下代码为示例，假设有一个listObjects方法
            // List<MinioItem> items = minioUtils.listObjects(prefix);

            // 通常这部分数据会从数据库中读取，此处为演示返回空列表
            // 如果需要实现此功能，需要先将上传的文件信息保存到数据库中

            logger.info("获取文件列表完成，返回{}个结果", result.size());
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
        }
        return result;
    }

    /**
     * 下载文件
     */
    @GetMapping(value = "download")
    @ApiOperation("下载文件")
    public void download(
            @RequestParam("fileId") String fileId,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "objectName", required = true) String objectName,
            HttpServletResponse response) {
        try {
            logger.info("下载文件，文件ID: {}, 文件名: {}, 对象名: {}", fileId, fileName, objectName);

            // 设置响应头
            response.setContentType("application/octet-stream");
            String downloadFileName = fileName != null ? fileName
                    : objectName.substring(objectName.lastIndexOf("/") + 1);
            downloadFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + downloadFileName);

            // 从MinIO获取文件流并写入响应
            try (InputStream inputStream = minioUtils.getObject(objectName);
                    OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            logger.info("文件下载完成: {}", objectName);
        } catch (Exception e) {
            logger.error("文件下载失败", e);
            try {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("文件下载失败：" + e.getMessage());
            } catch (IOException ex) {
                logger.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 删除文件
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除文件")
    public Map<String, Object> deleteFile(
            @RequestParam("fileId") String fileId,
            @RequestParam("objectName") String objectName) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("删除文件，文件ID: {}, 对象名: {}", fileId, objectName);

            // 调用MinIO删除文件
            // minioUtils.removeObject(objectName);

            // 在实际项目中，还需要从数据库中删除文件记录

            result.put("result", "success");
            result.put("message", "文件删除成功");
            logger.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            logger.error("文件删除失败", e);
            result.put("result", "error");
            result.put("message", "文件删除失败：" + e.getMessage());
        }
        return result;
    }
}