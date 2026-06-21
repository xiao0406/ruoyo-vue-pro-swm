package cn.iocoder.yudao.module.iot.config;

import cn.iocoder.yudao.module.iot.service.BeaconStationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 信标MAC地址缓存初始化器
 * 系统启动时自动初始化信标基站MAC地址缓存
 *
 * @author Shawn
 * @date 2025-01-31
 */
@Component
@Order(100) // 设置较低的优先级，确保在数据库连接等基础组件初始化完成后执行
public class BeaconCacheInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BeaconCacheInitializer.class);

    @Resource
    private BeaconStationService beaconStationService;

    /**
     * 应用启动后执行MAC地址缓存初始化
     *
     * @param args 应用启动参数
     * @throws Exception 初始化异常
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("################### 系统启动后开始初始化信标基站MAC地址缓存 ###################");

        long startTime = System.currentTimeMillis();

        try {
            // 初始化MAC地址缓存
            beaconStationService.initializeMacAddressCache();

            long endTime = System.currentTimeMillis();
            logger.info("################### 信标基站MAC地址缓存初始化成功，耗时: {} ms ###################",
                    endTime - startTime);

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("################### 信标基站MAC地址缓存初始化失败，耗时: {} ms ###################",
                    endTime - startTime, e);

            // 可以选择是否抛出异常以阻止应用启动
            // 这里选择不抛出异常，允许应用继续启动，但会记录错误日志
            logger.warn("缓存初始化失败不会阻止应用启动，请检查数据库连接和表结构");
        }
    }
}
