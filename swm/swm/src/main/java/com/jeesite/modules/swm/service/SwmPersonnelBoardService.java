package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.swm.dao.SwmPersonnelBoardDao;
import com.jeesite.modules.swm.entity.SwmPersonnelBoard;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;

/**
 * 人员看板表Service
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonnelBoardService extends CrudService<SwmPersonnelBoardDao, SwmPersonnelBoard> {

    private static final Logger logger = LoggerFactory.getLogger(SwmPersonnelBoardService.class);

    @Autowired
    private TDengineService tdengineService;

    @Autowired
    private SwmHelmetDeviceService helmetDeviceService;

    @Value("${tdengine.dbname}")
    private String dbname;

    /**
     * 获取单条数据
     * 
     * @param swmPersonnelBoard
     * @return
     */
    @Override
    public SwmPersonnelBoard get(SwmPersonnelBoard swmPersonnelBoard) {
        return super.get(swmPersonnelBoard);
    }

    /**
     * 查询分页数据
     *
     * @param swmPersonnelBoard
     * @return
     */
    public Page<SwmPersonnelBoard> findPage(SwmPersonnelBoard swmPersonnelBoard) {
        // 如果传入了工作状态查询参数，先预查询设备ID列表
        if (swmPersonnelBoard.getWorkStatus() != null && !swmPersonnelBoard.getWorkStatus().isEmpty()) {
            List<String> deviceIds = getDeviceIdsByWorkStatus(swmPersonnelBoard.getWorkStatus());
            swmPersonnelBoard.setDeviceIds(deviceIds);
            logger.debug("工作状态查询预处理：workStatus={}, 符合条件的设备ID数量={}",
                    swmPersonnelBoard.getWorkStatus(), deviceIds.size());
        }

        Page<SwmPersonnelBoard> page = super.findPage(swmPersonnelBoard);

        // 为每个人员设置工作状态和安全帽状态
        if (page != null && page.getList() != null) {
            for (SwmPersonnelBoard board : page.getList()) {
                setPersonnelStatus(board);
                // 设置休闲区统计数据
                setLeisureAreaStats(board, swmPersonnelBoard.getTimeType(), swmPersonnelBoard.getTimeValue());
            }
        }

        return page;
    }

    /**
     * 查询分页数据（带页面参数）
     * 
     * @param page              分页对象
     * @param swmPersonnelBoard
     * @return
     */
    public Page<SwmPersonnelBoard> findPage(Page<SwmPersonnelBoard> page, SwmPersonnelBoard swmPersonnelBoard) {
        // 设置分页参数
        swmPersonnelBoard.setPage(page);
        // 执行查询
        return this.findPage(swmPersonnelBoard);
    }

    /**
     * 查询所有数据
     * 
     * @param swmPersonnelBoard
     * @return
     */
    public List<SwmPersonnelBoard> findList(SwmPersonnelBoard swmPersonnelBoard) {
        return super.findList(swmPersonnelBoard);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmPersonnelBoard
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonnelBoard swmPersonnelBoard) {
        super.save(swmPersonnelBoard);
    }

    /**
     * 更新状态
     * 
     * @param swmPersonnelBoard
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmPersonnelBoard swmPersonnelBoard) {
        super.updateStatus(swmPersonnelBoard);
    }

    /**
     * 删除数据
     * 
     * @param swmPersonnelBoard
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonnelBoard swmPersonnelBoard) {
        super.delete(swmPersonnelBoard);
    }

    /**
     * 批量更新工作状态
     * 
     * @param ids        需要更新的ID列表
     * @param workStatus 新的工作状态
     */
    @Transactional(readOnly = false)
    public void batchUpdateWorkStatus(List<String> ids, String workStatus) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                SwmPersonnelBoard entity = new SwmPersonnelBoard(id);
                entity.setWorkStatus(workStatus);
                super.update(entity);
            }
        }
    }

    /**
     * 批量更新安全帽状态
     * 
     * @param ids          需要更新的ID列表
     * @param helmetStatus 新的安全帽状态
     */
    @Transactional(readOnly = false)
    public void batchUpdateHelmetStatus(List<String> ids, String helmetStatus) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                SwmPersonnelBoard entity = new SwmPersonnelBoard(id);
                entity.setHelmetStatus(helmetStatus);
                super.update(entity);
            }
        }
    }

    /**
     * 批量更新人员状态
     * 
     * @param ids             需要更新的ID列表
     * @param personnelStatus 新的人员状态
     */
    @Transactional(readOnly = false)
    public void batchUpdatePersonnelStatus(List<String> ids, String personnelStatus) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                SwmPersonnelBoard entity = new SwmPersonnelBoard(id);
                entity.setPersonnelStatus(personnelStatus);
                super.update(entity);
            }
        }
    }

    /**
     * 设置人员的工作状态和安全帽状态
     * 根据TDengine中5分钟内是否有数据来判断状态
     * 
     * @param board 人员看板数据
     */
    private void setPersonnelStatus(SwmPersonnelBoard board) {
        if (board == null || StringUtils.isBlank(board.getDeviceId())) {
            // 没有设备ID，设置为未分配设备状态
            board.setWorkStatus("2"); // 未分配设备
            board.setHelmetStatus("0"); // 脱帽
            return;
        }

        try {
            // 查询5分钟内是否有数据
            boolean hasDataInLast5Minutes = checkDeviceDataInLast5Minutes(board.getDeviceId());

            if (hasDataInLast5Minutes) {
                // 5分钟内有数据，设置为工作中
                board.setWorkStatus("1"); // 工作中

                // 检查是否有静默报警记录来判断安全帽状态
                boolean hasSilentAlarm = checkSilentAlarmInLast5Minutes(board.getDeviceId());
                if (hasSilentAlarm) {
                    // 有静默报警，设置为脱帽状态
                    board.setHelmetStatus("0"); // 脱帽
                    logger.debug("设备 {} 在5分钟内有静默报警，设置为工作中/脱帽", board.getDeviceId());
                } else {
                    // 无静默报警，设置为正常状态
                    board.setHelmetStatus("1"); // 正常
                    logger.debug("设备 {} 在5分钟内有数据且无静默报警，设置为工作中/正常", board.getDeviceId());
                }
            } else {
                // 5分钟内没有数据，设置为休息中和脱帽状态
                board.setWorkStatus("0"); // 休息中
                board.setHelmetStatus("0"); // 脱帽
                logger.debug("设备 {} 在5分钟内无数据，设置为休息中/脱帽", board.getDeviceId());
            }
        } catch (Exception e) {
            logger.error("设置人员状态失败，设备ID: {}, 错误: {}", board.getDeviceId(), e.getMessage(), e);
            // 异常情况下设置为休息中和脱帽状态
            board.setWorkStatus("0"); // 休息中
            board.setHelmetStatus("0"); // 脱帽
        }
    }

    /**
     * 检查设备在最近5分钟内是否有数据
     * 
     * @param deviceId 设备ID（安全帽编号）
     * @return true表示有数据，false表示没有数据
     */
    private boolean checkDeviceDataInLast5Minutes(String deviceId) {
        try {
            // 计算5分钟前的时间戳
            long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000;
            String fiveMinutesAgoStr = DateUtils.formatDate(new Date(fiveMinutesAgo), "yyyy-MM-dd HH:mm:ss");

            // 构建查询SQL - 查询5分钟内该设备是否有数据
            String sql = String.format(
                    "SELECT COUNT(*) FROM %s."+TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+" WHERE device_id='%s' AND time >= '%s'",
                    dbname, deviceId, fiveMinutesAgoStr);

            logger.debug("查询设备5分钟内数据SQL: {}", sql);

            R<cn.hutool.json.JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                cn.hutool.json.JSONObject data = result.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                if (rows != null && rows.size() > 0) {
                    cn.hutool.json.JSONArray row = rows.getJSONArray(0);
                    if (row != null && row.size() > 0) {
                        int count = row.getInt(0);
                        logger.debug("设备 {} 在5分钟内的数据条数: {}", deviceId, count);
                        return count > 0;
                    }
                }
            } else {
                logger.warn("查询设备数据失败，设备ID: {}, 错误: {}", deviceId, result.getMsg());
            }

            return false;
        } catch (Exception e) {
            logger.error("检查设备5分钟内数据异常，设备ID: {}", deviceId, e);
            return false;
        }
    }

    /**
     * 检查设备在最近5分钟内是否有静默报警记录
     * 
     * @param deviceId 设备ID（安全帽编号）
     * @return true表示有静默报警，false表示没有静默报警
     */
    private boolean checkSilentAlarmInLast5Minutes(String deviceId) {
        try {
            // 计算5分钟前的时间戳
            long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000;
            String fiveMinutesAgoStr = DateUtils.formatDate(new Date(fiveMinutesAgo), "yyyy-MM-dd HH:mm:ss");

            // 构建查询SQL - 查询5分钟内该设备是否有静默报警记录
            String sql = String.format(
                    "SELECT COUNT(*) FROM %s.swm_warning_management WHERE device_id='%s' AND warning_content='静默报警' AND alarm_time >= '%s'",
                    dbname, deviceId, fiveMinutesAgoStr);

            logger.debug("查询设备5分钟内静默报警SQL: {}", sql);

            R<cn.hutool.json.JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                cn.hutool.json.JSONObject data = result.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                if (rows != null && rows.size() > 0) {
                    cn.hutool.json.JSONArray row = rows.getJSONArray(0);
                    if (row != null && row.size() > 0) {
                        int count = row.getInt(0);
                        logger.debug("设备 {} 在5分钟内的静默报警条数: {}", deviceId, count);
                        return count > 0;
                    }
                }
            } else {
                logger.warn("查询设备静默报警失败，设备ID: {}, 错误: {}", deviceId, result.getMsg());
            }

            return false;
        } catch (Exception e) {
            logger.error("检查设备5分钟内静默报警异常，设备ID: {}", deviceId, e);
            return false;
        }
    }

    /**
     * 设置休闲区统计数据
     * 根据时间类型和值查询休闲区的进入次数和逗留时长
     * 
     * @param board     人员看板数据
     * @param timeType  时间类型：day, week, month
     * @param timeValue 时间值：YYYY-MM-DD, YYYY-WW, YYYY-MM
     */
    private void setLeisureAreaStats(SwmPersonnelBoard board, String timeType, String timeValue) {
        // 默认值初始化
        board.setLeisureCount(0);
        board.setLeisureDurationMin(0);

        // 检查必要的参数
        if (board == null) {
            logger.warn("人员看板数据为null，无法查询休闲区统计");
            return;
        }

        // 确保至少有身份证号或设备ID其中一个参数，否则无法查询
        boolean hasIdCard = StringUtils.isNotBlank(board.getIdCard());
        boolean hasDeviceId = StringUtils.isNotBlank(board.getDeviceId());

        if (!hasIdCard && !hasDeviceId) {
            logger.warn("人员ID: {}，既没有身份证号也没有设备ID，无法查询休闲区统计", board.getId());
            return;
        }

        try {
            // 计算查询的开始时间和结束时间
            String startTimeStr = "";
            String endTimeStr = "";

            // 获取当前时间作为默认结束时间
            Date now = new Date();
            endTimeStr = DateUtils.formatDate(now, "yyyy-MM-dd HH:mm:ss");

            // 根据时间类型和值计算开始时间
            if (timeType == null || "month".equals(timeType)) {
                // 月查询：如果没有指定月份，使用当前月份
                String monthStr = timeValue;
                if (StringUtils.isBlank(monthStr)) {
                    monthStr = DateUtils.formatDate(now, "yyyy-MM");
                }
                // 解析年月
                String[] parts = monthStr.split("-");
                if (parts.length >= 2) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);

                    // 创建月初日期
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month - 1); // 月份从0开始
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    startTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd HH:mm:ss");

                    // 计算月末（下个月的第一天减1秒）
                    cal.add(Calendar.MONTH, 1);
                    endTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
                } else {
                    // 格式错误，使用当月
                    startTimeStr = DateUtils.formatDate(now, "yyyy-MM") + "-01 00:00:00";
                }
            } else if ("day".equals(timeType)) {
                // 日查询：使用指定日期
                String dayStr = timeValue;
                if (StringUtils.isBlank(dayStr)) {
                    dayStr = DateUtils.formatDate(now, "yyyy-MM-dd");
                }

                // 设置为当天0点
                startTimeStr = dayStr + " 00:00:00";

                // 结束时间为当天23:59:59
                try {
                    Date startDate = DateUtils.parseDate(startTimeStr);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(startDate);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    cal.add(Calendar.SECOND, -1);
                    endTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
                } catch (Exception e) {
                    // 解析失败，使用当天
                    endTimeStr = dayStr + " 23:59:59";
                }
            } else if ("week".equals(timeType)) {
                // 周查询：解析类似"2025-30"这样的格式，表示2025年第30周
                if (StringUtils.isBlank(timeValue)) {
                    // 获取当前是一年中的第几周
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    int year = cal.get(Calendar.YEAR);
                    int week = cal.get(Calendar.WEEK_OF_YEAR);
                    timeValue = year + "-" + String.format("%02d", week);
                }

                logger.debug("解析周参数: {}", timeValue);

                // 解析年和周
                String[] parts = timeValue.split("-");
                if (parts.length >= 2) {
                    try {
                        int year = Integer.parseInt(parts[0]);
                        // 去除前导零（如有）
                        String weekPart = parts[1].replaceFirst("^0+(?!$)", "");
                        int week = Integer.parseInt(weekPart);

                        // 计算该周的第一天（周一）
                        Calendar cal = Calendar.getInstance();
                        cal.clear();
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.WEEK_OF_YEAR, week);
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        startTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd") + " 00:00:00";

                        // 计算该周的最后一天（周日）
                        cal.add(Calendar.DAY_OF_WEEK, 6);
                        endTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd") + " 23:59:59";
                    } catch (NumberFormatException e) {
                        logger.error("解析周参数失败: {}, 错误: {}", timeValue, e.getMessage());
                        // 使用当周
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(now);
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        startTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd") + " 00:00:00";
                        cal.add(Calendar.DAY_OF_WEEK, 6);
                        endTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd") + " 23:59:59";
                    }
                } else {
                    logger.error("无效的周参数格式: {}", timeValue);
                    // 使用当周
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    startTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd") + " 00:00:00";
                    cal.add(Calendar.DAY_OF_WEEK, 6);
                    endTimeStr = DateUtils.formatDate(cal.getTime(), "yyyy-MM-dd") + " 23:59:59";
                }
            }

            logger.debug("计算休闲区统计 - 时间范围: {} 至 {}, 时间类型: {}, 时间值: {}",
                    startTimeStr, endTimeStr, timeType, timeValue);

            // 构建查询条件，确保包含id_card或device_id
            StringBuilder whereClause = new StringBuilder();

            // 首选身份证号条件
            if (hasIdCard) {
                whereClause.append("id_card='").append(board.getIdCard()).append("'");
                logger.debug("使用身份证号进行查询: {}", board.getIdCard());
            }
            // 如果没有身份证号，则使用设备ID
            else if (hasDeviceId) {
                whereClause.append("device_id='").append(board.getDeviceId()).append("'");
                logger.debug("使用设备ID进行查询: {}", board.getDeviceId());
            }

            // 添加时间条件
            whereClause.append(" AND time >= '").append(startTimeStr).append("'");
            whereClause.append(" AND time <= '").append(endTimeStr).append("'");

            // 查询MySQL中area_type=3的休闲区域ID列表
            List<String> leisureAreaIds = findLeisureAreaIds();
            if (leisureAreaIds.isEmpty()) {
                logger.warn("未找到配置的休闲区域(area_type=3)");
                return;
            }

            // 构建休闲区ID的IN条件
            StringBuilder areaIdsClause = new StringBuilder();
            areaIdsClause.append("(");
            for (int i = 0; i < leisureAreaIds.size(); i++) {
                if (i > 0) {
                    areaIdsClause.append(",");
                }
                areaIdsClause.append("'").append(leisureAreaIds.get(i)).append("'");
            }
            areaIdsClause.append(")");

            // 查询进入休闲区的次数
            // 修改查询，不使用DISTINCT，改为查询所有area_id，然后在Java中去重
            String countSql = String.format(
                    "SELECT area_id FROM %s.area_fence_data WHERE %s AND area_id IN %s",
                    dbname, whereClause.toString(), areaIdsClause.toString());

            logger.debug("查询休闲区进入次数SQL: {}", countSql);

            // 记录当前查询的身份标识，便于日志追踪
            String personIdentifier = hasIdCard ? "id_card=" + board.getIdCard() : "device_id=" + board.getDeviceId();
            logger.debug("开始查询休闲区统计, 人员: {}, ID: {}", board.getName(), personIdentifier);

            R<cn.hutool.json.JSONObject> countResult = tdengineService.executeTDengineSQL(countSql);

            // 在Java中实现去重逻辑
            Set<String> uniqueAreaIds = new HashSet<>();
            if (countResult.getCode() == R.SUCCESS && countResult.getData() != null) {
                cn.hutool.json.JSONObject data = countResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");
                if (rows != null && rows.size() > 0) {
                    for (int i = 0; i < rows.size(); i++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String areaId = row.getStr(0);
                            if (StringUtils.isNotBlank(areaId)) {
                                uniqueAreaIds.add(areaId);
                            }
                        }
                    }
                }
            }

            // 休闲区进入次数为不同休闲区的数量
            Integer distinctAreaCount = uniqueAreaIds.size();

            // 查询在休闲区的总时长（以分钟为单位）
            // 简单查询在指定时间范围内进入休闲区的记录总数
            String durationSql = String.format(
                    "SELECT COUNT(*) FROM %s.area_fence_data WHERE %s AND area_id IN %s",
                    dbname, whereClause.toString(), areaIdsClause.toString());

            logger.debug("查询休闲区进入总次数SQL: {}", durationSql);
            R<cn.hutool.json.JSONObject> durationResult = tdengineService.executeTDengineSQL(durationSql);

            // 计算总次数，每次20秒，转换为分钟
            int totalEntryCount = 0;
            if (durationResult.getCode() == R.SUCCESS && durationResult.getData() != null) {
                cn.hutool.json.JSONObject data = durationResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");
                if (rows != null && rows.size() > 0) {
                    cn.hutool.json.JSONArray row = rows.getJSONArray(0);
                    if (row != null && row.size() > 0) {
                        totalEntryCount = row.getInt(0);
                    }
                }
            }

            // 将进入次数乘以20秒，再转换为分钟
            Integer totalDurationMinutes = totalEntryCount * 20 / 60;

            logger.debug("人员ID: {}, 设备ID: {}, 休闲区进入次数(去重): {}, 进入总次数: {}, 总时长: {}分钟",
                    board.getId(), board.getDeviceId(), distinctAreaCount, totalEntryCount, totalDurationMinutes);

            // 设置结果 - 使用真实的进入总次数而不是去重后的区域数量
            board.setLeisureCount(totalEntryCount);
            board.setLeisureDurationMin(totalDurationMinutes);

        } catch (Exception e) {
            logger.error("计算休闲区统计数据失败，ID: {}, 错误: {}", board.getId(), e.getMessage(), e);
            // 异常情况下设置为0
            board.setLeisureCount(0);
            board.setLeisureDurationMin(0);
        }
    }

    /**
     * 查询休闲区域ID列表（area_type=3的区域）
     * 
     * @return 休闲区域ID列表
     */
    private List<String> findLeisureAreaIds() {
        try {
            // 使用DAO接口提供的方法
            return this.dao.findLeisureAreaIds();
        } catch (Exception e) {
            logger.error("查询休闲区域ID失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据工作状态获取符合条件的设备ID列表
     * 
     * @param workStatus 工作状态：1-工作中，0-休息中
     * @return 符合条件的设备ID列表
     */
    private List<String> getDeviceIdsByWorkStatus(String workStatus) {
        List<String> deviceIds = new ArrayList<>();

        try {
            // 计算5分钟前的时间戳
            long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000;
            String fiveMinutesAgoStr = DateUtils.formatDate(new Date(fiveMinutesAgo), "yyyy-MM-dd HH:mm:ss");

            if ("1".equals(workStatus)) {
                // 查询工作中的设备：5分钟内有数据的设备
                String sql = String.format(
                        "SELECT DISTINCT device_id FROM %s." +TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+ " WHERE time >= '%s'",
                        dbname, fiveMinutesAgoStr);

                logger.debug("查询工作中设备SQL: {}", sql);

                R<cn.hutool.json.JSONObject> result = tdengineService.executeTDengineSQL(sql);

                if (result.getCode() == R.SUCCESS && result.getData() != null) {
                    cn.hutool.json.JSONObject data = result.getData();
                    cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                    if (rows != null && rows.size() > 0) {
                        for (int i = 0; i < rows.size(); i++) {
                            cn.hutool.json.JSONArray row = rows.getJSONArray(i);
                            if (row != null && row.size() > 0) {
                                String deviceId = row.getStr(0);
                                if (StringUtils.isNotBlank(deviceId)) {
                                    deviceIds.add(deviceId);
                                }
                            }
                        }
                    }
                }

                logger.debug("查询到工作中的设备数量: {}", deviceIds.size());

            } else if ("0".equals(workStatus)) {
                // 查询休息中的设备：需要特殊处理
                // 1. 先查询工作中的设备ID
                List<String> workingDeviceIds = new ArrayList<>();
                String sql = String.format(
                        "SELECT DISTINCT device_id FROM %s.%s WHERE time >= '%s'",
                        dbname,TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION, fiveMinutesAgoStr);

                logger.debug("查询工作中设备SQL（用于排除）: {}", sql);

                R<cn.hutool.json.JSONObject> result = tdengineService.executeTDengineSQL(sql);

                if (result.getCode() == R.SUCCESS && result.getData() != null) {
                    cn.hutool.json.JSONObject data = result.getData();
                    cn.hutool.json.JSONArray rows = data.getJSONArray("data");

                    if (rows != null && rows.size() > 0) {
                        for (int i = 0; i < rows.size(); i++) {
                            cn.hutool.json.JSONArray row = rows.getJSONArray(i);
                            if (row != null && row.size() > 0) {
                                String deviceId = row.getStr(0);
                                if (StringUtils.isNotBlank(deviceId)) {
                                    workingDeviceIds.add(deviceId);
                                }
                            }
                        }
                    }
                }

                // 2. 获取所有设备ID
                List<String> allDeviceIds = getAllDeviceIds();

                // 3. 找出休息中的设备ID（有设备但不在工作中的）
                for (String deviceId : allDeviceIds) {
                    if (!workingDeviceIds.contains(deviceId)) {
                        deviceIds.add(deviceId);
                    }
                }

                // 休息中只查询有设备但不活跃的人员，不包含无设备人员
                // 无设备人员通过状态"2"单独查询

                logger.debug("查询到休息中的设备数量: {}", deviceIds.size());

            } else if ("2".equals(workStatus)) {
                // 查询未分配设备：返回包含空字符串的列表
                // 这样XML中的条件会匹配 hd.device_id IS NULL OR hd.device_id = ''
                deviceIds.add("");
                logger.debug("查询未分配设备人员");
            }

        } catch (Exception e) {
            logger.error("根据工作状态查询设备ID失败：workStatus={}", workStatus, e);
        }

        // 根据不同的工作状态处理空结果
        if (deviceIds.isEmpty()) {
            if ("2".equals(workStatus)) {
                // 未分配设备状态：添加空字符串匹配无设备人员
                deviceIds.add("");
            } else {
                // 工作中/休息中状态：添加不存在的设备ID，避免匹配到任何人员
                deviceIds.add("NO_DEVICE_FOUND");
            }
        }

        return deviceIds;
    }

    /**
     * 获取所有设备ID列表
     * 
     * @return 所有设备ID列表
     */
    private List<String> getAllDeviceIds() {
        List<String> deviceIds = new ArrayList<>();

        try {
            // 通过SwmHelmetDeviceService查询所有已分配的设备
            SwmHelmetDevice queryCondition = new SwmHelmetDevice();
            List<SwmHelmetDevice> allDevices = helmetDeviceService.findList(queryCondition);

            for (SwmHelmetDevice device : allDevices) {
                if (StringUtils.isNotBlank(device.getDeviceId())) {
                    deviceIds.add(device.getDeviceId());
                }
            }

            logger.debug("查询到所有设备ID数量: {}", deviceIds.size());

        } catch (Exception e) {
            logger.error("查询所有设备ID失败", e);
        }

        return deviceIds;
    }
}