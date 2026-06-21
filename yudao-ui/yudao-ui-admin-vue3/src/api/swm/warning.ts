/**
 * @author Generated
 * @date 2023-05-20
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 预警信息接口
 */
export interface Warning {
  id: string; // 主键ID
  personName?: string; // 人员姓名
  warningType?: string; // 预警类型
  warningContent?: string; // 预警内容
  warningTime?: string; // 预警时间
  alarmRecord?: string; // 报警记录
  alarmTime?: string; // 报警时间
  triggerReason?: string; // 触发原因
  handler?: string; // 处理人
  handleTime?: string; // 处理时间
  handleProcess?: string; // 处理过程
  handleStatus?: string; // 处理状态uff1a0-未处理uff0c1-已处理
  attachment?: string; // 附件URL
  status?: string; // 状态
  remarks?: string; // 备注
  createDate?: string; // 创建时间
  updateDate?: string; // 更新时间
  disposalDuration?: number; // 处置时长(分钟)
  location?: string; // 位置
  area?: string; // 区域

  // 基础模型字段
  isNewRecord?: boolean;
  page?: any;
  dataMap?: any;
  sessionid?: string;
  permi?: string;
  userType?: string;

  // 文本展示字段
  warningTypeText?: string;
  warningContentText?: string;
  handleStatusText?: string; // 处理状态文本uff1a已处理/未处理
}

/**
 * 获取预警列表
 */
export function getWarningList(params?: any) {
  return request.get<Page<Warning>>({
    url: '/swm/warningManagement/listData',
    params,
  });
}

/**
 * 获取单个预警信息
 */
export function getWarning(id: string) {
  return request.get<Warning>({
    url: '/swm/warningManagement/form',
    params: { id },
  });
}

/**
 * 处理预警
 */
export function processWarning(params: any) {
  return request.post({
    url: '/swm/warningManagement/process',
    params,
  });
}

/**
 * 导出预警列表
 */
export function exportWarningList(params?: any) {
  return request.get(
    {
      url: '/swm/warningManagement/export',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}

/**
 * 获取预警类型和预警内容枚举选项
 */
export function getWarningEnumOptions() {
  return request.get({
    url: '/swm/warningManagement/enumOptions',
  });
}

/**
 * 获取需要弹框显示的告警数据
 */
export function getPopupWarnings() {
  return request.get<any>(
    {
      url: '/swm/warningManagement/getPopupWarnings',
    },
    {
      errorMessageMode: 'none', // 不显示错误消息，由组件自行处理
      isTransformResponse: false, // 不转换响应格式，直接返回原始响应
      joinTime: false, // 不添加时间戳
    },
  );
}

/**
 * 确认告警
 */
export function confirmWarning(id: string) {
  return request.get(
    {
      url: `/swm/warningManagement/confirmWarning/id=${id}`,
      // params: { id },
    },
    {
      errorMessageMode: 'none', // 不显示错误消息，由组件自行处理
      isTransformResponse: false, // 不转换响应格式，直接返回原始响应
    },
  );
}
