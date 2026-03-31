package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import com.jeesite.modules.swm.service.SwmSafetyPersonTrainingService;

/**
 * 视频培训记录（人员）Controller
 * @author wxy
 * @version 2026-01-19
 */
@Controller
@RequestMapping(value = "${adminPath}/swmSafetyPersonTraining")
@Api(value = "视频培训记录（人员）接口", tags = "视频培训记录（人员）")
public class SwmSafetyPersonTrainingController extends BaseController {

	@Resource
	private SwmSafetyPersonTrainingService swmSafetyPersonTrainingService;

//	@RequestMapping(value = "test")
//	@ResponseBody
//	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
//	public List<String> test(@RequestBody JSONObject params) { // 1. 接收正确的入参类型
//		// 一、预先定义好的 MAC 地址列表
//		List<JSONObject> macList = new ArrayList<>();
//
//		JSONObject obj1 = new JSONObject();
//		obj1.put("mac", "80ECCCD09FFF");
//		obj1.put("MAJOR", 10008);
//		obj1.put("MINOR", 10133);
//		macList.add(obj1);
//
//		JSONObject obj2 = new JSONObject();
//		obj2.put("mac", "80ECCCD23F3F");
//		obj2.put("MAJOR", 10018);
//		obj2.put("MINOR", 6932);
//		macList.add(obj2);
//
//		JSONObject obj3 = new JSONObject();
//		obj3.put("mac", "80ECCCD23EA0"); // 注意：原数据 O 改成 0（MAC 规范）
//		obj3.put("MAJOR", 10018);
//		obj3.put("MINOR", 7020);
//		macList.add(obj3);
//
//		JSONObject obj4 = new JSONObject();
//		obj4.put("mac", "80ECCCD21284");
//		obj4.put("MAJOR", 10018);
//		obj4.put("MINOR", 7113);
//		macList.add(obj4);
//
//		JSONObject obj5 = new JSONObject();
//		obj5.put("mac", "80ECCCD23F45");
//		obj5.put("MAJOR", 10018);
//		obj5.put("MINOR", 6967);
//		macList.add(obj5);
//
//		JSONObject obj6 = new JSONObject();
//		obj6.put("mac", "80ECCCD20B57");
//		obj6.put("MAJOR", 10018);
//		obj6.put("MINOR", 7082);
//		macList.add(obj6);
//
//		JSONObject obj7 = new JSONObject();
//		obj7.put("mac", "80ECCCD23F3B");
//		obj7.put("MAJOR", 10018);
//		obj7.put("MINOR", 6918);
//		macList.add(obj7);
//
//		JSONObject obj8 = new JSONObject();
//		obj8.put("mac", "80ECCCD23F2F");
//		obj8.put("MAJOR", 10018);
//		obj8.put("MINOR", 6921);
//		macList.add(obj8);
//
//		JSONObject obj9 = new JSONObject();
//		obj9.put("mac", "80ECCCD23F7C");
//		obj9.put("MAJOR", 10018);
//		obj9.put("MINOR", 7023);
//		macList.add(obj9);
//
//		JSONObject obj10 = new JSONObject();
//		obj10.put("mac", "80ECCCD23F78");
//		obj10.put("MAJOR", 10018);
//		obj10.put("MINOR", 7037);
//		macList.add(obj10);
//
//		JSONObject obj11 = new JSONObject();
//		obj11.put("mac", "80ECCCD24006");
//		obj11.put("MAJOR", 10018);
//		obj11.put("MINOR", 7094);
//		macList.add(obj11);
//
//		// 二、解析入参，获取 data 数组
//		List<JSONObject> dataList = params.getJSONArray("data").toList(JSONObject.class);
//
//		// 三、存储匹配到的 MAC 地址
//		List<String> resultMacList = new ArrayList<>();
//
//		// 四、循环匹配：根据 major + minor 找到对应 mac
//		for (JSONObject data : dataList) {
//			Integer major = data.getInt("major");
//			Integer minor = data.getInt("minor");
//
//			// 遍历本地 macList 匹配
//			for (JSONObject macObj : macList) {
//				Integer mMajor = macObj.getInt("MAJOR");
//				Integer mMinor = macObj.getInt("MINOR");
//
//				// major 和 minor 同时相等 → 匹配成功
//				if (major.equals(mMajor) && minor.equals(mMinor)) {
//					String mac = macObj.getStr("mac");
//					resultMacList.add(mac);
//					break;
//				}
//			}
//		}
//
//		// 返回匹配到的 MAC 列表
//		return resultMacList;
//	}
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmSafetyPersonTraining get(String id, boolean isNewRecord) {
		return swmSafetyPersonTrainingService.get(id, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequestMapping(value = {"list", ""})
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(SwmSafetyPersonTraining swmSafetyPersonTraining, Model model) {
		model.addAttribute("swmSafetyPersonTraining", swmSafetyPersonTraining);
		return "modules/swm/swmSafetyPersonTrainingList";
	}
	
	/**
	 * 查询分页列表数据
	 */
	@RequestMapping(value = "pageList")
	@ResponseBody
	@ApiOperation(value = "查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmSafetyPersonTraining> pageList(SwmSafetyPersonTraining swmSafetyPersonTraining, HttpServletRequest request, HttpServletResponse response) {
		swmSafetyPersonTraining.setPage(new Page<>(request, response));
		Page<SwmSafetyPersonTraining> page = swmSafetyPersonTrainingService.findPage(swmSafetyPersonTraining);
		return page;
	}



	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public List<SwmSafetyPersonTraining> listData(SwmSafetyPersonTraining swmSafetyPersonTraining, HttpServletRequest request, HttpServletResponse response) {
		List<SwmSafetyPersonTraining> list = swmSafetyPersonTrainingService.findList(swmSafetyPersonTraining);
		return list;
	}

	/**
	 * 查看编辑表单
	 */
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(SwmSafetyPersonTraining swmSafetyPersonTraining, Model model) {
		model.addAttribute("swmSafetyPersonTraining", swmSafetyPersonTraining);
		return "modules/swm/swmSafetyPersonTrainingForm";
	}

	/**
	 * 保存数据
	 */
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(@Validated SwmSafetyPersonTraining swmSafetyPersonTraining) {
		swmSafetyPersonTrainingService.save(swmSafetyPersonTraining);
		return renderResult(Global.TRUE, text("保存视频培训记录（人员）成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		swmSafetyPersonTrainingService.delete(swmSafetyPersonTraining);
		return renderResult(Global.TRUE, text("删除视频培训记录（人员）成功！"));
	}



	@RequestMapping(value = "appPageList")
	@ResponseBody
	@ApiOperation(value = "小程序 - 查询分页列表数据", notes = "查询分页列表数据")
	public Page<SwmSafetyPersonTraining> appPageList(SwmSafetyPersonTraining swmSafetyPersonTraining,HttpServletRequest request) {
		//鉴权
		if (!authentication(request)) {
			return null;
		}
		swmSafetyPersonTraining.setPage(new Page<>(swmSafetyPersonTraining.getPageNo(), swmSafetyPersonTraining.getPageSize()));
		Page<SwmSafetyPersonTraining> page = swmSafetyPersonTrainingService.appPageList(swmSafetyPersonTraining);
		return page;
	}

	@PostMapping(value = "appUpdate")
	@ResponseBody
	@ApiOperation(value = "小程序 - 保存数据", notes = "保存数据")
	public String appUpdate(@RequestBody SwmSafetyPersonTraining swmSafetyPersonTraining,HttpServletRequest request) {

		//鉴权
		if (!authentication(request)) {
			return renderResult(Global.FALSE, "非法请求");
		}

		swmSafetyPersonTrainingService.appUpdate(swmSafetyPersonTraining);
		return renderResult(Global.TRUE, text("保存视频培训记录（人员）成功！"));
	}

	/**
	  API 服务调用接口鉴权
	 * @param request
	 * @return
	 */
	private boolean authentication(HttpServletRequest request){

		String appToken = "4308AC04C32873ABC834BD3A74F561A9";

		//1， 校验 Token
		String token = request.getHeader("X-Token");
		if (!appToken.equals(token)) {
			return false;
		}

		// 2， 校验时间戳（防误调用/简单重放）
		String timestamp = request.getHeader("X-Timestamp");
		if (StrUtil.isBlank(timestamp)) {
			return false;
		}

		long reqTime;
		try {
			reqTime = Long.parseLong(timestamp);
		} catch (Exception e) {
			return false;
		}

		// 允许 5 分钟误差
		if (Math.abs(System.currentTimeMillis() - reqTime) > 5 * 60 * 1000) {
			return false;
		}
		return true;
	}
	
}