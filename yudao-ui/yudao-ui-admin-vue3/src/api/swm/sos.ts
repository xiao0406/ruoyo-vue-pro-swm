/**
 * @author zwf
 * @date 2025-06-22
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';
import { Warning } from './warning';

/**
 * 获取SOS报警列表（筛选一键SOS类型的预警）
 */
export function getSOSList(params?: any) {
  return request.get<Page<Warning>>({
    url: '/swm/warningManagement/sosListData',
    params,
  });
}

/**
 * 获取单个SOS报警信息（复用预警接口）
 */
export function getWarning(id: string) {
  return request.get<Warning>({
    url: '/swm/warningManagement/form',
    params: { id },
  });
}

/**
 * 发送SOS报警
 */
export function sendSOSAlarm(params?: any) {
  return request.post({
    url: '/swm/warningManagement/sendSOSAlarm',
    data: params || {},
  });
}

/**
 * 导出SOS报警列表
 */
export function exportSOSList(params?: any) {
  return request.get(
    {
      url: '/swm/warningManagement/sosExport',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}
