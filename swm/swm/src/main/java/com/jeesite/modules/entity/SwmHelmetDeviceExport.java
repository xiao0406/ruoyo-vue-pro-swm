package com.jeesite.modules.entity;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

@Data
public class SwmHelmetDeviceExport {

    @ExcelFields({
            @ExcelField(title = "设备编码", attrName = "deviceId", align = ExcelField.Align.CENTER, sort = 10,width = 256*30),
            @ExcelField(title = "头盔类型(1:便携式 2:头箍式)", attrName = "helmetType", align = ExcelField.Align.CENTER, sort = 10,width = 256*30),
            @ExcelField(title = "MAC地址", attrName = "macAddress", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
    })

    public SwmHelmetDeviceExport() {

    }

    private String deviceId;
    private String helmetType;
    private String macAddress;

}
