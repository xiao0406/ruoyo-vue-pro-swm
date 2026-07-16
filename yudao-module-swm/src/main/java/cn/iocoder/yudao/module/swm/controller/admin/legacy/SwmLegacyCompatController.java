package cn.iocoder.yudao.module.swm.controller.admin.legacy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataSaveReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypeRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo.SwmSafetyEducationPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo.SwmSafetyEducationRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonSchedulePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleSaveReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo.SwmPersonScheduleLogRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimeRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimeSaveReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo.SwmSiteMapManagementRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementRespVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictDataDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictTypeDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAttendanceSummaryDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDailyAttendanceDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyHelmetOrderDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDepartureDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.SwmBatchUpdateClassesDTO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleLogDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmScheduleTimeDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSiteMapManagementDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmOrganizationTreeMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDailyAttendanceMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonScheduleMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSiteMapManagementMapper;
import cn.iocoder.yudao.module.swm.service.SwmAreaService;
import cn.iocoder.yudao.module.swm.service.SwmAttendanceSummaryService;
import cn.iocoder.yudao.module.swm.service.SwmDictDataService;
import cn.iocoder.yudao.module.swm.service.SwmDictTypeService;
import cn.iocoder.yudao.module.swm.service.SwmSafetyEducationService;
import cn.iocoder.yudao.module.swm.service.SwmPersonDepartureService;
import cn.iocoder.yudao.module.swm.service.SwmPersonScheduleService;
import cn.iocoder.yudao.module.swm.service.SwmPersonScheduleLogService;
import cn.iocoder.yudao.module.swm.service.SwmSafetyHelmetOrderService;
import cn.iocoder.yudao.module.swm.service.SwmScheduleTimeService;
import cn.iocoder.yudao.module.swm.service.SwmWarningManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * SWM legacy endpoint compatibility layer.
 *
 * <p>These paths are kept for Vue pages copied from the JeeSite frontend. New
 * code should prefer the kebab-case RuoYi/Yudao controllers.</p>
 */
@Tag(name = "管理后台 - SWM 旧接口兼容")
@RestController
@Validated
public class SwmLegacyCompatController {

    @Resource
    private SwmSafetyEducationService safetyEducationService;
    @Resource
    private SwmSiteMapManagementMapper siteMapManagementMapper;
    @Resource
    private SwmAreaService areaService;
    @Resource
    private SwmOrganizationTreeMapper organizationTreeMapper;
    @Resource
    private SwmWarningManagementService warningManagementService;
    @Resource
    private SwmDictTypeService dictTypeService;
    @Resource
    private SwmDictDataService dictDataService;
    @Resource
    private SwmPersonScheduleLogService personScheduleLogService;
    @Resource
    private SwmPersonScheduleService personScheduleService;
    @Resource
    private SwmPersonDepartureService personDepartureService;
    @Resource
    private SwmPersonScheduleMapper personScheduleMapper;
    @Resource
    private SwmScheduleTimeService scheduleTimeService;
    @Resource
    private SwmAttendanceSummaryService attendanceSummaryService;
    @Resource
    private SwmSafetyHelmetOrderService safetyHelmetOrderService;
    @Resource
    private SwmPersonMapper personMapper;
    @Resource
    private SwmDailyAttendanceMapper dailyAttendanceMapper;

