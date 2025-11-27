package com.jeesite.modules.vo;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

@Data
public class SwmBeaconStationExport {


    @ExcelFields({
            @ExcelField(title = "MAC地址", attrName = "beaconId", align = ExcelField.Align.CENTER, sort = 10,width = 256*30),
            @ExcelField(title = "所属区域", attrName = "area", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
    })

    public SwmBeaconStationExport() {

    }

    private String beaconId; // MAC地址
    private String area; // MAC地址


}
