package com.jeesite.modules.entity;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

@Data
public class SwmBeaconStationExport {


    @ExcelFields({
            @ExcelField(title = "MAC地址", attrName = "beaconId", align = ExcelField.Align.CENTER, sort = 10,width = 256*20),
            @ExcelField(title = "所在位置", attrName = "location", align = ExcelField.Align.CENTER, sort = 10,width = 256*20),
            @ExcelField(title = "设备名称", attrName = "deviceName", align = ExcelField.Align.CENTER, sort = 20,width = 256*20),
            @ExcelField(title = "所属区域", attrName = "area", align = ExcelField.Align.CENTER, sort = 20,width = 256*20),
            @ExcelField(title = "图纸像素X坐标", attrName = "pixelX", align = ExcelField.Align.CENTER, sort = 20,width = 256*20),
            @ExcelField(title = "图纸像素Y坐标", attrName = "pixelY", align = ExcelField.Align.CENTER, sort = 20,width = 256*20),
            @ExcelField(title = "信标类型", attrName = "beaconType", align = ExcelField.Align.CENTER, sort = 20,width = 256*10,dictType = "beacon_type_enum"),
            @ExcelField(title = "信标状态（在线、离线）", attrName = "beaconStatus", align = ExcelField.Align.CENTER, sort = 20,width = 256*30,dictType = "beacon_status_enum"),
            @ExcelField(title = "major", attrName = "major", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
            @ExcelField(title = "minor", attrName = "minor", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
            @ExcelField(title = "所在建筑", attrName = "building", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
            @ExcelField(title = "所在楼层", attrName = "floor", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
    })

    public SwmBeaconStationExport() {

    }

    private String beaconId; // MAC地址
    private String deviceName; // 设备名称
    private String area; // MAC地址
    private Double pixelX; // 图纸像素X坐标
    private Double pixelY; // 图纸像素Y坐标
    private String location; // 所在位置
    private String beaconType; // 信标类型
    private String beaconStatus; // 信标状态
    private String major;
    private String minor;
    private String floor;      // 楼层名称
    private String building;   // 建筑名称（冗余存储）


}
