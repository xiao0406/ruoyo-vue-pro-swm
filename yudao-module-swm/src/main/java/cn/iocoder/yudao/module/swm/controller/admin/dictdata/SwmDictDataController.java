package cn.iocoder.yudao.module.swm.controller.admin.dictdata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictDataDO;
import cn.iocoder.yudao.module.swm.service.SwmDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 字典数据")
@RestController
@RequestMapping("/swm/dict-data")
@Validated
public class SwmDictDataController {

    @Resource
    private SwmDictDataService dictDataService;

    @PostMapping("/create")
    @Operation(summary = "创建字典数据")
    @PreAuthorize("@ss.hasPermission('swm:dict-data:create')")
    public CommonResult<String> createSwmDictData(@Valid @RequestBody SwmDictDataSaveReqVO createReqVO) {
        String id = dictDataService.createDictData(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新字典数据")
    @PreAuthorize("@ss.hasPermission('swm:dict-data:update')")
    public CommonResult<Boolean> updateSwmDictData(@Valid @RequestBody SwmDictDataSaveReqVO updateReqVO) {
        dictDataService.updateDictData(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典数据")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:dict-data:delete')")
    public CommonResult<Boolean> deleteSwmDictData(@RequestParam("id") String id) {
        dictDataService.deleteDictData(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取字典数据")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:dict-data:query')")
    public CommonResult<SwmDictDataRespVO> getSwmDictData(@RequestParam("id") String id) {
        SwmDictDataDO dictData = dictDataService.getDictData(id);
        return success(BeanUtils.toBean(dictData, SwmDictDataRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询字典数据")
    @PreAuthorize("@ss.hasPermission('swm:dict-data:query')")
    public CommonResult<PageResult<SwmDictDataRespVO>> getSwmDictDataPage(@Valid SwmDictDataPageReqVO pageReqVO) {
        PageResult<SwmDictDataDO> pageResult = dictDataService.getDictDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmDictDataRespVO.class));
    }

}
