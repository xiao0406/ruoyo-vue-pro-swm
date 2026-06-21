package cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo.SwmSafetyHelmetOrderSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyHelmetOrderDO;
import cn.iocoder.yudao.module.swm.service.SwmSafetyHelmetOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 安全帽订单")
@RestController
@RequestMapping("/swm/safety-helmet-order")
@Validated
public class SwmSafetyHelmetOrderController {

    @Resource
    private SwmSafetyHelmetOrderService safetyHelmetOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建安全帽订单")
    @PreAuthorize("@ss.hasPermission('swm:safety-helmet-order:create')")
    public CommonResult<String> createSwmSafetyHelmetOrder(@Valid @RequestBody SwmSafetyHelmetOrderSaveReqVO createReqVO) {
        String id = safetyHelmetOrderService.createSafetyHelmetOrder(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新安全帽订单")
    @PreAuthorize("@ss.hasPermission('swm:safety-helmet-order:update')")
    public CommonResult<Boolean> updateSwmSafetyHelmetOrder(@Valid @RequestBody SwmSafetyHelmetOrderSaveReqVO updateReqVO) {
        safetyHelmetOrderService.updateSafetyHelmetOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安全帽订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:safety-helmet-order:delete')")
    public CommonResult<Boolean> deleteSwmSafetyHelmetOrder(@RequestParam("id") String id) {
        safetyHelmetOrderService.deleteSafetyHelmetOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取安全帽订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:safety-helmet-order:query')")
    public CommonResult<SwmSafetyHelmetOrderRespVO> getSwmSafetyHelmetOrder(@RequestParam("id") String id) {
        SwmSafetyHelmetOrderDO safetyHelmetOrder = safetyHelmetOrderService.getSafetyHelmetOrder(id);
        return success(BeanUtils.toBean(safetyHelmetOrder, SwmSafetyHelmetOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询安全帽订单")
    @PreAuthorize("@ss.hasPermission('swm:safety-helmet-order:query')")
    public CommonResult<PageResult<SwmSafetyHelmetOrderRespVO>> getSwmSafetyHelmetOrderPage(@Valid SwmSafetyHelmetOrderPageReqVO pageReqVO) {
        PageResult<SwmSafetyHelmetOrderDO> pageResult = safetyHelmetOrderService.getSafetyHelmetOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmSafetyHelmetOrderRespVO.class));
    }

}
