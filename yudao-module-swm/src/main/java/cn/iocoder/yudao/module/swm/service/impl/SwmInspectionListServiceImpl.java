package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.inspectionlist.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionListDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmInspectionListMapper;
import cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.swm.enums.SwmEnums;
import cn.iocoder.yudao.module.swm.service.SwmInspectionListService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class SwmInspectionListServiceImpl implements SwmInspectionListService {
    @Resource
    private SwmInspectionListMapper mapper;

    @Override
    public String createSwmInspectionList(SwmInspectionListSaveReqVO reqVO) {
        SwmInspectionListDO list = BeanUtils.toBean(reqVO, SwmInspectionListDO.class);
        mapper.insert(list);
        return list.getId();
    }

    @Override
    public void updateSwmInspectionList(SwmInspectionListSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        mapper.updateById(BeanUtils.toBean(reqVO, SwmInspectionListDO.class));
    }

    @Override
    public void deleteSwmInspectionList(String id) {
        validateExists(id);
        mapper.deleteById(id);
    }

    @Override
    public SwmInspectionListDO getSwmInspectionList(String id) {
        return mapper.selectById(id);
    }

    @Override
    public PageResult<SwmInspectionListDO> getSwmInspectionListPage(SwmInspectionListPageReqVO pageReqVO) {
        return mapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmInspectionListDO>()
                        .orderByDesc(SwmInspectionListDO::getStartTime));
    }

    private void validateExists(String id) {
        if (mapper.selectById(id) == null) throw exception(ErrorCodeConstants.INSPECTION_LIST_NOT_EXISTS);
    }

    @Override
    public void createSwmInspectionList(SwmInspectionListDO inspectionList) {
        mapper.insert(inspectionList);
    }

    @Override
    public boolean existsByPlanIdAndDate(String planId, String dateStr) {
        // dateStr 格式为 yyyy-MM-dd，查询当天 00:00:00 ~ 23:59:59 范围内的记录
        LocalDateTime startOfDay = LocalDateTime.parse(dateStr + "T00:00:00");
        LocalDateTime endOfDay = LocalDateTime.parse(dateStr + "T23:59:59");
        LambdaQueryWrapper<SwmInspectionListDO> wrapper = new LambdaQueryWrapper<SwmInspectionListDO>()
                .eq(SwmInspectionListDO::getPlanId, planId)
                .ge(SwmInspectionListDO::getStartTime, startOfDay)
                .le(SwmInspectionListDO::getStartTime, endOfDay)
                .last("LIMIT 1");
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public SwmInspectionListDO getLastTaskByPlanId(String planId) {
        LambdaQueryWrapper<SwmInspectionListDO> wrapper = new LambdaQueryWrapper<SwmInspectionListDO>()
                .eq(SwmInspectionListDO::getPlanId, planId)
                .orderByDesc(SwmInspectionListDO::getCreateTime)
                .last("LIMIT 1");
        return mapper.selectOne(wrapper);
    }

    @Override
    public SwmInspectionListDO startTask(String id) {
        SwmInspectionListDO task = mapper.selectById(id);
        if (task == null || !SwmEnums.InspectionListStatusEnum.WAIT.getValue()
                .equals(task.getInspectionListStatus())) {
            return null;
        }
        task.setInspectionListStatus(SwmEnums.InspectionListStatusEnum.IN_PROGRESS.getValue());
        task.setStartTime(LocalDateTime.now());
        mapper.updateById(task);
        return task;
    }

    @Override
    public SwmInspectionListDO completeTask(SwmInspectionListSaveReqVO reqVO) {
        SwmInspectionListDO task = mapper.selectById(reqVO.getId());
        if (task == null || !SwmEnums.InspectionListStatusEnum.IN_PROGRESS.getValue()
                .equals(task.getInspectionListStatus())) {
            return null;
        }
        task.setInspectionListStatus(SwmEnums.InspectionListStatusEnum.COMPLETED.getValue());
        task.setEndTime(reqVO.getEndTime() == null ? LocalDateTime.now() : reqVO.getEndTime());
        if (StringUtils.isNotBlank(reqVO.getAttachmentPath())) {
            task.setAttachmentPath(reqVO.getAttachmentPath());
        }
        mapper.updateById(task);
        return task;
    }
}
