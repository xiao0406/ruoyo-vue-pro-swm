/**
 * @author Shawn
 * @date 2025-10-02
 */
package com.jeesite.modules.swm.web;

import com.alibaba.excel.EasyExcel;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmTcpDeviceCommandLog;
import com.jeesite.modules.swm.entity.vo.SwmTcpDeviceCommandLogVO;
import com.jeesite.modules.swm.service.SwmTcpDeviceCommandLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TDengine 下发指令到设备日志Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/swmTcpDeviceCommandLog")
public class SwmTcpDeviceCommandLogController extends BaseController {

    @Autowired
    private SwmTcpDeviceCommandLogService swmTcpDeviceCommandLogService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmTcpDeviceCommandLog get(String id, boolean isNewRecord) {
        return new SwmTcpDeviceCommandLog(id);
    }

    /**
     * 查询列表页面
     */
    @RequestMapping(value = {"list", ""})
    public String list(SwmTcpDeviceCommandLog swmTcpDeviceCommandLog, Model model) {
        model.addAttribute("swmTcpDeviceCommandLog", swmTcpDeviceCommandLog);
        return "modules/swm/tcpDeviceCommandLog/tcpDeviceCommandLogList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmTcpDeviceCommandLogVO> listData(SwmTcpDeviceCommandLog swmTcpDeviceCommandLog, HttpServletRequest request, HttpServletResponse response) {
        // 手动获取分页参数，绕过JeeSite框架的限制
        int pageNo = 1;
        int pageSize = 1000;

        String pageNoStr = request.getParameter("pageNo");
        String pageSizeStr = request.getParameter("pageSize");

        if (pageNoStr != null && !pageNoStr.isEmpty()) {
            try {
                pageNo = Integer.parseInt(pageNoStr);
            } catch (NumberFormatException e) {
                logger.warn("pageNo参数格式错误: {}", pageNoStr);
            }
        }

        if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
            try {
                pageSize = Integer.parseInt(pageSizeStr);
                // 限制最大10000条
                pageSize = Math.min(pageSize, 10000);
            } catch (NumberFormatException e) {
                logger.warn("pageSize参数格式错误: {}", pageSizeStr);
            }
        }

        // 直接调用新的分页方法，绕过Page对象限制
        List<SwmTcpDeviceCommandLogVO> list = swmTcpDeviceCommandLogService.findPageData(pageNo, pageSize, swmTcpDeviceCommandLog);

        // 获取总记录数
        long totalCount = swmTcpDeviceCommandLogService.count(swmTcpDeviceCommandLog);

        // 手动构建返回的Page对象
        Page<SwmTcpDeviceCommandLogVO> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setList(list);
        page.setCount(totalCount);

        return page;
    }

    /**
     * 导出Excel数据
     */
    @RequestMapping(value = "exportData")
    @ResponseBody
    public String exportData(SwmTcpDeviceCommandLog swmTcpDeviceCommandLog, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取分页参数
            int pageNo = 1;
            int pageSize = 1000;

            String pageNoStr = request.getParameter("pageNo");
            String pageSizeStr = request.getParameter("pageSize");

            if (pageNoStr != null && !pageNoStr.isEmpty()) {
                try {
                    pageNo = Integer.parseInt(pageNoStr);
                } catch (NumberFormatException e) {
                    logger.warn("pageNo参数格式错误: {}", pageNoStr);
                }
            }

            if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                try {
                    pageSize = Integer.parseInt(pageSizeStr);
                    pageSize = Math.min(pageSize, 10000); // 最大10000条
                } catch (NumberFormatException e) {
                    logger.warn("pageSize参数格式错误: {}", pageSizeStr);
                }
            }

            // 使用findPageData方法获取当前页数据
            List<SwmTcpDeviceCommandLogVO> list = swmTcpDeviceCommandLogService.findPageData(pageNo, pageSize, swmTcpDeviceCommandLog);
            if (list.isEmpty()) {
                return renderResult(Global.FALSE, text("没有符合条件的数据可以导出！"));
            }

            // 设置响应头
            String fileName = "tcp_device_command_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, "UTF-8"));

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), SwmTcpDeviceCommandLogExcelVO.class)
                    .sheet("下发指令到设备日志")
                    .doWrite(convertToExcelVOList(list, pageNo, pageSize));

            return null;

        } catch (IOException e) {
            logger.error("导出下发指令到设备日志失败", e);
            return renderResult(Global.FALSE, text("导出失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("导出下发指令到设备日志异常", e);
            return renderResult(Global.FALSE, text("导出异常：" + e.getMessage()));
        }
    }

    /**
     * 转换为Excel导出VO
     */
    private List<SwmTcpDeviceCommandLogExcelVO> convertToExcelVOList(List<SwmTcpDeviceCommandLogVO> list, int pageNo, int pageSize) {
        List<SwmTcpDeviceCommandLogExcelVO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SwmTcpDeviceCommandLogVO vo = list.get(i);
            SwmTcpDeviceCommandLogExcelVO excelVO = convertToExcelVO(vo);
            // 计算跨页序号
            int rowNum = (pageNo - 1) * pageSize + i + 1;
            excelVO.setRowNum(rowNum);
            result.add(excelVO);
        }
        return result;
    }

    /**
     * 转换单个对象为Excel VO
     */
    private SwmTcpDeviceCommandLogExcelVO convertToExcelVO(SwmTcpDeviceCommandLogVO vo) {
        SwmTcpDeviceCommandLogExcelVO excelVO = new SwmTcpDeviceCommandLogExcelVO();
        excelVO.setTime(vo.getTimeText());
        excelVO.setSendMessage(vo.getSendMessage());
        excelVO.setSendStatus(vo.getSendStatus());
        excelVO.setErrorMessage(vo.getErrorMessage());
        excelVO.setIdentityCard(vo.getIdentityCard());
        excelVO.setPersonName(vo.getPersonName());
        excelVO.setDeviceId(vo.getDeviceId());
        return excelVO;
    }

    /**
     * Excel导出VO类
     */
    public static class SwmTcpDeviceCommandLogExcelVO {
        @com.alibaba.excel.annotation.ExcelProperty("序号")
        private Integer rowNum;

        @com.alibaba.excel.annotation.ExcelProperty("time")
        private String time;

        @com.alibaba.excel.annotation.ExcelProperty("send_message")
        private String sendMessage;

        @com.alibaba.excel.annotation.ExcelProperty("send_status")
        private String sendStatus;

        @com.alibaba.excel.annotation.ExcelProperty("error_message")
        private String errorMessage;

        @com.alibaba.excel.annotation.ExcelProperty("identity_card")
        private String identityCard;

        @com.alibaba.excel.annotation.ExcelProperty("person_name")
        private String personName;

        @com.alibaba.excel.annotation.ExcelProperty("device_id")
        private String deviceId;

        // getter和setter方法
        public Integer getRowNum() {
            return rowNum;
        }

        public void setRowNum(Integer rowNum) {
            this.rowNum = rowNum;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getSendMessage() {
            return sendMessage;
        }

        public void setSendMessage(String sendMessage) {
            this.sendMessage = sendMessage;
        }

        public String getSendStatus() {
            return sendStatus;
        }

        public void setSendStatus(String sendStatus) {
            this.sendStatus = sendStatus;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getIdentityCard() {
            return identityCard;
        }

        public void setIdentityCard(String identityCard) {
            this.identityCard = identityCard;
        }

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
    }
}
