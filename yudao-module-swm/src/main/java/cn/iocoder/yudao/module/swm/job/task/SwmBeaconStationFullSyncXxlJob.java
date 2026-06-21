package cn.iocoder.yudao.module.swm.job.task;

import cn.iocoder.yudao.module.swm.enums.SyncDataOperateTypeEnum;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmBeaconStationMapper;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.util.MqSendUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于XXL-Job的信标全量同步任务
 * 特性：1. 分布式调度 2. 支持手动/定时触发 3. 异步分批发送MQ 4. 任务日志上报 5. 失败重试
 * （完全对齐设备全量同步的代码结构和规范）
 */
@Component
@Slf4j
public class SwmBeaconStationFullSyncXxlJob {
    private static final Logger logger = LoggerFactory.getLogger(SwmBeaconStationFullSyncXxlJob.class);

    @Resource
    private SwmBeaconStationMapper swmBeaconStationMapper;

    @Resource
    private MqSendUtil mqSendUtil;

    // 每批发送条数（和设备/人员同步保持一致）
    private static final int BATCH_SIZE = 200;

    // 生产级线程池配置（复用设备同步的线程池参数）
    private final ExecutorService MQ_SEND_EXECUTOR = new ThreadPoolExecutor(
            2, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "mq-send-beacon-sync-" + System.currentTimeMillis()),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // MQ发送超时时间（5分钟，和设备同步保持一致）
    private static final long MQ_SEND_TIMEOUT = 5 * 60 * 1000L;

    @XxlJob("swmBeaconStationFullSyncHandler")
    public void swmBeaconStationFullSyncHandler() {
        String jobId = String.valueOf(XxlJobHelper.getJobId());
        String jobParam = XxlJobHelper.getJobParam(); // 租户代码参数
        XxlJobHelper.log("【XXL-Job-{}】开始执行信标全量同步任务，参数：{}", jobId, jobParam == null ? "无" : jobParam);

        AtomicBoolean isSuccess = new AtomicBoolean(false);
        StringBuffer resultMsg = new StringBuffer();

        try {
            // ====================== 核心：查询所有正常状态的信标 ======================
            List<SwmBeaconStationDO> beaconList = swmBeaconStationMapper.selectList(
                    new LambdaQueryWrapperX<SwmBeaconStationDO>()
                            .eq(SwmBeaconStationDO::getStatus, "0")
            );

            int totalCount = beaconList == null ? 0 : beaconList.size();
            XxlJobHelper.log("【XXL-Job-{}】查询到有效信标数：{}", jobId, totalCount);

            if (totalCount == 0) {
                resultMsg.append("全量同步完成，无数据需发送");
                XxlJobHelper.log("【XXL-Job-{}】{}", jobId, resultMsg);
                isSuccess.set(true);
                return;
            }

            // ====================== 异步发送MQ ======================
            XxlJobHelper.log("【XXL-Job-{}】开始异步发送MQ，总条数：{}", jobId, totalCount);
            Future<?> future = MQ_SEND_EXECUTOR.submit(() -> {
                try {
                    sendFullSyncMq(beaconList, jobParam);
                    log.info("【XXL-Job-{}】MQ发送完成，条数：{}", jobId, totalCount);
                } catch (Exception e) {
                    log.error("【XXL-Job-{}】MQ发送异常", jobId, e);
                    throw new RuntimeException("MQ发送失败：" + e.getMessage(), e);
                }
            });

            // 等待异步任务完成（带超时）
            try {
                future.get(MQ_SEND_TIMEOUT, TimeUnit.MILLISECONDS);
                resultMsg.append(String.format("全量同步完成，发送%d条信标数据到MQ", totalCount));
                isSuccess.set(true);
            } catch (TimeoutException e) {
                resultMsg.append("MQ发送超时（5分钟），任务终止");
                log.error("【XXL-Job-{}】{}", jobId, resultMsg, e);
                future.cancel(true);
            } catch (ExecutionException e) {
                resultMsg.append("MQ发送异常：" + e.getCause().getMessage());
                log.error("【XXL-Job-{}】{}", jobId, resultMsg, e);
            } catch (InterruptedException e) {
                resultMsg.append("任务被中断：" + e.getMessage());
                log.error("【XXL-Job-{}】{}", jobId, resultMsg, e);
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            String errorMsg = "全量同步失败：" + e.getMessage();
            resultMsg.append(errorMsg);
            XxlJobHelper.log("【XXL-Job-{}】任务异常：{}", jobId, e.getMessage());
            log.error("【XXL-Job-{}】核心异常", jobId, e);
        } finally {
            // 上报任务最终状态
            if (isSuccess.get()) {
                XxlJobHelper.handleSuccess(resultMsg.toString());
            } else {
                XxlJobHelper.handleFail(resultMsg.toString());
            }
            XxlJobHelper.log("【XXL-Job-{}】任务结束，状态：{}，结果：{}",
                    jobId, isSuccess.get() ? "成功" : "失败", resultMsg);
        }
    }

    /**
     * 分批发送MQ消息（复用设备同步逻辑，适配信标场景）
     */
    private void sendFullSyncMq(List<SwmBeaconStationDO> beaconList, String jobParam) {
        int totalCount = beaconList.size();
        int batchNum = (totalCount + BATCH_SIZE - 1) / BATCH_SIZE;
        XxlJobHelper.log("开始分批发送MQ，总批次：{}，每批条数：{}", batchNum, BATCH_SIZE);

        for (int i = 0; i < batchNum; i++) {
            int start = i * BATCH_SIZE;
            int end = Math.min((i + 1) * BATCH_SIZE, totalCount);
            List<SwmBeaconStationDO> batchBeaconList = beaconList.subList(start, end);

            try {
                mqSendUtil.sendBeaconBatchChangeMsg(SyncDataOperateTypeEnum.BEACON_FULL_SYNC.getCode(), batchBeaconList);

                XxlJobHelper.log("第{}批MQ发送成功，条数：{}",i + 1, batchBeaconList.size());
            } catch (Exception e) {
                XxlJobHelper.log("第{}批MQ发送失败：{}", i + 1, e.getMessage());
                logger.error("【XXL-Job】第{}批MQ发送失败", i + 1, e);
            }
            // 限流：每批休眠50ms（和设备同步保持一致）
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
