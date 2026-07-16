package cn.iocoder.yudao.module.swm.controller.admin.area;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import cn.iocoder.yudao.module.swm.service.SwmAreaService;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmBeaconStationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 区域管理")
@RestController
@RequestMapping("/swm/area")
@Validated
public class SwmAreaController {

    @Resource
    private SwmAreaService areaService;

    @Resource
    private SwmBeaconStationMapper beaconStationMapper;

    @PostMapping("/create")
    @Operation(summary = "创建区域")
    @PreAuthorize("@ss.hasPermission('swm:area:create')")
    public CommonResult<String> createSwmArea(@Valid @RequestBody SwmAreaSaveReqVO createReqVO) {
        String id = areaService.createArea(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新区域")
    @PreAuthorize("@ss.hasPermission('swm:area:update')")
    public CommonResult<Boolean> updateSwmArea(@Valid @RequestBody SwmAreaSaveReqVO updateReqVO) {
        areaService.updateArea(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:area:delete')")
    public CommonResult<Boolean> deleteSwmArea(@RequestParam("id") String id) {
        areaService.deleteArea(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:area:query')")
    public CommonResult<SwmAreaRespVO> getSwmArea(@RequestParam("id") String id) {
        SwmAreaDO area = areaService.getArea(id);
        return success(BeanUtils.toBean(area, SwmAreaRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询区域")
    @PreAuthorize("@ss.hasPermission('swm:area:query')")
    public CommonResult<PageResult<SwmAreaRespVO>> getSwmAreaPage(@Valid SwmAreaPageReqVO pageReqVO) {
        PageResult<SwmAreaDO> pageResult = areaService.getAreaPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmAreaRespVO.class));
    }

    @PostMapping("/save-with-beacons")
    @Operation(summary = "保存区域及关联信标")
    public CommonResult<Map<String, Object>> saveAreaWithBeacons(@RequestBody Map<String, Object> data) {
        SwmAreaSaveReqVO reqVO = new SwmAreaSaveReqVO();
        reqVO.setId(stringValue(data.get("id")));
        reqVO.setAreaName(stringValue(data.get("areaName")));
        reqVO.setAreaType(stringValue(data.get("areaType")));
        reqVO.setAreaColor(stringValue(data.get("areaColor")));
        reqVO.setWorkShop(stringValue(data.get("workShop")));
        reqVO.setVoicePrompt(stringValue(data.get("voicePrompt")));
        reqVO.setFilePath(stringValue(data.get("filePath")));
        reqVO.setRemarks(stringValue(data.get("remarks")));
        List<String> beaconIds = stringList(data.get("bids"));
        reqVO.setBIds(String.join(",", beaconIds));

        String areaId;
        if (reqVO.getId() == null || reqVO.getId().isBlank()) {
            areaId = areaService.createArea(reqVO);
        } else {
            areaService.updateArea(reqVO);
            areaId = reqVO.getId();
        }

        // The old save flow replaced the complete beacon assignment of the area.
        beaconStationMapper.update(null, new LambdaUpdateWrapper<SwmBeaconStationDO>()
                .eq(SwmBeaconStationDO::getArea, areaId)
                .set(SwmBeaconStationDO::getArea, null));
        if (!beaconIds.isEmpty()) {
            beaconStationMapper.update(null, new LambdaUpdateWrapper<SwmBeaconStationDO>()
                    .and(wrapper -> wrapper.in(SwmBeaconStationDO::getId, beaconIds)
                            .or().in(SwmBeaconStationDO::getBeaconId, beaconIds))
                    .set(SwmBeaconStationDO::getArea, areaId)
                    .set(data.get("beaconColor") != null, SwmBeaconStationDO::getBeaconColor,
                            stringValue(data.get("beaconColor"))));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("result", true);
        result.put("id", areaId);
        result.put("message", "保存成功");
        return success(result);
    }

    @GetMapping("/beacon-coordinates")
    @Operation(summary = "获取区域关联信标坐标")
    public CommonResult<Map<String, Object>> getAreaBeaconCoordinates(@RequestParam("areaId") String areaId) {
        List<SwmBeaconStationDO> beacons = beaconStationMapper.selectList(
                new LambdaQueryWrapper<SwmBeaconStationDO>().eq(SwmBeaconStationDO::getArea, areaId));
        List<String> coordinates = beacons.stream()
                .map(item -> item.getMapCoord() != null ? item.getMapCoord()
                        : item.getPixelX() != null && item.getPixelY() != null
                                ? "(" + item.getPixelX() + ", " + item.getPixelY() + ")" : null)
                .filter(value -> value != null && !value.isBlank())
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("coordinates", String.join(", ", coordinates));
        result.put("list", coordinates);
        return success(result);
    }

    @PostMapping("/check-beacon-conflicts")
    @Operation(summary = "检查信标坐标冲突")
    public CommonResult<Map<String, Object>> checkBeaconConflicts(@RequestBody Map<String, Object> data) {
        String currentAreaId = stringValue(data.get("currentAreaId"));
        List<String> coordinates = parseCoordinates(stringValue(data.get("coordinateList")));
        List<SwmBeaconStationDO> conflicts = coordinates.isEmpty() ? Collections.emptyList()
                : beaconStationMapper.selectList(new LambdaQueryWrapper<SwmBeaconStationDO>()
                        .in(SwmBeaconStationDO::getMapCoord, coordinates)
                        .ne(currentAreaId != null && !currentAreaId.isBlank(), SwmBeaconStationDO::getArea, currentAreaId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("hasConflict", !conflicts.isEmpty());
        result.put("hasConflicts", !conflicts.isEmpty());
        result.put("conflictList", conflicts);
        result.put("conflicts", conflicts);
        return success(result);
    }

    @PostMapping("/update-beacon-colors")
    @Operation(summary = "批量更新区域信标颜色")
    public CommonResult<Map<String, Object>> updateAreaBeaconColors(@RequestBody Map<String, Object> data) {
        String areaId = stringValue(data.get("areaId"));
        String beaconColor = stringValue(data.get("beaconColor"));
        int updated = beaconStationMapper.update(null, new LambdaUpdateWrapper<SwmBeaconStationDO>()
                .eq(SwmBeaconStationDO::getArea, areaId)
                .set(SwmBeaconStationDO::getBeaconColor, beaconColor)
                .set(data.get("remarks") != null, SwmBeaconStationDO::getRemarks, stringValue(data.get("remarks"))));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("updated", updated);
        result.put("message", "更新成功");
        return success(result);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> values)) {
            return Collections.emptyList();
        }
        return values.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList();
    }

    private List<String> parseCoordinates(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\(([-+]?\\d+(?:\\.\\d+)?),\\s*([-+]?\\d+(?:\\.\\d+)?)\\)")
                .matcher(value);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            result.add("(" + matcher.group(1) + ", " + matcher.group(2) + ")");
        }
        return result;
    }

}
