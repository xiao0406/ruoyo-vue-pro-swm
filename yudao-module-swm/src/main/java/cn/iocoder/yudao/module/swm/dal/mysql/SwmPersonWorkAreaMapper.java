package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonWorkAreaDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmPersonWorkAreaMapper extends BaseMapperX<SwmPersonWorkAreaDO> {

    List<SwmPersonWorkAreaDO> findList(@Param("identityCard") String identityCard,
                                        @Param("personName") String personName,
                                        @Param("company") String company,
                                        @Param("department") String department,
                                        @Param("prodLine") String prodLine,
                                        @Param("team") String team,
                                        @Param("jobType") String jobType,
                                        @Param("areaId") String areaId,
                                        @Param("areaName") String areaName);

    int countActiveSameBind(@Param("identityCard") String identityCard,
                             @Param("areaId") String areaId,
                             @Param("id") String id);

    List<SwmPersonWorkAreaDO> findActiveByIdentityCard(@Param("identityCard") String identityCard);

    void deleteByIdentityCardExcludeAreaIds(@Param("identityCard") String identityCard,
                                             @Param("areaIds") List<String> areaIds);

    void deleteAllByIdentityCard(@Param("identityCard") String identityCard);

    void deleteInvalidAreaBinds();

    void insertBatch(@Param("list") List<SwmPersonWorkAreaDO> list);
}
