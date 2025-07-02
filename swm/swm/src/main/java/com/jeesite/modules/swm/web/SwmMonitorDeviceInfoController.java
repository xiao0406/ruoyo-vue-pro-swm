package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmMonitorDeviceInfo;
import com.jeesite.modules.swm.service.SwmMonitorDeviceInfoCoreService;
import com.jeesite.modules.swm.service.SwmMonitorDeviceInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 监控设备信息Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/api/public/swm/monitorDeviceInfo")
@Api(value = "监控设备信息接口", tags = "监控设备信息")
public class SwmMonitorDeviceInfoController extends BaseController {

	@Autowired
	private SwmMonitorDeviceInfoService swmMonitorDeviceInfoService;
	@Autowired
	private SwmMonitorDeviceInfoCoreService swmMonitorDeviceInfoCoreService;

	/**
	 * 获取数据
	 */
	@ModelAttribute
	public SwmMonitorDeviceInfo get(String id, boolean isNewRecord) {
		SwmMonitorDeviceInfo dto = swmMonitorDeviceInfoService.get(new SwmMonitorDeviceInfo(id));
		if (!Objects.isNull(dto)) {
			return dto;
		} else {
			dto = new SwmMonitorDeviceInfo();
			dto.setIsNewRecord(isNewRecord);
			return dto;
		}
	}

	/**
	 * 查询列表
	 */
	@RequiresPermissions("swm:monitorDeviceInfo:view")
	@RequestMapping(value = { "list", "" })
	@ApiOperation(value = "查询列表", notes = "查询列表")
	public String list(SwmMonitorDeviceInfo swmMonitorDeviceInfo, Model model) {
		model.addAttribute("swmMonitorDeviceInfo", swmMonitorDeviceInfo);
		return "modules/swm/swmMonitorDeviceInfoList";
	}

	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("swm:monitorDeviceInfo:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public Page<SwmMonitorDeviceInfo> listData(SwmMonitorDeviceInfo swmMonitorDeviceInfo, HttpServletRequest request,
			HttpServletResponse response) {
		swmMonitorDeviceInfo.setPage(new Page<>(request, response));
		Page<SwmMonitorDeviceInfo> page = swmMonitorDeviceInfoService.findPage(swmMonitorDeviceInfo);
		return page;
	}

	/**
	 * 查询列表数据
	 */
	@RequestMapping(value = "listAll")
	@ResponseBody
	@ApiOperation(value = "查询列表数据", notes = "查询列表数据")
	public String listAll(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
		List<SwmMonitorDeviceInfo> list = swmMonitorDeviceInfoService.findList(swmMonitorDeviceInfo);
		return renderResult(Global.TRUE, text("所有设备"), list);
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("swm:monitorDeviceInfo:view")
	@RequestMapping(value = "form")
	@ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
	public String form(SwmMonitorDeviceInfo swmMonitorDeviceInfo, Model model) {
		model.addAttribute("swmMonitorDeviceInfo", swmMonitorDeviceInfo);
		return "modules/swm/swmMonitorDeviceInfoForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("swm:monitorDeviceInfo:edit")
	@PostMapping(value = "save")
	@ResponseBody
	@ApiOperation(value = "保存数据", notes = "保存数据")
	public String save(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
		swmMonitorDeviceInfoService.save(swmMonitorDeviceInfo);
		return renderResult(Global.TRUE, text("保存监控设备信息成功！"));
	}

	/**
	 * 删除数据
	 */
	@RequiresPermissions("swm:monitorDeviceInfo:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	@ApiOperation(value = "删除数据", notes = "删除数据")
	public String delete(SwmMonitorDeviceInfo swmMonitorDeviceInfo) {
		swmMonitorDeviceInfoService.delete(swmMonitorDeviceInfo);
		return renderResult(Global.TRUE, text("删除监控设备信息成功！"));
	}

	/**
	 * 查询机构下的设备列表
	 */
	@RequestMapping(value = "officeDeviceList")
	@ResponseBody
	@ApiOperation(value = "查询机构下的设备列表", notes = "查询机构下的设备列表")
	public Page<SwmMonitorDeviceInfo> officeDeviceList(SwmMonitorDeviceInfo swmMonitorDeviceInfo,
			HttpServletRequest request, HttpServletResponse response) {
		// 检查是否有分页参数，如果没有则设置一个大的pageSize来获取所有数据
		String pageNo = request.getParameter("pageNo");
		String pageSize = request.getParameter("pageSize");

		if (pageNo != null || pageSize != null) {
			// 有分页参数时才设置分页
			swmMonitorDeviceInfo.setPage(new Page<>(request, response));
		} else {
			// 没有分页参数时，设置一个大的pageSize来获取所有数据
			Page<SwmMonitorDeviceInfo> unlimitedPage = new Page<>();
			unlimitedPage.setPageNo(1);
			unlimitedPage.setPageSize(10000); // 设置一个足够大的数值
			swmMonitorDeviceInfo.setPage(unlimitedPage);
		}

		Page<SwmMonitorDeviceInfo> page = swmMonitorDeviceInfoCoreService.officeDeviceListUpdate(swmMonitorDeviceInfo);
		return page;
	}
}
