package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.module.swm.dal.dataobject.TreeNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SwmOrganizationTreeMapper {

    List<TreeNode> getOfficeNodes();

    List<TreeNode> getWorkshopNodes(@Param("officeId") String officeId);

    List<TreeNode> getProdLineNodes(@Param("workshopId") String workshopId);

    List<TreeNode> getWorkGroupNodes(@Param("prodLineId") String prodLineId);

    List<TreeNode> getWorkerNodes(@Param("workGroupId") String workGroupId,
                                   @Param("personTypeList") List<String> personTypeList);

    List<Map<String, Object>> getCompanyOptions();

    List<Map<String, Object>> getDepartmentOptions();

    List<Map<String, Object>> getProdLineOptions();

    List<Map<String, Object>> getWorkGroupOptions();

    List<Map<String, Object>> getPersonsByNodeType(@Param("nodeType") String nodeType,
                                                   @Param("id") String id);

    List<Map<String, Object>> getOrganizationNames(@Param("ids") List<String> ids);
}

