/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmAlarmLight;
import com.jeesite.modules.swm.entity.SwmAlarmLightConfig;
import com.jeesite.modules.swm.service.SwmAlarmLightConfigService;
import com.jeesite.modules.swm.service.SwmAlarmLightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报警灯设备Controller
 * @author Shawn
 * @version 2025-01-19
 */
@Controller
@RequestMapping(value = "${adminPath}/swmAlarmLight")
public class SwmAlarmLightController extends BaseController {

	@Autowired
	private SwmAlarmLightService swmAlarmLightService;
	
	@Autowired
	private SwmAlarmLightConfigService swmAlarmLightConfigService;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmAlarmLight get(String id, boolean isNewRecord) {
		return swmAlarmLightService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	public String list(SwmAlarmLight swmAlarmLight, Model model) {
		model.addAttribute("swmAlarmLight", swmAlarmLight);
		return "modules/swm/swmAlarmLightList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	public Map<String, Object> listData(SwmAlarmLight swmAlarmLight, HttpServletRequest request, HttpServletResponse response) {
		swmAlarmLight.setPage(new Page<>(request, response));
		Page<SwmAlarmLight> page = swmAlarmLightService.findPage(swmAlarmLight);
		Map<String, Object> result = new HashMap<>();
		result.put("list", page.getList());
		result.put("count", page.getCount());
		result.put("pageNo", page.getPageNo());
		result.put("pageSize", page.getPageSize());
		return result;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	public String form(SwmAlarmLight swmAlarmLight, Model model) {
		model.addAttribute("swmAlarmLight", swmAlarmLight);
		return "modules/swm/swmAlarmLightForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated SwmAlarmLight swmAlarmLight) {
		swmAlarmLightService.save(swmAlarmLight);
		return renderResult(Global.TRUE, text("保存报警灯设备成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(SwmAlarmLight swmAlarmLight) {
		swmAlarmLightService.delete(swmAlarmLight);
		return renderResult(Global.TRUE, text("删除报警灯设备成功！"));
	}
	
	/**
	 * 获取报警灯配置列表
	 */
	@RequestMapping(value = "configList")
	@ResponseBody
	public Map<String, Object> configList(String lightId) {
		logger.debug("获取报警灯配置列表，lightId: {}", lightId);

		Map<String, Object> result = new HashMap<>();

		// 参数验证
		if (StringUtils.isBlank(lightId)) {
			logger.warn("lightId参数为空");
			result.put("success", false);
			result.put("message", "报警灯ID不能为空");
			result.put("list", new ArrayList<>());
			result.put("total", 0);
			return result;
		}

		try {
			// 查询报警灯基本信息
			SwmAlarmLight alarmLight = swmAlarmLightService.get(lightId);
			if (alarmLight == null) {
				logger.warn("未找到报警灯设备，lightId: {}", lightId);
				result.put("success", false);
				result.put("message", "未找到对应的报警灯设备");
				result.put("list", new ArrayList<>());
				result.put("total", 0);
				return result;
			}

			// 查询配置列表
			List<SwmAlarmLightConfig> configList = swmAlarmLightConfigService.findListByLightId(lightId);
			if (configList == null) {
				configList = new ArrayList<>();
			}

			logger.debug("查询到配置数量: {}", configList.size());

			// 构建返回结果
			result.put("success", true);
			result.put("message", configList.isEmpty() ? "暂无配置数据，可以添加新配置" : "查询成功");
			result.put("list", configList);
			result.put("total", configList.size());
			result.put("lightId", lightId);
			result.put("lightName", alarmLight.getLightName());
			result.put("isEmpty", configList.isEmpty());

			return result;

		} catch (Exception e) {
			logger.error("查询报警灯配置失败，lightId: {}", lightId, e);
			result.put("success", false);
			result.put("message", "查询配置失败：" + e.getMessage());
			result.put("list", new ArrayList<>());
			result.put("total", 0);
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	/**
	 * 保存报警灯配置
	 */
	@PostMapping(value = "saveConfig")
	@ResponseBody
	public String saveConfig(@Validated SwmAlarmLightConfig swmAlarmLightConfig) {
		try {
			// 验证必要字段
			if (StringUtils.isBlank(swmAlarmLightConfig.getLightId())) {
				return renderResult(Global.FALSE, "报警灯ID不能为空");
			}
			if (StringUtils.isBlank(swmAlarmLightConfig.getAlarmConfigId())) {
				return renderResult(Global.FALSE, "报警类型不能为空");
			}

			// 验证报警灯是否存在
			SwmAlarmLight alarmLight = swmAlarmLightService.get(swmAlarmLightConfig.getLightId());
			if (alarmLight == null) {
				return renderResult(Global.FALSE, "报警灯设备不存在");
			}

			// 检查配置是否已存在（只对状态为正常的记录进行唯一性检查）
			if ("0".equals(swmAlarmLightConfig.getStatus()) || StringUtils.isBlank(swmAlarmLightConfig.getStatus())) {
				boolean configExists = swmAlarmLightConfigService.checkConfigExists(
						swmAlarmLightConfig.getLightId(),
						swmAlarmLightConfig.getAlarmConfigId(),
						swmAlarmLightConfig.getId()
				);
				
				if (configExists) {
					return renderResult(Global.FALSE, "该报警灯已配置了相同的报警类型，请选择其他报警类型");
				}
			}

			swmAlarmLightConfigService.save(swmAlarmLightConfig);
			logger.info("保存报警灯配置成功，lightId: {}, configId: {}",
					swmAlarmLightConfig.getLightId(), swmAlarmLightConfig.getId());

			return renderResult(Global.TRUE, text("保存报警灯配置成功！"));

		} catch (Exception e) {
			logger.error("保存报警灯配置失败", e);
			return renderResult(Global.FALSE, "保存失败：" + e.getMessage());
		}
	}

	/**
	 * 删除报警灯配置
	 */
	@RequestMapping(value = "deleteConfig")
	@ResponseBody
	public String deleteConfig(SwmAlarmLightConfig swmAlarmLightConfig) {
		swmAlarmLightConfigService.delete(swmAlarmLightConfig);
		return renderResult(Global.TRUE, text("删除报警灯配置成功！"));
	}


	
}