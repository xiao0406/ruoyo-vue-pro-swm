/**
 * 隐患信息实体类
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 隐患信息实体类
 * 
 * @author Shawn
 * @date 2023-11-16
 */
@Table(name = "swm_hidden_danger", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "唯一标识ID", isPK = true),
        @Column(name = "danger_name", attrName = "dangerName", label = "隐患名称", queryType = QueryType.LIKE),
        @Column(name = "location", attrName = "location", label = "隐患位置", queryType = QueryType.LIKE),
        @Column(name = "inspection_plan_id", attrName = "inspectionPlanId", label = "关联的巡检计划ID"),
        @Column(name = "is_beacon_deployed", attrName = "isBeaconDeployed", label = "是否布设信标"),
        @Column(name = "is_handled", attrName = "isHandled", label = "是否已处置"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.create_date DESC")
public class SwmHiddenDanger extends DataEntity<SwmHiddenDanger> {

    private static final long serialVersionUID = 1L;

    private String dangerName; // 隐患名称
    private String location; // 隐患位置
    private String inspectionPlanId; // 关联的巡检计划ID
    private String isBeaconDeployed; // 是否布设信标
    private String isHandled; // 是否已处置

    // 用于显示的属性，不对应数据库字段
    private String isBeaconDeployedText; // 是否布设信标显示文本
    private String isHandledText; // 是否已处置显示文本
    private String inspectionPlanName; // 巡检计划名称

    public SwmHiddenDanger() {
        this(null);
    }

    public SwmHiddenDanger(String id) {
        super(id);
    }

    @NotBlank(message = "隐患名称不能为空")
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

    @Length(min = 0, max = 64, message = "关联的巡检计划ID长度不能超过 64 个字符")
    public String getInspectionPlanId() {
        return inspectionPlanId;
    }

    public void setInspectionPlanId(String inspectionPlanId) {
        this.inspectionPlanId = inspectionPlanId;
    }

    @Length(min = 0, max = 5, message = "是否布设信标长度不能超过 5 个字符")
    public String getIsBeaconDeployed() {
        return isBeaconDeployed;
    }

    public void setIsBeaconDeployed(String isBeaconDeployed) {
        this.isBeaconDeployed = isBeaconDeployed;
    }

    @Length(min = 0, max = 5, message = "是否已处置长度不能超过 5 个字符")
    public String getIsHandled() {
        return isHandled;
    }

    public void setIsHandled(String isHandled) {
        this.isHandled = isHandled;
    }

    /**
     * 获取是否布设信标显示文本
     */
    public String getIsBeaconDeployedText() {
        if (this.isBeaconDeployed == null) {
            return "";
        }
        return DictUtils.getDictLabel("is_beacon_deployed_enum", this.isBeaconDeployed, "");
    }

    /**
     * 获取是否已处置显示文本
     */
    public String getIsHandledText() {
        if (this.isHandled == null) {
            return "";
        }
        return DictUtils.getDictLabel("is_handled_enum", this.isHandled, "");
    }

    public String getInspectionPlanName() {
        return inspectionPlanName;
    }

    public void setInspectionPlanName(String inspectionPlanName) {
        this.inspectionPlanName = inspectionPlanName;
    }
}