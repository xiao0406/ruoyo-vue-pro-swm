package com.jeesite.modules.swm.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
 * @update 2025/06/24 by Shawn - 添加获取车间数据接口
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
	 * 获取设备参数配置
	 *
	 * @param deviceId 设备编号
	 * @return 设备参数配置
	 * @author Shawn
	 * @date 2025-08-04
	 */
	@GetMapping("getDeviceConfig")
	public SwmHelmetDevice getDeviceConfig(@RequestParam String deviceId) {
		return swmHelmetConfigService.getByDeviceId(deviceId);
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
	public Page<SwmHelmetConfig> listData(SwmHelmetConfig swmHelmetConfig, HttpServletRequest request,
			HttpServletResponse response) {
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
	public String delete(@RequestParam(required = false) String id, SwmHelmetConfig swmHelmetConfig,
			HttpServletRequest request) {

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

	/**
	 * 获取车间数据
	 * 
	 * @author Shawn
	 * @date 2025/06/24
	 * @description 根据用户要求的SQL查询车间数据：
	 *              SELECT id, '0001A110000000002ZWA' AS parent_id, position_name AS
	 *              title
	 *              FROM fms_position_archive WHERE type = 'CJ' AND region =
	 *              '0001A110000000002ZWA' AND status = '0'
	 */
	@RequestMapping(value = "getWorkshopData")
	@ResponseBody
	public List<Map<String, Object>> getWorkshopData() {
		List<Map<String, Object>> result = new ArrayList<>();

		try {
			// 调用Service层方法获取车间数据
			List<Map<String, Object>> workshopList = swmHelmetConfigService.getWorkshopData();
			if (workshopList != null && !workshopList.isEmpty()) {
				result = workshopList;
			}
		} catch (Exception e) {
			logger.error("获取车间数据失败", e);
		}

		return result;
	}

	/**
	 * 获取车间-产线-班组数据
	 * 
	 * @author Shawn
	 * @date 2025/06/24
	 * @description 根据用户要求的SQL查询车间-产线-班组数据，
	 *              将workshop_name、line_name、work_group_name拼接作为名称，
	 *              将id保存到key字段中用于颜色配置
	 */
	@RequestMapping(value = "getWorkshopLineGroupData")
	@ResponseBody
	public List<Map<String, Object>> getWorkshopLineGroupData() {
		List<Map<String, Object>> result = new ArrayList<>();

		try {
			// 调用Service层方法获取车间-产线-班组数据
			List<Map<String, Object>> workshopLineGroupList = swmHelmetConfigService.getWorkshopLineGroupData();
			if (workshopLineGroupList != null && !workshopLineGroupList.isEmpty()) {
				result = workshopLineGroupList;
			}
		} catch (Exception e) {
			logger.error("获取车间-产线-班组数据失败", e);
		}

		return result;
	}

	/**
	 * 获取人员类型字典数据
	 * 
	 * @author Shawn
	 * @date 2025/06/24
	 * @description 获取字典类型为person_type_enum的字典数据，
	 *              用于"按人员类型展示"的颜色配置
	 */
	@RequestMapping(value = "getPersonTypeEnumData")
	@ResponseBody
	public List<Map<String, Object>> getPersonTypeEnumData() {
		List<Map<String, Object>> result = new ArrayList<>();

		try {
			// 调用Service层方法获取人员类型字典数据
			List<Map<String, Object>> personTypeList = swmHelmetConfigService.getPersonTypeEnumData();
			if (personTypeList != null && !personTypeList.isEmpty()) {
				result = personTypeList;
			}
		} catch (Exception e) {
			logger.error("获取人员类型字典数据失败", e);
		}

		return result;
	}

	/**
	 * 获取工种数据
	 * 
	 * @author Shawn
	 * @date 2025/06/24
	 * @description 获取 swm_work_type 表中的工种数据，
	 *              用于"按工种展示"的颜色配置
	 */
	@RequestMapping(value = "getWorkTypeEnumData")
	@ResponseBody
	public List<Map<String, Object>> getWorkTypeEnumData() {
		List<Map<String, Object>> result = new ArrayList<>();

		try {
			// 调用Service层方法获取工种数据
			List<Map<String, Object>> workTypeList = swmHelmetConfigService.getWorkTypeEnumData();
			if (workTypeList != null && !workTypeList.isEmpty()) {
				result = workTypeList;
			}
		} catch (Exception e) {
			logger.error("获取工种数据失败", e);
		}

		return result;
	}

}