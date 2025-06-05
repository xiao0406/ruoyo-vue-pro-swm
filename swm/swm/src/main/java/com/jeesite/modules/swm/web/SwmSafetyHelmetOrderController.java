/**
 * @author Shawn
 * @date 2023-08-26
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmSafetyHelmetOrder;
import com.jeesite.modules.swm.service.SwmSafetyHelmetOrderService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全帽订购记录控制器
 */
@Controller
@RequestMapping(value = "${adminPath}/swmSafetyHelmetOrder")
public class SwmSafetyHelmetOrderController extends BaseController {

    @Autowired
    private SwmSafetyHelmetOrderService swmSafetyHelmetOrderService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmSafetyHelmetOrder get(String id, boolean isNewRecord) {
        return swmSafetyHelmetOrderService.get(id, isNewRecord);
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmSafetyHelmetOrder> listData(SwmSafetyHelmetOrder order, HttpServletRequest request,
            HttpServletResponse response) {
        // 设置分页参数
        order.setPage(new Page<>(request, response));
        // 调用修改后的findPage方法
        Page<SwmSafetyHelmetOrder> page = swmSafetyHelmetOrderService.findPage(order);
        return page;
    }

    /**
     * 根据人员ID查询订购记录
     */
    @GetMapping(value = "findByPersonId")
    @ResponseBody
    public Map<String, Object> findByPersonId(@RequestParam("personId") String personId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SwmSafetyHelmetOrder> orders = swmSafetyHelmetOrderService.findByPersonId(personId);
            result.put("success", true);
            result.put("data", orders);
        } catch (Exception e) {
            logger.error("查询安全帽订购记录异常", e);
            result.put("success", false);
            result.put("message", "查询安全帽订购记录失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据安全帽ID查询订购记录
     */
    @GetMapping(value = "findByDeviceId")
    @ResponseBody
    public Map<String, Object> findByDeviceId(@RequestParam("deviceId") String deviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SwmSafetyHelmetOrder> orders = swmSafetyHelmetOrderService.findByDeviceId(deviceId);
            result.put("success", true);
            result.put("data", orders);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询人员当前使用中的安全帽订单
     */
    @GetMapping(value = "findActiveOrdersByPersonId")
    @ResponseBody
    public Map<String, Object> findActiveOrdersByPersonId(@RequestParam("personId") String personId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SwmSafetyHelmetOrder> orders = swmSafetyHelmetOrderService.findActiveOrdersByPersonId(personId);
            result.put("success", true);
            result.put("data", orders);
        } catch (Exception e) {
            logger.error("查询使用中的安全帽订单异常", e);
            result.put("success", false);
            result.put("message", "查询使用中的安全帽订单失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询设备当前的使用订单
     */
    @GetMapping(value = "findActiveOrderByDeviceId")
    @ResponseBody
    public Map<String, Object> findActiveOrderByDeviceId(@RequestParam("deviceId") String deviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            SwmSafetyHelmetOrder order = swmSafetyHelmetOrderService.findActiveOrderByDeviceId(deviceId);
            result.put("success", true);
            result.put("data", order);
        } catch (Exception e) {
            logger.error("查询安全帽当前使用订单异常", e);
            result.put("success", false);
            result.put("message", "查询安全帽当前使用订单失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 保存安全帽订购记录
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmSafetyHelmetOrder order) {
        swmSafetyHelmetOrderService.save(order);
        return renderResult(Global.TRUE, text("保存安全帽订购记录成功！"));
    }

    /**
     * 删除安全帽订购记录
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmSafetyHelmetOrder order) {
        swmSafetyHelmetOrderService.delete(order);
        return renderResult(Global.TRUE, text("删除安全帽订购记录成功！"));
    }

    /**
     * 更新订购状态
     */
    @PostMapping(value = "updateStatus")
    @ResponseBody
    public String updateStatus(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        String orderStatus = (String) params.get("orderStatus");
        String receivedSign = (String) params.get("receivedSign");

        try {
            SwmSafetyHelmetOrder order = swmSafetyHelmetOrderService.get(id);
            if (order == null) {
                return renderResult(Global.FALSE, text("订购记录不存在"));
            }

            order.setOrderStatus(orderStatus);
            if (SwmSafetyHelmetOrder.OrderStatusEnum.DELIVERED.equals(orderStatus)) {
                order.setReceivedDate(new java.util.Date());
                order.setReceivedSign(receivedSign);
            }

            swmSafetyHelmetOrderService.updateOrderStatus(order);
            return renderResult(Global.TRUE, text("更新订购状态成功！"));
        } catch (Exception e) {
            logger.error("更新订购状态异常", e);
            return renderResult(Global.FALSE, text("更新订购状态失败：" + e.getMessage()));
        }
    }
    
    /**
     * 绑定安全帽
     */
    @PostMapping(value = "bindHelmet")
    @ResponseBody
    public String bindHelmet(@RequestBody Map<String, Object> params) {
        String orderId = (String) params.get("orderId");
        String binder = (String) params.get("binder"); // 获取绑定人员身份证
        
        try {
            SwmSafetyHelmetOrder order = swmSafetyHelmetOrderService.get(orderId);
            if (order == null) {
                return renderResult(Global.FALSE, text("订购记录不存在"));
            }
            
            // 调用带binder参数的绑定方法
            if (binder != null && !binder.isEmpty()) {
                swmSafetyHelmetOrderService.bindHelmet(orderId, binder);
            } else {
                swmSafetyHelmetOrderService.bindHelmet(orderId);
            }
            
            return renderResult(Global.TRUE, text("安全帽绑定成功！"));
        } catch (Exception e) {
            logger.error("安全帽绑定异常", e);
            return renderResult(Global.FALSE, text("安全帽绑定失败：" + e.getMessage()));
        }
    }
    
    /**
     * 解绑安全帽
     */
    @PostMapping(value = "unbindHelmet")
    @ResponseBody
    public String unbindHelmet(@RequestBody Map<String, Object> params) {
        String orderId = (String) params.get("orderId");
        
        try {
            SwmSafetyHelmetOrder order = swmSafetyHelmetOrderService.get(orderId);
            if (order == null) {
                return renderResult(Global.FALSE, text("订购记录不存在"));
            }
            
            if (!SwmSafetyHelmetOrder.UsageStatusEnum.USING.equals(order.getUsageStatus())) {
                return renderResult(Global.FALSE, text("该安全帽未处于使用状态，无法解绑"));
            }
            
            swmSafetyHelmetOrderService.unbindHelmet(orderId);
            return renderResult(Global.TRUE, text("安全帽解绑成功！"));
        } catch (Exception e) {
            logger.error("安全帽解绑异常", e);
            return renderResult(Global.FALSE, text("安全帽解绑失败：" + e.getMessage()));
        }
    }
}