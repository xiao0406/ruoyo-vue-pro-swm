/**
 * @author System
 * @date 2023-08-01
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 排班时间信息接口
 */
export interface ScheduleTimeInfo {
  id: string;
  shiftType: string; // 班次类型(1:早班,2:中班,3:晚班)
  shiftTypeText?: string; // 班次类型文本(早班/中班/晚班)
  startTime: string; // 开始时间
  endTime: string; // 结束时间
  restTime?: number; // 休息时长
  restDays?: string; // 休息日（逗号分隔，1-7代表周一到周日）
  restDaysText?: string; // 休息日显示文本（如：周一,周六,周日）
  createBy: string;
  createDate: string;
  updateBy?: string;
  updateDate?: string;
  remarks: string;
  status: string;
  isNewRecord?: boolean;
}

/**
 * 分页查询排班时间列表
 */
export function getScheduleTimeList(params) {
  return request.get<{
    pageNo: number;
    count: number;
    pageSize: number;
    list: ScheduleTimeInfo[];
  }>({
    url: '/swm/scheduleTime/listData',
    params,
  });
}

/**
 * 获取单个排班时间信息
 */
export function getScheduleTime(id: string) {
  return request.get<ScheduleTimeInfo>({
    url: '/swm/scheduleTime/form',
    params: { id },
  });
}

/**
 * 保存排班时间信息
 */
export function saveScheduleTime(params) {
  return request.post({
    url: '/swm/scheduleTime/save',
    params,
  });
}

/**
 * 保存所有排班时间信息
 * @param params
 */
export function saveAllScheduleTime(params: ScheduleTimeInfo[]) {
  return request.post({
    url: '/swm/scheduleTime/saveAll',
    data: params,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

/**
 * 删除排班时间信息
 */
export function deleteScheduleTime(id: string) {
  return request.post({
    url: '/swm/scheduleTime/delete',
    params: { id },
  });
}

/**
 * 批量删除排班时间信息
 */
export function batchDeleteScheduleTime(ids: string) {
  return request.post({
    url: '/swm/scheduleTime/deleteAll',
    params: { ids },
  });
}

/**
 * 获取所有有效的排班时间配置
 */
export function getAllScheduleTime() {
  return request.get<ScheduleTimeInfo[]>({
    url: '/swm/scheduleTime/getAllScheduleTime',
  });
}
