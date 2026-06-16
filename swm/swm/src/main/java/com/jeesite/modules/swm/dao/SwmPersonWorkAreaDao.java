package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPersonWorkArea;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人员工作区域绑定 DAO。
 */
@MyBatisDao
public interface SwmPersonWorkAreaDao extends CrudDao<SwmPersonWorkArea> {

    int countActiveSameBind(SwmPersonWorkArea swmPersonWorkArea);

    long insertBatch(@Param("list") List<SwmPersonWorkArea> list);

    List<SwmPersonWorkArea> findActiveByIdentityCard(@Param("identityCard") String identityCard);

    int deleteByIdentityCardExcludeAreaIds(@Param("identityCard") String identityCard,
                                           @Param("areaIds") List<String> areaIds);

    int deleteAllByIdentityCard(@Param("identityCard") String identityCard);

    int deleteInvalidAreaBinds();
}
