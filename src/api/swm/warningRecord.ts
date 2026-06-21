/**
 * @author zwf
 * @date 2025-05-21
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 预警处置记录接口
 */
export interface WarningRecord {
  id: string; // 主键ID
  recordName?: string; // 记录名称
  warningId?: string; // 预警ID
  warningRecord?: string; // 报警记录
  alarmTime?: string; // 报警时间
  triggerReason?: string; // 触发原因
  personName?: string; // 人员姓名
  handler?: string; // 处理人
  handleTime?: string; // 处理时间
  handleProcess?: string; // 处理过程
  handleStatus?: string; // 处理状态：0-未处理，1-已处理
  attachment?: string; // 附件URL
  status?: string; // 状态
  remarks?: string; // 备注
  createDate?: string; // 创建时间
  updateDate?: string; // 更新时间

  // 基础模型字段
  isNewRecord?: boolean;
  createBy?: string; // 创建人
  updateBy?: string; // 更新人

  // 文本展示字段
  handleStatusText?: string; // 处理状态文本：已处理/未处理
  warningContent?: string; // 预警内容（从关联的预警记录中获取）
}

/**
 * 未处置预警接口
 */
export interface UnhandledWarning {
  id: string; // 主键ID
  personName?: string; // 人员姓名
  warningType?: string; // 预警类型
  warningContent?: string; // 预警内容
  warningTime?: string; // 预警时间
  alarmRecord?: string; // 报警记录
  alarmTime?: string; // 报警时间
  triggerReason?: string; // 触发原因
  handleStatus?: string; // 处理状态：0-未处理，1-已处理
  remarks?: string; // 备注
  status?: string; // 状态
  createDate?: string; // 创建时间
  updateDate?: string; // 更新时间
  isNewRecord?: boolean;
}

/**
 * 获取预警处置记录列表
 */
export function getWarningRecordList(params?: any) {
  return request.get<Page<WarningRecord>>({
    url: '/swm/handleRecord/listData',
    params,
  });
}

/**
 * 获取单个预警处置记录信息
 */
export function getWarningRecord(id: string) {
  return request.get<WarningRecord>({
    url: '/swm/handleRecord/form',
    params: { id },
  });
}

/**
 * 获取未处置预警列表
 * @param params 查询参数，包含keyword, pageNum, pageSize
 */
export function getUnhandledWarnings(params?: {
  keyword?: string;
  pageNum?: number;
  pageSize?: number;
}) {
  return request.get<{
    success: boolean;
    count: number;
    total: number;
    pageNum: number;
    pageSize: number;
    totalPages: number;
    list: UnhandledWarning[];
  }>({
    url: '/swm/handleRecord/unhandledWarnings',
    params,
  });
}

/**
 * 保存预警处置记录
 */
export function saveWarningRecord(data: any) {
  return request.post({
    url: '/swm/handleRecord/save',
    data,
  });
}

/**
 * 导出预警处置记录列表
 */
export function exportWarningRecordList(params?: any) {
  return request.get(
    {
      url: '/swm/warningManagement/recordExport',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}
