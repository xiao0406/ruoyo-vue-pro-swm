package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplatePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo.SwmVoiceTemplateSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmVoiceTemplateDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmVoiceTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.VOICE_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class SwmVoiceTemplateServiceImpl implements SwmVoiceTemplateService {

    @Resource
    private SwmVoiceTemplateMapper swmVoiceTemplateMapper;

    @Override
    public String createVoiceTemplate(SwmVoiceTemplateSaveReqVO createReqVO) {
        SwmVoiceTemplateDO voiceTemplate = BeanUtils.toBean(createReqVO, SwmVoiceTemplateDO.class);
        voiceTemplate.setEnableStatus("0");
        swmVoiceTemplateMapper.insert(voiceTemplate);
        return voiceTemplate.getId();
    }

    @Override
    public void updateVoiceTemplate(SwmVoiceTemplateSaveReqVO updateReqVO) {
        validateVoiceTemplateExists(updateReqVO.getId());
        SwmVoiceTemplateDO updateObj = BeanUtils.toBean(updateReqVO, SwmVoiceTemplateDO.class);
        swmVoiceTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteVoiceTemplate(String id) {
        validateVoiceTemplateExists(id);
        swmVoiceTemplateMapper.deleteById(id);
    }

    @Override
    public SwmVoiceTemplateDO getVoiceTemplate(String id) {
        return swmVoiceTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<SwmVoiceTemplateDO> getVoiceTemplatePage(SwmVoiceTemplatePageReqVO pageReqVO) {
        return swmVoiceTemplateMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmVoiceTemplateDO>()
                        .likeIfPresent(SwmVoiceTemplateDO::getTemplateName, pageReqVO.getTemplateName())
                        .likeIfPresent(SwmVoiceTemplateDO::getTemplateCode, pageReqVO.getTemplateCode())
                        .eqIfPresent(SwmVoiceTemplateDO::getLanguage, pageReqVO.getLanguage())
                        .eqIfPresent(SwmVoiceTemplateDO::getEnableStatus, pageReqVO.getStatus())
                        .orderByDesc(SwmVoiceTemplateDO::getCreateTime));
    }

    @Override
    public void updateStatus(String id, String status) {
        validateVoiceTemplateExists(id);
        swmVoiceTemplateMapper.update(null, new LambdaUpdateWrapper<SwmVoiceTemplateDO>()
                .eq(SwmVoiceTemplateDO::getId, id)
                .set(SwmVoiceTemplateDO::getEnableStatus, status));
    }

    @Override
    public List<SwmVoiceTemplateDO> getActiveVoiceTemplates() {
        return swmVoiceTemplateMapper.selectList(new LambdaQueryWrapper<SwmVoiceTemplateDO>()
                .eq(SwmVoiceTemplateDO::getEnableStatus, "0"));
    }

    private void validateVoiceTemplateExists(String id) {
        if (swmVoiceTemplateMapper.selectById(id) == null) {
            throw exception(VOICE_TEMPLATE_NOT_EXISTS);
        }
    }

}
