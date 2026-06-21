/**
 * 考勤记录汇总API
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 考勤汇总记录接口
 */
export interface AttendanceSummary {
  id: string;
  createBy?: string;
  createDate?: string;
  updateBy?: string;
  updateDate?: string;
  status?: string;
  remarks?: string;
  isNewRecord?: boolean;

  // 考勤数据字段
  employeeName?: string; // 员工名称
  department?: string; // 所属车间
  workProcess?: string; // 所属工序
  team?: string; // 所属班组
  jobType?: string; // 工种
  workShift?: string; // 所属班次
  month?: string; // 月份
  scheduledDays?: number; // 应出勤天数(天)
  actualDays?: number; // 实际出勤天数(天)
  attendanceRate?: number; // 出勤率
  scheduledHours?: number; // 应考勤时间(h)
  actualHours?: number; // 实际工作时间(h)
  attendanceAchievementRate?: number; // 考勤达成率
  totalWorkingHours?: number; // 总工时长(h)
  efficiency?: number; // 工效
}

/**
 * 获取考勤汇总记录列表
 */
export function getAttendanceSummaryList(params?: any) {
  return request.get<Page<AttendanceSummary>>({
    url: '/swm/swmAttendanceSummary/listData',
    params,
  });
}

/**
 * 导出考勤汇总记录
 */
export function exportAttendanceSummary(params?: any) {
  return request.get(
    {
      url: '/swm/swmAttendanceSummary/exportData',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}
