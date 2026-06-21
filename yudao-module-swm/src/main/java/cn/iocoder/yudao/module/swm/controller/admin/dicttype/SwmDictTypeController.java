package cn.iocoder.yudao.module.swm.controller.admin.dicttype;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypeRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictTypeDO;
import cn.iocoder.yudao.module.swm.service.SwmDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 字典类型")
@RestController
@RequestMapping("/swm/dict-type")
@Validated
public class SwmDictTypeController {

    @Resource
    private SwmDictTypeService dictTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建字典类型")
    @PreAuthorize("@ss.hasPermission('swm:dict-type:create')")
    public CommonResult<String> createSwmDictType(@Valid @RequestBody SwmDictTypeSaveReqVO createReqVO) {
        String id = dictTypeService.createDictType(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新字典类型")
    @PreAuthorize("@ss.hasPermission('swm:dict-type:update')")
    public CommonResult<Boolean> updateSwmDictType(@Valid @RequestBody SwmDictTypeSaveReqVO updateReqVO) {
        dictTypeService.updateDictType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:dict-type:delete')")
    public CommonResult<Boolean> deleteSwmDictType(@RequestParam("id") String id) {
        dictTypeService.deleteDictType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取字典类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:dict-type:query')")
    public CommonResult<SwmDictTypeRespVO> getSwmDictType(@RequestParam("id") String id) {
        SwmDictTypeDO dictType = dictTypeService.getDictType(id);
        return success(BeanUtils.toBean(dictType, SwmDictTypeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询字典类型")
    @PreAuthorize("@ss.hasPermission('swm:dict-type:query')")
    public CommonResult<PageResult<SwmDictTypeRespVO>> getSwmDictTypePage(@Valid SwmDictTypePageReqVO pageReqVO) {
        PageResult<SwmDictTypeDO> pageResult = dictTypeService.getDictTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmDictTypeRespVO.class));
    }

}
