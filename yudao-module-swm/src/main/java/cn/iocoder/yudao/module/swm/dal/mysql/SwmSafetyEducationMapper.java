package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmSafetyEducationMapper extends BaseMapperX<SwmSafetyEducationDO> {

    List<SwmSafetyEducationDO> findList();

    List<SwmSafetyEducationDO> findAllWithoutStatusFilter();

    List<SwmSafetyEducationDO> findByCustomConditions(@Param("theme") String theme,
                                                       @Param("safetyEducationType") String safetyEducationType,
                                                       @Param("participationType") String participationType,
                                                       @Param("safetyStatus") String safetyStatus);

    List<SwmSafetyEducationDO> findByIdentityCard(@Param("identityCard") String identityCard);
}
