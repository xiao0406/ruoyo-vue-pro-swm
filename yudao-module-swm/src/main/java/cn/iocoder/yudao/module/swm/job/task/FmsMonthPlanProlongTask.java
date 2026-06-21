package cn.iocoder.yudao.module.swm.job.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FMS 月度计划延期任务
 *
 * @author cjie
 */
@Slf4j
@Component
public class FmsMonthPlanProlongTask {

    /**
     * 执行月度计划延期检查
     */
    @XxlJob("fmsMonthPlanProlongTask")
    public void execute() {
        log.info("[FmsMonthPlanProlongTask][execute 开始执行月度计划延期任务]");
        // TODO: 实现月度计划延期逻辑
        log.info("[FmsMonthPlanProlongTask][execute 月度计划延期任务完成]");
    }

}
