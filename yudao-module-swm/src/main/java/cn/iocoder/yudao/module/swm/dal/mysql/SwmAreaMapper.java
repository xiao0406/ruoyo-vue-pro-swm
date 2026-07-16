package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.AreaTrajectoryVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmAreaMapper extends BaseMapperX<SwmAreaDO> {

    List<SwmAreaDO> findList(@Param("random") Integer random);

    List<AreaTrajectoryVO> findAddressList();
}

