package cn.iocoder.yudao.module.swm.controller.admin.personSchedule;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonSchedulePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员排班")
@RestController
@RequestMapping("/swm/person-schedule")
@Validated
public class SwmPersonScheduleController {

    @Resource
    private SwmPersonScheduleService personScheduleService;

    @PostMapping("/create")
    @Operation(summary = "创建人员排班")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:create')")
    public CommonResult<String> createSwmPersonSchedule(@Valid @RequestBody SwmPersonScheduleSaveReqVO createReqVO) {
        String id = personScheduleService.createPersonSchedule(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员排班")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:update')")
    public CommonResult<Boolean> updateSwmPersonSchedule(@Valid @RequestBody SwmPersonScheduleSaveReqVO updateReqVO) {
        personScheduleService.updatePersonSchedule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员排班")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:delete')")
    public CommonResult<Boolean> deleteSwmPersonSchedule(@RequestParam("id") String id) {
        personScheduleService.deletePersonSchedule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员排班")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<SwmPersonScheduleRespVO> getSwmPersonSchedule(@RequestParam("id") String id) {
        SwmPersonScheduleDO personSchedule = personScheduleService.getPersonSchedule(id);
        return success(BeanUtils.toBean(personSchedule, SwmPersonScheduleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员排班")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<PageResult<SwmPersonScheduleRespVO>> getSwmPersonSchedulePage(@Valid SwmPersonSchedulePageReqVO pageReqVO) {
        PageResult<SwmPersonScheduleDO> pageResult = personScheduleService.getPersonSchedulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonScheduleRespVO.class));
    }

}
