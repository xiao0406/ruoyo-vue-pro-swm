/**
 * @author System
 * @date 2023-08-01
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 人员排班信息接口
 */
export interface StaffScheduleInfo {
  id: string;
  personId: string; // 人员ID
  personName: string; // 人员姓名
  month: string; // 排班月份(格式:yyyy-MM)
  scheduleDate: string; // 排班日期(格式:yyyy-MM-dd)
  classes: string; // 班次(早班/中班/晚班)
  idCard: string; // 身份证号码
  workGroupName: string; // 班组名称
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
  remarks: string;
  status: string;
}

/**
 * 月度排班统计
 */
export interface MonthScheduleStats {
  month: string; // 月份
  totalPerson: number; // 总人数
  earlyShiftCount: number; // 早班人数
  middleShiftCount: number; // 中班人数
  nightShiftCount: number; // 晚班人数
}

/**
 * 分页查询人员排班列表
 */
export function getStaffScheduleList(params) {
  return request.get<Page<StaffScheduleInfo>>({
    url: '/swm/personSchedule/listData',
    params,
  });
}

/**
 * 获取单个人员排班信息
 */
export function getStaffSchedule(id: string) {
  return request.get<StaffScheduleInfo>({
    url: '/swm/personSchedule/form',
    params: { id },
  });
}

/**
 * 保存人员排班信息
 */
export function saveStaffSchedule(params) {
  return request.post({
    url: '/swm/personSchedule/save',
    params,
  });
}

/**
 * 批量保存人员排班
 */
export function batchSaveStaffSchedule(params) {
  return request.post({
    url: '/swm/personSchedule/batchSave',
    data: params, // 使用data而不是params，确保以JSON格式发送
    headers: {
      'Content-Type': 'application/json', // 明确指定Content-Type为application/json
    },
  });
}

/**
 * 删除人员排班信息
 */
export function deleteStaffSchedule(id: string) {
  return request.post({
    url: '/swm/personSchedule/delete',
    params: { id },
  });
}

/**
 * 批量删除人员排班信息
 */
export function batchDeleteStaffSchedule(ids: string) {
  return request.post({
    url: '/swm/personSchedule/deleteAll',
    params: { ids },
  });
}

/**
 * 获取月度排班统计
 */
export function getMonthScheduleStats(month: string) {
  return request.get<MonthScheduleStats>({
    url: '/swm/personSchedule/monthStats',
    params: { month },
  });
}

/**
 * 导出人员排班Excel
 */
export function exportStaffSchedule(params) {
  return request.get(
    {
      url: '/swm/personSchedule/export',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}

/**
 * 导入人员排班Excel
 */
export function importStaffSchedule(params: FormData) {
  return request.post(
    {
      url: '/swm/personSchedule/import',
      params,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
    { isTransformResponse: false },
  );
}

/**
 * 下载人员排班导入模板
 */
export function downloadStaffScheduleTemplate() {
  return request.get(
    {
      url: '/swm/personSchedule/importTemplate',
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}

/**
 * 获取班组下拉列表
 */
export function getWorkGroupList() {
  return request.get<{ label: string; value: string }[]>({
    url: '/swm/personSchedule/getWorkGroups',
  });
}

// 批量修改人员班次
export function batchUpdateClasses(params) {
  return request.postJson({
    url: '/swm/personSchedule/batchUpdateClasses',
    params,
  });
}

// 获取全部人员id
export function getPersonIdList(params) {
  return request.get({
    url: '/swm/personSchedule/getPersonIdList',
    params,
  });
}

// 排班记录
export function getScheduleLog(id: string) {
  return request.get({
    url: `/swm/personScheduleLog/get/${id}`,
  });
}
