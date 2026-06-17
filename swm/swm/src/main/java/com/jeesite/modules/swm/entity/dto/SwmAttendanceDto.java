package com.jeesite.modules.swm.entity.dto;

import com.jeesite.common.entity.DataEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * 工作区考勤时长和怠工（休闲区）停留时长
 */
@Data
public class SwmAttendanceDto extends DataEntity<SwmAttendanceDto> {

    //入参==============
    @ApiModelProperty(value = "考勤日期")
    private Date attendanceDate;
    @ApiModelProperty(value = "身份证号")
    private String identityCard;
    @ApiModelProperty(value = "设备号")
    private String deviceId;
    @ApiModelProperty(value = "区域类型（work-工作区，slack-休闲区），为空时返回所有区域")
    private String types;

    //返回参数============

    @ApiModelProperty(value = "进入时间")
    private Date enterTime;

    @ApiModelProperty(value = "进入区域名称")
    private String enterAreaName;

    @ApiModelProperty(value = "离开时间")
    private Date outTime;

    @ApiModelProperty(value = "离开区域名称")
    private String outAreaName;

    @ApiModelProperty(value = "停留时长")
    private String durationOf;

    @ApiModelProperty(value = "工作区总时长")
    private String  workOf ;

    @ApiModelProperty(value = "休闲区总时长")
    private String  restOf ;

    @ApiModelProperty(value = "工作区数据")
    private List<SwmAttendanceDto> workList ;

    @ApiModelProperty(value = "休闲区数据")
    private List<SwmAttendanceDto> restList ;

    @NotBlank (message = "考勤日期不能为空")
    public Date getAttendanceDate() {
        return attendanceDate;
    }

    @NotBlank (message = "身份证号不能为空")
    public String getIdentityCard() {
        return identityCard;
    }

    @NotBlank (message = "设备号不能为空")
    public String getDeviceId() {
        return deviceId;
    }

}