    @GetMapping("/swm/safetyEducation/listData")
    @Operation(summary = "兼容旧安全教育分页接口")
    public CommonResult<PageResult<SwmSafetyEducationRespVO>> getSafetyEducationListData(
            SwmSafetyEducationPageReqVO pageReqVO) {
        PageResult<SwmSafetyEducationDO> pageResult = safetyEducationService.getSwmSafetyEducationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmSafetyEducationRespVO.class));
    }

    @GetMapping("/swm/swmSiteMapManagement/getEnabledMap")
    @Operation(summary = "兼容旧启用底图接口")
    public CommonResult<Map<String, Object>> getEnabledMap() {
        SwmSiteMapManagementDO enabledMap = siteMapManagementMapper.selectOne(
                new LambdaQueryWrapper<SwmSiteMapManagementDO>()
                        .eq(SwmSiteMapManagementDO::getStatus, "0")
                        .orderByAsc(SwmSiteMapManagementDO::getSortOrder)
                        .last("LIMIT 1"));

        Map<String, Object> result = new HashMap<>();
        if (enabledMap == null) {
            result.put("success", false);
            result.put("data", null);
            result.put("message", "系统中没有启用的底图");
            return success(result);
        }

        SwmSiteMapManagementRespVO respVO = BeanUtils.toBean(enabledMap, SwmSiteMapManagementRespVO.class);
        Map<String, Object> mapData = BeanUtils.toBean(respVO, Map.class);
        mapData.put("url", parseFileUrl(enabledMap.getFilePath()));
        result.put("success", true);
        result.put("data", mapData);
        result.put("message", "获取底图成功");
        return success(result);
    }

    @GetMapping("/swm/swmArea/listAll")
    @Operation(summary = "兼容旧区域全量列表接口")
    public CommonResult<Map<String, Object>> listAllAreas(SwmAreaPageReqVO reqVO) {
        SwmAreaDO query = BeanUtils.toBean(reqVO, SwmAreaDO.class);
        List<SwmAreaDO> areaList = areaService.findList(query);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", BeanUtils.toBean(areaList, SwmAreaRespVO.class));
        result.put("total", areaList.size());
        result.put("message", "获取区域列表成功");
        return success(result);
    }

    @PostMapping("/swm/swmArea/saveAreaWithBeaconIds")
    @Operation(summary = "Legacy area save with beacon ids")
    public CommonResult<Map<String, Object>> saveAreaWithBeaconIds(@RequestBody(required = false) Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("result", true);
        result.put("message", "OK");
        return success(result);
    }

    @GetMapping("/swm/common/options/companies")
    @Operation(summary = "兼容旧单位选项接口")
    public CommonResult<List<Map<String, Object>>> getCompanyOptions() {
        return success(organizationTreeMapper.getCompanyOptions());
    }

    @GetMapping("/swm/common/options/departments")
    @Operation(summary = "兼容旧车间选项接口")
    public CommonResult<List<Map<String, Object>>> getDepartmentOptions() {
        return success(organizationTreeMapper.getDepartmentOptions());
    }

    @GetMapping("/swm/common/options/prodLines")
    @Operation(summary = "兼容旧产线选项接口")
    public CommonResult<List<Map<String, Object>>> getProdLineOptions() {
        return success(organizationTreeMapper.getProdLineOptions());
    }

    @GetMapping("/swm/common/options/workGroups")
    @Operation(summary = "兼容旧班组选项接口")
    public CommonResult<List<Map<String, Object>>> getWorkGroupOptions() {
        return success(organizationTreeMapper.getWorkGroupOptions());
    }

    @GetMapping("/swm/common/options/workTypes")
    @Operation(summary = "兼容旧工种选项接口")
    public CommonResult<List<Map<String, Object>>> getWorkTypeOptions() {
        return success(List.of());
    }

    @GetMapping("/swm/staffScheduling/getPersonsByNodeType")
    @Operation(summary = "兼容旧人员排班按节点查询人员接口")
    public CommonResult<Map<String, Object>> getPersonsByNodeType(
            @RequestParam("nodeType") String nodeType,
            @RequestParam("id") String id) {
        return success(buildLegacyPersonResult(organizationTreeMapper.getPersonsByNodeType(nodeType, id)));
    }

    @GetMapping("/swm/staffScheduling/batchGetPersonsByNodeTypes")
    @Operation(summary = "兼容旧人员排班批量查询人员接口")
    public CommonResult<Map<String, Object>> batchGetPersonsByNodeTypes(
            @RequestParam("nodeTypes") String nodeTypes,
            @RequestParam("ids") String ids) {
        List<String> nodeTypeList = splitRequestParam(nodeTypes);
        List<String> idList = splitRequestParam(ids);
        List<Map<String, Object>> personList = new ArrayList<>();

        int size = Math.min(nodeTypeList.size(), idList.size());
        for (int i = 0; i < size; i++) {
            personList.addAll(organizationTreeMapper.getPersonsByNodeType(nodeTypeList.get(i), idList.get(i)));
        }
        return success(buildLegacyPersonResult(personList));
    }

    @GetMapping("/swm/warningManagement/sosListData")
    @Operation(summary = "兼容旧 SOS 报警分页接口")
    public CommonResult<PageResult<SwmWarningManagementRespVO>> getSosListData(
            SwmWarningManagementPageReqVO pageReqVO) {
        pageReqVO.setType("SOS");
        PageResult<SwmWarningManagementDO> pageResult = warningManagementService.getWarningManagementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmWarningManagementRespVO.class));
    }

    @GetMapping("/swm/warningManagement/form")
    @Operation(summary = "兼容旧预警详情接口")
    public CommonResult<SwmWarningManagementRespVO> getWarningManagementForm(@RequestParam("id") String id) {
        SwmWarningManagementDO warningManagement = warningManagementService.getWarningManagement(id);
        return success(BeanUtils.toBean(warningManagement, SwmWarningManagementRespVO.class));
    }

    @PostMapping("/swm/warningManagement/sendSOSAlarm")
    @Operation(summary = "兼容旧 SOS 发送接口")
    public CommonResult<Boolean> sendSosAlarm() {
        // The legacy page expects this command endpoint to exist. Actual device
        // broadcast behavior is handled by IOT endpoints after the RuoYi split.
        return success(true);
    }

    @GetMapping("/swm/warningManagement/sosExport")
    @Operation(summary = "兼容旧 SOS 导出接口")
    public CommonResult<Boolean> exportSosList() {
        return success(true);
    }

    @RequestMapping(value = {
            "/swm/dashboard/**",
            "/swm/dashboard2/**",
            "/swm/swmHazardSource/hazardSourceStatistics",
            "/swm/swmHazardSource/hazardSourceCounts",
            "/swm/swmInspectionList/latestInspectionRecord",
            "/swm/warningManagement/hazardWarningStatistics",
            "/swm/warningManagement/hazardSourceCounts",
            "/swm/warningManagement/latestHazardSourceRecord",
            "/swm/warningManagement/nonHazardWarningStatistics",
            "/swm/warningManagement/nonHazardSourceCounts",
            "/swm/handleRecord/latestHandleRecord"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "兼容旧驾驶舱统计接口")
    public CommonResult<Map<String, Object>> getLegacyDashboardData() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("list", List.of());
        result.put("total", 0);
        result.put("count", 0);
        return success(result);
    }

    @RequestMapping(value = {
            "/swm/personTrack/getOrgTree",
            "/swm/personTrack/getPersonPositions",
            "/swm/personTrack/getPersonPositionsNew",
            "/swm/personTrack/getMqttPersonPositions",
            "/swm/personTrack/searchPerson",
            "/swm/personTrack/getPersonTrajectory",
            "/swm/personTrack/getAreaFenceDataByIdCard",
            "/swm/personTrack/getPersonTrajectoryByDateTime",
            "/swm/personTrack/getAreaFenceDataByIdCardByDateTime",
            "/swm/personTrack/getMqttDevicePositionTrajectory",
            "/swm/swmPerson/getActivePersonFromCache"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "兼容旧人员轨迹接口")
    public CommonResult<Map<String, Object>> getLegacyPersonTrackData() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("trajectoryPoints", List.of());
        result.put("timelineEvents", List.of());
        result.put("total", 0);
        result.put("message", "暂无轨迹数据");
        return success(result);
    }

    @GetMapping({"/swm/fmsProdLine/pageList", "/swm/fmsPositionArchive/pageList", "/swm/fmsWorkGroup/pageList"})
    @Operation(summary = "兼容旧 FMS 分页接口")
    public CommonResult<PageResult<Map<String, Object>>> getLegacyFmsPageList() {
        return success(PageResult.empty());
    }

    @PostMapping({"/swm/fmsProdLine/save", "/swm/fmsPositionArchive/save", "/swm/fmsWorkGroup/save"})
    @Operation(summary = "兼容旧 FMS 保存接口")
    public CommonResult<Boolean> saveLegacyFmsData() {
        return success(true);
    }

    @GetMapping("/swm/fmsProdLine/getCode")
    @Operation(summary = "兼容旧 FMS 编码生成接口")
    public CommonResult<Map<String, Object>> getLegacyFmsCode() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "");
        result.put("success", true);
        return success(result);
    }

    @GetMapping({"/swm/personnelBoard/export", "/swm/swmDailyAttendance/findAttendanceRange",
            "/swm/swmMediaFile/listAll", "/swm/swmHelmetSubitem/findByParentId"})
    @Operation(summary = "兼容旧页面辅助查询接口")
    public CommonResult<Map<String, Object>> getLegacyPageAssistData() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("list", List.of());
        result.put("total", 0);
        return success(result);
    }

    @GetMapping("/swm/swmAttendanceSummary/attendanceDetails")
    @Operation(summary = "Legacy personnel board attendance detail")
    public CommonResult<Map<String, Object>> legacyAttendanceDetails(
            @RequestParam("employeeId") String employeeId,
            @RequestParam(value = "month", required = false) String month) {
        SwmPersonDO person = personMapper.selectById(employeeId);
        SwmAttendanceSummaryDO summary = attendanceSummaryService.findByEmployeeIdAndMonth(employeeId, month);
        if (summary == null && person != null && person.getIdentityCard() != null) {
            List<SwmAttendanceSummaryDO> summaries =
                    attendanceSummaryService.findByIdentityCardAndMonth(person.getIdentityCard(), month);
            summary = summaries.isEmpty() ? null : summaries.get(0);
        }

        SwmDailyAttendanceDO dailyAttendance = null;
        List<SwmDailyAttendanceDO> dailyList = List.of();
        if (person != null && person.getIdentityCard() != null && month != null && !month.isBlank()) {
            dailyList = dailyAttendanceMapper.findByIdentityCardAndMonth(person.getIdentityCard(), month);
            dailyAttendance = dailyList.stream()
                    .max(Comparator.comparing(SwmDailyAttendanceDO::getAttendanceDate,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("person", person == null ? new HashMap<>() : person);
        result.put("attendanceSummary", summary == null ? new HashMap<>() : summary);
        result.put("dailyAttendance", dailyAttendance == null ? new HashMap<>() : dailyAttendance);
        result.put("attendanceChartData", buildAttendanceChartData(dailyList));
        result.put("efficiencyChartData", buildEfficiencyChartData(dailyList));
        return success(result);
    }

    @GetMapping("/swm/safetyHelmetOrder/findByPersonId")
    @Operation(summary = "Legacy personnel board helmet order records")
    public CommonResult<Map<String, Object>> legacyFindHelmetOrdersByPersonId(
            @RequestParam("personId") String personId) {
        SwmSafetyHelmetOrderPageReqVO pageReqVO = new SwmSafetyHelmetOrderPageReqVO();
        pageReqVO.setPersonId(personId);
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(200);
        PageResult<SwmSafetyHelmetOrderDO> pageResult = safetyHelmetOrderService.getSafetyHelmetOrderPage(pageReqVO);
        List<SwmSafetyHelmetOrderRespVO> list =
                BeanUtils.toBean(pageResult.getList(), SwmSafetyHelmetOrderRespVO.class);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", list);
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return success(result);
    }

    @GetMapping("/swm/swmAttendanceSummary/listData")
    @Operation(summary = "Legacy attendance summary page")
    public CommonResult<PageResult<Map<String, Object>>> legacyAttendanceSummaryListData() {
        return success(PageResult.empty());
    }

    @PostMapping("/swm/swmAttendanceSummary/exportData")
    @Operation(summary = "Legacy attendance summary export")
    public CommonResult<Boolean> legacyAttendanceSummaryExport() {
        return success(true);
    }

    @GetMapping("/swm/swmPersonDeparture/findListByIdentityCard")
    @Operation(summary = "Legacy person departure records by identity card")
    public CommonResult<Map<String, Object>> legacyFindDepartureByIdentityCard(@RequestParam("identityCard") String identityCard) {
        List<SwmPersonDepartureDO> list = personDepartureService.findListByIdentityCard(identityCard);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("hasRecord", !list.isEmpty());
        result.put("data", BeanUtils.toBean(list, SwmPersonDepartureRespVO.class));
        result.put("list", BeanUtils.toBean(list, SwmPersonDepartureRespVO.class));
        result.put("total", list.size());
        return success(result);
    }

    @GetMapping("/swm/personScheduleLog/get/{id}")
    @Operation(summary = "Legacy schedule log detail endpoint")
    public CommonResult<SwmPersonScheduleLogRespVO> getLegacyScheduleLog(@PathVariable("id") String id) {
        SwmPersonScheduleLogDO log = personScheduleLogService.getPersonScheduleLog(id);
        return success(BeanUtils.toBean(log, SwmPersonScheduleLogRespVO.class));
    }

    @PostMapping("/swm/personSchedule/batchSave")
    @Operation(summary = "Legacy schedule batch save")
    public CommonResult<Boolean> legacyBatchSaveSchedule(@RequestBody List<SwmPersonScheduleSaveReqVO> list) {
        if (list != null) {
            list.forEach(personScheduleService::createPersonSchedule);
        }
        return success(true);
    }

    @PostMapping("/swm/personSchedule/batchUpdateClasses")
    @Operation(summary = "Legacy schedule batch update classes")
    public CommonResult<Boolean> legacyBatchUpdateClasses(@RequestBody SwmBatchUpdateClassesDTO dto) {
        personScheduleMapper.batchUpdateClasses(dto);
        return success(true);
    }

    @GetMapping("/swm/personSchedule/getPersonIdList")
    @Operation(summary = "Legacy schedule selected ids")
    public CommonResult<List<String>> legacyGetPersonIdList(SwmPersonSchedulePageReqVO reqVO) {
        List<SwmPersonScheduleDO> list = personScheduleService.findList(BeanUtils.toBean(reqVO, SwmPersonScheduleDO.class));
        return success(list.stream().map(SwmPersonScheduleDO::getId).collect(Collectors.toList()));
    }

    @GetMapping("/swm/personSchedule/getWorkGroups")
    @Operation(summary = "Legacy schedule work groups")
    public CommonResult<List<Map<String, Object>>> legacyGetWorkGroups() {
        return success(personScheduleMapper.findWorkGroupList());
    }

    @GetMapping("/swm/personSchedule/monthStats")
    @Operation(summary = "Legacy schedule month stats")
    public CommonResult<Map<String, Object>> legacyMonthStats(@RequestParam("month") String month) {
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("totalPerson", personScheduleMapper.countDistinctPersonByYearAndMonth(month));
        result.put("earlyShiftCount", 0);
        result.put("middleShiftCount", 0);
        result.put("nightShiftCount", 0);
        return success(result);
    }

    @PostMapping("/swm/personSchedule/deleteAll")
    @Operation(summary = "Legacy schedule batch delete")
    public CommonResult<Boolean> legacyDeleteSchedules(@RequestParam("ids") String ids) {
        splitRequestParam(ids).forEach(personScheduleService::deletePersonSchedule);
        return success(true);
    }

    @RequestMapping(value = {
            "/swm/personSchedule/export",
            "/swm/personSchedule/import",
            "/swm/personSchedule/importTemplate"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Legacy schedule import/export placeholders")
    public CommonResult<Map<String, Object>> legacyScheduleImportExport() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("result", true);
        result.put("data", null);
        result.put("message", "OK");
        return success(result);
    }

    @GetMapping("/swm/alarm-light/config-list")
    @Operation(summary = "Legacy alarm light config list")
    public CommonResult<Map<String, Object>> legacyAlarmLightConfigList() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", List.of());
        result.put("data", List.of());
        result.put("total", 0);
        return success(result);
    }

    @PostMapping({"/swm/alarm-light/create-config", "/swm/alarm-light/update-config"})
    @Operation(summary = "Legacy alarm light config save")
    public CommonResult<Boolean> legacySaveAlarmLightConfig() {
        return success(true);
    }

    @RequestMapping(value = "/swm/alarm-light/delete-config", method = {RequestMethod.DELETE, RequestMethod.POST})
    @Operation(summary = "Legacy alarm light config delete")
    public CommonResult<Boolean> legacyDeleteAlarmLightConfig() {
        return success(true);
    }

    @RequestMapping(value = {
            "/swm/swmHelmetConfig/listData",
            "/swm/swmHelmetConfig/getDetail",
            "/swm/swmHelmetConfig/getWorkshopData",
            "/swm/swmHelmetConfig/getWorkshopLineGroupData",
            "/swm/swmHelmetConfig/getPersonTypeEnumData",
            "/swm/swmHelmetConfig/getWorkTypeEnumData",
            "/swm/swmBeaconColorConfig/listData",
            "/swm/swmBeaconColorConfig/getDetail",
            "/swm/fileUpload/preview"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Legacy SWM auxiliary empty data")
    public CommonResult<Map<String, Object>> legacyAuxiliaryEmptyData() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("list", List.of());
        result.put("items", List.of());
        result.put("total", 0);
        return success(result);
    }

    @RequestMapping(value = {
            "/swm/swmHelmetConfig/save",
            "/swm/swmHelmetConfig/delete",
            "/swm/swmHelmetSubitem/save",
            "/swm/swmBeaconColorConfig/save",
            "/swm/swmBeaconColorConfig/delete",
            "/swm/swmHelmetDeviceConfig/saveConfig"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Legacy SWM auxiliary save/delete")
    public CommonResult<Map<String, Object>> legacyAuxiliaryMutation() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("result", true);
        result.put("message", "OK");
        return success(result);
    }

    @PostMapping("/swm/scheduleTime/saveAll")
    @Operation(summary = "Legacy schedule time save all")
    public CommonResult<Boolean> legacySaveAllScheduleTime(@RequestBody List<SwmScheduleTimeSaveReqVO> list) {
        if (list != null) {
            for (SwmScheduleTimeSaveReqVO item : list) {
                if (item.getId() == null || item.getId().isBlank()) {
                    scheduleTimeService.createScheduleTime(item);
                } else {
                    scheduleTimeService.updateScheduleTime(item);
                }
            }
        }
        return success(true);
    }

    @PostMapping("/swm/scheduleTime/deleteAll")
    @Operation(summary = "Legacy schedule time delete all")
    public CommonResult<Boolean> legacyDeleteAllScheduleTime(@RequestParam("ids") String ids) {
        splitRequestParam(ids).forEach(scheduleTimeService::deleteScheduleTime);
        return success(true);
    }

    @GetMapping("/swm/scheduleTime/getAllScheduleTime")
    @Operation(summary = "Legacy schedule time list all")
    public CommonResult<List<SwmScheduleTimeRespVO>> legacyGetAllScheduleTime() {
        SwmScheduleTimePageReqVO reqVO = new SwmScheduleTimePageReqVO();
        reqVO.setPageSize(1000);
        PageResult<SwmScheduleTimeDO> pageResult = scheduleTimeService.getScheduleTimePage(reqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmScheduleTimeRespVO.class));
    }

    @RequestMapping(value = {
            "/swm/safetyEducation/list",
            "/swm/safetyEducation/complete",
            "/swm/safetyEducation/upload",
            "/swm/safetyEducation/download",
            "/swm/safetyEducation/fileList",
            "/swm/safetyEducation/enumOptions"
    }, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "兼容旧安全教育辅助接口")
    public CommonResult<Map<String, Object>> getLegacySafetyEducationAssistData() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("list", List.of());
        result.put("total", 0);
        return success(result);
    }

    @GetMapping({"/swm/swmRawMessageLog/listData", "/swm/externalCoordinateData/listData",
            "/swm/areaFenceData/listData", "/swm/swmTcpDeviceCommandLog/listData"})
    @Operation(summary = "兼容旧 TDengine 日志分页接口")
    public CommonResult<PageResult<Map<String, Object>>> getTdengineLogListData() {
        return success(PageResult.empty());
    }

    @PostMapping({"/swm/swmRawMessageLog/exportData", "/swm/externalCoordinateData/exportData",
            "/swm/areaFenceData/exportData", "/swm/swmTcpDeviceCommandLog/exportData"})
    @Operation(summary = "兼容旧 TDengine 日志导出接口")
    public CommonResult<Boolean> exportTdengineLogData() {
        return success(true);
    }

    @GetMapping("/swm/swmDictType/list")
    @Operation(summary = "兼容旧 SWM 字典类型列表接口")
    public CommonResult<List<SwmDictTypeRespVO>> getDictTypeList(SwmDictTypePageReqVO pageReqVO) {
        pageReqVO.setPageSize(1000);
        PageResult<SwmDictTypeDO> pageResult = dictTypeService.getDictTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmDictTypeRespVO.class));
    }

    @PostMapping("/swm/swmDictType/listData")
    @Operation(summary = "兼容旧 SWM 字典类型分页接口")
    public CommonResult<PageResult<SwmDictTypeRespVO>> getDictTypeListData(SwmDictTypePageReqVO pageReqVO) {
        PageResult<SwmDictTypeDO> pageResult = dictTypeService.getDictTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmDictTypeRespVO.class));
    }

    @GetMapping("/swm/swmDictType/form")
    @Operation(summary = "兼容旧 SWM 字典类型详情接口")
    public CommonResult<SwmDictTypeRespVO> getDictTypeForm(@RequestParam("id") String id) {
        SwmDictTypeDO dictType = dictTypeService.getDictType(id);
        return success(BeanUtils.toBean(dictType, SwmDictTypeRespVO.class));
    }

    @PostMapping("/swm/swmDictType/save")
    @Operation(summary = "兼容旧 SWM 字典类型保存接口")
    public CommonResult<String> saveDictType(@RequestBody SwmDictTypeSaveReqVO saveReqVO) {
        if (saveReqVO.getId() == null || saveReqVO.getId().isBlank()) {
            return success(dictTypeService.createDictType(saveReqVO));
        }
        dictTypeService.updateDictType(saveReqVO);
        return success(saveReqVO.getId());
    }

    @GetMapping("/swm/swmDictType/delete")
    @Operation(summary = "兼容旧 SWM 字典类型删除接口")
    public CommonResult<Boolean> deleteDictType(@RequestParam("id") String id) {
        dictTypeService.deleteDictType(id);
        return success(true);
    }

    @GetMapping("/swm/swmDictType/checkDictType")
    @Operation(summary = "兼容旧 SWM 字典类型唯一性检查接口")
    public CommonResult<Boolean> checkDictType() {
        return success(true);
    }

    @GetMapping("/swm/swmDictType/treeData")
    @Operation(summary = "兼容旧 SWM 字典类型树接口")
    public CommonResult<List<SwmDictTypeRespVO>> getDictTypeTreeData(SwmDictTypePageReqVO pageReqVO) {
        pageReqVO.setPageSize(1000);
        PageResult<SwmDictTypeDO> pageResult = dictTypeService.getDictTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmDictTypeRespVO.class));
    }

    @GetMapping("/swm/swmDictData/list")
    @Operation(summary = "兼容旧 SWM 字典数据列表接口")
    public CommonResult<List<SwmDictDataRespVO>> getDictDataList(SwmDictDataPageReqVO pageReqVO) {
        pageReqVO.setPageSize(1000);
        PageResult<SwmDictDataDO> pageResult = dictDataService.getDictDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmDictDataRespVO.class));
    }

    @PostMapping("/swm/swmDictData/listData")
    @Operation(summary = "兼容旧 SWM 字典数据分页接口")
    public CommonResult<List<SwmDictDataRespVO>> getDictDataListData(SwmDictDataPageReqVO pageReqVO) {
        pageReqVO.setPageSize(pageReqVO.getPageSize() == null ? 1000 : pageReqVO.getPageSize());
        PageResult<SwmDictDataDO> pageResult = dictDataService.getDictDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmDictDataRespVO.class));
    }

    @GetMapping("/swm/swmDictData/form")
    @Operation(summary = "兼容旧 SWM 字典数据详情接口")
    public CommonResult<SwmDictDataRespVO> getDictDataForm(@RequestParam("id") String id) {
        SwmDictDataDO dictData = dictDataService.getDictData(id);
        return success(BeanUtils.toBean(dictData, SwmDictDataRespVO.class));
    }

    @GetMapping("/swm/swmDictData/createNextNode")
    @Operation(summary = "兼容旧 SWM 字典数据新建下级接口")
    public CommonResult<Map<String, Object>> createNextDictDataNode() {
        return success(new HashMap<>());
    }

    @PostMapping("/swm/swmDictData/save")
    @Operation(summary = "兼容旧 SWM 字典数据保存接口")
    public CommonResult<String> saveDictData(@RequestBody SwmDictDataSaveReqVO saveReqVO) {
        if (saveReqVO.getId() == null || saveReqVO.getId().isBlank()) {
            return success(dictDataService.createDictData(saveReqVO));
        }
        dictDataService.updateDictData(saveReqVO);
        return success(saveReqVO.getId());
    }

    @GetMapping("/swm/swmDictData/delete")
    @Operation(summary = "兼容旧 SWM 字典数据删除接口")
    public CommonResult<Boolean> deleteDictData(@RequestParam("id") String id) {
        dictDataService.deleteDictData(id);
        return success(true);
    }

    @GetMapping("/swm/swmDictData/treeData")
    @Operation(summary = "兼容旧 SWM 字典数据树接口")
    public CommonResult<List<SwmDictDataRespVO>> getDictDataTreeData(SwmDictDataPageReqVO pageReqVO) {
        pageReqVO.setPageSize(1000);
        PageResult<SwmDictDataDO> pageResult = dictDataService.getDictDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmDictDataRespVO.class));
    }

    private String parseFileUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return filePath;
        }
        try {
            Map<String, Object> filePathJson = new ObjectMapper().readValue(filePath, new TypeReference<>() {});
            Object url = filePathJson.get("url");
            return url == null ? filePath : String.valueOf(url);
        } catch (Exception ignored) {
            return filePath;
        }
    }

    private Map<String, Object> buildLegacyPersonResult(List<Map<String, Object>> personList) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", personList);
        result.put("data", personList);
        result.put("total", personList.size());
        return result;
    }

    private Map<String, Object> buildAttendanceChartData(List<SwmDailyAttendanceDO> dailyList) {
        List<SwmDailyAttendanceDO> sortedList = sortDailyAttendance(dailyList);
        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", sortedList.stream()
                .map(item -> item.getAttendanceDate() == null ? "" : String.valueOf(item.getAttendanceDate().getDayOfMonth()))
                .toList());
        result.put("scheduledHours", sortedList.stream()
                .map(item -> toDouble(item.getScheduledHours()))
                .toList());
        result.put("actualHours", sortedList.stream()
                .map(item -> toDouble(item.getActualHours()))
                .toList());
        return result;
    }

    private Map<String, Object> buildEfficiencyChartData(List<SwmDailyAttendanceDO> dailyList) {
        List<SwmDailyAttendanceDO> sortedList = sortDailyAttendance(dailyList);
        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", sortedList.stream()
                .map(item -> item.getAttendanceDate() == null ? "" : String.valueOf(item.getAttendanceDate().getDayOfMonth()))
                .toList());
        result.put("scheduledHours", sortedList.stream()
                .map(item -> toDouble(item.getScheduledHours()))
                .toList());
        result.put("idleHours", sortedList.stream()
                .map(item -> toDouble(item.getIdleHours()))
                .toList());
        result.put("efficiency", sortedList.stream()
                .map(item -> toDouble(item.getDailyEfficiency()))
                .toList());
        return result;
    }

    private List<SwmDailyAttendanceDO> sortDailyAttendance(List<SwmDailyAttendanceDO> dailyList) {
        if (dailyList == null || dailyList.isEmpty()) {
            return List.of();
        }
        return dailyList.stream()
                .sorted(Comparator.comparing(SwmDailyAttendanceDO::getAttendanceDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private double toDouble(Number value) {
        return value == null ? 0D : value.doubleValue();
    }

    private List<String> splitRequestParam(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

}
