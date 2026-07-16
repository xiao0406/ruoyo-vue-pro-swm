package cn.iocoder.yudao.module.swm.controller.admin.legacy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDifyDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyPersonTrainingDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDifyMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSafetyPersonTrainingMapper;
import cn.iocoder.yudao.module.swm.service.SwmDifyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Real implementations for report pages retained under their JeeSite URLs.
 * The URLs remain stable for the migrated Vue pages, while persistence and
 * tenant isolation use RuoYi/Yudao infrastructure.
 */
@Tag(name = "管理后台 - SWM 报表兼容接口")
@RestController
public class SwmLegacyReportController {

    @Resource private SwmDifyMapper difyMapper;
    @Resource private SwmDifyService difyService;
    @Resource private SwmSafetyPersonTrainingMapper trainingMapper;

    @RequestMapping(value = "/swm/swmDify/pageList", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "AI 日报分页")
    public CommonResult<PageResult<SwmDifyDO>> getDifyPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(Math.max(pageNo, 1));
        pageParam.setPageSize(Math.min(Math.max(pageSize, 1), 200));
        LambdaQueryWrapper<SwmDifyDO> query = new LambdaQueryWrapper<SwmDifyDO>()
                .ge(startDate != null, SwmDifyDO::getDate, startDate == null ? null : startDate.atStartOfDay())
                .lt(endDate != null, SwmDifyDO::getDate,
                        endDate == null ? null : endDate.plusDays(1).atStartOfDay())
                .orderByDesc(SwmDifyDO::getDate);
        return success(difyMapper.selectPage(pageParam, query));
    }

    @GetMapping("/swm/swmDify/list")
    @Operation(summary = "AI 日报列表")
    public CommonResult<List<SwmDifyDO>> getDifyList() {
        return success(difyMapper.selectList(new LambdaQueryWrapper<SwmDifyDO>()
                .orderByDesc(SwmDifyDO::getDate)));
    }

    @GetMapping("/swm/swmDify/form")
    @Operation(summary = "AI 日报详情")
    public CommonResult<Map<String, Object>> getDify(@RequestParam("id") String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("swmDify", difyService.getSwmDify(id));
        return success(result);
    }

    @PostMapping("/swm/swmDify/save")
    @Operation(summary = "保存 AI 日报")
    public CommonResult<String> saveDify(@RequestBody SwmDifyDO dify) {
        difyService.save(dify);
        return success(dify.getId());
    }

    @RequestMapping(value = "/swm/swmDify/delete", method = {RequestMethod.GET, RequestMethod.DELETE})
    @Operation(summary = "删除 AI 日报")
    public CommonResult<Boolean> deleteDify(@RequestParam("id") String id) {
        difyMapper.deleteById(id);
        return success(true);
    }

    @RequestMapping(value = "/swm/swmSafetyPersonTraining/pageList", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "视频培训记录分页")
    public CommonResult<PageResult<SwmSafetyPersonTrainingDO>> getTrainingPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String personName,
            @RequestParam(required = false) String pushDate,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String prodLine,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String completeStatus) {
        List<SwmSafetyPersonTrainingDO> list = trainingMapper.findList(personName, pushDate, title,
                department, prodLine, team, company, TenantContextHolder.getTenantId());
        if (completeStatus != null && !completeStatus.isBlank()) {
            list = list.stream().filter(item -> completeStatus.equals(item.getCompleteStatus())).toList();
        }
        int size = Math.min(Math.max(pageSize, 1), 200);
        int from = Math.min((Math.max(pageNo, 1) - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return success(new PageResult<>(list.subList(from, to), (long) list.size()));
    }

    @GetMapping("/swm/swmSafetyPersonTraining/list")
    @Operation(summary = "视频培训记录列表")
    public CommonResult<List<SwmSafetyPersonTrainingDO>> getTrainingList() {
        return success(trainingMapper.findList(null, null, null, null, null, null, null,
                TenantContextHolder.getTenantId()));
    }

    @GetMapping("/swm/swmSafetyPersonTraining/form")
    @Operation(summary = "视频培训记录详情")
    public CommonResult<SwmSafetyPersonTrainingDO> getTraining(@RequestParam("id") String id) {
        return success(trainingMapper.selectById(id));
    }

    @PostMapping("/swm/swmSafetyPersonTraining/save")
    @Operation(summary = "保存视频培训记录")
    public CommonResult<String> saveTraining(@RequestBody SwmSafetyPersonTrainingDO training) {
        if (training.getId() == null || training.getId().isBlank()) {
            trainingMapper.insert(training);
        } else {
            trainingMapper.updateById(training);
        }
        return success(training.getId());
    }

    @RequestMapping(value = "/swm/swmSafetyPersonTraining/delete", method = {RequestMethod.GET, RequestMethod.DELETE})
    @Operation(summary = "删除视频培训记录")
    public CommonResult<Boolean> deleteTraining(@RequestParam("id") String id) {
        trainingMapper.deleteById(id);
        return success(true);
    }
}
