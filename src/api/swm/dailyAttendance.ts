import request from '@/config/axios';

// 日考勤记录
export function getDailyAttendanceListData(params) {
  return request.get({ url: '/swm/swmDailyAttendance/listData', params });
}

// 日考勤记录详情
export function getDailyAttendanceDetail(params) {
  return request.get({ url: '/swm/swmDailyAttendance/get', params });
}

// 导出日考勤记录
export function exportDailyAttendance(params) {
  return request.get({ url: '/swm/swmDailyAttendance/exportData', params });
}

// 查询考勤休闲和工作详情
export function getfindAttendanceRange(params) {
  return request.get({ url: '/swm/swmDailyAttendance/findAttendanceRange', params });
}
