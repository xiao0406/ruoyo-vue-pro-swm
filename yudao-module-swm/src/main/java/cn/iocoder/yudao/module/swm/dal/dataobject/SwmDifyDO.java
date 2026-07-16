package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Dify 配置 DO
 * 表: swm_dify
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_dify")
public class SwmDifyDO extends SwmBaseDO {

    private LocalDateTime date;
    private String projectName;
    private String manufacture;
    private Integer teamActualHours;
    private String text;
    private BigDecimal safetyIndex;
    private BigDecimal workerIndex;

    /** Runtime-only Dify mode used by the report job; the legacy table has no such column. */
    @TableField(exist = false)
    private String difyType;
    @TableField(exist = false)
    private BigDecimal attendanceIndex;

    public String getDifyName() {
        return projectName;
    }

    public void setDifyName(String difyName) {
        this.projectName = difyName;
    }

    public String getReportContent() {
        return text;
    }

    public void setReportContent(String reportContent) {
        this.text = reportContent;
    }

    public void setReportDate(String reportDate) {
        this.date = reportDate == null || reportDate.isBlank()
                ? null : java.time.LocalDate.parse(reportDate).atStartOfDay();
    }

}
