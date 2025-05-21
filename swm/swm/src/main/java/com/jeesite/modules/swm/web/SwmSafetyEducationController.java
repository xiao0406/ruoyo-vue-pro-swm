package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmSafetyEducation;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.SwmSafetyEducationService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.util.MinioUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 安全教育Controller
 * 
 * @author zwf
 * @version 2025-05-14
 */
@Controller
@RequestMapping(value = "${adminPath}/safetyEducation")
@Api(value = "安全教育管理接口", tags = "安全教育管理接口")
public class SwmSafetyEducationController extends BaseController {

    @Autowired
    private SwmSafetyEducationService swmSafetyEducationService;

    @Autowired
    private SwmPersonService swmPersonService;

    @Autowired
    private MinioUtils minioUtils;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmSafetyEducation get(String id, boolean isNewRecord) {
        return swmSafetyEducationService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmSafetyEducation swmSafetyEducation, Model model) {
        model.addAttribute("swmSafetyEducation", swmSafetyEducation);
        return "modules/swm/safetyEducationList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmSafetyEducation> listData(SwmSafetyEducation swmSafetyEducation, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建分页对象
        Page<SwmSafetyEducation> page = new Page<>(request, response);

        // 使用不带状态过滤的方法查询所有记录
        List<SwmSafetyEducation> allRecords = swmSafetyEducationService.findAllWithoutStatusFilter();

        // 应用其他过滤条件（如果有的话）
        List<SwmSafetyEducation> filteredRecords = filterRecords(allRecords, swmSafetyEducation);

        // 设置分页结果
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int count = filteredRecords.size();

        // 计算起止索引
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, count);

        // 防止越界
        if (fromIndex >= count) {
            fromIndex = Math.max(0, count - pageSize);
            toIndex = count;
        }

        // 获取当前页数据
        List<SwmSafetyEducation> pageRecords = (fromIndex < toIndex) ? filteredRecords.subList(fromIndex, toIndex)
                : new ArrayList<>();

        // 处理枚举显示值并将状态替换为文本
        for (SwmSafetyEducation education : pageRecords) {
            // 先获取文本值
            String statusText = education.getStatusText();
            String typeText = education.getSafetyEducationTypeText();
            String participationTypeText = education.getParticipationTypeText();

            // 将字段的原始值替换为文本值
            education.setStatus(statusText);
            education.setSafetyEducationType(typeText);
            education.setParticipationType(participationTypeText);

            // 处理参与对象字段的展示
            String participants = education.getParticipants();
            if (participants != null && !participants.isEmpty()) {
                // 检查是否是JSON格式
                if (participants.startsWith("[") && participants.endsWith("]")) {
                    try {
                        // 解析JSON数组
                        ObjectMapper mapper = new ObjectMapper();
                        List<String> participantList = mapper.readValue(participants,
                                new TypeReference<List<String>>() {
                                });
                        // 将列表转换为逗号分隔的字符串以便显示
                        education.setParticipants(String.join(", ", participantList));
                    } catch (Exception e) {
                        // 解析失败时保持原样
                        logger.error("解析参与对象JSON失败: {}", e.getMessage());
                    }
                }
            }

            // 确保内容描述不为null
            if (education.getContentDescription() == null) {
                education.setContentDescription("");
            }
        }

        // 设置分页对象属性
        page.setList(pageRecords);
        page.setCount(count);

