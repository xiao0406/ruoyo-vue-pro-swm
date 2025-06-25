package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.PersonTrackInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 人员追踪查询DAO接口
 * 
 * @author Shawn
 * @date 2025-01-14
 */
@MyBatisDao
public interface PersonTrackDao extends CrudDao<PersonTrackInfo> {

    /**
     * 查询人员追踪信息
     * 
     * @param searchName      搜索人员姓名（可选）
     * @param organizationKey 组织节点key（可选）
     * @return 人员追踪信息列表
     * @author Shawn
     * @date 2025-01-14
     */
    List<PersonTrackInfo> findPersonTrackInfo(@Param("searchName") String searchName,
            @Param("organizationKey") String organizationKey);

    /**
     * 根据身份证号码查询人员信息
     * 
     * @param identityCard 身份证号码
     * @return 人员信息
     * @author Shawn
     * @date 2025-01-14
     */
    PersonTrackInfo getPersonByIdCard(@Param("identityCard") String identityCard);

    /**
     * 根据key值查询颜色信息
     * 
     * @param key 关键字段
     * @return 颜色代码
     * @author Shawn
     * @date 2025/06/24
     */
    String getColorByKey(@Param("key") String key);

    /**
     * 批量查询颜色信息
     * 
     * @param keys 关键字段列表
     * @return 颜色信息Map，key为关键字段，value为颜色代码
     * @author Shawn
     * @date 2025/06/24
     */
    List<Map<String, Object>> getColorsByKeys(@Param("keys") List<String> keys);
}