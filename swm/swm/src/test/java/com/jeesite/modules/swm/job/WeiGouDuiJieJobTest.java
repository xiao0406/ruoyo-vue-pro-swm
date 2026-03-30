package com.jeesite.modules.swm.job;

import com.jeesite.modules.SwmApplication;
import com.jeesite.modules.job.task.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 维构对接同步测试
 *
 * @author fangxiaolong
 * @since 2026-01-20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SwmApplication.class)
public class WeiGouDuiJieJobTest {


    @Autowired
    private SwmPersonFullSyncXxlJob swmPersonFullSyncXxlJob;

    @Autowired
    private SwmHelmetDeviceFullSyncXxlJob swmHelmetDeviceFullSyncXxlJob;

    @Autowired
    private SwmBeaconStationFullSyncXxlJob swmBeaconStationFullSyncXxlJob;




    /**
     * 单元测试：测试人员信息全量
     */
    @Test
    public void testRenYuanXinXi() {
        swmPersonFullSyncXxlJob.swmPersonFullSyncHandler();
    }


    /**
     * 单元测试：测试设备信息全量
     */
    @Test
    public void testSheBeiXinXi() {
        swmHelmetDeviceFullSyncXxlJob.swmHelmetDeviceFullSyncHandler();
    }

    /**
     * 单元测试：测试信标信息全量
     */
    @Test
    public void testXinBiaoXinXi() {
        swmBeaconStationFullSyncXxlJob.swmBeaconStationFullSyncHandler();
    }


}