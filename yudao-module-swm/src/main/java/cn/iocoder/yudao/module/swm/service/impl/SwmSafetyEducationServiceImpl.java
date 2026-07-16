package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSafetyEducationMapper;
import cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.swm.service.SwmSafetyEducationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class SwmSafetyEducationServiceImpl implements SwmSafetyEducationService {
    @Resource
    private SwmSafetyEducationMapper mapper;

    @Override
    public String createSwmSafetyEducation(SwmSafetyEducationSaveReqVO reqVO) {
        SwmSafetyEducationDO edu = BeanUtils.toBean(reqVO, SwmSafetyEducationDO.class);
        mapper.insert(edu);
        return edu.getId();
    }

    @Override
    public void updateSwmSafetyEducation(SwmSafetyEducationSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        mapper.updateById(BeanUtils.toBean(reqVO, SwmSafetyEducationDO.class));
    }

    @Override
    public void deleteSwmSafetyEducation(String id) {
        validateExists(id);
        mapper.deleteById(id);
    }

    @Override
    public SwmSafetyEducationDO getSwmSafetyEducation(String id) {
        return mapper.selectById(id);
    }

    @Override
    public PageResult<SwmSafetyEducationDO> getSwmSafetyEducationPage(SwmSafetyEducationPageReqVO pageReqVO) {
        QueryWrapper<SwmSafetyEducationDO> wrapper = new QueryWrapper<SwmSafetyEducationDO>()
                // swm_safety_education is an old JeeSite table whose audit
                // columns are create_time/update_time and it has no remarks
                // column. Select explicit columns to avoid inherited BaseDO
                // fields producing unknown-column SQL.
                .select("id",
                        "theme",
                        "content_description",
                        "safety_education_type",
                        "start_time",
                        "participants",
                        "participants_name",
                        "safety_status",
                        "participation_type",
                        "attachment_url",
                        "create_time AS create_date",
                        "update_time AS update_date",
                        "status",
                        "tenant_id")
                .like(pageReqVO.getTheme() != null && !pageReqVO.getTheme().isBlank(),
                        "theme", pageReqVO.getTheme())
                .eq(pageReqVO.getSafetyEducationType() != null && !pageReqVO.getSafetyEducationType().isBlank(),
                        "safety_education_type", pageReqVO.getSafetyEducationType())
                .eq(pageReqVO.getSafetyStatus() != null && !pageReqVO.getSafetyStatus().isBlank(),
                        "safety_status", pageReqVO.getSafetyStatus())
                .orderByDesc("update_time");
        return mapper.selectPage(pageReqVO, wrapper);
    }

    @Override
    public List<SwmSafetyEducationDO> findByIdentityCard(String identityCard) {
        if (identityCard == null || identityCard.isBlank()) {
            return List.of();
        }
        return mapper.selectList(baseSelectWrapper()
                .like("participants", identityCard)
                .orderByDesc("start_time"));
    }

    private QueryWrapper<SwmSafetyEducationDO> baseSelectWrapper() {
        return new QueryWrapper<SwmSafetyEducationDO>().select("id", "theme", "content_description",
                "safety_education_type", "start_time", "participants", "participants_name",
                "safety_status", "participation_type", "attachment_url",
                "create_time AS create_date", "update_time AS update_date", "status", "tenant_id");
    }

    private void validateExists(String id) {
        if (mapper.selectById(id) == null) throw exception(ErrorCodeConstants.SAFETY_EDUCATION_NOT_EXISTS);
    }
}
