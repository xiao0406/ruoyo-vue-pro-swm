package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplatePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplateSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmVoiceTemplateDO;

import jakarta.validation.Valid;
import java.util.List;

public interface SwmVoiceTemplateService {

    String createVoiceTemplate(@Valid SwmVoiceTemplateSaveReqVO createReqVO);

    void updateVoiceTemplate(@Valid SwmVoiceTemplateSaveReqVO updateReqVO);

    void deleteVoiceTemplate(String id);

    SwmVoiceTemplateDO getVoiceTemplate(String id);

    PageResult<SwmVoiceTemplateDO> getVoiceTemplatePage(SwmVoiceTemplatePageReqVO pageReqVO);

    void updateStatus(String id, String status);

    List<SwmVoiceTemplateDO> getActiveVoiceTemplates();

}
