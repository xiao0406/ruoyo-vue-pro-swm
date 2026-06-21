package cn.iocoder.yudao.module.swm.controller.admin.personnelboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonnelBoardDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonnelBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员看板")
@RestController
@RequestMapping("/swm/personnel-board")
@Validated
public class SwmPersonnelBoardController {

    @Resource
    private SwmPersonnelBoardService personnelBoardService;

    @PostMapping("/create")
    @Operation(summary = "创建人员看板")
    @PreAuthorize("@ss.hasPermission('swm:personnel-board:create')")
    public CommonResult<String> createSwmPersonnelBoard(@Valid @RequestBody SwmPersonnelBoardSaveReqVO createReqVO) {
        String id = personnelBoardService.createPersonnelBoard(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员看板")
    @PreAuthorize("@ss.hasPermission('swm:personnel-board:update')")
    public CommonResult<Boolean> updateSwmPersonnelBoard(@Valid @RequestBody SwmPersonnelBoardSaveReqVO updateReqVO) {
        personnelBoardService.updatePersonnelBoard(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员看板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:personnel-board:delete')")
    public CommonResult<Boolean> deleteSwmPersonnelBoard(@RequestParam("id") String id) {
        personnelBoardService.deletePersonnelBoard(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员看板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:personnel-board:query')")
    public CommonResult<SwmPersonnelBoardRespVO> getSwmPersonnelBoard(@RequestParam("id") String id) {
        SwmPersonnelBoardDO personnelBoard = personnelBoardService.getPersonnelBoard(id);
        return success(BeanUtils.toBean(personnelBoard, SwmPersonnelBoardRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员看板")
    @PreAuthorize("@ss.hasPermission('swm:personnel-board:query')")
    public CommonResult<PageResult<SwmPersonnelBoardRespVO>> getSwmPersonnelBoardPage(@Valid SwmPersonnelBoardPageReqVO pageReqVO) {
        PageResult<SwmPersonnelBoardDO> pageResult = personnelBoardService.getPersonnelBoardPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonnelBoardRespVO.class));
    }

}
