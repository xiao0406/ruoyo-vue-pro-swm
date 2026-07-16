package cn.iocoder.yudao.module.swm.api.warning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Public warning creation request shared by IOT and SWM.
 */
@Data
public class SwmWarningCreateReqDTO implements Serializable {

    @NotBlank(message = "deviceId cannot be blank")
    private String deviceId;

    private String idCard;
    private String personName;

    @NotBlank(message = "warningType cannot be blank")
    private String warningType;

    @NotBlank(message = "warningContent cannot be blank")
    private String warningContent;

    private String warningLevel;
    private String warningSource;
    private LocalDateTime warningTime;
    private String alarmRecord;
    private LocalDateTime alarmTime;
    private String triggerReason;
    private String handleStatus;
    private String frontAlarm;
    private String type;
    private String x;
    private String y;
    private String hazardCategory;
    private String location;
    private String area;
    private Boolean autoHandle;
    private Map<String, Object> extraData;
}
