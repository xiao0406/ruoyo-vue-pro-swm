package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonnelBoardDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonnelBoardMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_NOT_EXISTS;

/**
 * 人员看板 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmPersonnelBoardServiceImpl implements SwmPersonnelBoardService {

    @Resource
    private SwmPersonnelBoardMapper swmPersonnelBoardMapper;

    @Override
    public String createPersonnelBoard(SwmPersonnelBoardSaveReqVO createReqVO) {
        // 插入人员看板
        SwmPersonnelBoardDO personnelBoard = BeanUtils.toBean(createReqVO, SwmPersonnelBoardDO.class);
        swmPersonnelBoardMapper.insert(personnelBoard);
        return personnelBoard.getId();
    }

    @Override
    public void updatePersonnelBoard(SwmPersonnelBoardSaveReqVO updateReqVO) {
        // 校验存在
        validatePersonnelBoardExists(updateReqVO.getId());

        // 更新人员看板
        SwmPersonnelBoardDO updateObj = BeanUtils.toBean(updateReqVO, SwmPersonnelBoardDO.class);
        swmPersonnelBoardMapper.updateById(updateObj);
    }

    @Override
    public void deletePersonnelBoard(String id) {
        // 校验存在
        validatePersonnelBoardExists(id);

        // 删除人员看板
        swmPersonnelBoardMapper.deleteById(id);
    }

    @Override
    public SwmPersonnelBoardDO getPersonnelBoard(String id) {
        return swmPersonnelBoardMapper.selectById(id);
    }

    @Override
    public PageResult<SwmPersonnelBoardDO> getPersonnelBoardPage(SwmPersonnelBoardPageReqVO pageReqVO) {
        return swmPersonnelBoardMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validatePersonnelBoardExists(String id) {
        if (swmPersonnelBoardMapper.selectById(id) == null) {
            throw exception(PERSON_NOT_EXISTS);
        }
    }

}
