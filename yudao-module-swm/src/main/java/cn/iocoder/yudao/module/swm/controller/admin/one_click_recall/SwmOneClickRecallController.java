package cn.iocoder.yudao.module.swm.controller.admin.one_click_recall;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmOneClickRecallDO;
import cn.iocoder.yudao.module.swm.service.SwmOneClickRecallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 一键召回")
@RestController
@RequestMapping("/swm/one-click-recall")
@Validated
public class SwmOneClickRecallController {

    @Resource
    private SwmOneClickRecallService oneClickRecallService;

    @PostMapping("/create")
    @Operation(summary = "创建一键召回")
    @PreAuthorize("@ss.hasPermission('swm:one-click-recall:create')")
    public CommonResult<String> createSwmOneClickRecall(@Valid @RequestBody SwmOneClickRecallSaveReqVO createReqVO) {
        String id = oneClickRecallService.createOneClickRecall(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新一键召回")
    @PreAuthorize("@ss.hasPermission('swm:one-click-recall:update')")
    public CommonResult<Boolean> updateSwmOneClickRecall(@Valid @RequestBody SwmOneClickRecallSaveReqVO updateReqVO) {
        oneClickRecallService.updateOneClickRecall(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除一键召回")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:one-click-recall:delete')")
    public CommonResult<Boolean> deleteSwmOneClickRecall(@RequestParam("id") String id) {
        oneClickRecallService.deleteOneClickRecall(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取一键召回")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:one-click-recall:query')")
    public CommonResult<SwmOneClickRecallRespVO> getSwmOneClickRecall(@RequestParam("id") String id) {
        SwmOneClickRecallDO oneClickRecall = oneClickRecallService.getOneClickRecall(id);
        return success(BeanUtils.toBean(oneClickRecall, SwmOneClickRecallRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询一键召回")
    @PreAuthorize("@ss.hasPermission('swm:one-click-recall:query')")
    public CommonResult<PageResult<SwmOneClickRecallRespVO>> getSwmOneClickRecallPage(@Valid SwmOneClickRecallPageReqVO pageReqVO) {
        PageResult<SwmOneClickRecallDO> pageResult = oneClickRecallService.getOneClickRecallPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmOneClickRecallRespVO.class));
    }

}
