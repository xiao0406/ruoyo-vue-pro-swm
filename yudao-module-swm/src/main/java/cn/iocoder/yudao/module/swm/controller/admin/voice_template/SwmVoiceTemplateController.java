package cn.iocoder.yudao.module.swm.controller.admin.voice_template;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplatePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplateRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplateSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmVoiceTemplateDO;
import cn.iocoder.yudao.module.swm.service.SwmVoiceTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 语音模板")
@RestController
@RequestMapping("/swm/voice-template")
@Validated
public class SwmVoiceTemplateController {

    @Resource
    private SwmVoiceTemplateService voiceTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建语音模板")
    @PreAuthorize("@ss.hasPermission('swm:voice-template:create')")
    public CommonResult<String> createSwmVoiceTemplate(@Valid @RequestBody SwmVoiceTemplateSaveReqVO createReqVO) {
        String id = voiceTemplateService.createVoiceTemplate(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新语音模板")
    @PreAuthorize("@ss.hasPermission('swm:voice-template:update')")
    public CommonResult<Boolean> updateSwmVoiceTemplate(@Valid @RequestBody SwmVoiceTemplateSaveReqVO updateReqVO) {
        voiceTemplateService.updateVoiceTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除语音模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:voice-template:delete')")
    public CommonResult<Boolean> deleteSwmVoiceTemplate(@RequestParam("id") String id) {
        voiceTemplateService.deleteVoiceTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取语音模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:voice-template:query')")
    public CommonResult<SwmVoiceTemplateRespVO> getSwmVoiceTemplate(@RequestParam("id") String id) {
        SwmVoiceTemplateDO voiceTemplate = voiceTemplateService.getVoiceTemplate(id);
        return success(BeanUtils.toBean(voiceTemplate, SwmVoiceTemplateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询语音模板")
    @PreAuthorize("@ss.hasPermission('swm:voice-template:query')")
    public CommonResult<PageResult<SwmVoiceTemplateRespVO>> getSwmVoiceTemplatePage(@Valid SwmVoiceTemplatePageReqVO pageReqVO) {
        PageResult<SwmVoiceTemplateDO> pageResult = voiceTemplateService.getVoiceTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmVoiceTemplateRespVO.class));
    }

}
