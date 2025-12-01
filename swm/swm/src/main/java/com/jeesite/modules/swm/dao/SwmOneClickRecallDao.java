/**
 * 一键召回记录表DAO接口
 * @author zwf
 * @date 2024-05-30
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmOneClickRecall;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 一键召回记录表DAO接口
 * 
 * @author zwf
 */
@MyBatisDao
public interface SwmOneClickRecallDao extends CrudDao<SwmOneClickRecall> {

    /**
     * 根据节点类型和节点列表查询人员信息
     *
     * @param selectedNodes 选中的节点列表，包含节点类型、ID等信息
     * @return 人员信息列表，包含身份证、姓名等信息
     * @author Shawn
     * @date 2025-01-06
     */
    List<Map<String, Object>> findPersonnelByNodes(@Param("selectedNodes") List<Map<String, Object>> selectedNodes);

    /**
     * 根据身份证列表查询设备信息
     *
     * @param identityCards 身份证号码列表
     * @return 设备信息列表，包含设备ID、身份证等信息
     * @author Shawn
     * @date 2025-01-06
     */
    List<Map<String, Object>> findDevicesByIdentityCards(@Param("identityCards") List<String> identityCards);

    /**
     * 查询所有应该接收全体推送的目标人员
     * 包括所有在职且有设备分配的人员
     *
     * @return 目标人员信息列表，包含设备ID、人员姓名、身份证等信息
     * @author Shawn
     * @date 2025-08-23
     */
    List<Map<String, Object>> findAllTargetPersonnelForBroadcast();

    List<Map<String, Object>> findAllTargetPersonnelForBroadcastByPersonType(List<String> personTypeList);
}