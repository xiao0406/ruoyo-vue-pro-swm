package cn.iocoder.yudao.module.swm.controller.admin.organization;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.swm.dal.dataobject.TreeNode;
import cn.iocoder.yudao.module.swm.service.SwmOrganizationTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - SWM 组织树")
@RestController
@RequestMapping("/swm/organizationTree")
@Validated
public class SwmOrganizationTreeController {

    @Resource
    private SwmOrganizationTreeService organizationTreeService;

    @GetMapping("/getNodes")
    @Operation(summary = "获取组织树节点")
    public CommonResult<List<TreeNode>> getNodes(
            @Parameter(description = "节点类型：root、office、workshop、prodLine、workGroup", required = true)
            @RequestParam("nodeType") String nodeType,
            @Parameter(description = "父节点 ID")
            @RequestParam(value = "parentId", required = false) String parentId,
            @Parameter(description = "人员类型")
            @RequestParam(value = "personTypeList", required = false) List<String> personTypeList) {
        return success(organizationTreeService.getNodes(nodeType, parentId, personTypeList));
    }

}
