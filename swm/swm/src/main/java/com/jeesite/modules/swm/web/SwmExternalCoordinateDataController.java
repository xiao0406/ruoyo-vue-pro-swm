/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.web;

import com.alibaba.excel.EasyExcel;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmExternalCoordinateData;
import com.jeesite.modules.swm.entity.vo.SwmExternalCoordinateDataVO;
import com.jeesite.modules.swm.service.SwmExternalCoordinateDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping(value = "${adminPath}/externalCoordinateData")
public class SwmExternalCoordinateDataController extends BaseController {

    @Autowired
    private SwmExternalCoordinateDataService externalCoordinateDataService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @ModelAttribute
    public SwmExternalCoordinateData get(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = "false") boolean isNewRecord) {
        return new SwmExternalCoordinateData(id);
    }

    @RequestMapping(value = {"list", ""})
    public String list(SwmExternalCoordinateData entity, Model model) {
        model.addAttribute("externalCoordinateData", entity);
        return "modules/swm/externalCoordinateData/externalCoordinateDataList";
    }

    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmExternalCoordinateDataVO> listData(SwmExternalCoordinateData entity, HttpServletRequest request, HttpServletResponse response) {
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

        List<SwmExternalCoordinateDataVO> list = externalCoordinateDataService.findPageData(pageNo, pageSize, entity);
        long totalCount = externalCoordinateDataService.count(entity);

        Page<SwmExternalCoordinateDataVO> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setList(list);
        page.setCount(totalCount);

        return page;
    }

    @RequestMapping(value = "exportData", method = RequestMethod.POST)
    @ResponseBody
    public String exportData(SwmExternalCoordinateData entity, HttpServletRequest request, HttpServletResponse response) {
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

            List<SwmExternalCoordinateDataVO> list = externalCoordinateDataService.findPageData(pageNo, pageSize, entity);
            if (list.isEmpty()) {
                return renderResult(Global.FALSE, text("没有符合条件的数据可以导出！"));
            }

            String fileName = "external_coordinate_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, "UTF-8"));

            EasyExcel.write(response.getOutputStream(), SwmExternalCoordinateDataExcelVO.class)
                .sheet("外部坐标数据")
                .doWrite(convertToExcelVOList(list, pageNo, pageSize));

            return null;
        } catch (IOException e) {
            logger.error("导出外部坐标数据失败", e);
            return renderResult(Global.FALSE, text("导出失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("导出外部坐标数据异常", e);
            return renderResult(Global.FALSE, text("导出异常：" + e.getMessage()));
        }
    }

    private List<SwmExternalCoordinateDataExcelVO> convertToExcelVOList(List<SwmExternalCoordinateDataVO> list, int pageNo, int pageSize) {
        List<SwmExternalCoordinateDataExcelVO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SwmExternalCoordinateDataVO vo = list.get(i);
            SwmExternalCoordinateDataExcelVO excelVO = convertToExcelVO(vo);
            excelVO.setRowNum((pageNo - 1) * pageSize + i + 1);
            result.add(excelVO);
        }
        return result;
    }

    private SwmExternalCoordinateDataExcelVO convertToExcelVO(SwmExternalCoordinateDataVO vo) {
        SwmExternalCoordinateDataExcelVO excelVO = new SwmExternalCoordinateDataExcelVO();
        excelVO.setTime(vo.getTimeText());
        excelVO.setAddress(vo.getAddress());
        excelVO.setMapId(vo.getMapId());
        excelVO.setX(vo.getX());
        excelVO.setY(vo.getY());
        excelVO.setOrgCd(vo.getOrgCd());
        excelVO.setTimeStr(vo.getTimeStr());
        excelVO.setType(vo.getType());
        excelVO.setAppId(vo.getAppId());
        excelVO.setWarningId(vo.getWarningId());
        excelVO.setOriginalX(vo.getOriginalX());
        excelVO.setOriginalY(vo.getOriginalY());
        excelVO.setScaleX(vo.getScaleX());
        excelVO.setScaleY(vo.getScaleY());
        excelVO.setNearestBeacon(vo.getNearestBeacon());
        excelVO.setUsedBeacons(vo.getUsedBeacons());
        excelVO.setElderId(vo.getElderId());
        excelVO.setIdCard(vo.getIdCard());
        return excelVO;
    }

    public static class SwmExternalCoordinateDataExcelVO {
        @com.alibaba.excel.annotation.ExcelProperty("序号")
        private Integer rowNum;

        @com.alibaba.excel.annotation.ExcelProperty("time")
        private String time;

        @com.alibaba.excel.annotation.ExcelProperty("address")
        private String address;

        @com.alibaba.excel.annotation.ExcelProperty("map_id")
        private Integer mapId;

        @com.alibaba.excel.annotation.ExcelProperty("x")
        private Double x;

        @com.alibaba.excel.annotation.ExcelProperty("y")
        private Double y;

        @com.alibaba.excel.annotation.ExcelProperty("org_cd")
        private String orgCd;

        @com.alibaba.excel.annotation.ExcelProperty("time_str")
        private String timeStr;

        @com.alibaba.excel.annotation.ExcelProperty("type")
        private String type;

        @com.alibaba.excel.annotation.ExcelProperty("app_id")
        private String appId;

        @com.alibaba.excel.annotation.ExcelProperty("warning_id")
        private Integer warningId;

        @com.alibaba.excel.annotation.ExcelProperty("original_x")
        private Double originalX;

        @com.alibaba.excel.annotation.ExcelProperty("original_y")
        private Double originalY;

        @com.alibaba.excel.annotation.ExcelProperty("scale_x")
        private Double scaleX;

        @com.alibaba.excel.annotation.ExcelProperty("scale_y")
        private Double scaleY;

        @com.alibaba.excel.annotation.ExcelProperty("nearest_beacon")
        private String nearestBeacon;

        @com.alibaba.excel.annotation.ExcelProperty("used_beacons")
        private String usedBeacons;

        @com.alibaba.excel.annotation.ExcelProperty("elder_id")
        private String elderId;

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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getMapId() {
            return mapId;
        }

        public void setMapId(Integer mapId) {
            this.mapId = mapId;
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

        public String getOrgCd() {
            return orgCd;
        }

        public void setOrgCd(String orgCd) {
            this.orgCd = orgCd;
        }

        public String getTimeStr() {
            return timeStr;
        }

        public void setTimeStr(String timeStr) {
            this.timeStr = timeStr;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public Integer getWarningId() {
            return warningId;
        }

        public void setWarningId(Integer warningId) {
            this.warningId = warningId;
        }

        public Double getOriginalX() {
            return originalX;
        }

        public void setOriginalX(Double originalX) {
            this.originalX = originalX;
        }

        public Double getOriginalY() {
            return originalY;
        }

        public void setOriginalY(Double originalY) {
            this.originalY = originalY;
        }

        public Double getScaleX() {
            return scaleX;
        }

        public void setScaleX(Double scaleX) {
            this.scaleX = scaleX;
        }

        public Double getScaleY() {
            return scaleY;
        }

        public void setScaleY(Double scaleY) {
            this.scaleY = scaleY;
        }

        public String getNearestBeacon() {
            return nearestBeacon;
        }

        public void setNearestBeacon(String nearestBeacon) {
            this.nearestBeacon = nearestBeacon;
        }

        public String getUsedBeacons() {
            return usedBeacons;
        }

        public void setUsedBeacons(String usedBeacons) {
            this.usedBeacons = usedBeacons;
        }

        public String getElderId() {
            return elderId;
        }

        public void setElderId(String elderId) {
            this.elderId = elderId;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }
}
