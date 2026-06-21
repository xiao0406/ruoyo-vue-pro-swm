package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.module.swm.dal.dataobject.TreeNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmOrganizationTreeMapper {

    List<TreeNode> getOfficeNodes();

    List<TreeNode> getWorkshopNodes(@Param("officeId") String officeId);

    List<TreeNode> getProdLineNodes(@Param("workshopId") String workshopId);

    List<TreeNode> getWorkGroupNodes(@Param("prodLineId") String prodLineId);

    List<TreeNode> getWorkerNodes(@Param("workGroupId") String workGroupId,
                                   @Param("personTypeList") List<String> personTypeList);
}
