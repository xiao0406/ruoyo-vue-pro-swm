package com.jeesite.modules.swm.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHelmetSubitem;
import com.jeesite.modules.swm.entity.SwmHelmetConfig;
import com.jeesite.modules.swm.service.SwmHelmetSubitemService;
import com.jeesite.modules.swm.service.SwmHelmetConfigService;

/**
 * 安全帽子项颜色表Controller
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Controller
@RequestMapping(value = "${adminPath}/swmHelmetSubitem")
public class SwmHelmetSubitemController extends BaseController {

	@Autowired
	private SwmHelmetSubitemService swmHelmetSubitemService;

	@Autowired
	private SwmHelmetConfigService swmHelmetConfigService;

	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmHelmetSubitem get(String id, boolean isNewRecord) {
		return swmHelmetSubitemService.get(id, isNewRecord);
	}

	/**
	 * 查询列表
	 */
	@RequestMapping(value = { "list", "" })
	public String list(SwmHelmetSubitem swmHelmetSubitem, Model model) {
		model.addAttribute("swmHelmetSubitem", swmHelmetSubitem);
		return "modules/swm/swmHelmetSubitemList";
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<SwmHelmetSubitem> listData(SwmHelmetSubitem swmHelmetSubitem, HttpServletRequest request,
			HttpServletResponse response) {
		swmHelmetSubitem.setPage(new Page<>(request, response));
		Page<SwmHelmetSubitem> page = swmHelmetSubitemService.findPage(swmHelmetSubitem);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	public String form(SwmHelmetSubitem swmHelmetSubitem, Model model) {
		// 如果是新记录，但有parentId，则预设父ID
		if (swmHelmetSubitem.getIsNewRecord() && swmHelmetSubitem.getParentId() != null) {
			SwmHelmetConfig parent = swmHelmetConfigService.get(swmHelmetSubitem.getParentId());
			if (parent != null) {
				swmHelmetSubitem.setHelmetConfig(parent);
			}
		}
		model.addAttribute("swmHelmetSubitem", swmHelmetSubitem);
		return "modules/swm/swmHelmetSubitemForm";
	}

	/**
	 * 保存安全帽子项颜色表
	 */
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated SwmHelmetSubitem swmHelmetSubitem) {
		swmHelmetSubitemService.save(swmHelmetSubitem);
		return renderResult(Global.TRUE, text("保存安全帽子项颜色表成功！"));
	}

	/**
	 * 删除安全帽子项颜色表
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(SwmHelmetSubitem swmHelmetSubitem) {
		swmHelmetSubitemService.delete(swmHelmetSubitem);
		return renderResult(Global.TRUE, text("删除安全帽子项颜色表成功！"));
	}

	/**
	 * 批量删除安全帽子项颜色表
	 */
	@RequestMapping(value = "deleteAll")
	@ResponseBody
	public String deleteAll(String ids) {
		String[] idArray = ids.split(",");
		for (String id : idArray) {
			SwmHelmetSubitem swmHelmetSubitem = swmHelmetSubitemService.get(id);
			if (swmHelmetSubitem != null) {
				swmHelmetSubitemService.delete(swmHelmetSubitem);
			}
		}
		return renderResult(Global.TRUE, text("批量删除安全帽子项颜色表成功！"));
	}

	/**
	 * 根据父ID查询子项列表
	 */
	@RequestMapping(value = "findByParentId")
	@ResponseBody
	public List<Map<String, Object>> findByParentId(@RequestParam("parentId") String parentId) {
		List<Map<String, Object>> result = new ArrayList<>();

		List<SwmHelmetSubitem> subitemList = swmHelmetSubitemService.findByParentId(parentId);
		for (SwmHelmetSubitem subitem : subitemList) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", subitem.getId());
			map.put("parentId", subitem.getParentId());
			map.put("subitemName", subitem.getSubitemName());
			map.put("color", subitem.getColor());
			map.put("key", subitem.getKey());

			result.add(map);
		}

		return result;
	}
}