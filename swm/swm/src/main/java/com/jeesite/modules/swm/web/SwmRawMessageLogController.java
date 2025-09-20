/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.web;

import com.alibaba.excel.EasyExcel;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmRawMessageLog;
import com.jeesite.modules.swm.entity.vo.SwmRawMessageLogVO;
import com.jeesite.modules.swm.service.SwmRawMessageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TDengine原始消息日志Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/swmRawMessageLog")
public class SwmRawMessageLogController extends BaseController {

    @Autowired
    private SwmRawMessageLogService swmRawMessageLogService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmRawMessageLog get(String id, boolean isNewRecord) {
        return new SwmRawMessageLog(id);
    }

    /**
     * 查询列表页面
     */
    @RequestMapping(value = {"list", ""})
    public String list(SwmRawMessageLog swmRawMessageLog, Model model) {
        model.addAttribute("swmRawMessageLog", swmRawMessageLog);
        return "modules/swm/rawMessageLog/rawMessageLogList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmRawMessageLogVO> listData(SwmRawMessageLog swmRawMessageLog, HttpServletRequest request, HttpServletResponse response) {
        // 创建分页对象，最大支持10000条
        Page<SwmRawMessageLogVO> page = new Page<>(request, response);
        if (page.getPageSize() > 10000) {
            page.setPageSize(10000);
        }

        // 查询数据
        page = swmRawMessageLogService.findPage(page, swmRawMessageLog);

        return page;
    }

    /**
     * 导出Excel数据
     */
    @RequestMapping(value = "exportData")
    @ResponseBody
    public String exportData(SwmRawMessageLog swmRawMessageLog, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取导出页数和页大小参数
            String pageSizeStr = request.getParameter("pageSize");
            int pageSize = 1000; // 默认1000条
            if (pageSizeStr != null) {
                try {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize > 10000) {
                        pageSize = 10000; // 最大10000条
                    }
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }

            // 查询数据
            List<SwmRawMessageLogVO> list = swmRawMessageLogService.findList(swmRawMessageLog, pageSize);
            if (list.isEmpty()) {
                return renderResult(Global.FALSE, text("没有符合条件的数据可以导出！"));
            }

            // 设置响应头
            String fileName = "raw_message_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, "UTF-8"));

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), SwmRawMessageLogExcelVO.class)
                    .sheet("原始消息日志")
                    .doWrite(convertToExcelVOList(list));

            return null;

        } catch (IOException e) {
            logger.error("导出原始消息日志失败", e);
            return renderResult(Global.FALSE, text("导出失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("导出原始消息日志异常", e);
            return renderResult(Global.FALSE, text("导出异常：" + e.getMessage()));
        }
    }

    /**
     * 转换为Excel导出VO
     */
    private List<SwmRawMessageLogExcelVO> convertToExcelVOList(List<SwmRawMessageLogVO> list) {
        return list.stream().map(this::convertToExcelVO).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 转换单个对象为Excel VO
     */
    private SwmRawMessageLogExcelVO convertToExcelVO(SwmRawMessageLogVO vo) {
        SwmRawMessageLogExcelVO excelVO = new SwmRawMessageLogExcelVO();
        excelVO.setTime(vo.getTimeText());
        excelVO.setSessionId(vo.getSessionId());
        // 将message_time转换为字符串，保持原始值
        excelVO.setMessageTime(vo.getMessageTime() != null ? String.valueOf(vo.getMessageTime()) : "");
        excelVO.setMessageContent(vo.getMessageContent());
        excelVO.setOriginalLength(vo.getOriginalLength());
        excelVO.setIsTruncated(vo.getIsTruncatedText());
        excelVO.setDeviceId(vo.getDeviceId());
        return excelVO;
    }

    /**
     * Excel导出VO类
     */
    public static class SwmRawMessageLogExcelVO {
        @com.alibaba.excel.annotation.ExcelProperty("time")
        private String time;

        @com.alibaba.excel.annotation.ExcelProperty("session_id")
        private String sessionId;

        @com.alibaba.excel.annotation.ExcelProperty("message_time")
        private String messageTime;

        @com.alibaba.excel.annotation.ExcelProperty("message_content")
        private String messageContent;

        @com.alibaba.excel.annotation.ExcelProperty("original_length")
        private Integer originalLength;

        @com.alibaba.excel.annotation.ExcelProperty("is_truncated")
        private String isTruncated;

        @com.alibaba.excel.annotation.ExcelProperty("device_id")
        private String deviceId;

        // getter和setter方法
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessageTime() {
            return messageTime;
        }

        public void setMessageTime(String messageTime) {
            this.messageTime = messageTime;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }

        public Integer getOriginalLength() {
            return originalLength;
        }

        public void setOriginalLength(Integer originalLength) {
            this.originalLength = originalLength;
        }

        public String getIsTruncated() {
            return isTruncated;
        }

        public void setIsTruncated(String isTruncated) {
            this.isTruncated = isTruncated;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
    }
}