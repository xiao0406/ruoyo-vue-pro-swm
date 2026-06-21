package cn.iocoder.yudao.module.swm.job.task;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.swm.enums.SyncDataOperateTypeEnum;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.util.MqSendUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于XXL-Job的人员全量同步任务
 */
@Component
@Slf4j
public class SwmPersonFullSyncXxlJob {
    private static final Logger logger = LoggerFactory.getLogger(SwmPersonFullSyncXxlJob.class);

    @Resource
    private SwmPersonMapper swmPersonMapper;

    @Resource
    private MqSendUtil mqSendUtil;

    private static final int BATCH_SIZE = 200;
    private static final long MQ_SEND_TIMEOUT = 5 * 60 * 1000L;

    private final ExecutorService MQ_SEND_EXECUTOR = new ThreadPoolExecutor(
            2, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "mq-send-person-sync-" + System.currentTimeMillis()),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @XxlJob("swmPersonFullSyncHandler")
    public void swmPersonFullSyncHandler() {
        String jobId = String.valueOf(XxlJobHelper.getJobId());
        XxlJobHelper.log("【XXL-Job-{}】开始执行人员全量同步任务", jobId);

        AtomicBoolean isSuccess = new AtomicBoolean(false);
        StringBuffer resultMsg = new StringBuffer();

        try {
            // ZJGGGD tenantId = 1
            List<SwmPersonDO> personList = new ArrayList<>();
            TenantUtils.execute(1L, () -> {
                List<SwmPersonDO> list = swmPersonMapper.selectList(
                        new LambdaQueryWrapperX<SwmPersonDO>()
                                .eq(SwmPersonDO::getStatus, "0")
                                .eq(SwmPersonDO::getPersonnelStatus, "1")
                );
                if (list != null) {
                    personList.addAll(list);
                }
            });

            int totalCount = personList.size();
            XxlJobHelper.log("【XXL-Job-{}】查询到有效人员数：{}", jobId, totalCount);

            if (totalCount == 0) {
                resultMsg.append("全量同步完成，无数据需发送");
                XxlJobHelper.log("【XXL-Job-{}】{}", jobId, resultMsg);
                isSuccess.set(true);
                return;
            }

            XxlJobHelper.log("【XXL-Job-{}】开始异步发送MQ，总条数：{}", jobId, totalCount);
            Future<?> future = MQ_SEND_EXECUTOR.submit(() -> {
                try {
                    sendFullSyncMq(personList);
                    log.info("【XXL-Job-{}】MQ发送完成，条数：{}", jobId, totalCount);
                } catch (Exception e) {
                    log.error("【XXL-Job-{}】MQ发送异常", jobId, e);
                    throw new RuntimeException("MQ发送失败：" + e.getMessage(), e);
                }
            });

            try {
                future.get(MQ_SEND_TIMEOUT, TimeUnit.MILLISECONDS);
                resultMsg.append(String.format("全量同步完成，发送%d条人员数据到MQ", totalCount));
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
            if (isSuccess.get()) {
                XxlJobHelper.handleSuccess(resultMsg.toString());
            } else {
                XxlJobHelper.handleFail(resultMsg.toString());
            }
            XxlJobHelper.log("【XXL-Job-{}】任务结束，状态：{}，结果：{}",
                    jobId, isSuccess.get() ? "成功" : "失败", resultMsg);
        }
    }

    private void sendFullSyncMq(List<SwmPersonDO> personList) {
        int totalCount = personList.size();
        int batchNum = (totalCount + BATCH_SIZE - 1) / BATCH_SIZE;
        XxlJobHelper.log("开始分批发送MQ，总批次：{}，每批条数：{}", batchNum, BATCH_SIZE);

        for (int i = 0; i < batchNum; i++) {
            int start = i * BATCH_SIZE;
            int end = Math.min((i + 1) * BATCH_SIZE, totalCount);
            List<SwmPersonDO> batchPersonList = personList.subList(start, end);

            try {
                mqSendUtil.sendPersonBatchChangeMsg(SyncDataOperateTypeEnum.PERSON_FULL_SYNC.getCode(), batchPersonList);
                XxlJobHelper.log("第{}批MQ发送成功，条数：{}", i + 1, batchPersonList.size());
            } catch (Exception e) {
                XxlJobHelper.log("第{}批MQ发送失败：{}", i + 1, e.getMessage());
                logger.error("【XXL-Job】第{}批MQ发送失败", i + 1, e);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
