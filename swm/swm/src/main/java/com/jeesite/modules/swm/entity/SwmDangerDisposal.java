/**
 * 隐患处置信息实体类
 * @author Shawn
 * @date 2024-06-28
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 隐患处置信息实体类
 * 
 * @author Shawn
 * @date 2024-06-28
 */
@Table(name = "swm_danger_disposal", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "唯一标识ID", isPK = true),
        @Column(name = "hidden_danger_id", attrName = "hiddenDangerId", label = "关联的隐患ID"),
        @Column(name = "danger_name", attrName = "dangerName", label = "隐患名称", queryType = QueryType.LIKE),
        @Column(name = "location", attrName = "location", label = "隐患位置", queryType = QueryType.LIKE),
        @Column(name = "disposal_time", attrName = "disposalTime", label = "处置时间"),
        @Column(name = "disposal_plan", attrName = "disposalPlan", label = "处置方案描述"),
        @Column(name = "disposer", attrName = "disposer", label = "处置人"),
        @Column(name = "attachment", attrName = "attachment", label = "附件信息"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.create_date DESC")
public class SwmDangerDisposal extends DataEntity<SwmDangerDisposal> {

    private static final long serialVersionUID = 1L;

    private String hiddenDangerId; // 关联的隐患ID
    private String dangerName; // 隐患名称
    private String location; // 隐患位置
    private Date disposalTime; // 处置时间
    private String disposalPlan; // 处置方案描述
    private String disposer; // 处置人
    private String attachment; // 附件信息

    public SwmDangerDisposal() {
        this(null);
    }

    public SwmDangerDisposal(String id) {
        super(id);
    }

    @NotBlank(message = "关联的隐患ID不能为空")
    @Length(min = 0, max = 64, message = "关联的隐患ID长度不能超过 64 个字符")
    public String getHiddenDangerId() {
        return hiddenDangerId;
    }

    public void setHiddenDangerId(String hiddenDangerId) {
        this.hiddenDangerId = hiddenDangerId;
    }

    @Length(min = 0, max = 255, message = "隐患名称长度不能超过 255 个字符")
    public String getDangerName() {
        return dangerName;
    }

    public void setDangerName(String dangerName) {
        this.dangerName = dangerName;
    }

    @Length(min = 0, max = 500, message = "隐患位置长度不能超过 500 个字符")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getDisposalTime() {
        return disposalTime;
    }

    public void setDisposalTime(Date disposalTime) {
        this.disposalTime = disposalTime;
    }

    public String getDisposalPlan() {
        return disposalPlan;
    }

    public void setDisposalPlan(String disposalPlan) {
        this.disposalPlan = disposalPlan;
    }

    @Length(min = 0, max = 64, message = "处置人长度不能超过 64 个字符")
    public String getDisposer() {
        return disposer;
    }

    public void setDisposer(String disposer) {
        this.disposer = disposer;
    }

    @Length(min = 0, max = 1000, message = "附件信息长度不能超过 1000 个字符")
    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
}