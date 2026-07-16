package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFilePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFileSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmMediaFileDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmMediaFileMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.COMMON_OPTIONS_NOT_EXISTS;

/**
 * 媒体文件 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmMediaFileServiceImpl implements SwmMediaFileService {

    @Resource
    private SwmMediaFileMapper swmMediaFileMapper;

    @Override
    public String createMediaFile(SwmMediaFileSaveReqVO createReqVO) {
        // 插入媒体文件
        SwmMediaFileDO mediaFile = BeanUtils.toBean(createReqVO, SwmMediaFileDO.class);
        swmMediaFileMapper.insert(mediaFile);
        return mediaFile.getId();
    }

    @Override
    public void updateMediaFile(SwmMediaFileSaveReqVO updateReqVO) {
        // 校验存在
        validateMediaFileExists(updateReqVO.getId());

        // 更新媒体文件
        SwmMediaFileDO updateObj = BeanUtils.toBean(updateReqVO, SwmMediaFileDO.class);
        swmMediaFileMapper.updateById(updateObj);
    }

    @Override
    public void deleteMediaFile(String id) {
        // 校验存在
        validateMediaFileExists(id);

        // 删除媒体文件
        swmMediaFileMapper.deleteById(id);
    }

    @Override
    public SwmMediaFileDO getMediaFile(String id) {
        return swmMediaFileMapper.selectById(id);
    }

    @Override
    public PageResult<SwmMediaFileDO> getMediaFilePage(SwmMediaFilePageReqVO pageReqVO) {
        return swmMediaFileMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmMediaFileDO>()
                        .likeIfPresent(SwmMediaFileDO::getFileName, pageReqVO.getFileName())
                        .eqIfPresent(SwmMediaFileDO::getFileType, pageReqVO.getFileType())
                        .eqIfPresent(SwmMediaFileDO::getBusinessType, pageReqVO.getBusinessType())
                        .likeIfPresent(SwmMediaFileDO::getUploadBy, pageReqVO.getUploadBy())
                        .orderByDesc(SwmMediaFileDO::getUploadTime));
    }

    private void validateMediaFileExists(String id) {
        if (swmMediaFileMapper.selectById(id) == null) {
            throw exception(COMMON_OPTIONS_NOT_EXISTS);
        }
    }

}
