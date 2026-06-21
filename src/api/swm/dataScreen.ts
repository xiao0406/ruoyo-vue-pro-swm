/**
 * @author shaodi
 */
import request from '@/config/axios';

// 接口前缀
const apiPrefix = '/swm/dashboard';

export interface LaborForceModel {
  company: string;
  department: string;
  workProcess: string;
  team: string;
  jobType: string;
}

/**
 * 近七日报警统计
 */
export const warningStatisticsForPast7Days = () =>
  request.get({ url: `${apiPrefix}/warningStatisticsForPast7Days` });

// 今日作业人数变化趋势
export const hourWorkingCount = () => request.get({ url: '/swm/dashboard2/hourWorkingCount24' });
// 今日作业人数变化趋势(夜班)
export const hourNightWorkingCount = () =>
  request.get({ url: '/swm/dashboard2/hourNightWorkingCount' });

// 报警统计
export const warningStatistics = () => request.get({ url: `${apiPrefix}/warningStatistics` });

/**
 * 今日预警报警记录
 */
export const warningStatisticsForToday = () =>
  request.get({ url: `${apiPrefix}/warningStatisticsForToday` });
export const warningStatisticsForTodayNew = () =>
  request.get({ url: `${apiPrefix}/warningStatisticsForTodayNew` });

/**
 * 厂区人数中间三项统计
 */
export const warningPersonCount = () => request.get({ url: `${apiPrefix}/warning/count` });

/**
 * 危险源
 */
export const hazard = (params: { year: string | number; month: string | number }) =>
  request.get({ url: `${apiPrefix}/hazard`, params });

/**
 * 劳动力管理
 */
export const laborForce = (params?: LaborForceModel) =>
  request.get({ url: `${apiPrefix}/attendance/dashboard`, params });

// 获取机构
export const companiesF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/common/options/companies`, params });

// 获取车间
export const departmentsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/common/options/departments`, params });

// 获取工序
export const prodLinesF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/common/options/prodLines`, params });

// 获取班组
export const workGroupsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/common/options/workGroups`, params });

// 获取工序
export const workTypesF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/common/options/workTypes`, params });

// 危险源总数趋势-ECharts图表类数据
export const hazardSourceStatisticsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/swmHazardSource/hazardSourceStatistics`, params });

// 1-热力图-危险源数量统计-(中间4块数量)
export const hazardSourceCountsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/swmHazardSource/hazardSourceCounts`, params });

// 1-最新巡检记录

export const latestInspectionRecordF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/swmInspectionList/latestInspectionRecord`, params });

//2-违规数量趋势-Echarts图表类数据
export const hazardWarningStatisticsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/warningManagement/hazardWarningStatistics`, params });

//2-违规数量趋势-中间数据
export const warningHazardSourceCountsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/warningManagement/hazardSourceCounts`, params });

// 2-违规数量趋势-最新危险源报警记录
export const latestHazardSourceRecordF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/warningManagement/latestHazardSourceRecord`, params });

// 3-告警数量趋势-Echarts图表类数据
export const nonHazardWarningStatisticsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/warningManagement/nonHazardWarningStatistics`, params });

// 3-告警数量趋势-中间数据
export const nonHazardSourceCountsF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/warningManagement/nonHazardSourceCounts`, params });

// 3-告警数量趋势-中间数据
export const latestHandleRecordF = (params?: LaborForceModel) =>
  request.get({ url: `/swm/handleRecord/latestHandleRecord`, params });

// 获取劳务管理数据
export const getLaborList = (params) =>
  request.get({
    url: `/swm/dashboard2/labor`,
    params,
  });

// 获取劳务管理数据
export const getPersonnel = (params?: LaborForceModel) =>
  request.get({
    url: `/swm/dashboard2/personnel`,
    params,
  });
// 获取劳务管理数据
export const getAnalysisList = (params?: LaborForceModel) =>
  request.get({
    url: `/swm/dashboard2/workshop/attendance/analysis`,
    params,
  });
// 获取劳务管理数据
export const getTeamList = (params?: LaborForceModel) =>
  request.get({
    url: `/swm/dashboard2/team/attendance/analysis`,
    params,
  });
export const getTeamListNew = (params?: LaborForceModel) =>
  request.get({
    url: `/swm/dashboard2/team/attendance/analysisNew`,
    params,
  });
// 注册工人数列表
export const getworkerList = (params?: LaborForceModel) =>
  request.get({
    url: '/swm/dashboard2/worker/list',
    params,
  });
// 今日出勤人数
export const getAttendanceList = (params?: LaborForceModel) =>
  request.get({
    url: '/swm/dashboard2/attendance/list',
    params,
  });
// 实时作业人数
export const getWorkingList = (params?: LaborForceModel) =>
  request.get({
    url: '/swm/dashboard2/working/list',
    params,
  });
// 5天以上未出勤人员
export const getAbnormalList = (params?: LaborForceModel) =>
  request.get({
    url: '/swm/dashboard2/abnormalAttendance/list',
    params,
  });

// 迟到人员
export const getLatePersonList = (params) =>
  request.get({
    url: '/swm/dashboard2/beLatePerson',
    params,
  });

// 早退人员
export const getLeaveEarlyPersonList = (params) =>
  request.get({
    url: '/swm/dashboard2/leaveEarlyPerson',
    params,
  });

// 长时间未出勤人员
export const getNoAttendancePersonList = (params) =>
  request.get({
    url: '/swm/dashboard2/noAttendancePerson',
    params,
  });

// 休闲区停留时长
export const getPersonIdleHoursRanking = (params) =>
  request.get({
    url: '/swm/dashboard2/personIdleHoursRanking',
    params,
  });

// 管理人员在岗情况
export const getPersonManagementOnDuty = (params) =>
  request.get({
    url: '/swm/dashboard2/personManagementOnDuty',
    params,
  });

// 车间出勤率
export const getDepartmentAttendanceAnalysis = (params) =>
  request.get({
    url: '/swm/dashboard2/departmentAttendanceAnalysis',
    params,
  });

// 班组出勤率
export const getTeamAttendanceAnalysis = (params) =>
  request.get({
    url: '/swm/dashboard2/teamAttendanceAnalysis',
    params,
  });

// 今日出勤管理员
export const getTodayAttendanceManagerList = (params) =>
  request.get({
    url: '/swm/dashboard2/attendance/manageList',
    params,
  });

// 今日出勤工人
export const getTodayAttendanceWorkerList = (params) =>
  request.get({
    url: '/swm/dashboard2/attendance/workList',
    params,
  });

// 导出
export const noAttendancePersonExport = (params?: any) =>
  request.get({
    url: '/swm/dashboard2/noAttendancePersonExport',
    params,
  });
