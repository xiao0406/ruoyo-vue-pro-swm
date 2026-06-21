package cn.iocoder.yudao.module.swm.controller.admin.mediafile;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFilePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFileRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFileSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmMediaFileDO;
import cn.iocoder.yudao.module.swm.service.SwmMediaFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 媒体文件")
@RestController
@RequestMapping("/swm/media-file")
@Validated
public class SwmMediaFileController {

    @Resource
    private SwmMediaFileService mediaFileService;

    @PostMapping("/create")
    @Operation(summary = "创建媒体文件")
    @PreAuthorize("@ss.hasPermission('swm:media-file:create')")
    public CommonResult<String> createSwmMediaFile(@Valid @RequestBody SwmMediaFileSaveReqVO createReqVO) {
        String id = mediaFileService.createMediaFile(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新媒体文件")
    @PreAuthorize("@ss.hasPermission('swm:media-file:update')")
    public CommonResult<Boolean> updateSwmMediaFile(@Valid @RequestBody SwmMediaFileSaveReqVO updateReqVO) {
        mediaFileService.updateMediaFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除媒体文件")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:media-file:delete')")
    public CommonResult<Boolean> deleteSwmMediaFile(@RequestParam("id") String id) {
        mediaFileService.deleteMediaFile(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取媒体文件")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:media-file:query')")
    public CommonResult<SwmMediaFileRespVO> getSwmMediaFile(@RequestParam("id") String id) {
        SwmMediaFileDO mediaFile = mediaFileService.getMediaFile(id);
        return success(BeanUtils.toBean(mediaFile, SwmMediaFileRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询媒体文件")
    @PreAuthorize("@ss.hasPermission('swm:media-file:query')")
    public CommonResult<PageResult<SwmMediaFileRespVO>> getSwmMediaFilePage(@Valid SwmMediaFilePageReqVO pageReqVO) {
        PageResult<SwmMediaFileDO> pageResult = mediaFileService.getMediaFilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmMediaFileRespVO.class));
    }

}
