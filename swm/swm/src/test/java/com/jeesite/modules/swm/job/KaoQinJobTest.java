package com.jeesite.modules.swm.job;

import com.jeesite.modules.SwmApplication;
import com.jeesite.modules.job.task.AttendanceTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 考勤有效时长测试
 *
 * @author fangxiaolong
 * @since 2025-12-29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SwmApplication.class)
public class KaoQinJobTest {


    @Autowired
    private AttendanceTask attendanceTask;


    /**
     * 单元测试：考勤有效时长测试
     */
    @Test
    public void testKaoQin() {
        attendanceTask.calculateAttendanceByTimeRange();
    }

}