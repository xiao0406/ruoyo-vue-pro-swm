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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHelmetConfig;
import com.jeesite.modules.swm.entity.SwmHelmetSubitem;
import com.jeesite.modules.swm.service.SwmHelmetConfigService;
import com.jeesite.modules.swm.service.SwmHelmetSubitemService;

/**
 * 安全帽主配置表Controller
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Controller
@RequestMapping(value = "${adminPath}/swmHelmetConfig")
public class SwmHelmetConfigController extends BaseController {

	@Autowired
	private SwmHelmetConfigService swmHelmetConfigService;
	
	@Autowired
	private SwmHelmetSubitemService swmHelmetSubitemService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmHelmetConfig get(String id, boolean isNewRecord) {
		return swmHelmetConfigService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = { "list", "" })
	public String list(SwmHelmetConfig swmHelmetConfig, Model model) {
		model.addAttribute("swmHelmetConfig", swmHelmetConfig);
		return "modules/swm/swmHelmetConfigList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<SwmHelmetConfig> listData(SwmHelmetConfig swmHelmetConfig, HttpServletRequest request, HttpServletResponse response) {
		swmHelmetConfig.setPage(new Page<>(request, response));
		Page<SwmHelmetConfig> page = swmHelmetConfigService.findPage(swmHelmetConfig);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	public String form(SwmHelmetConfig swmHelmetConfig, Model model) {
		model.addAttribute("swmHelmetConfig", swmHelmetConfig);
		return "modules/swm/swmHelmetConfigForm";
	}

	/**
	 * 保存安全帽主配置表
	 */
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated SwmHelmetConfig swmHelmetConfig) {
		swmHelmetConfigService.save(swmHelmetConfig);
		return renderResult(Global.TRUE, text("保存安全帽主配置表成功！"));
	}
	
	/**
	 * 删除安全帽主配置表
	 */
	@DeleteMapping(value = "delete")
	@ResponseBody
	public String delete(@RequestParam(required = false) String id, SwmHelmetConfig swmHelmetConfig, HttpServletRequest request) {

		

		
		// 确保获取到ID参数
		if ((id == null || id.isEmpty()) && request != null) {
			id = request.getParameter("id");
		}
		
		if (id != null && !id.isEmpty()) {
			swmHelmetConfig = new SwmHelmetConfig(id);
		} else if (swmHelmetConfig != null && swmHelmetConfig.getId() != null && !swmHelmetConfig.getId().isEmpty()) {
			// 如果从URL参数未获取到，但对象中已有ID，则继续使用
			System.out.println("使用对象中的ID: " + swmHelmetConfig.getId());
		} else {
			// 最后尝试从查询字符串中解析
			String queryString = request.getQueryString();
			if (queryString != null && queryString.contains("id=")) {
				String[] params = queryString.split("&");
				for (String param : params) {
					if (param.startsWith("id=")) {
						id = param.substring(3);
						swmHelmetConfig = new SwmHelmetConfig(id);
						break;
					}
				}
			}
		}
		
		// 确保对象不为空且id不为空
		if (swmHelmetConfig == null || swmHelmetConfig.getId() == null || swmHelmetConfig.getId().isEmpty()) {
			System.out.println("删除失败: ID为空");
			return renderResult(Global.FALSE, text("删除失败：ID不能为空！"));
		}
		
		System.out.println("即将删除的ID: " + swmHelmetConfig.getId());
		swmHelmetConfigService.delete(swmHelmetConfig);
		return renderResult(Global.TRUE, text("删除安全帽主配置表成功！"));
	}
	
	/**
	 * 批量删除安全帽主配置表
	 */
	@RequestMapping(value = "deleteAll")
	@ResponseBody
	public String deleteAll(String ids) {
		String[] idArray = ids.split(",");
		for (String id : idArray) {
			SwmHelmetConfig swmHelmetConfig = swmHelmetConfigService.get(id);
			if (swmHelmetConfig != null) {
				swmHelmetConfigService.delete(swmHelmetConfig);
			}
		}
		return renderResult(Global.TRUE, text("批量删除安全帽主配置表成功！"));
	}
	
	/**
	 * 获取安全帽配置列表（用于下拉选择）
	 */
	@RequestMapping(value = "getConfigList")
	@ResponseBody
	public List<Map<String, Object>> getConfigList() {
		List<Map<String, Object>> result = new ArrayList<>();
		
		List<SwmHelmetConfig> configList = swmHelmetConfigService.findList(new SwmHelmetConfig());
		for (SwmHelmetConfig config : configList) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", config.getId());
			map.put("name", config.getName());
			map.put("subitemCount", config.getSubitemCount());
			
			result.add(map);
		}
		
		return result;
	}



	
} 