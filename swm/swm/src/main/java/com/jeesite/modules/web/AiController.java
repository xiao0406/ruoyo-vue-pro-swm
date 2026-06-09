package com.jeesite.modules.web;


import com.jeesite.common.entity.Page;
import com.jeesite.modules.service.AiServiceImpl;
import com.jeesite.modules.entity.AiDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/ai")
@Api(value = "ai接口")
public class AiController {

    @Autowired
    private AiServiceImpl aiServiceImpl;


    /**
     * 白班应到、白班实到、白班出勤率、白班有效作业时长，
     * 夜班应到、夜班实到、夜班出勤率、夜班有效作业时长，
     * 全天应到、全天实到、全天出勤率、全天有效作业时长
     *
     * @return
     */
    @PostMapping("workEfficiencyTask")
    @ResponseBody
    @ApiOperation(value = "工效统计")
    public Map<String, Object> workEfficiencyTask(String startDate, String endDate) {
        Map<String, Object> result =  aiServiceImpl.workEfficiencyTask(startDate,endDate);
        return result;
    }


    @PostMapping("workerFatigue")
    @ResponseBody
    @ApiOperation(value = "工人疲劳")
    public Page<AiDto.WorkerFatigue> workerFatigue(AiDto.WorkerFatigue vo ) {
        Page<AiDto.WorkerFatigue> result =  aiServiceImpl.workerFatigue(vo);
        return result;
    }

    @PostMapping("riskStatistics")
    @ResponseBody
    @ApiOperation(value = "风险统计-工人")
    public Page<AiDto.RiskStatistics> riskStatistics(AiDto.RiskStatistics vo ) {
        initPage(vo);
        Page<AiDto.RiskStatistics> result =  aiServiceImpl.riskStatistics(vo);
        return result;
    }

    @PostMapping("riskStatisticsArea")
    @ResponseBody
    @ApiOperation(value = "风险统计-区域")
    public Page<AiDto.RiskStatistics> riskStatisticsArea(AiDto.RiskStatistics vo ) {
        initPage(vo);
        Page<AiDto.RiskStatistics> result =  aiServiceImpl.riskStatisticsArea(vo);
        return result;
    }


    @PostMapping("riskStatisticsDate")
    @ResponseBody
    @ApiOperation(value = "风险统计-时间段")
    public Page<AiDto.RiskStatistics> riskStatisticsDate(AiDto.RiskStatistics vo ) {
//        initPage(vo);
        Page<AiDto.RiskStatistics> result =  aiServiceImpl.riskStatisticsDate(vo);
        return result;
    }

    @PostMapping("riskStatisticsAreaDate")
    @ResponseBody
    @ApiOperation(value = "风险统计-时间段+区域")
    public Page<AiDto.RiskStatisticsAreaDate> riskStatisticsAreaDate(AiDto.RiskStatisticsAreaDate vo ) {
        Page<AiDto.RiskStatisticsAreaDate> result =  aiServiceImpl.riskStatisticsAreaDate(vo);
        return result;
    }

    private void initPage(AiDto.RiskStatistics vo) {
        if (vo.getPageNo() == null || vo.getPageNo() < 1) vo.setPageNo(1);
        if (vo.getPageSize() == null || vo.getPageSize() < 1) vo.setPageSize(10);
    }


    @PostMapping("trajectory")
    @ResponseBody
    @ApiOperation(value = "轨迹定位")
    public Page<AiDto.Trajectory> trajectory(AiDto.Trajectory vo ) {
        Page<AiDto.Trajectory> result =  aiServiceImpl.trajectory(vo);
        return result;
    }

    @PostMapping("trajectoryV1")
    @ResponseBody
    @ApiOperation(value = "轨迹定位")
    public Page<AiDto.Trajectory> trajectoryV1(AiDto.Trajectory vo ) {
        Page<AiDto.Trajectory> result =  aiServiceImpl.trajectoryV1(vo);
        return result;
    }

    @PostMapping("teamActualHours")
    @ResponseBody
    @ApiOperation(value = "班组有效时长")
    public List<AiDto.TeamActualHours> teamActualHours(AiDto.TeamActualHours vo ) {
        List<AiDto.TeamActualHours> result =  aiServiceImpl.teamActualHours(vo);
        return result;
    }

    @PostMapping("unregisteredPersonnel")
    @ResponseBody
    @ApiOperation(value = "未注册人员")
    public List<AiDto.UnregisteredPersonnel> unregisteredPersonnel(AiDto.UnregisteredPersonnel vo) {
        return aiServiceImpl.unregisteredPersonnel(vo);
    }

    @PostMapping("deviceAnomalies")
    @ResponseBody
    @ApiOperation(value = "设备异常-低电量")
    public List<AiDto.DeviceAnomaly> deviceAnomalies(AiDto.DeviceAnomaly vo) {
        return aiServiceImpl.deviceAnomalies(vo);
    }

    @PostMapping("closedLoopTracking")
    @ResponseBody
    @ApiOperation(value = "闭环追踪-报警处置统计")
    public List<AiDto.ClosedLoopTracking> closedLoopTracking(AiDto.ClosedLoopTracking vo) {
        return aiServiceImpl.closedLoopTracking(vo);
    }

    @PostMapping("attendanceAnomaly")
    @ResponseBody
    @ApiOperation(value = "考勤异常-迟到早退人员")
    public AiDto.AttendanceAnomaly attendanceAnomaly(AiDto.AttendanceAnomaly vo) {
        return aiServiceImpl.attendanceAnomaly(vo);
    }
}
