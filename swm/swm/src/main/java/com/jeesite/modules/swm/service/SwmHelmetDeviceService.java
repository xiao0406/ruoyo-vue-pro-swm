/**
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.service.CrudService;
import com.jeesite.common.utils.excel.ExcelImport;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.entity.SwmBeaconStationExport;
import com.jeesite.modules.entity.SwmHelmetDeviceExport;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.dao.SwmHelmetDeviceDao;
import com.jeesite.modules.swm.dao.SwmSafetyHelmetOrderDao;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.jeesite.modules.utils.R;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * 头盔设备管理服务
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class SwmHelmetDeviceService extends CrudService<SwmHelmetDeviceDao, SwmHelmetDevice> {

    private static final Logger logger = LoggerFactory.getLogger(SwmHelmetDeviceService.class);

    // 简单的内存缓存，用于缓存头盔信息
    private final ConcurrentMap<String, SwmHelmetDevice> helmetCache = new ConcurrentHashMap<>();

    @Autowired
    private SwmSafetyHelmetOrderDao swmSafetyHelmetOrderDao;

    @Autowired
    private SwmPersonService swmPersonService;

    @Autowired
    private TDengineService tdengineService;

    @Autowired
    private SwmHelmetCacheService helmetCacheService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserService userService;

    /**
     * 安全帽超级表名称
     */
    private static final String HELMET_SUPER_TABLE_NAME = "helmet_runde_ca_report_location";

    /**
     * 程序启动时初始化设备缓存
     * 框架会自动添加status='0'条件，只查询正常状态的设备
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    @PostConstruct
    public void initCache() {
        try {
            // 检查Redis连接状态
            if (!helmetCacheService.isRedisAvailable()) {
                logger.warn("Redis连接不可用，跳过缓存初始化");
                return;
            }

            // 查询所有头盔设备数据（框架自动添加status='0'条件）
            List<SwmHelmetDevice> allDevices = new ArrayList<>();

            //获取系统所有租户信息
            List<User> corpList = userService.findCorpList(new User());
            if (CollectionUtils.isEmpty(corpList)) {
                XxlJobHelper.log("没有租户信息");
                return;
            }
            //为每个租户都生成排班计划
            for (User user : corpList) {
                try {
                    String corpCode1 = CorpUtils.getCurrentCorpCode();
                    String corpCode = user.getCorpCode();
                    String corpName = user.getCorpName();
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    // 查询所有头盔设备数据（框架自动添加status='0'条件）
                    SwmHelmetDevice queryCondition = new SwmHelmetDevice();
                    queryCondition.setRandom(new Random().nextInt(1_000_000));
                    queryCondition.setCorpCode(corpCode);
                    String corpCode2 = CorpUtils.getCurrentCorpCode();
                    List<SwmHelmetDevice> list = this.findListInit(queryCondition);
                    allDevices.addAll( list);
                } catch (Exception e) {
                    logger.error("初始化头盔设备Redis缓存失败", e);
                }finally {
                    CorpUtils.setCurrentCorpCode(null,null);
                }
            }

            // 初始化Redis缓存
            helmetCacheService.initHelmetCache(allDevices);

            logger.info("头盔设备Redis缓存初始化完成，共{}条正常状态设备", allDevices.size());
        } catch (Exception e) {
            logger.error("初始化头盔设备Redis缓存失败", e);
        }
    }

    private List<SwmHelmetDevice> findListInit(SwmHelmetDevice queryCondition) {
        return this.dao.findListInit(queryCondition);
    }

    /**
     * 获取单条数据
     */
    @Override
    public SwmHelmetDevice get(String id) {
        return super.get(id);
    }

    /**
     * 根据头盔编号获取头盔设备
     */
    public SwmHelmetDevice getByDeviceId(String deviceId) {
        SwmHelmetDevice result = dao.getByDeviceId(deviceId);
        if (result != null) {
            // 放入内存缓存
            helmetCache.put(deviceId, result);
        }

        return result;
    }

    /**
     * 直接从数据库获取头盔设备（不使用缓存）
     */
    public SwmHelmetDevice getByDeviceIdFromDB(String deviceId) {
        return dao.getByDeviceId(deviceId);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmHelmetDevice> findPage(SwmHelmetDevice device) {
        // 确保设备对象有Page对象
        if (device.getPage() == null) {
            device.setPage(new Page<>());
        }

        // 判断是否有电量查询条件
        if (device.getBatteryLevel() != null) {
            // 走两阶段查询流程
            return findPageWithBatteryFilter(device);
        } else {
            // 走原有查询流程
            List<String> deviceIdList = new ArrayList<>();
            //查询设备在线数量
            try {
                String corpCode = CorpUtils.getCurrentCorpCode();
                Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
                if (deviceIds != null) {
                    deviceIdList = deviceIds.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    device.setDeviceOnlist(deviceIdList);
                } else {
                    device.setDeviceOnlist(new ArrayList<>());
                }
            } catch (Exception e) {
                log.warn("获取在线设备列表失败，使用空列表", e);
                device.setDeviceOnlist(new ArrayList<>());
            }
            Page<SwmHelmetDevice> result = findPageNormal(device);
            if (result != null) {
                for (SwmHelmetDevice swmHelmetDevice : result.getList()) {
                    if (deviceIdList.contains(swmHelmetDevice.getDeviceId())){
                        swmHelmetDevice.setPowerOnStatus("0");
                    }else {
                        swmHelmetDevice.setPowerOnStatus("1");
                    }
                }
            }
            return result;
        }
    }

    /**
     * 带电量条件的两阶段查询
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    private Page<SwmHelmetDevice> findPageWithBatteryFilter(SwmHelmetDevice device) {
        Page<SwmHelmetDevice> page = device.getPage();

        try {
            // 第一阶段：从TDengine查询符合电量条件的设备ID列表
            List<String> filteredDeviceIds = findDeviceIdsByBatteryLevel(device.getBatteryLevel());

            if (filteredDeviceIds.isEmpty()) {
                // 没有符合条件的设备，返回空结果
                page.setCount(0);
                page.setList(new ArrayList<>());
                return page;
            }

            // 第二阶段：根据设备ID列表查询设备详情
            // 手动实现分页逻辑
            int pageNum = page.getPageNo();
            int pageSize = page.getPageSize();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredDeviceIds.size());

            // 如果有其他查询条件，需要重新计算总数
            if (hasOtherConditions(device)) {
                // 创建查询条件对象，设置设备ID列表（包含所有符合电量条件的设备）
                SwmHelmetDevice countDevice = new SwmHelmetDevice();
                countDevice.setDeviceIdList(filteredDeviceIds);
                countDevice.setHelmetType(device.getHelmetType());
                countDevice.setAssignedPerson(device.getAssignedPerson());

                // 查询符合所有条件的设备总数
                List<SwmHelmetDevice> allFilteredDevices = dao.findHelmetDeviceListByDeviceIds(countDevice);
                page.setCount(allFilteredDevices.size());

                // 重新计算分页
                if (start >= allFilteredDevices.size()) {
                    page.setList(new ArrayList<>());
                    return page;
                }

                end = Math.min(start + pageSize, allFilteredDevices.size());
                // 获取当前页的数据
                List<SwmHelmetDevice> list = allFilteredDevices.subList(start, end);

                // 为每个设备设置最新电量
                for (SwmHelmetDevice swmHelmetDevice : list) {
                    updateDeviceBatteryLevel(swmHelmetDevice);
                }

                page.setList(list);
                return page;
            } else {
                // 没有其他查询条件，直接使用设备ID列表的大小作为总数
                page.setCount(filteredDeviceIds.size());

                if (start >= filteredDeviceIds.size()) {
                    // 页码超出范围
                    page.setList(new ArrayList<>());
                    return page;
                }

                // 获取当前页的设备ID
                List<String> pageDeviceIds = filteredDeviceIds.subList(start, end);

                // 创建查询条件对象，设置设备ID列表
                SwmHelmetDevice queryDevice = new SwmHelmetDevice();
                queryDevice.setDeviceIdList(pageDeviceIds);

                // 根据设备ID列表查询设备详情
                List<SwmHelmetDevice> list = dao.findHelmetDeviceListByDeviceIds(queryDevice);

                // 为每个设备设置最新电量
                for (SwmHelmetDevice swmHelmetDevice : list) {
                    updateDeviceBatteryLevel(swmHelmetDevice);
                }

                page.setList(list);
                return page;
            }

        } catch (Exception e) {
            logger.error("电量条件查询失败，降级到普通查询", e);
            // 降级到普通查询
            return findPageNormal(device);
        }
    }

    /**
     * 检查是否有除了电量之外的其他查询条件
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    private boolean hasOtherConditions(SwmHelmetDevice device) {
        return (device.getHelmetType() != null && !device.getHelmetType().trim().isEmpty())
                || (device.getAssignedPerson() != null && !device.getAssignedPerson().trim().isEmpty())
                || (device.getDeviceId() != null && !device.getDeviceId().trim().isEmpty());
    }

    /**
     * 普通查询流程（原有逻辑）
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    private Page<SwmHelmetDevice> findPageNormal(SwmHelmetDevice device) {
        Page<SwmHelmetDevice> page = device.getPage();

        // 使用框架原生的COUNT机制，避免自定义COUNT查询被错误添加分页参数
        long count = super.findCount(device);
        page.setCount(count);

        // 如果总数为0，则直接返回空列表
        if (count <= 0) {
            page.setList(new ArrayList<>());
            return page;
        }

        // 查询数据列表，仍使用自定义查询以支持复杂的LEFT JOIN
        List<SwmHelmetDevice> list = dao.findHelmetDeviceListWithRelations(device);

        // 为每个设备设置最新电量
        for (SwmHelmetDevice swmHelmetDevice : list) {
            updateDeviceBatteryLevel(swmHelmetDevice);
        }

        // 设置查询结果
        page.setList(list);
        return page;
    }

    /**
     * 更新设备电量信息
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    private void updateDeviceBatteryLevel(SwmHelmetDevice swmHelmetDevice) {
        // 默认将电量设置为空，如果时序数据库中没有查到，则返回null
        swmHelmetDevice.setBatteryLevel(null);

        String sql = String.format(
                "select time , bat_l from %s.%s " +
                        "where device_id = '%s' " +
                        "AND time <= NOW() " +
                        "AND time >= NOW() - 5m \n" +
                        "ORDER BY time DESC \n" +
                        "LIMIT 1;",
                dbname, HELMET_SUPER_TABLE_NAME, swmHelmetDevice.getDeviceId());

        try {
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result != null && result.getData() != null) {
                JSONObject obj = result.getData();
                JSONArray dataArray = obj.getJSONArray("data");

                if (dataArray != null && dataArray.size() > 0) {
                    int batL = dataArray.getJSONArray(0).getInt(1);
                    swmHelmetDevice.setBatteryLevel(batL);
                    // 如果能查到电量，说明设备在线，设置运动状态为'1'
                    swmHelmetDevice.setMotionStatus("1");
                } else {
                    // 如果在指定时间范围内没有数据，则认为设备离线
                    swmHelmetDevice.setMotionStatus("0");
                }
            } else {
                // 如果查询结果为空，也认为设备离线
                swmHelmetDevice.setMotionStatus("0");
            }
        } catch (Exception e) {
            // 如果查询时序数据库时发生异常，则电量保持为空 (null)，并标记为离线
            swmHelmetDevice.setMotionStatus("0");
            logger.error("查询设备 {} 的电量失败: {}", swmHelmetDevice.getDeviceId(), e.getMessage());
        }
    }

    /**
     * 根据电量条件从TDengine查询设备ID列表
     * 使用LAST_ROW函数确保查询的是每个设备的最新电量状态
     * 
     * @author Shawn
     * @date 2025-01-13
     */
    public List<String> findDeviceIdsByBatteryLevel(Integer batteryLevel) {
        List<String> deviceIds = new ArrayList<>();

        // 使用LAST_ROW函数获取每个设备在最近5分钟内的最新电量记录
        // 只返回最新电量为指定值的设备
        String sql = String.format(
                "SELECT device_id, LAST_ROW(bat_l) as latest_battery " +
                        "FROM %s.%s " +
                        "WHERE time >= NOW() - 5m " +
                        "GROUP BY device_id " +
                        "HAVING LAST_ROW(bat_l) <= %d;",
                dbname, HELMET_SUPER_TABLE_NAME, batteryLevel);

        try {
            logger.info("根据电量条件{}%查询设备ID的SQL: {}", batteryLevel, sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result != null && result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONObject obj = result.getData();
                JSONArray dataArray = obj.getJSONArray("data");

                if (dataArray != null && dataArray.size() > 0) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        String deviceId = row.getStr(0);
                        Integer latestBattery = row.getInt(1);

                        if (deviceId != null && !deviceId.trim().isEmpty()) {
                            deviceIds.add(deviceId);
                            logger.debug("设备 {} 的最新电量: {}%", deviceId, latestBattery);
                        }
                    }
                }
            } else {
                logger.warn("TDengine查询失败或无数据, 返回码: {}, 消息: {}",
                        result != null ? result.getCode() : "null",
                        result != null ? result.getMsg() : "null");
            }

            logger.info("根据电量条件{}%查询到{}个设备（确保是最新状态）", batteryLevel, deviceIds.size());
            return deviceIds;

        } catch (Exception e) {
            logger.error("根据电量条件查询设备ID失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据电量条件从TDengine查询设备ID列表
     * 使用LAST_ROW函数确保查询的是每个设备的最新电量状态
     *
     * @author Shawn
     * @date 2025-01-13
     */
    public List<Map<String, String>> findDeviceIdAndIdBatteryByBatteryLevel(Integer batteryLevel) {
        List<Map<String, String>> mapList = new ArrayList<>();

        // 使用LAST_ROW函数获取每个设备在最近5分钟内的最新电量记录
        // 只返回最新电量为指定值的设备
        String sql = String.format(
                "SELECT device_id, LAST_ROW(bat_l) as latest_battery " +
                        "FROM %s.%s " +
                        "WHERE time >= NOW() - 5m " +
                        "GROUP BY device_id " +
                        "HAVING LAST_ROW(bat_l) <= %d;",
                dbname, HELMET_SUPER_TABLE_NAME, batteryLevel);

        try {
            logger.info("根据电量条件{}%查询设备ID的SQL: {}", batteryLevel, sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result != null && result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONObject obj = result.getData();
                JSONArray dataArray = obj.getJSONArray("data");

                if (dataArray != null && dataArray.size() > 0) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        String deviceId = row.getStr(0);
                        Integer latestBattery = row.getInt(1);

                        if (deviceId != null && !deviceId.trim().isEmpty()) {
                            Map<String, String> map = new HashMap<>();
                            map.put("deviceId", deviceId);
                            map.put("latestBattery", String.valueOf(latestBattery));
                            mapList.add(map);
                            logger.debug("设备 {} 的最新电量: {}%", deviceId, latestBattery);
                        }
                    }
                }
            } else {
                logger.warn("TDengine查询失败或无数据, 返回码: {}, 消息: {}",
                        result != null ? result.getCode() : "null",
                        result != null ? result.getMsg() : "null");
            }

            logger.info("根据电量条件{}%查询到{}个设备（确保是最新状态）", batteryLevel, mapList.size());
            return mapList;

        } catch (Exception e) {
            logger.error("根据电量条件查询设备ID失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询可用的安全帽列表（未绑定人员的）
     */
    public List<SwmHelmetDevice> findAvailableHelmets(String keyword) {
        return dao.findAvailableHelmets(keyword);
    }

    /**
     * 根据绑定人员查询设备
     */
    public List<SwmHelmetDevice> findByAssignedPerson(String assignedPerson) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedPerson(assignedPerson);
        return this.findList(device);
    }

    /**
     * 根据所属车间查询设备
     */
    public List<SwmHelmetDevice> findByWorkshop(String assignedWorkshop) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedWorkshop(assignedWorkshop);
        return this.findList(device);
    }

    /**
     * 根据所属工序查询设备
     */
    public List<SwmHelmetDevice> findByProcess(String assignedProcess) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedProcess(assignedProcess);
        return this.findList(device);
    }

    /**
     * 根据所属班组查询设备
     */
    public List<SwmHelmetDevice> findByTeam(String assignedTeam) {
        // 使用实体类注解查询 - 无需特殊DAO方法
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setAssignedTeam(assignedTeam);
        return this.findList(device);
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmHelmetDevice device) {
        super.save(device);

        // 更新缓存
        if (device.getDeviceId() != null) {
            // 更新内存缓存
            helmetCache.put(device.getDeviceId(), device);
            // 更新分配关系缓存
            helmetCacheService.updateDevicePersonMapping(device.getDeviceId(), device.getAssignedPerson());
            logger.debug("已更新设备缓存: {}", device.getDeviceId());
        }
    }

    /**
     * 更新设备信息（专门用于更新现有设备）
     * 
     * @author Shawn
     * @date 2025-05-31
     */
    @Transactional(readOnly = false)
    public void updateDevice(SwmHelmetDevice device) {
        if (device == null || device.getId() == null) {
            throw new IllegalArgumentException("设备信息或设备ID不能为空");
        }

        // 确保这是更新操作，设置isNewRecord为false
        device.setIsNewRecord(false);

        // 直接调用DAO的update方法，避免save方法的插入/更新判断逻辑
        dao.update(device);

        // 更新缓存
        if (device.getDeviceId() != null) {
            // 更新内存缓存
            helmetCache.put(device.getDeviceId(), device);
            // 更新分配关系缓存
            helmetCacheService.updateDevicePersonMapping(device.getDeviceId(), device.getAssignedPerson());
            logger.debug("已更新设备缓存: {}", device.getDeviceId());
        }
    }

    /**
     * 强制清空设备绑定信息（将assigned_person等字段设置为null）
     * 
     * @author Shawn
     * @date 2025-05-31
     */
    @Transactional(readOnly = false)
    public void clearDeviceAssignment(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("设备ID不能为空");
        }

        // 直接使用DAO执行SQL更新，强制将字段设置为null
        int result = dao.clearDeviceAssignment(deviceId);
        if (result > 0) {
            // 从内存缓存中移除，下次查询时会重新从数据库加载
            helmetCache.remove(deviceId);
            // 更新Redis缓存 - 清除分配关系
            helmetCacheService.updateDevicePersonMapping(deviceId, null);
            logger.info("已强制清空设备{}的绑定信息", deviceId);
        } else {
            logger.warn("清空设备{}绑定信息失败，可能设备不存在", deviceId);
        }
    }

    /**
     * 批量保存数据
     */
    @Transactional(readOnly = false)
    public void saveBatch(List<SwmHelmetDevice> deviceList) {
        for (SwmHelmetDevice device : deviceList) {
            this.save(device);
        }
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmHelmetDevice device) {
        super.delete(device);

        // 从缓存中移除
        if (device.getDeviceId() != null) {
            // 从内存缓存中移除
            helmetCache.remove(device.getDeviceId());
            // 清除Redis缓存
            helmetCacheService.clearDeviceCache(device.getDeviceId());
            logger.debug("已从缓存中移除安全帽: {}", device.getDeviceId());
        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        // 清除内存缓存
        helmetCache.clear();
        // 清除Redis缓存
        helmetCacheService.clearAllHelmetCache();
        logger.info("已清除所有安全帽缓存");
    }

    /**
     * 获取安全帽使用记录
     * 根据设备ID查询所有安全帽订单记录
     */
    public List<Map<String, Object>> getHelmetUsageRecords(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("设备ID不能为空");
        }

        List<SwmSafetyHelmetOrder> orderList = swmSafetyHelmetOrderDao.findByDeviceId(deviceId);
        List<Map<String, Object>> resultList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (SwmSafetyHelmetOrder order : orderList) {
            Map<String, Object> recordMap = new HashMap<>();

            // 获取需要的字段信息
            recordMap.put("id", order.getId());
            recordMap.put("personId", order.getPersonId());
            recordMap.put("deviceId", order.getDeviceId());

            // 绑定时间
            if (order.getBindTime() != null) {
                recordMap.put("bindTime", sdf.format(order.getBindTime()));
            } else {
                recordMap.put("bindTime", null);
            }

            // 解绑时间
            if (order.getUnbindTime() != null) {
                recordMap.put("unbindTime", sdf.format(order.getUnbindTime()));
            } else {
                recordMap.put("unbindTime", null);
            }

            // 绑定时长
            recordMap.put("bindDuration", order.getBindDuration());

            // 使用状态
            recordMap.put("usageStatus", order.getUsageStatus());
            recordMap.put("usageStatusText", order.getUsageStatusText());

            // 绑定人员身份证
            recordMap.put("binder", order.getBinder());

            // 查询人员名称
            SwmPerson person = swmPersonService.get(order.getPersonId());
            if (person != null) {
                recordMap.put("personName", person.getName());
            } else {
                recordMap.put("personName", null);
            }

            resultList.add(recordMap);
        }

        return resultList;
    }

    public List<SwmHelmetDevice> findDeviceCorpMapping(SwmHelmetDevice device) {
        return this.dao.findDeviceCorpMapping(device);
    }

    @Transactional(readOnly = false)
    public Integer importData(MultipartFile file) {
        ExcelImport excelImport = null;
        List<SwmHelmetDevice> deviceList = new ArrayList<>();
        Integer count = 0;
        try {
            excelImport = new ExcelImport(file, 2, 0);
            List<SwmHelmetDeviceExport> list = excelImport.getDataList(SwmHelmetDeviceExport.class);
            if (CollectionUtil.isNotEmpty(list)){
                for (SwmHelmetDeviceExport swmHelmetDeviceExport : list) {
                    SwmHelmetDevice device = new SwmHelmetDevice();
                    device.setDeviceId(swmHelmetDeviceExport.getDeviceId());
                    device.setMacAddress(swmHelmetDeviceExport.getMacAddress());
                    device.setHelmetType(swmHelmetDeviceExport.getHelmetType());
                    deviceList.add(device);
                }

                List<List<SwmHelmetDevice>> lists = BatchOperationsUtil.batchCutting(deviceList, 100);
                for (List<SwmHelmetDevice> list1 : lists) {
                    this.dao.insertBatch(list1);
                }
                count = list.size();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return count;
    }
}