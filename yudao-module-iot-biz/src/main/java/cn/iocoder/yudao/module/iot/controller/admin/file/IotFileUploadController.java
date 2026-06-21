package cn.iocoder.yudao.module.iot.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.file.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotFileUploadDO;
import cn.iocoder.yudao.module.iot.service.IotFileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT文件上传")
@RestController
@RequestMapping("/iot/file-upload")
@Validated
public class IotFileUploadController {

    @Resource
    private IotFileUploadService iotFileUploadService;

    @PostMapping("/create")
    @Operation(summary = "创建文件上传记录")
    @PreAuthorize("@ss.hasPermission('iot:file-upload:create')")
    public CommonResult<String> create(@Valid @RequestBody IotFileUploadSaveReqVO reqVO) {
        return success(iotFileUploadService.createIotFileUpload(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文件上传记录")
    @PreAuthorize("@ss.hasPermission('iot:file-upload:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody IotFileUploadSaveReqVO reqVO) {
        iotFileUploadService.updateIotFileUpload(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件上传记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:file-upload:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") String id) {
        iotFileUploadService.deleteIotFileUpload(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取文件上传记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:file-upload:query')")
    public CommonResult<IotFileUploadRespVO> get(@RequestParam("id") String id) {
        IotFileUploadDO upload = iotFileUploadService.getIotFileUpload(id);
        return success(BeanUtils.toBean(upload, IotFileUploadRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询文件上传记录")
    @PreAuthorize("@ss.hasPermission('iot:file-upload:query')")
    public CommonResult<PageResult<IotFileUploadRespVO>> getPage(@Valid IotFileUploadPageReqVO pageReqVO) {
        PageResult<IotFileUploadDO> pageResult = iotFileUploadService.getIotFileUploadPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotFileUploadRespVO.class));
    }
}