        return page;
    }

    /**
     * 根据查询条件过滤记录
     */
    private List<SwmSafetyEducation> filterRecords(List<SwmSafetyEducation> allRecords, SwmSafetyEducation criteria) {
        if (criteria == null) {
            return allRecords;
        }

        return allRecords.stream()
                .filter(record -> {
                    // 根据主题过滤（模糊匹配）
                    if (criteria.getTheme() != null && !criteria.getTheme().isEmpty()) {
                        if (record.getTheme() == null || !record.getTheme().contains(criteria.getTheme())) {
                            return false;
                        }
                    }

                    // 根据内容描述过滤（模糊匹配）
                    if (criteria.getContentDescription() != null && !criteria.getContentDescription().isEmpty()) {
                        if (record.getContentDescription() == null
                                || !record.getContentDescription().contains(criteria.getContentDescription())) {
                            return false;
                        }
                    }

                    // 根据安全教育类型过滤（精确匹配）
                    if (criteria.getSafetyEducationType() != null && !criteria.getSafetyEducationType().isEmpty()) {
                        if (record.getSafetyEducationType() == null
                                || !record.getSafetyEducationType().equals(criteria.getSafetyEducationType())) {
                            return false;
                        }
                    }

                    // 根据参与类型过滤（精确匹配）
                    if (criteria.getParticipationType() != null && !criteria.getParticipationType().isEmpty()) {
                        if (record.getParticipationType() == null
                                || !record.getParticipationType().equals(criteria.getParticipationType())) {
                            return false;
                        }
                    }

                    // 根据参与对象过滤（模糊匹配）
                    if (criteria.getParticipants() != null && !criteria.getParticipants().isEmpty()) {
                        if (record.getParticipants() == null) {
                            return false;
                        }

                        String participants = record.getParticipants();
                        // 检查是否为JSON格式的参与者列表
                        if (participants.startsWith("[") && participants.endsWith("]")) {
                            try {
                                // 解析JSON数组
                                ObjectMapper mapper = new ObjectMapper();
                                List<String> participantList = mapper.readValue(participants,
                                        new TypeReference<List<String>>() {
                                        });
                                // 将列表转换为逗号分隔的字符串用于搜索
                                String participantsText = String.join(", ", participantList);
                                if (!participantsText.contains(criteria.getParticipants())) {
                                    return false;
                                }
                            } catch (Exception e) {
                                logger.error("过滤时解析参与对象JSON失败: {}", e.getMessage());
                                if (!participants.contains(criteria.getParticipants())) {
                                    return false;
                                }
                            }
                        } else if (!participants.contains(criteria.getParticipants())) {
                            return false;
                        }
                    }

                    // 根据状态过滤（精确匹配）
                    if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                        if (record.getStatus() == null || !record.getStatus().equals(criteria.getStatus())) {
                            return false;
                        }
                    }

                    // 根据附件URL过滤（模糊匹配）
                    if (criteria.getAttachmentUrl() != null && !criteria.getAttachmentUrl().isEmpty()) {
                        if (record.getAttachmentUrl() == null
                                || !record.getAttachmentUrl().contains(criteria.getAttachmentUrl())) {
                            return false;
                        }
                    }

                    // 根据开始时间过滤
                    if (criteria.getStartTime() != null) {
                        if (record.getStartTime() == null || record.getStartTime().before(criteria.getStartTime())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmSafetyEducation swmSafetyEducation) {
        Map<String, Object> result = new HashMap<>();
        if (swmSafetyEducation != null) {
            Map<String, Object> educationData = new HashMap<>();
            // 复制基本属性
            educationData.put("id", swmSafetyEducation.getId());
            educationData.put("theme", swmSafetyEducation.getTheme());
            educationData.put("contentDescription", swmSafetyEducation.getContentDescription());
            educationData.put("startTime", swmSafetyEducation.getStartTime());
            educationData.put("participants", swmSafetyEducation.getParticipants());
            educationData.put("remarks", swmSafetyEducation.getRemarks());
            educationData.put("attachmentUrl", swmSafetyEducation.getAttachmentUrl());

            // 处理枚举值
            educationData.put("status", swmSafetyEducation.getStatus());
            educationData.put("statusText", swmSafetyEducation.getStatusText());
            educationData.put("safetyEducationType", swmSafetyEducation.getSafetyEducationType());
            educationData.put("safetyEducationTypeText", swmSafetyEducation.getSafetyEducationTypeText());
            educationData.put("participationType", swmSafetyEducation.getParticipationType());
            educationData.put("participationTypeText", swmSafetyEducation.getParticipationTypeText());

            // 处理时间
            educationData.put("createTime", swmSafetyEducation.getCreateTime());
            educationData.put("updateTime", swmSafetyEducation.getUpdateTime());

            result.putAll(educationData);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmSafetyEducation swmSafetyEducation) {
        // 设置创建/更新时间
        Date now = new Date();
        if (swmSafetyEducation.getIsNewRecord()) {
            swmSafetyEducation.setCreateTime(now);
        }
        swmSafetyEducation.setUpdateTime(now);

        // 添加日志输出
        logger.info("保存安全教育信息 - ID: {}, 主题: {}, 内容描述: {}, 是否新记录: {}",
                swmSafetyEducation.getId(),
                swmSafetyEducation.getTheme(),
                swmSafetyEducation.getContentDescription(),
                swmSafetyEducation.getIsNewRecord());

        // 记录参与对象格式
        String participants = swmSafetyEducation.getParticipants();
        if (participants != null && participants.startsWith("[") && participants.endsWith("]")) {
            logger.info("参与对象已使用JSON格式保存: {}", participants);
        } else {
            logger.info("参与对象使用普通字符串格式: {}", participants);
        }

        swmSafetyEducationService.save(swmSafetyEducation);
        return renderResult(Global.TRUE, text("保存安全教育成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmSafetyEducation swmSafetyEducation) {
        swmSafetyEducationService.delete(swmSafetyEducation);
        return renderResult(Global.TRUE, text("删除安全教育成功！"));
    }

    /**
     * 批量删除数据
     */
    @PostMapping(value = "deleteAll")
    @ResponseBody
    @ApiOperation("批量删除数据")
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmSafetyEducation swmSafetyEducation = swmSafetyEducationService.get(id);
            if (swmSafetyEducation != null) {
                swmSafetyEducationService.delete(swmSafetyEducation);
            }
        }
        return renderResult(Global.TRUE, text("批量删除安全教育成功！"));
    }

    /**
     * 获取枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取枚举选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 状态选项
        Map<String, String> statusOptions = new HashMap<>();
        statusOptions.put(SwmSafetyEducation.StatusEnum.NOT_STARTED, "未开始");
        statusOptions.put(SwmSafetyEducation.StatusEnum.COMPLETED, "已完成");
        result.put("statusOptions", statusOptions);

        // 安全教育类型选项
        Map<String, String> typeOptions = new HashMap<>();
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.ENTRY, "人员入职安全教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.WEEKLY, "周安全教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.MONTHLY, "月度教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.QUARTERLY, "季度教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.SPECIAL, "专题教育");
        result.put("educationTypeOptions", typeOptions);

        // 参与类型选项
        Map<String, String> participationTypeOptions = new HashMap<>();
        participationTypeOptions.put(SwmSafetyEducation.ParticipationTypeEnum.TEAM, "班组");
        participationTypeOptions.put(SwmSafetyEducation.ParticipationTypeEnum.PROCESS, "工序");
        participationTypeOptions.put(SwmSafetyEducation.ParticipationTypeEnum.WORKSHOP, "车间");
        result.put("participationTypeOptions", participationTypeOptions);

        return result;
    }

    /**
     * 导出数据
     */
    @RequestMapping(value = "exportData")
    @ApiOperation("导出数据")
    public void exportData(SwmSafetyEducation swmSafetyEducation, HttpServletResponse response) {
        try {
            // 使用不带状态过滤的方法获取所有记录
            List<SwmSafetyEducation> allRecords = swmSafetyEducationService.findAllWithoutStatusFilter();

            // 应用其他过滤条件
            List<SwmSafetyEducation> filteredRecords = filterRecords(allRecords, swmSafetyEducation);

            // 处理枚举显示值并将状态替换为文本
            for (SwmSafetyEducation education : filteredRecords) {
                // 先获取文本值
                String statusText = education.getStatusText();
                String typeText = education.getSafetyEducationTypeText();
                String participationTypeText = education.getParticipationTypeText();

                // 将字段的原始值替换为文本值
                education.setStatus(statusText);
                education.setSafetyEducationType(typeText);
                education.setParticipationType(participationTypeText);

                // 处理参与对象字段的展示（导出时）
                String participants = education.getParticipants();
                if (participants != null && !participants.isEmpty()) {
                    // 检查是否是JSON格式
                    if (participants.startsWith("[") && participants.endsWith("]")) {
                        try {
                            // 解析JSON数组
                            ObjectMapper mapper = new ObjectMapper();
                            List<String> participantList = mapper.readValue(participants,
                                    new TypeReference<List<String>>() {
                                    });
                            // 将列表转换为逗号分隔的字符串以便显示
                            education.setParticipants(String.join(", ", participantList));
                        } catch (Exception e) {
                            // 解析失败时保持原样
                            logger.error("导出数据时解析参与对象JSON失败: {}", e.getMessage());
                        }
                    }
                }
            }

            String fileName = "安全教育数据" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 由于BaseController没有exportExcel方法，这里省略导出逻辑
            // 如需实现，可以使用POI或EasyExcel等库
        } catch (Exception e) {
            logger.error("导出安全教育数据失败！", e);
        }
    }

    /**
     * 上传安全教育完成附件
     */
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @ApiOperation("上传安全教育完成附件")
    public Map<String, Object> upload(HttpServletRequest request,
            @RequestParam(value = "recordId", required = false) String recordId) {
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
            logger.info("处理文件上传，文件名: {}, 文件大小: {}, 记录ID: {}",
                    originalFilename, file.getSize(), recordId);

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 构建安全教育附件的存储路径
            String objectName = "safety-education/" + (recordId != null ? recordId + "/" : "") + fileName;
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

            logger.info("文件上传处理完成，返回结果: {}", result);

        } catch (Exception e) {
            logger.error("文件上传失败", e);
            result.put("result", "error");
            result.put("message", "文件上传失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 完成安全教育
     */
    @PostMapping(value = "complete")
    @ResponseBody
    @ApiOperation("完成安全教育")
    public Map<String, Object> complete(@RequestParam("id") String id,
            @RequestParam("fileIds") String fileIds,
            @RequestParam(value = "attachmentUrl", required = false) String attachmentUrl,
            @RequestParam(value = "status", required = false) String status) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("完成安全教育，ID: {}, 文件IDs: {}, 附件URLs: {}, 状态: {}", id, fileIds, attachmentUrl, status);

            // 获取安全教育记录
            SwmSafetyEducation education = swmSafetyEducationService.get(id);
            if (education == null) {
                result.put("result", "error");
                result.put("message", "未找到安全教育记录");
                return result;
            }

            // 更新状态为已完成
            education.setStatus(SwmSafetyEducation.StatusEnum.COMPLETED);

            // 保存附件信息，优先使用完整的附件URL（包含文件路径）
            if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
                education.setAttachmentUrl(attachmentUrl);
                logger.info("保存完整的附件信息: {}", attachmentUrl);
            } else {
                // 兼容处理：如果没有提供完整URL，则只保存文件IDs
                education.setAttachmentUrl(fileIds);
                logger.info("仅保存文件IDs: {}", fileIds);
            }

            // 更新时间
            education.setUpdateTime(new Date());

            // 保存更新
            swmSafetyEducationService.save(education);

            // 根据参与对象更新人员管理中的安全教育状态
            updatePersonnelSafetyEducationStatus(education);

            result.put("result", "success");
            result.put("message", "安全教育已完成");
            logger.info("安全教育完成保存成功，ID: {}", id);

        } catch (Exception e) {
            logger.error("完成安全教育失败", e);
            result.put("result", "error");
            result.put("message", "完成安全教育失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取安全教育附件列表
     */
    @GetMapping(value = "fileList")
    @ResponseBody
    @ApiOperation("获取安全教育附件列表")
    public List<Map<String, Object>> getFileList(@RequestParam("id") String id) {
        List<Map<String, Object>> fileList = new ArrayList<>();
        try {
            logger.info("获取安全教育附件列表，ID: {}", id);

            // 获取安全教育记录
            SwmSafetyEducation education = swmSafetyEducationService.get(id);
            if (education == null) {
                logger.warn("未找到安全教育记录，ID: {}", id);
                return fileList;
            }

            // 获取附件信息
            String attachmentUrl = education.getAttachmentUrl();
            if (attachmentUrl == null || attachmentUrl.isEmpty()) {
                logger.info("安全教育记录没有附件，ID: {}", id);
                return fileList;
            }

            // 尝试解析JSON格式的附件信息
            try {
                // 检查是否是JSON格式
                if (attachmentUrl.startsWith("[") && attachmentUrl.endsWith("]")) {
                    // 使用Jackson解析JSON数组
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> parsedList = mapper.readValue(
                            attachmentUrl,
                            new TypeReference<List<Map<String, Object>>>() {
                            });

                    for (Map<String, Object> item : parsedList) {
                        Map<String, Object> fileInfo = new HashMap<>();
                        String fileId = item.get("fileId") != null ? item.get("fileId").toString() : "";
                        fileInfo.put("fileId", fileId);
                        fileInfo.put("id", fileId); // 保持与前端一致
                        fileInfo.put("fileName",
                                item.get("fileName") != null ? item.get("fileName").toString() : "未命名文件");
                        String url = item.get("url") != null ? item.get("url").toString() : "";
                        fileInfo.put("url", url);

                        // 添加预览URL
                        // 从URL中提取objectName或构造预览路径
                        String previewUrl = "";
                        if (item.get("previewUrl") != null) {
                            // 如果原数据已经包含previewUrl，直接使用
                            previewUrl = item.get("previewUrl").toString();
                        } else if (url.contains("/swm/")) {
                            // 尝试从URL中提取对象路径
                            String objectName = url.substring(url.indexOf("/swm/") + 5);
                            if (objectName.indexOf("/") > 0) {
                                objectName = objectName.substring(objectName.indexOf("/") + 1);
                                previewUrl = "fileUpload/preview?objectName=" + objectName;
                            }
                        } else {
                            // 如果无法提取，则使用通用格式
                            previewUrl = "fileUpload/preview?objectName=safety-education/" + id + "/" + fileId;
                        }
                        fileInfo.put("previewUrl", previewUrl);

                        fileList.add(fileInfo);
                    }

                    logger.info("成功解析JSON格式附件信息，文件数量: {}", fileList.size());
                } else {
                    // 如果不是JSON格式，尝试解析旧格式（逗号分隔的文件ID列表）
                    String[] fileIds = attachmentUrl.split(",");
                    for (String fileId : fileIds) {
                        if (fileId.trim().isEmpty()) {
                            continue;
                        }

                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("fileId", fileId.trim());
                        fileInfo.put("id", fileId.trim()); // 保持与前端一致
                        fileInfo.put("fileName", "文件" + fileId.trim());
                        // 由于没有URL信息，可以尝试从MinIO构建一个
                        String url = constructFileUrl(fileId.trim(), id);
                        fileInfo.put("url", url);

                        // 添加预览URL
                        String previewUrl = "fileUpload/preview?objectName=safety-education/" + id + "/"
                                + fileId.trim();
                        fileInfo.put("previewUrl", previewUrl);

                        fileList.add(fileInfo);
                    }

                    logger.info("成功解析旧格式附件信息，文件数量: {}", fileList.size());
                }
            } catch (Exception e) {
                logger.error("解析附件信息失败", e);
                // 失败时返回空列表
            }

        } catch (Exception e) {
            logger.error("获取安全教育附件列表失败", e);
        }

        return fileList;
    }

    /**
     * 根据安全教育参与对象更新人员管理中的安全教育状态
     * 
     * @param education 安全教育记录
     */
    private void updatePersonnelSafetyEducationStatus(SwmSafetyEducation education) {
        if (education == null || education.getParticipants() == null || education.getParticipants().isEmpty()) {
            logger.warn("安全教育记录为空或参与对象为空，无法更新人员管理的安全教育状态");
            return;
        }

        try {
            // 获取参与对象
            String participants = education.getParticipants();
            List<String> workshopList = new ArrayList<>();

            // 解析参与对象（支持JSON格式和逗号分隔的字符串）
            if (participants.startsWith("[") && participants.endsWith("]")) {
                // JSON格式
                ObjectMapper mapper = new ObjectMapper();
                workshopList = mapper.readValue(participants, new TypeReference<List<String>>() {
                });
            } else {
                // 逗号分隔的字符串
                workshopList = java.util.Arrays.asList(participants.split(","))
                        .stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            // 直接将参与对象视为车间名称进行更新
            logger.info("安全教育ID: {}，主题: {}，开始更新人员安全教育状态",
                    education.getId(), education.getTheme());

            // 查询每个车间的人员并更新
            int updatedCount = 0;
            for (String workshop : workshopList) {
                // 根据部门查询人员并更新
                int count = updatePersonByDepartment(workshop);
                logger.info("车间 [{}] 更新了 {} 名人员的安全教育状态", workshop, count);
                updatedCount += count;
            }
            logger.info("安全教育（ID: {}, 主题: {}）完成后成功更新了 {} 名人员的安全教育状态",
                    education.getId(), education.getTheme(), updatedCount);
        } catch (Exception e) {
            logger.error("更新人员安全教育状态时出现异常", e);
        }
    }

    /**
     * 根据部门更新人员的安全教育状态
     * 
     * @param department 部门名称
     * @return 更新的记录数
     */
    private int updatePersonByDepartment(String department) {
        if (department == null || department.isEmpty()) {
            return 0;
        }

        try {
            // 创建查询条件
            SwmPerson query = new SwmPerson();
            query.setDepartment(department);
            query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // 只更新在职人员

            // 查询符合条件的人员
            List<SwmPerson> personList = swmPersonService.findList(query);
            if (personList == null || personList.isEmpty()) {
                logger.info("未找到部门 [{}] 的人员记录", department);
                return 0;
            }

            logger.info("部门 [{}] 找到 {} 名人员", department, personList.size());

            // 更新每个人员的安全教育状态
            int count = 0;
            for (SwmPerson person : personList) {
                // 只更新安全教育状态为未开始的人员
                if (SwmPerson.SafetyEducationEnum.NOT_STARTED.equals(person.getSafetyEducation())) {
                    person.setSafetyEducation(SwmPerson.SafetyEducationEnum.COMPLETED);
                    swmPersonService.save(person);
                    count++;
                }
            }

            logger.info("部门 [{}] 共更新了 {} 名人员的安全教育状态", department, count);
            return count;
        } catch (Exception e) {
            logger.error("更新部门 [{}] 人员的安全教育状态时出现异常: {}", department, e.getMessage());
            return 0;
        }
    }

    /**
     * 构建文件URL
     * 注意：这里根据文件ID和记录ID推断文件路径，实际使用时可能需要调整
     */
    private String constructFileUrl(String fileId, String recordId) {
        try {
            // 构建对象路径
            String objectPath = "safety-education/" + recordId + "/" + fileId;

            // 由于我们没有具体的文件对象，这里创建一个下载URL
            // 将文件请求重定向到我们的下载接口
            return Global.getAdminPath() + "/safetyEducation/download?id=" + recordId + "&fileId=" + fileId;
        } catch (Exception e) {
            logger.error("构建文件URL失败", e);
            // 出错时仍然返回下载接口地址
            return Global.getAdminPath() + "/safetyEducation/download?id=" + recordId + "&fileId=" + fileId;
        }
    }

    /**
     * 下载安全教育附件
     */
    @GetMapping(value = "download")
    @ApiOperation("下载安全教育附件")
    public void downloadFile(
            @RequestParam("id") String id,
            @RequestParam("fileId") String fileId,
            HttpServletResponse response) {
        try {
            logger.info("请求下载附件，教育ID: {}, 文件ID: {}", id, fileId);

            // 获取安全教育记录
            SwmSafetyEducation education = swmSafetyEducationService.get(id);
            if (education == null) {
                logger.warn("未找到安全教育记录，ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 获取附件信息
            String attachmentUrl = education.getAttachmentUrl();
            if (attachmentUrl == null || attachmentUrl.isEmpty()) {
                logger.warn("安全教育记录没有附件，ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 查找指定fileId的文件信息
            Map<String, String> fileInfo = null;
            String fileName = "安全教育附件_" + fileId;

            // 尝试解析JSON
            if (attachmentUrl.startsWith("[") && attachmentUrl.endsWith("]")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, String>> filesList = mapper.readValue(
                            attachmentUrl,
                            new TypeReference<List<Map<String, String>>>() {
                            });

                    // 查找匹配的文件ID
                    for (Map<String, String> file : filesList) {
                        if (fileId.equals(file.get("fileId"))) {
                            fileInfo = file;
                            fileName = file.get("fileName");
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("解析附件JSON失败", e);
                }
            }

            // 如果找到了文件信息并且有URL
            if (fileInfo != null && fileInfo.containsKey("url") && fileInfo.get("url") != null) {
                // 重定向到实际的文件URL
                response.sendRedirect(fileInfo.get("url"));
                return;
            }

            // 如果没有找到URL或解析失败，尝试从MinIO直接获取
            String objectPath = "safety-education/" + id + "/" + fileId;

            // 使用MinioUtils的fileDownload方法下载文件
            minioUtils.fileDownload(objectPath, false, response);

        } catch (Exception e) {
            logger.error("下载附件失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                logger.error("设置响应状态失败", ex);
            }
        }
    }
}