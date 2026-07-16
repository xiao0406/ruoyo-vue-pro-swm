package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.controller.admin.persontrack.vo.PersonTrackInfoVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PersonTrackMapper extends BaseMapperX<SwmWarningManagementDO> {

    List<PersonTrackInfoVO> findPersonTrackInfo(@Param("searchName") String searchName,
                                                 @Param("personTypeList") List<String> personTypeList);

    PersonTrackInfoVO getPersonByIdCard(@Param("identityCard") String identityCard);

    String getColorByKey(@Param("key") String key);

    List<Map<String, Object>> getColorsByKeys(@Param("keys") List<String> keys);
}

