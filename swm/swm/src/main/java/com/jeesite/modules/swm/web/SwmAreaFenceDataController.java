package com.jeesite.modules.swm.web;

import com.alibaba.excel.EasyExcel;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.swm.entity.SwmAreaFenceData;
import com.jeesite.modules.swm.entity.vo.SwmAreaFenceDataVO;
import com.jeesite.modules.swm.service.SwmAreaFenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "${adminPath}/areaFenceData")
public class SwmAreaFenceDataController extends BaseController {

    @Autowired
    private SwmAreaFenceDataService areaFenceDataService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @ModelAttribute
    public SwmAreaFenceData get(String id, boolean isNewRecord) {
        return new SwmAreaFenceData(id);
    }

    @RequestMapping(value = {"list", ""})
    public String list(SwmAreaFenceData entity, Model model) {
        model.addAttribute("areaFenceData", entity);
        return "modules/swm/areaFenceData/areaFenceDataList";
    }

    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmAreaFenceDataVO> listData(SwmAreaFenceData entity, HttpServletRequest request) {
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
                pageSize = Math.min(pageSize, 10000);
            } catch (NumberFormatException e) {
                logger.warn("pageSize参数格式错误: {}", pageSizeStr);
            }
        }

        List<SwmAreaFenceDataVO> list = areaFenceDataService.findPageData(pageNo, pageSize, entity);
        long totalCount = areaFenceDataService.count(entity);

        Page<SwmAreaFenceDataVO> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setList(list);
        page.setCount(totalCount);

        return page;
    }

    @RequestMapping(value = "exportData", method = RequestMethod.POST)
    @ResponseBody
    public String exportData(SwmAreaFenceData entity, HttpServletRequest request, HttpServletResponse response) {
        try {
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
                    pageSize = Math.min(pageSize, 10000);
                } catch (NumberFormatException e) {
                    logger.warn("pageSize参数格式错误: {}", pageSizeStr);
                }
            }

            List<SwmAreaFenceDataVO> list = areaFenceDataService.findPageData(pageNo, pageSize, entity);
            if (list.isEmpty()) {
                return renderResult(Global.FALSE, text("没有符合条件的数据可以导出！"));
            }

            String fileName = TdengineSuperTableConstant.AREA_FENCE_DATA+"_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, "UTF-8"));

            EasyExcel.write(response.getOutputStream(), SwmAreaFenceDataExcelVO.class)
                .sheet("区域围栏数据")
                .doWrite(convertToExcelVOList(list, pageNo, pageSize));

            return null;
        } catch (IOException e) {
            logger.error("导出区域围栏数据失败", e);
            return renderResult(Global.FALSE, text("导出失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("导出区域围栏数据异常", e);
            return renderResult(Global.FALSE, text("导出异常：" + e.getMessage()));
        }
    }

    private List<SwmAreaFenceDataExcelVO> convertToExcelVOList(List<SwmAreaFenceDataVO> list, int pageNo, int pageSize) {
        List<SwmAreaFenceDataExcelVO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SwmAreaFenceDataVO vo = list.get(i);
            SwmAreaFenceDataExcelVO excelVO = convertToExcelVO(vo);
            excelVO.setRowNum((pageNo - 1) * pageSize + i + 1);
            result.add(excelVO);
        }
        return result;
    }

    private SwmAreaFenceDataExcelVO convertToExcelVO(SwmAreaFenceDataVO vo) {
        SwmAreaFenceDataExcelVO excelVO = new SwmAreaFenceDataExcelVO();
        excelVO.setTime(vo.getTimeText());
        excelVO.setX(vo.getX());
        excelVO.setY(vo.getY());
        excelVO.setAreaName(vo.getAreaName());
        excelVO.setAreaId(vo.getAreaId());
        excelVO.setRemarks(vo.getRemarks());
        excelVO.setAreaType(vo.getAreaType());
        excelVO.setDeviceId(vo.getDeviceId());
        excelVO.setIdCard(vo.getIdCard());
        return excelVO;
    }

    public static class SwmAreaFenceDataExcelVO {
        @com.alibaba.excel.annotation.ExcelProperty("序号")
        private Integer rowNum;

        @com.alibaba.excel.annotation.ExcelProperty("time")
        private String time;

        @com.alibaba.excel.annotation.ExcelProperty("x")
        private Double x;

        @com.alibaba.excel.annotation.ExcelProperty("y")
        private Double y;

        @com.alibaba.excel.annotation.ExcelProperty("area_name")
        private String areaName;

        @com.alibaba.excel.annotation.ExcelProperty("area_id")
        private String areaId;

        @com.alibaba.excel.annotation.ExcelProperty("remarks")
        private String remarks;

        @com.alibaba.excel.annotation.ExcelProperty("area_type")
        private String areaType;

        @com.alibaba.excel.annotation.ExcelProperty("device_id")
        private String deviceId;

        @com.alibaba.excel.annotation.ExcelProperty("id_card")
        private String idCard;

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

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public String getAreaId() {
            return areaId;
        }

        public void setAreaId(String areaId) {
            this.areaId = areaId;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getAreaType() {
            return areaType;
        }

        public void setAreaType(String areaType) {
            this.areaType = areaType;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }
}
