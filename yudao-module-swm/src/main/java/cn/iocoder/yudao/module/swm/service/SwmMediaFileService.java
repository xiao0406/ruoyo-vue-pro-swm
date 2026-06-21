package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFilePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo.SwmMediaFileSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmMediaFileDO;
import jakarta.validation.Valid;

/**
 * 媒体文件 Service 接口
 */
public interface SwmMediaFileService {

    /**
     * 创建媒体文件
     *
     * @param createReqVO 创建信息
     * @return 媒体文件编号
     */
    String createMediaFile(@Valid SwmMediaFileSaveReqVO createReqVO);

    /**
     * 更新媒体文件
     *
     * @param updateReqVO 更新信息
     */
    void updateMediaFile(@Valid SwmMediaFileSaveReqVO updateReqVO);

    /**
     * 删除媒体文件
     *
     * @param id 媒体文件编号
     */
    void deleteMediaFile(String id);

    /**
     * 获得媒体文件
     *
     * @param id 媒体文件编号
     * @return 媒体文件
     */
    SwmMediaFileDO getMediaFile(String id);

    /**
     * 获得媒体文件分页
     *
     * @param pageReqVO 分页查询
     * @return 媒体文件分页
     */
    PageResult<SwmMediaFileDO> getMediaFilePage(SwmMediaFilePageReqVO pageReqVO);

}
