package com.jeesite.modules.swm.web;

import java.util.List;
import java.util.Map;

import com.jeesite.modules.swm.entity.SwmDictType;
import com.jeesite.modules.swm.service.SwmDictTypeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.jeesite.common.config.Global;
import com.jeesite.common.collect.ListUtils;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.idgen.IdGen;
import com.jeesite.modules.sys.utils.UserUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmDictData;
import com.jeesite.modules.swm.service.SwmDictDataService;

/**
 * 字典数据表Controller
 * @author wxy
 * @version 2026-04-24
 */
@Controller
@RequestMapping(value = "${adminPath}/swmDictData")
@Api(value = "字典数据表接口", tags = "字典数据表")
public class SwmDictDataController extends BaseController {

	@Resource
	private SwmDictTypeService swmDictTypeService;

	@Resource
	private SwmDictDataService swmDictDataService;



	/**
	 * createNextNode
	 */
	@RequestMapping("createNextNode")
	@ResponseBody
	public SwmDictData createNextNode(SwmDictData dictData) {

		if (StringUtils.isNotBlank(dictData.getParentCode())) {
			dictData.setParent(swmDictDataService.get(dictData.getParentCode()));
		}

		if (dictData.getIsNewRecord()) {
			SwmDictData where = new SwmDictData();
			where.setDictType(dictData.getDictType());
			where.setParentCode(dictData.getParentCode());

			SwmDictData last = swmDictDataService.getLastByParentCode(where);

			if (last != null) {
				dictData.setTreeSort(last.getTreeSort() + 30);
				dictData.setDictCode(IdGen.nextCode(last.getDictCode()));

				if (dictData.getIsSys() == null) {
					dictData.setIsSys(last.getIsSys());
				}

			} else if (dictData.getParent() != null) {
				dictData.setDictCode(dictData.getParent().getDictCode() + "001");

				if (dictData.getIsSys() == null) {
					SwmDictType type = new SwmDictType();
					type.setDictType(dictData.getDictType());
					type = swmDictTypeService.get(type);
					if (type != null) {
						dictData.setIsSys(type.getIsSys());
					}
				}
			}
		}

		if (dictData.getTreeSort() == null) {
			dictData.setTreeSort(30);
		}

		return dictData;
	}

	/**
	 * treeData
	 */
	@RequestMapping("treeData")
	@ResponseBody
	public List<Map<String, Object>> treeData(String dictType, String excludeCode,
											  String isShowCode, boolean isShowRawName) {

		List<Map<String, Object>> list = ListUtils.newArrayList();
		SwmDictData swmDictData = new SwmDictData();
		swmDictData.setDictType(dictType);
		List<SwmDictData> dataList = swmDictDataService.findList(swmDictData);
		if (dataList == null){
			return list;
		}

		for (SwmDictData e : dataList) {
			if (("0".equals(e.getStatus()) || e.getStatus() == null)
					&& (StringUtils.isBlank(excludeCode)
					|| (!e.getId().equals(excludeCode)
					&& !e.getParentCodes().contains("," + excludeCode + ",")))) {

				Map<String, Object> map = MapUtils.newHashMap();
				map.put("id", e.getId());
				map.put("pId", e.getParentCode());
				map.put("name", StringUtils.getTreeNodeName(
						isShowCode,
						e.getDictValue(),
						isShowRawName ? e.getDictLabel() : e.getDictLabel()
				));
				map.put("dictValue", e.getDictValue());

				if (StringUtils.isNotBlank(e.getDictIcon())) {
					map.put("icon", e.getDictIcon());
				}
				if (StringUtils.isNotBlank(e.getCssClass())) {
					map.put("cssClass", e.getCssClass());
				}
				if (StringUtils.isNotBlank(e.getCssStyle())) {
					map.put("cssStyle", e.getCssStyle());
				}

				list.add(map);
			}
		}
		return list;
	}

	/**
	 * save
	 */
	@PostMapping("save")
	@ResponseBody
	public String save( SwmDictData dictData, HttpServletRequest request) {


		SwmDictType type = new SwmDictType();
		type.setDictType(dictData.getDictType());
		List<SwmDictType> list = swmDictTypeService.findList(type);

		if (type == null) {
			return renderResult("false", text("字典类型不存在：{0}", dictData.getDictType()));
		}


		if (StringUtils.isBlank(dictData.getIsSys())) {
			dictData.setIsSys(type.getIsSys());
		}
		dictData.setTreeNames(dictData.getDictLabel());
		swmDictDataService.save(dictData);
		return renderResult("true", text("保存成功！"));
	}

	/**
	 * enable
	 */
	@RequestMapping("enable")
	@ResponseBody
	public String enable(SwmDictData dictData, HttpServletRequest request) {

		dictData.setStatus("0");
		swmDictDataService.updateStatus(dictData);

		return renderResult("true", text("启用成功！"));
	}

	/**
	 * delete
	 */
	@RequestMapping("delete")
	@ResponseBody
	public String delete(SwmDictData dictData, HttpServletRequest request) {

		swmDictDataService.delete(dictData);
		return renderResult("true", text("删除成功！"));
	}

	/**
	 * disable
	 */
	@RequestMapping("disable")
	@ResponseBody
	public String disable(SwmDictData dictData, HttpServletRequest request) {

		// 子节点校验
		SwmDictData check = new SwmDictData();
		check.setStatus("0");
		check.setParentCodes("," + dictData.getId() + ",");

		if (swmDictDataService.findCount(check) > 0) {
			return renderResult("false", text("存在子节点，不能禁用！"));
		}

		dictData.setStatus("2");
		swmDictDataService.updateStatus(dictData);

		return renderResult("true", text("禁用成功！"));
	}

	/**
	 * list
	 */
	@RequestMapping("list")
	public String list(SwmDictData dictData, Model model) {
		model.addAttribute("swmDictData", dictData);
		return "modules/swm/swmDictDataList";
	}

	/**
	 * fixTreeData
	 */
	@RequestMapping("fixTreeData")
	@ResponseBody
	public String fixTreeData() {
		swmDictDataService.fixTreeData();
		return renderResult("true", text("修复成功！"));
	}

	/**
	 * listData
	 */
	@RequestMapping("listData")
	@ResponseBody
	public List<SwmDictData> listData(SwmDictData dictData) {

		if (StringUtils.isBlank(dictData.getParentCode())) {
			dictData.setParentCode("0");
		}

		return swmDictDataService.findList(dictData);
	}

	/**
	 * form
	 */
	@RequestMapping("form")
	public SwmDictData form(SwmDictData dictData, Model model) {
//		dictData = createNextNode(dictData);
		SwmDictData swmDictData = swmDictDataService.get(dictData);
		return swmDictData;
	}
}