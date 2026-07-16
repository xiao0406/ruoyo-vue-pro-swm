package cn.iocoder.yudao.module.swm.controller.admin.hazard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourcePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourceRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHazardSourceDO;
import cn.iocoder.yudao.module.swm.service.SwmHazardSourceService;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHazardSourceMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmInspectionListMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmInspectionPlanMapper;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionListDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionPlanDO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 危险源管理")
@RestController
@RequestMapping("/swm/hazard-source")
@Validated
public class SwmHazardSourceController {

    @Resource
    private SwmHazardSourceService hazardSourceService;

    @Resource
    private SwmHazardSourceMapper hazardSourceMapper;
    @Resource
    private SwmInspectionPlanMapper inspectionPlanMapper;
    @Resource
    private SwmInspectionListMapper inspectionListMapper;

    @PostMapping("/create")
    @Operation(summary = "创建危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:create')")
    public CommonResult<String> createSwmHazardSource(@Valid @RequestBody SwmHazardSourceSaveReqVO createReqVO) {
        String id = hazardSourceService.createHazardSource(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:update')")
    public CommonResult<Boolean> updateSwmHazardSource(@Valid @RequestBody SwmHazardSourceSaveReqVO updateReqVO) {
        hazardSourceService.updateHazardSource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除危险源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:delete')")
    public CommonResult<Boolean> deleteSwmHazardSource(@RequestParam("id") String id) {
        hazardSourceService.deleteHazardSource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取危险源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<SwmHazardSourceRespVO> getSwmHazardSource(@RequestParam("id") String id) {
        SwmHazardSourceDO hazardSource = hazardSourceService.getHazardSource(id);
        return success(BeanUtils.toBean(hazardSource, SwmHazardSourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<PageResult<SwmHazardSourceRespVO>> getSwmHazardSourcePage(@Valid SwmHazardSourcePageReqVO pageReqVO) {
        PageResult<SwmHazardSourceDO> pageResult = hazardSourceService.getHazardSourcePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmHazardSourceRespVO.class));
    }

    @PostMapping("/temp-save")
    @Operation(summary = "兼容旧前端暂存危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:create')")
    public CommonResult<String> tempSaveHazardSource(@Valid @RequestBody SwmHazardSourceSaveReqVO reqVO) {
        return success(hazardSourceService.createHazardSource(reqVO));
    }

    @GetMapping("/find-by-beacon")
    @Operation(summary = "按信标查询危险源")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<Map<String, Object>> findByBeacon(@RequestParam("beaconIdentifier") String beaconIdentifier) {
        List<SwmHazardSourceDO> list = hazardSourceMapper.selectList(
                new LambdaQueryWrapperX<SwmHazardSourceDO>()
                        .likeIfPresent(SwmHazardSourceDO::getBeaconIdentifier, beaconIdentifier)
                        .orderByDesc(SwmHazardSourceDO::getCreateTime));
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", list.isEmpty() ? null : list.get(0));
        result.put("list", list);
        result.put("total", list.size());
        return success(result);
    }

    @GetMapping("/inspection-records")
    @Operation(summary = "危险源巡检记录")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<Map<String, Object>> getInspectionRecords(@RequestParam("hazardSourceId") String hazardSourceId) {
        SwmHazardSourceDO hazardSource = hazardSourceService.getHazardSource(hazardSourceId);
        List<SwmInspectionPlanDO> plans = inspectionPlanMapper.selectList(
                new LambdaQueryWrapperX<SwmInspectionPlanDO>()
                        .eq(SwmInspectionPlanDO::getHazardSourceId, hazardSourceId));
        List<String> planIds = plans.stream().map(SwmInspectionPlanDO::getId).collect(Collectors.toList());
        List<SwmInspectionListDO> records = planIds.isEmpty() ? List.of()
                : inspectionListMapper.selectList(new LambdaQueryWrapperX<SwmInspectionListDO>()
                        .in(SwmInspectionListDO::getPlanId, planIds)
                        .orderByDesc(SwmInspectionListDO::getStartTime));
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("hazardSource", hazardSource);
        result.put("plans", plans);
        result.put("records", records);
        result.put("data", records);
        result.put("list", records);
        result.put("total", records.size());
        return success(result);
    }

    @GetMapping("/not-patrolled-list")
    @Operation(summary = "未加入巡检危险源列表")
    @PreAuthorize("@ss.hasPermission('swm:hazard-source:query')")
    public CommonResult<List<SwmHazardSourceRespVO>> getNotPatrolledList() {
        List<SwmHazardSourceDO> list = hazardSourceMapper.selectList(
                new LambdaQueryWrapperX<SwmHazardSourceDO>()
                        .eq(SwmHazardSourceDO::getIsPatrolIncluded, "0")
                        .orderByDesc(SwmHazardSourceDO::getCreateTime));
        return success(BeanUtils.toBean(list, SwmHazardSourceRespVO.class));
    }
}
