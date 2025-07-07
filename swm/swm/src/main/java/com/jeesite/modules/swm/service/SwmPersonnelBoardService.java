package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonnelBoardDao;
import com.jeesite.modules.swm.entity.SwmPersonnelBoard;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.utils.R;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.lang.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

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
        Page<SwmPersonnelBoard> page = super.findPage(swmPersonnelBoard);

        // 为每个人员设置工作状态和安全帽状态
        if (page != null && page.getList() != null) {
            for (SwmPersonnelBoard board : page.getList()) {
                setPersonnelStatus(board);
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
            // 没有设备ID，设置为休息中和脱帽状态
            board.setWorkStatus("0"); // 休息中
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
                    "SELECT COUNT(*) FROM %s.helmet_runde_ca_report_location WHERE device_id='%s' AND time >= '%s'",
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
}