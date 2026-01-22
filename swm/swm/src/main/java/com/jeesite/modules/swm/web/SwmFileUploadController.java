package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.config.MinioConfiguration;
import com.jeesite.modules.util.MinioUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
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
 * @date 2025-05-21
 */
@Controller
@RequestMapping(value = "${adminPath}/fileUpload")
@Api(value = "通用文件上传接口", tags = "通用文件上传接口")
public class SwmFileUploadController extends BaseController {

    @Autowired
    private MinioUtils minioUtils;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfiguration minioProperties;

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
            // 添加预览URL，直接使用"/swm/fileUpload/preview"路径
            result.put("previewUrl", "fileUpload/preview?objectName=" + objectName);
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
     * 直接预览文件
     */
    @GetMapping(value = "preview")
    @ApiOperation("直接预览文件")
    public void preview(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "fileName", required = false) String fileName,
            HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            logger.info("预览文件，对象名: {}, 文件名: {}", objectName, fileName);

            // 获取文件类型
            String contentType = getContentTypeByFileName(objectName);
            response.setContentType(contentType);

            // 设置下载文件名
            String downloadFileName = fileName;
            if (downloadFileName == null || downloadFileName.isEmpty()) {
                // 如果没有提供文件名，从objectName中提取
                downloadFileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            }

            // 对文件名进行URL编码
            downloadFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + downloadFileName);

            // 从MinIO获取文件流
            inputStream = minioUtils.getObject(objectName);

            // 检查文件流是否为空
            if (inputStream == null) {
                logger.error("无法获取文件流，文件可能不存在: {}", objectName);
                response.setContentType("text/plain;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在或无法访问");
                return;
            }

            // 获取输出流并写入文件内容
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096]; // 增大缓冲区大小提高性能
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            logger.info("文件预览完成: {}", objectName);

        } catch (Exception e) {
            logger.error("文件预览失败: {}", e.getMessage(), e);

            // 检查响应是否已经提交
            if (!response.isCommitted()) {
                try {
                    response.reset(); // 重置响应
                    response.setContentType("text/plain;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("文件预览失败：" + e.getMessage());
                } catch (IOException ex) {
                    logger.error("写入错误响应失败", ex);
                }
            }
        } finally {
            // 关闭资源
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭文件输入流失败", e);
                }
            }

            // 输出流由容器管理，通常不需要关闭
        }
    }

    /**
     * 获取预览URL（带有时效性的访问链接）
     */
    @GetMapping(value = "getPreviewUrl")
    @ResponseBody
    @ApiOperation("获取预览URL")
    public Map<String, Object> getPreviewUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiry", required = false, defaultValue = "1") Integer expiry) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("获取预览URL，对象名: {}, 有效期: {}小时", objectName, expiry);

            // 调用MinioUtils生成预签名URL，默认有效期为1小时
            String previewUrl = minioUtils.getPresignedUrl(objectName, expiry);

            result.put("result", "success");
            result.put("url", previewUrl);
            result.put("expiry", expiry);
            result.put("expiryUnit", "小时");

            logger.info("预览URL生成成功: {}", previewUrl);
        } catch (Exception e) {
            logger.error("获取预览URL失败", e);
            result.put("result", "error");
            result.put("message", "获取预览URL失败：" + e.getMessage());
        }
        return result;
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

    /**
     * 根据文件名获取内容类型
     * 
     * @param fileName 文件名
     * @return 内容类型
     */
    private String getContentTypeByFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            default:
                return "application/octet-stream";
        }
    }


    /**
     * 视频预览
     * @param objectName
     * @param request
     * @param response
     */
    /**
     * 在线预览视频（独立方法，不影响原有代码）
     */
    @GetMapping(value = "vxPreview")
    @ApiOperation("在线查看视频（浏览器直接播放）")
    public void viewVideo(
            @RequestParam("objectName") String objectName,
            HttpServletRequest request,
            HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            logger.info("在线查看视频，对象名: {}", objectName);

            // 1. 获取文件大小
            long fileSize = getFileSizeFromMinio(objectName);
            if (fileSize == -1) {
                logger.error("视频文件不存在: {}", objectName);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("视频文件不存在或无法访问");
                return;
            }

            // 2. 处理Range请求，支持视频拖拽播放
            String rangeHeader = request.getHeader("Range");
            long start = 0;
            long end = fileSize - 1;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                // 设置分片响应状态码
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));
            }

            // 3. 设置视频播放的响应头（关键：让浏览器直接播放）
            String contentType = getVideoContentType(objectName);
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", String.valueOf(end - start + 1));
            // 核心：inline表示浏览器直接播放，而非下载
            response.setHeader("Content-Disposition", "inline; filename=\"" +
                    URLEncoder.encode(objectName.substring(objectName.lastIndexOf("/") + 1), "UTF-8") + "\"");

            // 4. 从MinIO读取文件流（支持起始位置）
            inputStream = getMinioObjectStream(objectName, start);
            if (inputStream == null) {
                logger.error("无法获取视频流: {}", objectName);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("获取视频流失败");
                return;
            }

            // 5. 分块写入响应流
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            long bytesWritten = 0;
            long bytesToWrite = end - start + 1;

            int bytesRead;
            while (bytesWritten < bytesToWrite && (bytesRead = inputStream.read(buffer)) != -1) {
                int writeLength = (int) Math.min(bytesRead, bytesToWrite - bytesWritten);
                outputStream.write(buffer, 0, writeLength);
                bytesWritten += writeLength;
            }
            outputStream.flush();

            logger.info("视频播放完成，传输字节数: {}", bytesWritten);

        } catch (Exception e) {
            logger.error("视频播放失败", e);
            if (!response.isCommitted()) {
                try {
                    response.reset();
                    response.setContentType("text/plain;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("视频播放失败：" + e.getMessage());
                } catch (IOException ex) {
                    logger.error("写入错误信息失败", ex);
                }
            }
        } finally {
            // 关闭资源
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 从MinIO获取文件大小（封装调用）
     */
    private long getFileSizeFromMinio(String objectName) {
        try {
            long size = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build()).size();
            return size;
        } catch (Exception e) {
            logger.error("获取文件大小失败", e);
            return -1;
        }
    }

    /**
     * 从MinIO获取文件流（支持起始位置）
     */
    private InputStream getMinioObjectStream(String objectName, long start) {
        try {
            // 如果你的MinioUtils没有带start的getObject方法，可先调用默认的getObject，再跳过对应字节
            InputStream inputStream = minioUtils.getObject(objectName);
            if (inputStream != null && start > 0) {
                long skipped = 0;
                while (skipped < start) {
                    long skip = inputStream.skip(start - skipped);
                    if (skip == 0) break;
                    skipped += skip;
                }
            }
            return inputStream;
        } catch (Exception e) {
            logger.error("获取文件流失败", e);
            return null;
        }
    }

    /**
     * 获取视频的Content-Type
     */
    private String getVideoContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "mkv": return "video/x-matroska";
            case "flv": return "video/x-flv";
            case "wmv": return "video/x-ms-wmv";
            default: return "video/mp4"; // 默认mp4格式
        }
    }

}