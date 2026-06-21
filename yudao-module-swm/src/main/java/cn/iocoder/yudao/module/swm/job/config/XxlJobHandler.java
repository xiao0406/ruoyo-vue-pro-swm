package cn.iocoder.yudao.module.swm.job.config;

import cn.iocoder.yudao.module.swm.job.task.FmsMonthPlanProlongTask;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法",
 * destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过
 * "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */

@Component
@Slf4j
public class XxlJobHandler {

    @Resource
    private FmsMonthPlanProlongTask fmsMonthPlanProlongTask;

    private static Logger logger = LoggerFactory.getLogger(XxlJobHandler.class);

    /**
     * 构件定额编码相关信息更新
     */
    @XxlJob("componentUnitCodeInfoUpdateTask")
    public void componentUnitCodeInfoUpdateTask() throws Exception {
        XxlJobHelper.log("XXL-JOB, 自动下月排班.");
        fmsMonthPlanProlongTask.execute();
    }

}
