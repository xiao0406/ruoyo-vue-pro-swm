package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonnelBoardDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonnelBoardMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
        List<SwmPersonnelBoardDO> allList = swmPersonnelBoardMapper.findList(
                pageReqVO.getName(),
                pageReqVO.getOrganization(),
                pageReqVO.getWorkshop(),
                pageReqVO.getProcess(),
                pageReqVO.getTeam(),
                null,
                pageReqVO.getPersonnelStatus(),
                pageReqVO.getTimeType(),
                pageReqVO.getTimeValue());
        List<SwmPersonnelBoardDO> filteredList = filterByWorkStatus(allList, pageReqVO.getWorkStatus());
        return buildPageResult(filteredList, pageReqVO.getPageNo(), pageReqVO.getPageSize());
    }

    private List<SwmPersonnelBoardDO> filterByWorkStatus(List<SwmPersonnelBoardDO> list, String workStatus) {
        if (workStatus == null || workStatus.isBlank() || list == null || list.isEmpty()) {
            return list;
        }
        return list.stream()
                .filter(item -> workStatus.equals(item.getWorkStatus()))
                .collect(Collectors.toList());
    }

    private PageResult<SwmPersonnelBoardDO> buildPageResult(List<SwmPersonnelBoardDO> list, Integer pageNo, Integer pageSize) {
        if (list == null || list.isEmpty()) {
            return PageResult.empty();
        }
        int currentPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int currentPageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int fromIndex = Math.min((currentPageNo - 1) * currentPageSize, list.size());
        int toIndex = Math.min(fromIndex + currentPageSize, list.size());
        return new PageResult<>(new ArrayList<>(list.subList(fromIndex, toIndex)), (long) list.size());
    }

    private void validatePersonnelBoardExists(String id) {
        if (swmPersonnelBoardMapper.selectById(id) == null) {
            throw exception(PERSON_NOT_EXISTS);
        }
    }

}
