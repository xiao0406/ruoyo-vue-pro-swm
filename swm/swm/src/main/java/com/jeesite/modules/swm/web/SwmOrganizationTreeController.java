package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.TreeNode;
import com.jeesite.modules.swm.service.SwmOrganizationTreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 组织树控制器
 * 
 * @author AI-Generated
 * @version 2025-05-28
 */
@Controller
@RequestMapping(value = "${adminPath}/organizationTree")
@Api(value = "组织树数据接口", tags = "组织树数据接口")
public class SwmOrganizationTreeController extends BaseController {

    @Autowired
    private SwmOrganizationTreeService swmOrganizationTreeService;

    /**
     * 获取组织树节点数据
     * 
     * @param nodeType 节点类型: root(根节点), office(厂间), workshop(车间), prodLine(产线), workGroup(班组)
     * @param parentId 父节点ID，如果获取根节点可不传
     * @return 组织树节点列表
     */
    @GetMapping("getNodes")
    @ResponseBody
    @ApiOperation("获取组织树节点数据")
    public List<TreeNode> getNodes(
            @ApiParam(value = "节点类型", required = true) @RequestParam String nodeType,
            @ApiParam(value = "父节点ID") @RequestParam(required = false) String parentId) {
        
        return swmOrganizationTreeService.getNodes(nodeType, parentId);
    }
} 